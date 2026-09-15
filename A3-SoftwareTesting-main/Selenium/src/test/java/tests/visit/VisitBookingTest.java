package tests.visit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Visit Booking - Date & Description Validation - FR-V2, FR-V3 (SCRUM-16, SCRUM-64, SCRUM-65, SCRUM-66)")
public class VisitBookingTest {

    private static final String BASE_URL = "http://localhost:8080";

    // Native <input type="date"> fields need MM/dd/yyyy keystrokes with no separators;
    // sendKeys("yyyy-MM-dd") types into the segmented picker and corrupts the value.
    private static final DateTimeFormatter DATE_INPUT_FMT = DateTimeFormatter.ofPattern("MMddyyyy");

    private static WebDriver driver;
    private static WebDriverWait wait;

    // ------------------------------------------------------------------ lifecycle

    @BeforeAll
    static void openBrowser() {
        driver = new ChromeDriver(buildChromeOptions());
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @BeforeEach
    void createFreshOwnerAndPet() {
        // Each test needs its own owner/pet so the visit-booking action under test starts clean.
        createOwnerAndPet();
    }

    @AfterAll
    static void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ------------------------------------------------------------------ SCRUM-16 / US-VISIT-01

    @Test
    @DisplayName("TC-VISIT-01 (SCRUM-64) [AC-1.1, FR-V2]: a visit dated today is rejected")
    void tcVisit01_visitDatedToday_isRejected() {
        // Covers FR-V2: the Date field must be strictly after today. Bypasses the browser's
        // native min-date constraint via JS first, so this exercises the SERVER's own validation
        // rather than only confirming Chrome's date picker refuses bad input.
        boolean success = bookVisit(0, "Same-day booking test");

        assertAll("TC-VISIT-01 (SCRUM-64) visit dated today",
                () -> assertFalse(success,
                        "TC-VISIT-01 [FR-V2]: a visit dated today must NOT be accepted"),
                () -> assertTrue(currentValidationError().contains("future"),
                        "TC-VISIT-01 [FR-V2]: expected a validation error mentioning the date "
                                + "must be in the future"));
    }

    @Test
    @DisplayName("TC-VISIT-02 (SCRUM-65) [AC-1.2, FR-V2 boundary]: a visit dated tomorrow is accepted")
    void tcVisit02_visitDatedTomorrow_isAccepted() {
        // Covers the FR-V2 boundary: tomorrow is the earliest date the spec allows.
        boolean success = bookVisit(1, "Next-day booking test");

        assertTrue(success,
                "TC-VISIT-02 [FR-V2 boundary]: a visit dated tomorrow must be accepted");
    }

    @Test
    @DisplayName("TC-VISIT-03 (SCRUM-66) [AC-1.3, FR-V3]: a visit with an empty description is rejected")
    void tcVisit03_emptyDescription_isRejected() {
        // Covers FR-V3: Description is required. Also strips the field's client-side "required"
        // attribute via JS, so the SERVER's own validation is what's actually being exercised.
        boolean success = bookVisit(1, "");

        assertAll("TC-VISIT-03 (SCRUM-66) empty description",
                () -> assertFalse(success,
                        "TC-VISIT-03 [FR-V3]: a visit with an empty description must NOT be accepted"),
                () -> assertTrue(!currentValidationError().isBlank(),
                        "TC-VISIT-03 [FR-V3]: expected a validation error for the empty "
                                + "description field"));
    }

    // ------------------------------------------------------------------ helpers

    /** Creates a fresh owner + pet, leaving the browser on the owner detail page. */
    private void createOwnerAndPet() {
        driver.get(BASE_URL + "/owners/new");
        driver.findElement(By.id("firstName")).sendKeys("Trong");
        driver.findElement(By.id("lastName")).sendKeys("Tran");
        driver.findElement(By.id("address")).sendKeys("Brown street");
        driver.findElement(By.id("city")).sendKeys("Ho Chi Minh city");
        driver.findElement(By.id("telephone")).sendKeys("0123456789");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("success-message")));

        driver.findElement(By.linkText("Add New Pet")).click();
        driver.findElement(By.id("name")).sendKeys("Dirty");
        new Select(driver.findElement(By.id("type"))).selectByValue("cat");
        driver.findElement(By.id("birthDate")).sendKeys(LocalDate.now().format(DATE_INPUT_FMT));
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    /**
     * Books a visit at the given offset (days) from today with the given description.
     * Strips the date field's native "min" constraint and the description field's "required"
     * attribute before submitting, so a client-side block can never mask a missing server-side
     * check. Returns whether the success banner appeared.
     */
    private boolean bookVisit(int offsetDays, String description) {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add Visit"))).click();

        WebElement dateInput = driver.findElement(By.id("date"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('min');", dateInput);
        String visitDate = LocalDate.now().plusDays(offsetDays).format(DATE_INPUT_FMT);
        dateInput.clear();
        dateInput.sendKeys(visitDate);

        WebElement descriptionInput = driver.findElement(By.id("description"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('required');", descriptionInput);
        descriptionInput.clear();
        descriptionInput.sendKeys(description);

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("success-message")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private String currentValidationError() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".invalid-feedback"))).getText();
    }

    private static ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");

        options.addArguments("--lang=en-US");
        options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "en-US,en"));

        String explicitBinary = System.getProperty("chrome.binary");
        if (explicitBinary != null && !explicitBinary.isBlank()) {
            options.setBinary(explicitBinary);
        } else if (!Files.isExecutable(Path.of("/usr/bin/google-chrome"))
                && Files.isExecutable(Path.of("/usr/bin/chromium"))) {
            options.setBinary("/usr/bin/chromium");
        }
        return options;
    }
}