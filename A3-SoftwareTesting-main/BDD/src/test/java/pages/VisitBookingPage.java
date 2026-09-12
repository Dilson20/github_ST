package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page object for the Add Visit form (e.g. /owners/{id}/pets/{id}/visits/new).
 * Used by BDD billing step definitions to book a visit and navigate to its
 * Manage Services page.
 */
public class VisitBookingPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By VISIT_DATE_INPUT   = By.id("date");
    private static final By DESCRIPTION_INPUT  = By.id("description");
    private static final By SUBMIT_BUTTON      = By.cssSelector("button[type='submit']");
    private static final By OWNER_DETAIL_READY = By.xpath("//h2[contains(text(),'Owner Information')]");
    private static final By MANAGE_SERVICES    = By.cssSelector("a[href$='/services']");
    private static final By ADD_VISIT_LINK     = By.cssSelector("a[href$='/visits/new']");
    private static final By ERROR_FEEDBACK     = By.cssSelector(".invalid-feedback, .has-error, .alert-danger");

    public VisitBookingPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Set the date input via JavaScript (compatible with Chrome date pickers on macOS).
     */
    public VisitBookingPage setDate(String isoDate) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(VISIT_DATE_INPUT));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];" +
                "arguments[0].dispatchEvent(new Event('input',  {bubbles:true}));" +
                "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                input, isoDate);
        return this;
    }

    public VisitBookingPage setDescription(String text) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(DESCRIPTION_INPUT));
        field.clear();
        field.sendKeys(text);
        return this;
    }

    /** Submit and expect success → redirects to owner detail page. */
    public void submitExpectingSuccess() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(OWNER_DETAIL_READY));
    }

    /** Submit and stay on the form (expecting a validation rejection). */
    public void submitExpectingRejection() {
        String currentUrl = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
        // Wait until either an error appears OR a redirect happens
        wait.until(d ->
                !d.getCurrentUrl().equals(currentUrl) ||
                !d.findElements(ERROR_FEEDBACK).isEmpty()
        );
    }

    /**
     * Clicks Submit without waiting for any outcome.
     * Use this when the expected outcome (accept vs reject) depends on a SUT defect
     * and must be evaluated in the Then step after a brief pause.
     */
    public void submitNoWait() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
    }

    /** True if a validation error banner / inline message is visible. */
    public boolean isRejected() {
        // If still on the form page AND an error element is present → rejected
        return !driver.findElements(ERROR_FEEDBACK).stream()
                .filter(WebElement::isDisplayed)
                .toList().isEmpty();
    }

    /** Navigates to the Add Visit form for a specific owner/pet pair. */
    public static VisitBookingPage openFor(WebDriver driver, String baseUrl, int ownerId, int petId) {
        driver.get(baseUrl + "/owners/" + ownerId + "/pets/" + petId + "/visits/new");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.visibilityOfElementLocated(VISIT_DATE_INPUT));
        return new VisitBookingPage(driver);
    }

    /**
     * After a successful visit creation, click the "Manage Services" link for
     * the newly created visit and return the billing services page object.
     */
    public BillingServicesPage openManageServices() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(MANAGE_SERVICES));
        link.click();
        return new BillingServicesPage(driver);
    }

    /** Navigate to owner detail, find the add-visit link and click it. */
    public static VisitBookingPage openFromOwnerDetail(WebDriver driver, String baseUrl, int ownerId) {
        driver.get(baseUrl + "/owners/" + ownerId);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.elementToBeClickable(ADD_VISIT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(VISIT_DATE_INPUT));
        return new VisitBookingPage(driver);
    }
}
