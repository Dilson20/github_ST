package tests.pet;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pages.owner.OwnerDetailsPage;
import pages.pet.PetFormPage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetManagementTest {

    private static final String BASE_URL = "http://localhost:8080";
    private WebDriver driver;
    private OwnerDetailsPage ownerDetailsPage;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new"); // Headless mode disabled to display Chrome browser window
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        ownerDetailsPage = new OwnerDetailsPage(driver);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("TC-PET-01 [AC-01]: Successfully add a new pet for an existing owner")
    public void testAddNewPetSuccess() {
        ownerDetailsPage.navigateTo(BASE_URL, 1);

        PetFormPage petForm = ownerDetailsPage.clickAddNewPet();
        String petName = "Maximus" + (System.currentTimeMillis() % 1000);
        petForm.enterName(petName)
               .enterBirthDate("2023-04-15")
               .selectType("dog");

        OwnerDetailsPage resultPage = petForm.clickSubmitExpectingSuccess();
        // [IMPROVED] Verify every submitted field, not only the pet name.
        assertTrue(resultPage.isPetDetailsCorrect(petName, "dog", "2023-04-15"),
                "Newly added pet should preserve name, type, and birth date.");
    }

    @ParameterizedTest(name = "TC-PET-02 [AC-01, AC-04]: Data-Driven Add Pet [Name: {0}, Species: {2}]")
    @CsvSource({
            "Whiskers, 2022-01-10, cat",
            "Tweety, 2023-03-15, bird",
            "Hammy, 2022-11-20, hamster",
            "Sly, 2021-08-05, snake",
            "Iggy, 2020-05-12, lizard"
    })
    @Order(2)
    @DisplayName("TC-PET-02 [AC-01, AC-04]: Data-driven pet creation across diverse species")
    public void testDataDrivenAddPet(String name, String birthDate, String type) {
        ownerDetailsPage.navigateTo(BASE_URL, 2);

        PetFormPage petForm = ownerDetailsPage.clickAddNewPet();
        String uniqueName = name + (System.currentTimeMillis() % 1000);

        petForm.enterName(uniqueName)
               .enterBirthDate(birthDate)
               .selectType(type);

        OwnerDetailsPage resultPage = petForm.clickSubmitExpectingSuccess();
        // [IMPROVED] The data-driven test now verifies the species and date as well as the name.
        assertTrue(resultPage.isPetDetailsCorrect(uniqueName, type, birthDate),
                "Pet should preserve name, type, and birth date.");
    }

    @Test
    @Order(3)
    @DisplayName("TC-PET-03 [AC-02]: Validate pet creation fails when Name is blank")
    public void testAddPetWithBlankNameValidation() {
        ownerDetailsPage.navigateTo(BASE_URL, 1);

        PetFormPage petForm = ownerDetailsPage.clickAddNewPet();
        String formUrl = petForm.getCurrentUrl();
        petForm.enterName("")
               .enterBirthDate("2023-01-01")
               .selectType("dog")
               .clickSubmitExpectingError();

        // [IMPROVED] The old URL check was always true on the registration page.
        assertEquals(formUrl, petForm.getCurrentUrl(), "Blank name must not redirect to the owner page.");
        assertTrue(petForm.isErrorMessageDisplayed(), "Blank name should produce a validation error.");
    }

    @Test
    @Order(4)
    @DisplayName("TC-PET-04 [AC-03] (Defect Detection): Validate system rejection of future birth dates")
    public void testFutureBirthDateBehavior() {
        ownerDetailsPage.navigateTo(BASE_URL, 1);

        PetFormPage petForm = ownerDetailsPage.clickAddNewPet();
        // [IMPROVED] Always generate a date that is tomorrow, so the test does not expire.
        String futureDate = LocalDate.now().plusDays(1).toString();
        String futurePetName = "FuturePup" + (System.currentTimeMillis() % 1000);

        petForm.enterName(futurePetName)
               .enterBirthDate(futureDate)
               .selectType("dog");

        // The specification prohibits future birth dates.
        petForm.clickSubmitExpectingError();
        // [IMPROVED] Assert both rejection and non-creation; a redirect is a real test failure.
        assertFalse(driver.getCurrentUrl().matches(".*/owners/\\d+(?:;[^/?]*)?/?$"),
                "DEFECT DETECTED: SUT accepted future birth date (" + futureDate + ").");
        assertTrue(petForm.isErrorMessageDisplayed(), "Future birth date should produce a validation error.");

        ownerDetailsPage.navigateTo(BASE_URL, 1);
        assertFalse(ownerDetailsPage.hasPetNamed(futurePetName),
                "Future-dated pet must not be created.");
    }

    @Test
    @Order(5)
    @DisplayName("TC-PET-05 [AC-05]: Edit an existing pet and update its profile")
    public void testEditExistingPet() {
        ownerDetailsPage.navigateTo(BASE_URL, 1);

        // Create a unique pet for this test to ensure test isolation and repeatability
        String petToEdit = "PetForEdit" + (System.currentTimeMillis() % 1000);
        PetFormPage petForm = ownerDetailsPage.clickAddNewPet();
        petForm.enterName(petToEdit)
               .enterBirthDate("2023-01-01")
               .selectType("dog")
               .clickSubmitExpectingSuccess();

        // Edit this newly created pet
        PetFormPage editForm = ownerDetailsPage.clickEditPet(petToEdit);
        assertEquals(petToEdit, editForm.getPetNameValue(), "Form should be pre-populated with pet name: " + petToEdit);

        String updatedName = petToEdit + "Updated";
        editForm.enterName(updatedName)
                .selectType("bird");

        OwnerDetailsPage resultPage = editForm.clickSubmitExpectingSuccess();
        // [IMPROVED] Verify that edit updates the requested fields and keeps the pet record visible.
        assertTrue(resultPage.isPetDetailsCorrect(updatedName, "bird", "2023-01-01"),
                "Edited pet should display the updated name, type, and birth date.");
    }
}
