package petmanagement;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OwnerFormPage extends BasePage {

    private final By firstNameInput = By.id("firstName");
    private final By lastNameInput = By.id("lastName");
    private final By addressInput = By.id("address");
    private final By cityInput = By.id("city");
    private final By telephoneInput = By.id("telephone");
    private final By submitButton = By.cssSelector("button[type='submit']");

    public OwnerFormPage(WebDriver driver) {
        super(driver);
    }

    public OwnerFormPage open(String baseUrl) {
        driver.get(baseUrl + "/owners/new");
        waitForVisibility(firstNameInput);
        return this;
    }

    public int registerOwner(String firstName, String lastName, String address, String city, String telephone) {
        waitForVisibility(firstNameInput).sendKeys(firstName);
        driver.findElement(lastNameInput).sendKeys(lastName);
        driver.findElement(addressInput).sendKeys(address);
        driver.findElement(cityInput).sendKeys(city);
        driver.findElement(telephoneInput).sendKeys(telephone);
        waitForClickable(submitButton).click();
        // [IMPROVED] Match only the owner details route so a redisplayed form is not mistaken for success.
        // [IMPROVED] Anchor with \$ as in PetFormPage: urlMatches uses find(), so an unanchored pattern
        // would also accept any deeper /owners/{id}/... page.
        wait.until(ExpectedConditions.urlMatches(".*/owners/\\d+(?:;[^/?]*)?/?\$"));
        Matcher ownerId = Pattern.compile("/owners/(\\d+)").matcher(driver.getCurrentUrl());
        if (!ownerId.find()) {
            throw new IllegalStateException("Could not read the new owner's id from " + driver.getCurrentUrl());
        }
        return Integer.parseInt(ownerId.group(1));
    }
}
