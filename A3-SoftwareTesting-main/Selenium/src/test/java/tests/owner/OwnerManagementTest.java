package tests.owner;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OwnerManagementTest {

    private static final String BASE_URL = "http://localhost:8080";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setUpClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Uncomment the next line to run headless (e.g. on CI):
        // options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        try { driver.manage().window().maximize(); }
        catch (Exception e) { System.out.println("Warning: window maximize failed, continuing anyway: " + e.getMessage()); }
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void goToAddOwnerForm() {
        driver.get(BASE_URL + "/owners/new");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-owner-form")));
    }

    private void fillOwnerForm(String firstName, String lastName, String address,
                                String city, String telephone) {
        fillField(By.id("firstName"), firstName);
        fillField(By.id("lastName"), lastName);
        fillField(By.id("address"), address);
        fillField(By.name("city"), city);
        fillField(By.id("telephone"), telephone);
    }

    private void fillField(By locator, String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(value);
    }

    private void submitOwnerForm() {
        WebElement submitButton = driver.findElement(
                By.cssSelector("#add-owner-form button[type='submit']"));
        submitButton.click();
    }

    private String getValidationErrorText() {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".invalid-feedback")));
        return error.getText();
    }

    private String getSuccessMessageText() {
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("success-message")));
        return message.getText();
    }

    @Test
    @DisplayName("TC-OWN-01: Add owner with valid 10-digit telephone (boundary - pass)")
    void tcOwn01_addOwnerWithValid10DigitTelephone() {
        goToAddOwnerForm();
        fillOwnerForm("John", "Carter", "123 Main St", "Melbourne", "0412345678");
        submitOwnerForm();

        wait.until(ExpectedConditions.urlMatches(".*/owners/\\d+.*"));

        String successText = getSuccessMessageText();
        assertEquals("New Owner Created", successText,
                "Owner with a valid 10-digit telephone should be saved successfully");

        String pageBody = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageBody.contains("0412345678"),
                "Owner Details page should display the saved telephone number");
    }

    @Test
    @DisplayName("TC-OWN-02: Add owner with 9-digit telephone is rejected (regression)")
    void tcOwn02_addOwnerWith9DigitTelephoneRejected() {
        goToAddOwnerForm();
        fillOwnerForm("Jane", "Doe", "45 King St", "Sydney", "041234567"); // 9 digits
        submitOwnerForm();

        assertTrue(driver.getCurrentUrl().endsWith("/owners/new"),
                "Invalid submission should redisplay the Add Owner form, not redirect");

        String errorText = getValidationErrorText();
        assertEquals("Telephone must be a 10-digit number", errorText,
                "A 9-digit telephone number must be rejected with the correct validation message");

        WebElement telephoneField = driver.findElement(By.id("telephone"));
        assertTrue(telephoneField.getAttribute("class").contains("is-invalid"),
                "Telephone field should be visually flagged as invalid");
    }

    @Test
    @DisplayName("TC-OWN-03: Add owner with 11-digit telephone is rejected (boundary)")
    void tcOwn03_addOwnerWith11DigitTelephoneRejected() {
        goToAddOwnerForm();
        fillOwnerForm("Mike", "Smith", "9 Park Ave", "Brisbane", "04123456789"); // 11 digits
        submitOwnerForm();

        assertTrue(driver.getCurrentUrl().endsWith("/owners/new"));
        assertEquals("Telephone must be a 10-digit number", getValidationErrorText());
    }

    @Test
    @DisplayName("TC-OWN-04: Add owner with non-digit characters in telephone is rejected")
    void tcOwn04_addOwnerWithNonDigitTelephoneRejected() {
        goToAddOwnerForm();
        fillOwnerForm("Anna", "Lee", "12 Queen St", "Perth", "041-234-56A");
        submitOwnerForm();

        assertTrue(driver.getCurrentUrl().endsWith("/owners/new"));
        assertEquals("Telephone must be a 10-digit number", getValidationErrorText());
    }

    @Test
    @DisplayName("TC-OWN-05: Add owner with blank telephone field is rejected")
    void tcOwn05_addOwnerWithBlankTelephoneRejected() {
        goToAddOwnerForm();
        fillOwnerForm("Tom", "Nguyen", "7 York St", "Adelaide", "");
        submitOwnerForm();

        assertTrue(driver.getCurrentUrl().endsWith("/owners/new"));

        String errorText = getValidationErrorText();
        assertTrue(errorText.contains("must not be blank"),
                "Blank telephone should show a required-field error");
    }

    @ParameterizedTest(name = "Telephone \"{0}\" ({1}) should be rejected")
    @DisplayName("TC-OWN-02/03/04 (data-driven): Telephone boundary values are rejected")
    @CsvSource({
            "12345,            far too short",
            "041234567,        9 digits - one under boundary",
            "04123456789,      11 digits - one over boundary",
            "0412345678a,      11 chars with trailing letter",
            "0412-345-678,     digits with hyphens"
    })
    void tcOwn02to04_telephoneBoundaryValuesRejected(String telephone, String description) {
        goToAddOwnerForm();
        fillOwnerForm("Data", "Driven", "1 Test St", "Testville", telephone);
        submitOwnerForm();

        assertTrue(driver.getCurrentUrl().endsWith("/owners/new"),
                () -> "Telephone value [" + telephone + "] (" + description + ") should be rejected");
        assertFalse(driver.findElements(By.cssSelector(".invalid-feedback")).isEmpty(),
                () -> "Expected a validation error for telephone [" + telephone + "]");
    }

    private String createOwnerAndGetDetailsUrl(String firstName, String lastName,
                                                String address, String city, String telephone) {
        goToAddOwnerForm();
        fillOwnerForm(firstName, lastName, address, city, telephone);
        submitOwnerForm();
        wait.until(ExpectedConditions.urlMatches(".*/owners/\\d+.*"));
        return driver.getCurrentUrl();
    }

    private void clickEditOwner() {
        WebElement editLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Edit Owner")));
        editLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-owner-form")));
    }

    @Test
    @DisplayName("TC-OWN-06: Edit owner with valid details succeeds")
    void tcOwn06_editOwnerWithValidDetails() {
        createOwnerAndGetDetailsUrl("John", "Carter", "123 Main St", "Melbourne", "0412345678");
        clickEditOwner();

        fillOwnerForm("Johnathan", "Carterson", "456 New St", "Geelong", "0498765432");
        submitOwnerForm();

        wait.until(ExpectedConditions.urlMatches(".*/owners/\\d+$"));
        assertEquals("Owner Values Updated", getSuccessMessageText());

        String pageBody = driver.findElement(By.tagName("body")).getText();
        assertAll("Updated owner details should be reflected on the Owner Details page",
                () -> assertTrue(pageBody.contains("Johnathan Carterson")),
                () -> assertTrue(pageBody.contains("Geelong")),
                () -> assertTrue(pageBody.contains("0498765432"))
        );
    }

    @Test
    @DisplayName("TC-OWN-07: Edit owner leaving a mandatory field blank is rejected")
    void tcOwn07_editOwnerWithBlankMandatoryFieldRejected() {
        String detailsUrl = createOwnerAndGetDetailsUrl(
                "Jane", "Doe", "45 King St", "Sydney", "0412345678");
        clickEditOwner();

        fillOwnerForm("Jane", "", "45 King St", "Sydney", "0412345678"); // blank last name
        submitOwnerForm();

        assertTrue(driver.getCurrentUrl().endsWith("/edit"),
                "Invalid edit submission should redisplay the Edit Owner form");
        assertEquals("must not be blank", getValidationErrorText());

        driver.get(detailsUrl);
        String pageBody = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageBody.contains("Jane Doe"),
                "Original owner name should remain unchanged after a rejected edit");
    }

    @Test
    @DisplayName("TC-OWN-08: Edit owner telephone to 9 digits is rejected (boundary)")
    void tcOwn08_editOwnerTelephoneTooShortRejected() {
        String detailsUrl = createOwnerAndGetDetailsUrl(
                "Mike", "Smith", "9 Park Ave", "Brisbane", "0412345678");
        clickEditOwner();

        fillOwnerForm("Mike", "Smith", "9 Park Ave", "Brisbane", "041234567"); // 9 digits
        submitOwnerForm();

        assertTrue(driver.getCurrentUrl().endsWith("/edit"));
        assertEquals("Telephone must be a 10-digit number", getValidationErrorText());

        driver.get(detailsUrl);
        String pageBody = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageBody.contains("0412345678"),
                "Original telephone number should remain unchanged after a rejected edit");
    }

    @Test
    @DisplayName("TC-OWN-09: Edit owner telephone to 11 digits / non-digits is rejected")
    void tcOwn09_editOwnerTelephoneTooLongOrNonDigitRejected() {
        createOwnerAndGetDetailsUrl("Anna", "Lee", "12 Queen St", "Perth", "0412345678");
        clickEditOwner();

        fillOwnerForm("Anna", "Lee", "12 Queen St", "Perth", "04123456789"); // 11 digits
        submitOwnerForm();

        assertTrue(driver.getCurrentUrl().endsWith("/edit"));
        assertEquals("Telephone must be a 10-digit number", getValidationErrorText());
    }

    @Test
    @DisplayName("DEFECT CHECK - FR-O4: Owner search must be case-insensitive")
    void frO4_ownerSearchShouldBeCaseInsensitive() {
        driver.get(BASE_URL + "/owners/find");

        WebElement lastNameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("lastName")));
        lastNameField.clear();
        lastNameField.sendKeys("davis");
        driver.findElement(By.cssSelector("#search-owner-form button[type='submit']")).click();

        List<WebElement> notFoundError = driver.findElements(By.cssSelector(".invalid-feedback"));
        boolean notFoundShown = notFoundError.stream().anyMatch(
                el -> el.getText().toLowerCase().contains("not"));

        assertFalse(notFoundShown,
                "FR-O4 requires case-insensitive last-name search; lowercase 'davis' "
                        + "should still match owner 'Davis', but the app reports not found.");
    }

    @Test
    @DisplayName("DEFECT CHECK - FR-PG1: Owner list must paginate at 5 per page")
    void frPg1_ownerListShouldShowFiveOwnersPerPage() {
        driver.get(BASE_URL + "/owners?lastName=");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table")));
        List<WebElement> ownerRows = driver.findElements(
                By.cssSelector("table tbody tr"));

        assertEquals(5, ownerRows.size(),
                "FR-PG1 requires 5 owners per page; the release candidate shows "
                        + ownerRows.size() + " instead.");
    }
}
