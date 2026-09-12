package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Creates a fresh Owner → Pet → Visit fixture and navigates to the
 * Manage Services (billing) page.
 *
 * Mirrors the Selenium module's VisitFixtureFlow but lives inside the BDD
 * module so step definitions have no cross-module dependency.
 */
public class BillingFixtureFlow {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    // Owner form
    private static final By FIRST_NAME  = By.id("firstName");
    private static final By LAST_NAME   = By.id("lastName");
    private static final By ADDRESS     = By.id("address");
    private static final By CITY        = By.id("city");
    private static final By TELEPHONE   = By.id("telephone");

    // Pet form
    private static final By PET_NAME        = By.id("name");
    private static final By PET_BIRTH_DATE  = By.id("birthDate");
    private static final By PET_TYPE        = By.id("type");

    // Visit form
    private static final By VISIT_DATE        = By.id("date");
    private static final By VISIT_DESCRIPTION = By.id("description");

    private static final By SUBMIT               = By.cssSelector("button[type='submit']");
    private static final By ADD_PET_LINK         = By.xpath("//a[contains(@href, 'pets/new')]");
    private static final By ADD_VISIT_LINK       = By.cssSelector("a[href$='/visits/new']");
    private static final By MANAGE_SERVICES_LINK = By.cssSelector("a[href$='/services']");
    private static final By OWNER_DETAIL_READY   = By.xpath("//h2[contains(text(),'Owner Information')]");

    public BillingFixtureFlow(WebDriver driver, String baseUrl) {
        this.driver  = driver;
        this.baseUrl = baseUrl;
        this.wait    = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Creates a unique owner with one pet and one future-dated visit, then
     * opens the Manage Services page and returns its page object.
     */
    public BillingServicesPage createOwnerPetVisitAndOpenServices() {
        createOwner();
        createPet();
        createVisit(LocalDate.now().plusDays(1).toString());
        return openServices();
    }

    /**
     * Creates a unique owner with one pet, then attempts to book a visit on
     * {@code isoDate} and stays on the visit form (for date-validation tests).
     */
    public VisitBookingPage createOwnerPetAndOpenVisitForm() {
        createOwner();
        createPet();
        // Explicitly wait for owner detail page before clicking Add Visit link
        wait.until(ExpectedConditions.presenceOfElementLocated(OWNER_DETAIL_READY));
        // Click Add Visit
        wait.until(ExpectedConditions.elementToBeClickable(ADD_VISIT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(VISIT_DATE));
        return new VisitBookingPage(driver);
    }

    // ------------------------------------------------------------------ private steps

    private void createOwner() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        driver.get(baseUrl + "/owners/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME)).sendKeys("BDD");
        driver.findElement(LAST_NAME).sendKeys("Probe" + uid);
        driver.findElement(ADDRESS).sendKeys("1 BDD Street");
        driver.findElement(CITY).sendKeys("Testville");
        driver.findElement(TELEPHONE).sendKeys("0400000001");
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(OWNER_DETAIL_READY));
    }

    private void createPet() {
        wait.until(ExpectedConditions.elementToBeClickable(ADD_PET_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PET_NAME)).sendKeys("BDDPet");
        setDate(PET_BIRTH_DATE, "2020-06-01");
        WebElement typeEl = wait.until(ExpectedConditions.visibilityOfElementLocated(PET_TYPE));
        new Select(typeEl).selectByVisibleText("dog");
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(OWNER_DETAIL_READY));
    }

    private void createVisit(String isoDate) {
        wait.until(ExpectedConditions.elementToBeClickable(ADD_VISIT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(VISIT_DATE));
        setDate(VISIT_DATE, isoDate);
        wait.until(ExpectedConditions.visibilityOfElementLocated(VISIT_DESCRIPTION))
                .sendKeys("BDD billing fixture");
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(OWNER_DETAIL_READY));
    }

    private BillingServicesPage openServices() {
        wait.until(ExpectedConditions.elementToBeClickable(MANAGE_SERVICES_LINK)).click();
        return new BillingServicesPage(driver);
    }

    private void setDate(By locator, String isoDate) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];" +
                "arguments[0].dispatchEvent(new Event('input',  {bubbles:true}));" +
                "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                el, isoDate);
    }
}
