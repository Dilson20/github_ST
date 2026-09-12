package steps.billing

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.common.WebUiCommonHelper as WebUiCommonHelper
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import org.openqa.selenium.WebElement as WebElement
import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import io.cucumber.java.en.Then
import io.cucumber.java.Before
import io.cucumber.java.After

class VisitServicesAndBillingSteps {

    static final String PAGE = 'Page_Testing-Site  a Spring Framework demonstration'

    @Before("@US-VISIT-BILLING")
    void setUp() {
        WebUI.openBrowser(null)
        WebUI.maximizeWindow()
    }

    @After("@US-VISIT-BILLING")
    void tearDown() {
        WebUI.closeBrowser()
    }

    private void setDateField(String testObjectPath, String dateStr) {
        WebElement el = WebUiCommonHelper.findWebElement(findTestObject(testObjectPath), 10)
        WebUI.executeJavaScript(
            "arguments[0].value = arguments[1];" +
            "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
            "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
            Arrays.asList(el, dateStr))
    }

    // Shared helper: creates a fresh owner + pet, lands back on owner detail page
    def createOwnerAndPet() {
        WebUI.navigateToUrl('http://localhost:8080/')
        WebUI.click(findTestObject(PAGE + '/span_Find Owners'))
        WebUI.click(findTestObject(PAGE + '/a_Add Owner'))
        WebUI.setText(findTestObject(PAGE + '/input_First Name'), 'Trong')
        WebUI.setText(findTestObject(PAGE + '/input_Last Name'), 'Tran')
        WebUI.setText(findTestObject(PAGE + '/input_Address'), 'Brown street')
        WebUI.setText(findTestObject(PAGE + '/input_City'), 'Ho Chi Minh city')
        WebUI.setText(findTestObject(PAGE + '/input_Telephone'), '0123456789')
        WebUI.click(findTestObject(PAGE + '/button_Add Owner'))

        def todayDate = new Date().format('yyyy-MM-dd')
        WebUI.click(findTestObject(PAGE + '/a_Add New Pet'))
        WebUI.setText(findTestObject(PAGE + '/input_Name'), 'Dirty')
        WebUI.selectOptionByValue(findTestObject(PAGE + '/select_Type'), 'cat', false)
        setDateField(PAGE + '/input_Birth Date', todayDate)
        WebUI.click(findTestObject(PAGE + '/button_Add Pet'))
        WebUI.waitForElementVisible(findTestObject(PAGE + '/a_Add Visit'), 10)
    }

    private String sanitizeServiceName(String name) {
        if (name == null) return ''
        return name.replace('-', '').replace(' ', '')
    }

    // ===== Visit date boundary scenarios =====

    @Given("an owner has a pet with no recorded visits")
    def owner_has_pet_with_no_visits() {
        createOwnerAndPet()
    }

    @When("I attempt to book a visit dated {int} day\\(s\\) from today")
    def book_visit_with_offset(Integer offset) {
        def cal = Calendar.getInstance()
        cal.add(Calendar.DATE, offset)
        def visitDate = cal.getTime().format('yyyy-MM-dd')

        WebUI.waitForElementVisible(findTestObject(PAGE + '/a_Add Visit'), 10)
        WebUI.click(findTestObject(PAGE + '/a_Add Visit'))
        setDateField(PAGE + '/input_Date_date', visitDate)
        WebUI.setText(findTestObject(PAGE + '/input_Description'), 'Checkup - offset ' + offset + ' day(s) test')
        WebUI.click(findTestObject(PAGE + '/button_Add Visit'))
    }

    @Then("the booking should be {word}")
    def booking_should_be(String result) {
        WebUI.delay(1)
        String currentUrl = WebUI.getUrl()
        boolean accepted = !currentUrl.contains('/visits/new')
        if (result == 'accepted') {
            WebUI.verifyEqual(accepted, true)
        } else {
            WebUI.verifyEqual(accepted, false)
        }
    }

    @When("I attempt to book a visit dated today")
    def book_visit_today() {
        def todayDate = new Date().format('yyyy-MM-dd')
        WebUI.waitForElementVisible(findTestObject(PAGE + '/a_Add Visit'), 10)
        WebUI.click(findTestObject(PAGE + '/a_Add Visit'))
        setDateField(PAGE + '/input_Date_date', todayDate)
        WebUI.setText(findTestObject(PAGE + '/input_Description'), 'Same-day booking test')
        WebUI.click(findTestObject(PAGE + '/button_Add Visit'))
    }

    @Then("the visit should be rejected with a validation error")
    def visit_should_be_rejected() {
        WebUI.delay(1)
        String currentUrl = WebUI.getUrl()
        boolean rejected = currentUrl.contains('/visits/new')
        WebUI.verifyEqual(rejected, true)
    }

    // ===== Billing scenarios =====

    @Given("a visit exists with no services selected")
    def visit_exists_with_no_services() {
        createOwnerAndPet()

        def cal = Calendar.getInstance()
        cal.add(Calendar.DATE, 1)
        def visitDate = cal.getTime().format('yyyy-MM-dd')

        WebUI.waitForElementVisible(findTestObject(PAGE + '/a_Add Visit'), 10)
        WebUI.click(findTestObject(PAGE + '/a_Add Visit'))
        setDateField(PAGE + '/input_Date_date', visitDate)
        WebUI.setText(findTestObject(PAGE + '/input_Description'), 'Billing scenario visit')
        WebUI.click(findTestObject(PAGE + '/button_Add Visit'))
        WebUI.waitForElementVisible(findTestObject(PAGE + '/a_Manage Services'), 10)
        WebUI.click(findTestObject(PAGE + '/a_Manage Services'))
    }

    @When("I add {string} and {string} to the visit")
    def add_services_to_visit(String service1, String service2) {
        if (service1 != null && !service1.trim().isEmpty()) {
            WebUI.click(findTestObject(PAGE + '/button_Add_' + sanitizeServiceName(service1)))
        }
        if (service2 != null && !service2.trim().isEmpty()) {
            WebUI.click(findTestObject(PAGE + '/button_Add_' + sanitizeServiceName(service2)))
        }
    }

    @Then("the displayed total should be {string}")
    def verify_displayed_total(String expectedTotal) {
        WebUI.verifyElementText(findTestObject(PAGE + '/td_TotalValue'), expectedTotal)
    }
}
