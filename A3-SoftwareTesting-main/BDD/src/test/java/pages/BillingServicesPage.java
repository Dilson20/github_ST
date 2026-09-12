package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Page object for the Visit Services (billing) page
 * (e.g. /owners/{id}/pets/{id}/visits/{id}/services).
 *
 * Mirrors the Selenium module's VisitServicesPage but lives in the BDD module's
 * {@code pages} package so BDD step definitions can use it without depending on
 * the Selenium module.
 */
public class BillingServicesPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By PAGE_READY     = By.cssSelector("a.btn-secondary");
    private static final By CATALOGUE_ROWS = By.cssSelector("tr:has(form[action$='/add'])");
    private static final By SELECTED_ROWS  = By.cssSelector("tr:has(form[action$='/remove'])");
    private static final By ROW_BUTTON     = By.cssSelector("button[type='submit']");
    private static final By NAME_CELL      = By.cssSelector("td:first-child");
    private static final By ALL_TABLE_ROWS = By.cssSelector("table tr");

    public BillingServicesPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_READY));
    }

    // ---------------------------------------------------------------------- service actions

    public BillingServicesPage addService(String name) {
        clickRowButton(CATALOGUE_ROWS, name, "Service Catalogue");
        return this;
    }

    public BillingServicesPage removeService(String name) {
        clickRowButton(SELECTED_ROWS, name, "Selected Services");
        return this;
    }

    // ---------------------------------------------------------------------- billing values

    public String subtotal() { return breakdownValue("Subtotal"); }
    public String discount() { return breakdownValue("Discount"); }
    public String tax()      { return breakdownValue("Tax"); }
    public String total()    { return breakdownValue("Total"); }

    // ---------------------------------------------------------------------- service lists

    public List<String> selectedServiceNames() { return namesIn(SELECTED_ROWS); }
    public List<String> catalogueServiceNames() { return namesIn(CATALOGUE_ROWS); }

    // ---------------------------------------------------------------------- private helpers

    private void clickRowButton(By rowLocator, String name, String tableLabel) {
        WebElement button = findRow(rowLocator, name, tableLabel).findElement(ROW_BUTTON);
        WebElement htmlTag = driver.findElement(By.tagName("html"));
        wait.until(ExpectedConditions.elementToBeClickable(button)).click();
        wait.until(ExpectedConditions.stalenessOf(htmlTag));
        wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_READY));
    }

    private WebElement findRow(By rowLocator, String name, String tableLabel) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_READY));
        for (WebElement row : driver.findElements(rowLocator)) {
            if (row.findElement(NAME_CELL).getText().trim().equalsIgnoreCase(name)) {
                return row;
            }
        }
        throw new NoSuchElementException(
                "Service '" + name + "' not listed under " + tableLabel +
                ". Found: " + namesIn(rowLocator));
    }

    private List<String> namesIn(By rowLocator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_READY));
        List<String> names = new ArrayList<>();
        for (WebElement row : driver.findElements(rowLocator)) {
            names.add(row.findElement(NAME_CELL).getText().trim());
        }
        return names;
    }

    private String breakdownValue(String label) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_READY));
        for (WebElement row : driver.findElements(ALL_TABLE_ROWS)) {
            List<WebElement> headers = row.findElements(By.tagName("th"));
            List<WebElement> cells   = row.findElements(By.tagName("td"));
            if (headers.size() == 1 && cells.size() == 1
                    && headers.get(0).getText().trim().equalsIgnoreCase(label)) {
                return cells.get(0).getText().trim();
            }
        }
        throw new NoSuchElementException("No billing row labelled '" + label + "'");
    }
}
