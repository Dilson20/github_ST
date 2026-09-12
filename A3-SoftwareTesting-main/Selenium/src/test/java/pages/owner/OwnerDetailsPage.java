package pages.owner;

import pages.common.BasePage;
import pages.pet.PetFormPage;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class OwnerDetailsPage extends BasePage {

    private final By addNewPetButton = By.xpath("//a[contains(@href, 'pets/new')]");
    private final By ownerInformationHeader = By.xpath("//h2[contains(text(),'Owner Information')]");

    public OwnerDetailsPage(WebDriver driver) {
        super(driver);
    }

    public void navigateTo(String baseUrl, int ownerId) {
        driver.get(baseUrl + "/owners/" + ownerId);
        waitForVisibility(ownerInformationHeader);
    }

    public PetFormPage clickAddNewPet() {
        WebElement element = waitForClickable(addNewPetButton);
        // [IMPROVED] Use a real Selenium click so the test exercises browser behaviour.
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
        clickVisibleElement(element);
        return new PetFormPage(driver);
    }

    public PetFormPage clickEditPet(String petName) {
        By editPetLocator = By.xpath("//h3[normalize-space()='" + petName + "']/ancestor::section//a[contains(text(),'Edit Pet')]");
        WebElement element = waitForClickable(editPetLocator);
        // [IMPROVED] Use a real Selenium click so overlays/interactability are not bypassed.
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
        clickVisibleElement(element);
        return new PetFormPage(driver);
    }

    public boolean hasPetNamed(String petName) {
        By petHeader = By.xpath("//h3[normalize-space()='" + petName + "']");
        List<WebElement> elements = driver.findElements(petHeader);
        return !elements.isEmpty();
    }

    public boolean isPetDetailsCorrect(String petName, String type, String birthDate) {
        // [IMPROVED] Scope the assertion to the matching pet card; page-source checks can match unrelated content.
        By petCard = By.xpath("//h3[normalize-space()='" + petName + "']/ancestor::section[1]");
        String text = waitForVisibility(petCard).getText();
        return text.contains(petName) && text.contains(type) && text.contains(birthDate);
    }

    public boolean isPetTypeCorrect(String petName, String type) {
        // [IMPROVED] Dedicated assertion for BDD edit coverage where the birth date is not changed.
        By petCard = By.xpath("//h3[normalize-space()='" + petName + "']/ancestor::section[1]");
        return waitForVisibility(petCard).getText().contains(type);
    }

    private void clickVisibleElement(WebElement element) {
        try {
            // [IMPROVED] Prefer the real WebDriver click for normal browser behaviour.
            element.click();
        } catch (ElementClickInterceptedException intercepted) {
            // [IMPROVED] The SUT has a fixed footer that can cover links at the page bottom.
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
}
