package pages.billing;

import pages.common.BasePage;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

public class VisitServicesPage extends BasePage {

    private static final By CATALOGUE_ROWS = By.cssSelector("tr:has(form[action$='/add'])");
    private static final By SELECTED_ROWS = By.cssSelector("tr:has(form[action$='/remove'])");
    private static final By ROW_BUTTON = By.cssSelector("button[type='submit']");
    private static final By NAME_CELL = By.cssSelector("td:first-child");
    private static final By PAGE_READY = By.cssSelector("a.btn-secondary");
    private static final By ALL_TABLE_ROWS = By.cssSelector("table tr");

    public VisitServicesPage(WebDriver driver) {
        super(driver);
    }

    public VisitServicesPage waitUntilLoaded() {
        waitForVisibility(PAGE_READY);
        return this;
    }

    // ------------------------------------------------------------------ FR-S2 / FR-S3

    public VisitServicesPage addService(String serviceName) {
        clickRowButton(CATALOGUE_ROWS, serviceName, "Service Catalog");
        return this;
    }

    public VisitServicesPage removeService(String serviceName) {
        clickRowButton(SELECTED_ROWS, serviceName, "Selected Services");
        return this;
    }

    private void clickRowButton(By rowLocator, String serviceName, String tableLabel) {
        WebElement button = findServiceRow(rowLocator, serviceName, tableLabel).findElement(ROW_BUTTON);
        WebElement oldDocument = driver.findElement(By.tagName("html"));
        wait.until(ExpectedConditions.elementToBeClickable(button)).click();
        wait.until(ExpectedConditions.stalenessOf(oldDocument));
        waitForVisibility(PAGE_READY);
    }

    private WebElement findServiceRow(By rowLocator, String serviceName, String tableLabel) {
        waitForVisibility(PAGE_READY);
        for (WebElement row : driver.findElements(rowLocator)) {
            if (row.findElement(NAME_CELL).getText().trim().equalsIgnoreCase(serviceName)) {
                return row;
            }
        }
        throw new NoSuchElementException(
                "Service '" + serviceName + "' is not listed under " + tableLabel
                        + ". Currently listed: " + namesIn(rowLocator));
    }

    // ------------------------------------------------------------------ FR-S1 lists

    public List<String> selectedServiceNames() {
        return namesIn(SELECTED_ROWS);
    }
    public List<String> catalogueServiceNames() {
        return namesIn(CATALOGUE_ROWS);
    }

    private List<String> namesIn(By rowLocator) {
        waitForVisibility(PAGE_READY);
        List<String> names = new ArrayList<>();
        for (WebElement row : driver.findElements(rowLocator)) {
            names.add(row.findElement(NAME_CELL).getText().trim());
        }
        return names;
    }

    // ------------------------------------------------------------------ FR-S1 breakdown

    public String subtotal() {
        return breakdownValue("Subtotal");
    }

    public String discount() {
        return breakdownValue("Discount");
    }

    public String tax() {
        return breakdownValue("Tax");
    }

    public String total() {
        return breakdownValue("Total");
    }

    private String breakdownValue(String label) {
        waitForVisibility(PAGE_READY);
        for (WebElement row : driver.findElements(ALL_TABLE_ROWS)) {
            List<WebElement> headers = row.findElements(By.tagName("th"));
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (headers.size() == 1 && cells.size() == 1
                    && headers.get(0).getText().trim().equalsIgnoreCase(label)) {
                return cells.get(0).getText().trim();
            }
        }
        throw new NoSuchElementException("No Billing Breakdown row labelled '" + label + "'");
    }
}
