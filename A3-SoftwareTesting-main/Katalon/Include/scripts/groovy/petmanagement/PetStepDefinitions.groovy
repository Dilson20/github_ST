package petmanagement;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.LocalDate;

public class PetStepDefinitions {

    private WebDriver driver;
    private OwnerDetailsPage ownerDetailsPage;
    private PetFormPage petFormPage;
    private String baseUrl = "http://localhost:8080";
    private String futurePetName;
    private int editedPetOwnerId;

    @Before
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new"); // Headless mode disabled to display Chrome browser window
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        ownerDetailsPage = new OwnerDetailsPage(driver);
        petFormPage = new PetFormPage(driver);
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Given("the clinic application is running on {string}")
    public void the_clinic_application_is_running_on(String url) {
        this.baseUrl = url;
        driver.get(url);
    }

    @And("an existing owner with ID {int} is present in the system")
    public void an_existing_owner_with_id_is_present_in_the_system(Integer ownerId) {
        ownerDetailsPage.navigateTo(baseUrl, ownerId);
    }

    @Given("I navigate to the pet registration form for owner {int}")
    public void i_navigate_to_the_pet_registration_form_for_owner(Integer ownerId) {
        ownerDetailsPage.navigateTo(baseUrl, ownerId);
        petFormPage = ownerDetailsPage.clickAddNewPet();
    }

    @When("I submit pet details with name {string}, birth date {string}, and type {string}")
    public void i_submit_pet_details_with_name_birth_date_and_type(String name, String birthDate, String type) {
        petFormPage.enterName(name)
                   .enterBirthDate(birthDate)
                   .selectType(type)
                   .clickSubmitExpectingSuccess();
    }

    @When("I submit pet details with name {string} and type {string}")
    public void i_submit_pet_details_with_name_and_type(String name, String type) {
        petFormPage.enterName(name)
                   .enterBirthDate("2023-01-01")
                   .selectType(type)
                   .clickSubmitExpectingError();
    }

    @When("I submit pet details with name {string} and a future birth date {string}")
    public void i_submit_pet_details_with_name_and_a_future_birth_date(String name, String futureDate) {
        futurePetName = name;
        // [IMPROVED] Resolve the business value "tomorrow" at runtime so the scenario never expires.
        String dateToSubmit = "tomorrow".equalsIgnoreCase(futureDate)
                ? LocalDate.now().plusDays(1).toString()
                : futureDate;
        petFormPage.enterName(name)
                   .enterBirthDate(dateToSubmit)
                   .selectType("dog")
                   .clickSubmitExpectingError();
    }

    @Given("I navigate to the owner details page for owner {int}")
    public void i_navigate_to_the_owner_details_page_for_owner(Integer ownerId) {
        ownerDetailsPage.navigateTo(baseUrl, ownerId);
    }

    @Given("an owner has a pet named {string}")
    public void an_owner_has_a_pet_named(String petName) {
        // [IMPROVED] Create the owner and pet this scenario edits, so repeated runs never depend on seeded data
        // that an earlier run has already renamed.
        editedPetOwnerId = new OwnerFormPage(driver)
                .open(baseUrl)
                .registerOwner("Pet", "Edit" + System.currentTimeMillis(), "1 Test Street", "Melbourne", "0400111222");
        // [IMPROVED] Start as a cat so the later change to a different type is a real change, not a no-op.
        petFormPage = ownerDetailsPage.clickAddNewPet();
        petFormPage.enterName(petName)
                   .enterBirthDate("2010-09-07")
                   .selectType("cat")
                   .clickSubmitExpectingSuccess();
        assert ownerDetailsPage.hasPetNamed(petName) : ("Pet " + petName + " should exist before it is edited.");
    }

    @When("I edit the pet named {string} and change the name to {string} and type to {string}")
    public void i_edit_the_pet_named_and_change_the_name_to_and_type_to(String oldName, String newName, String newType) {
        // [IMPROVED] Do not create missing data inside the When step; fail if the Given state is wrong.
        assert ownerDetailsPage.hasPetNamed(oldName) : ("Expected existing pet: " + oldName);
        petFormPage = ownerDetailsPage.clickEditPet(oldName);
        petFormPage.enterName(newName)
                   .selectType(newType)
                   .clickSubmitExpectingSuccess();
    }

    @Then("the pet {string} should be successfully recorded under owner {int}")
    public void the_pet_should_be_successfully_recorded_under_owner(String petName, Integer ownerId) {
        ownerDetailsPage.navigateTo(baseUrl, ownerId);
        assert ownerDetailsPage.hasPetNamed(petName) : ("Pet " + petName + " should exist on owner " + ownerId + " page.");
    }

    @Then("the pet {string} with type {string} should be successfully recorded under owner {int}")
    public void the_pet_with_type_should_be_successfully_recorded_under_owner(String petName, String type, Integer ownerId) {
        ownerDetailsPage.navigateTo(baseUrl, ownerId);
        // [IMPROVED] Verify the type supplied by the scenario, not only the name.
        assert ownerDetailsPage.isPetTypeCorrect(petName, type) : ("Pet " + petName + " should have type " + type + ".");
    }

    @Then("that owner's pet {string} should be recorded with type {string}")
    public void that_owners_pet_should_be_recorded_with_type(String petName, String type) {
        ownerDetailsPage.navigateTo(baseUrl, editedPetOwnerId);
        assert ownerDetailsPage.isPetTypeCorrect(petName, type) : ("Pet " + petName + " should have type " + type + ".");
    }

    @Then("the system should reject the submission and display a validation error")
    public void the_system_should_reject_the_submission_and_display_a_validation_error() {
        // [IMPROVED] URL substring checks alone created a false positive on the form page.
        assert !driver.getCurrentUrl().matches(".*/owners/\\d+(?:;[^/?]*)?/?") : "Invalid pet data must not redirect to the owner page.";
        assert petFormPage.isErrorMessageDisplayed() : "Invalid pet data should display a validation error.";
    }

    @Then("the system should refuse the future birth date and prevent creation")
    public void the_system_should_refuse_the_future_birth_date_and_prevent_creation() {
        // [IMPROVED] Check rejection and persistence separately.
        assert !driver.getCurrentUrl().matches(".*/owners/\\d+(?:;[^/?]*)?/?") : "System should not accept a future birth date.";
        assert petFormPage.isErrorMessageDisplayed() : "Future birth date should display a validation error.";
        ownerDetailsPage.navigateTo(baseUrl, 1);
        assert !ownerDetailsPage.hasPetNamed(futurePetName) : "Future-dated pet must not be created.";
    }
}
