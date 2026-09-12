package pages.billing;

import pages.common.BasePage;
import pages.owner.OwnerDetailsPage;
import pages.pet.PetFormPage;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDate;
import java.util.UUID;

public class VisitFixtureFlow extends BasePage {

    private static final By OWNER_FIRST_NAME = By.id("firstName");
    private static final By OWNER_LAST_NAME = By.id("lastName");
    private static final By OWNER_ADDRESS = By.id("address");
    private static final By OWNER_CITY = By.id("city");
    private static final By OWNER_TELEPHONE = By.id("telephone");

    private static final By VISIT_DATE = By.id("date");
    private static final By VISIT_DESCRIPTION = By.id("description");

    private static final By SUBMIT = By.cssSelector("button[type='submit']");

    private static final By ADD_VISIT_LINK = By.cssSelector("a[href$='/visits/new']");
    private static final By MANAGE_SERVICES_LINK = By.cssSelector("a[href$='/services']");

    private static final String OWNER_DETAIL_URL = ".*/owners/\\d+(?:;[^/?]*)?/?$";

    private final String baseUrl;

    public VisitFixtureFlow(WebDriver driver, String baseUrl) {
        super(driver);
        this.baseUrl = baseUrl;
    }

    public VisitServicesPage createOwnerWithPetAndVisit() {
        registerOwner();
        addPet();
        addVisit();
        return openManageServices();
    }

    private void registerOwner() {
        String uniqueLastName = "Probe" + UUID.randomUUID().toString().substring(0, 8);
        driver.get(baseUrl + "/owners/new");
        waitForVisibility(OWNER_FIRST_NAME).sendKeys("Cal");
        waitForVisibility(OWNER_LAST_NAME).sendKeys(uniqueLastName);
        waitForVisibility(OWNER_ADDRESS).sendKeys("1 Test Street");
        waitForVisibility(OWNER_CITY).sendKeys("Melbourne");
        waitForVisibility(OWNER_TELEPHONE).sendKeys("0400111222");
        waitForClickable(SUBMIT).click();
        wait.until(ExpectedConditions.urlMatches(OWNER_DETAIL_URL));
    }

    private void addPet() {
        PetFormPage petForm = new OwnerDetailsPage(driver).clickAddNewPet();
        petForm.enterName("Rex")
               .enterBirthDate("2020-01-01")
               .selectType("dog")
               .clickSubmitExpectingSuccess();
    }

    private void addVisit() {
        waitForClickable(ADD_VISIT_LINK).click();
        setDateInput(VISIT_DATE, LocalDate.now().plusDays(1).toString());
        waitForVisibility(VISIT_DESCRIPTION).sendKeys("Billing scenario fixture");
        waitForClickable(SUBMIT).click();
        wait.until(ExpectedConditions.urlMatches(OWNER_DETAIL_URL));
    }

    private VisitServicesPage openManageServices() {
        // The fixture owner has exactly one pet and one visit, so exactly one such link exists.
        waitForClickable(MANAGE_SERVICES_LINK).click();
        return new VisitServicesPage(driver).waitUntilLoaded();
    }

    private void setDateInput(By locator, String isoDate) {
        WebElement input = waitForVisibility(locator);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));"
                        + "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                input, isoDate);
    }
}
