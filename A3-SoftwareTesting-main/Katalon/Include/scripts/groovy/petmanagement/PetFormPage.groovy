package petmanagement;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class PetFormPage extends BasePage {

    // Stable Locators
    private final By nameInput = By.id("name");
    private final By birthDateInput = By.id("birthDate");
    private final By typeSelect = By.id("type");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By errorMessage = By.cssSelector(".has-error, .invalid-feedback, .help-block, .alert-danger");
    private final By form = By.cssSelector("form");

    public PetFormPage(WebDriver driver) {
        super(driver);
    }

    public PetFormPage enterName(String name) {
        WebElement element = waitForVisibility(nameInput);
        element.clear();
        if (name != null && !name.isEmpty()) {
            element.sendKeys(name);
        }
        return this;
    }

    public PetFormPage enterBirthDate(String birthDate) {
        WebElement element = waitForVisibility(birthDateInput);
        // [IMPROVED] Keep the browser-compatible date assignment, then dispatch the normal form events.
        // Chrome date inputs can reject ISO text sent as keystrokes on macOS.
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];" +
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                element, birthDate);
        return this;
    }

    public PetFormPage selectType(String petType) {
        WebElement selectElement = waitForVisibility(typeSelect);
        Select select = new Select(selectElement);
        if (petType != null && !petType.isEmpty()) {
            select.selectByVisibleText(petType);
        }
        return this;
    }

    public OwnerDetailsPage clickSubmitExpectingSuccess() {
        WebElement btn = waitForClickable(submitButton);
        // [IMPROVED] A normal click preserves native form validation behaviour.
        btn.click();
        // [IMPROVED] Match only the owner details route; /owners/{id}/pets/new is not success.
        // [IMPROVED] Anchor with \$: urlMatches uses find(), so without it the pet form URL
        // /owners/{id}/pets/new already matches and the wait returns before the save completes.
        wait.until(ExpectedConditions.urlMatches(".*/owners/\\d+(?:;[^/?]*)?/?\$"));
        return new OwnerDetailsPage(driver);
    }

    public PetFormPage clickSubmitExpectingError() {
        // [IMPROVED] Accept either native/server validation or a redirect, then let the test assert the outcome.
        String formUrl = driver.getCurrentUrl();
        WebElement btn = waitForClickable(submitButton);
        btn.click();
        wait.until(new org.openqa.selenium.support.ui.ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return !d.getCurrentUrl().equals(formUrl) || hasVisibleError(d) || isFormInvalid(d);
            }
        });
        return this;
    }

    public boolean isErrorMessageDisplayed() {
        return hasVisibleError(driver) || isFormInvalid(driver);
    }

    public boolean isOnForm() {
        return !driver.findElements(form).isEmpty();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPetNameValue() {
        return waitForVisibility(nameInput).getAttribute("value");
    }

    public String getSelectedType() {
        Select select = new Select(waitForVisibility(typeSelect));
        return select.getFirstSelectedOption().getText();
    }

    private boolean hasVisibleError(WebDriver currentDriver) {
        try {
            for (WebElement el : currentDriver.findElements(errorMessage)) {
                if (el.isDisplayed()) {
                    return true;
                }
            }
            return false;
        } catch (StaleElementReferenceException ignored) {
            return false;
        }
    }

    private boolean isFormInvalid(WebDriver currentDriver) {
        List<WebElement> forms = currentDriver.findElements(form);
        if (forms.isEmpty()) {
            return false;
        }
        try {
            Object valid = ((JavascriptExecutor) currentDriver)
                    .executeScript("return arguments[0].checkValidity();", forms.get(0));
            return Boolean.FALSE.equals(valid);
        } catch (StaleElementReferenceException ignored) {
            return false;
        }
    }
}
