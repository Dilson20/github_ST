package tests.veterinarian;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Veterinarian Directory — FR-V1 (PC-105)")
public class VeterinarianDirectoryTest {
    static WebDriver driver;
    static WebDriverWait wait;
    static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("TC-VET-01 [FR-V1]: Veterinarian list is displayed and vet names are non-blank")
    void testVeterinarianDirectoryWithSpecialities() {
        driver.get(BASE_URL + "/vets.html");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#vets tbody tr")));
        List<WebElement> rows = driver.findElements(By.cssSelector("#vets tbody tr"));
        assertFalse(rows.isEmpty(), "Veterinarian list should not be empty");

        WebElement firstRow = rows.get(0);
        String vetName = firstRow.findElement(By.cssSelector("td:nth-child(1) span")).getText();
        assertFalse(vetName.isBlank(), "Veterinarian name should not be blank");
    }

    @Test
    @DisplayName("TC-VET-02 [FR-V1]: Veterinarians without specialties are displayed without errors")
    void testVeterinarianDirectoryWithoutSpecialities() {
        driver.get(BASE_URL + "/vets.html");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#vets tbody tr")));
        List<WebElement> rows = driver.findElements(By.cssSelector("#vets tbody tr"));
        boolean foundVetWithoutSpecialities = rows.stream().anyMatch(row ->
                row.findElements(By.cssSelector(".badge.text-bg-secondary")).isEmpty());
        assertTrue(foundVetWithoutSpecialities, "Veterinarian without specialities should display without error");
    }

    @Test
    @DisplayName("TC-VET-03 [FR-PG-V]: Veterinarian list supports pagination to page 2")
    void testVetListPagination(){
        driver.get(BASE_URL + "/vets.html");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Next"))).click();
        wait.until(ExpectedConditions.urlContains("page=2"));
        assertTrue(driver.getCurrentUrl().contains("page=2"));
    }
}
