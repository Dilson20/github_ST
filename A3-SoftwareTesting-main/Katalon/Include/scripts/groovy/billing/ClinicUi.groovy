package billing

import java.time.LocalDate

import org.openqa.selenium.WebElement

import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

class ClinicUi {
	static final String BASE_URL = 'http://localhost:8080'
	private static final int TIMEOUT = 10
	private static final String TOTAL_COLUMN_CELL =
			"/td[count(ancestor::table[1]//th[normalize-space()='Total']/preceding-sibling::th)+1]"

	private String ownerUrl
	private String billingScreenUrl
	private String visitId

	String tomorrow() {
		return LocalDate.now().plusDays(1).toString()
	}

	private static TestObject xpath(String name, String expression) {
		TestObject object = new TestObject(name)
		object.addProperty('xpath', ConditionType.EQUALS, expression)
		return object
	}

	private static TestObject field(String id) {
		return xpath("field_" + id, "//*[@id='" + id + "']")
	}

	private static TestObject submitButton() {
		return xpath('submitButton', "//button[@type='submit']")
	}

	private static TestObject addPetLink() {
		return xpath('addPetLink', "//a[contains(@href,'/pets/new')]")
	}

	private static TestObject addVisitLink() {
		return xpath('addVisitLink', "//a[contains(@href,'/visits/new')]")
	}

	private static TestObject manageServicesLink() {
		return xpath('manageServicesLink', "//a[contains(@href,'/services')]")
	}

	private static TestObject billingBreakdownHeading() {
		return xpath('billingBreakdownHeading', "//h3[normalize-space()='Billing Breakdown']")
	}

	private static TestObject catalogueAddButton(String service) {
		return xpath("add_" + service,
				"//h3[normalize-space()='Service Catalog']/following-sibling::table[1]" +
				"//tr[td[1][normalize-space()='" + service + "']]//button")
	}

	private static TestObject selectedRemoveButton(String service) {
		return xpath("remove_" + service,
				"//h3[normalize-space()='Selected Services']/following-sibling::table[1]" +
				"//tr[td[1][normalize-space()='" + service + "']]//button")
	}

	private static TestObject selectedServiceRow(String service) {
		return xpath("selected_" + service,
				"//h3[normalize-space()='Selected Services']/following-sibling::table[1]" +
				"//tr[td[1][normalize-space()='" + service + "']]")
	}

	private static TestObject previousVisitsHeading() {
		return xpath('previousVisitsHeading', "//h3[normalize-space()='Previous Visits']")
	}

	private TestObject ownerPageTotalCell() {
		return xpath("ownerPageTotal_" + visitId,
				"//a[contains(@href,'/visits/" + visitId + "/services')]/ancestor::tr[1]" + TOTAL_COLUMN_CELL)
	}

	private TestObject visitHistoryTotalCell() {
		return xpath("visitHistoryTotal_" + visitId,
				"//h3[normalize-space()='Previous Visits']/following::table[1]" +
				"//a[contains(@href,'/visits/" + visitId + "/services')]/ancestor::tr[1]" + TOTAL_COLUMN_CELL)
	}

	private static TestObject billingCell(String label) {
		return xpath("billing_" + label,
				"//h3[normalize-space()='Billing Breakdown']/following-sibling::table[1]" +
				"//tr[th[normalize-space()='" + label + "']]/td")
	}

	void registerOwner() {
		WebUI.navigateToUrl(BASE_URL + '/owners/new')
		waitUntilShown(field('firstName'), 'open the new owner form')
		WebUI.setText(field('firstName'), 'Automation')
		WebUI.setText(field('lastName'), 'Fixture' + System.currentTimeMillis())
		WebUI.setText(field('address'), '1 Test Street')
		WebUI.setText(field('city'), 'Melbourne')
		WebUI.setText(field('telephone'), '0400111222')
		clickOn(submitButton())
		waitUntilShown(addPetLink(), 'register the fixture owner')
		ownerUrl = withoutSessionId(WebUI.getUrl())
	}

	void addPet() {
		clickOn(addPetLink())
		waitUntilShown(field('name'), 'open the new pet form')
		WebUI.setText(field('name'), 'Rex')
		setDateField('birthDate', '2020-01-01')
		WebUI.selectOptionByValue(field('type'), 'dog', false)
		clickOn(submitButton())
		waitUntilShown(addVisitLink(), 'add the fixture pet')
	}

	void startBookingVisit() {
		clickOn(addVisitLink())
		waitUntilShown(field('date'), 'open the visit booking form')
	}

	void enterVisitDate(String isoDate) {
		setDateField('date', isoDate)
	}

	void submitVisitForm() {
		WebUI.setText(field('description'), 'Automated billing fixture')
		clickOn(submitButton())
	}

	void bookVisitOn(String isoDate) {
		startBookingVisit()
		enterVisitDate(isoDate)
		submitVisitForm()
		waitUntilShown(manageServicesLink(), 'book the fixture visit for ' + isoDate)
	}

	void openManageServices() {
		clickOn(manageServicesLink())
		waitUntilShown(billingBreakdownHeading(), 'open the Manage Services page')
		String url = withoutSessionId(WebUI.getUrl())
		def visit = (url =~ /(\/owners\/\d+\/pets\/\d+\/visits\/(\d+)\/services)/)
		if (!visit.find()) {
			KeywordUtil.markFailedAndStop('TEST MECHANICS, not a billing result: could not identify the booked visit from ' + url)
		}
		billingScreenUrl = BASE_URL + visit.group(1)
		visitId = visit.group(2)
	}

	void addService(String service) {
		clickOn(catalogueAddButton(service))
		waitUntilShown(selectedServiceRow(service), 'add ' + service + ' to the visit')
	}

	void removeService(String service) {
		clickOn(selectedRemoveButton(service))
		require(WebUI.waitForElementNotPresent(selectedServiceRow(service), TIMEOUT),
				'remove ' + service + ' from the visit')
		waitUntilShown(billingBreakdownHeading(), 'redisplay Manage Services after removing ' + service)
	}

	String billingFigure(String label) {
		waitUntilShown(billingCell(label), 'read the ' + label + ' figure')
		return WebUI.getText(billingCell(label)).trim()
	}

	String ownerPageTotal() {
		WebUI.navigateToUrl(ownerUrl)
		waitUntilShown(ownerPageTotalCell(), "find the visit on the owner's page")
		return WebUI.getText(ownerPageTotalCell()).trim()
	}

	String visitHistoryTotal() {
		WebUI.navigateToUrl(ownerUrl)
		clickOn(addVisitLink())
		waitUntilShown(previousVisitsHeading(), 'open the Previous Visits list on the New Visit form')
		waitUntilShown(visitHistoryTotalCell(), "find the visit in the pet's visit history")
		return WebUI.getText(visitHistoryTotalCell()).trim()
	}

	void returnToBillingScreen() {
		WebUI.navigateToUrl(billingScreenUrl)
		waitUntilShown(billingBreakdownHeading(), 'return to the Manage Services page')
	}

	private static String withoutSessionId(String url) {
		return url.replaceAll(';jsessionid=[^/?#]*', '')
	}

	private void clickOn(TestObject target) {
		require(WebUI.waitForElementClickable(target, TIMEOUT), 'find a clickable ' + target.getObjectId())
		WebElement element = WebUI.findWebElement(target, TIMEOUT)
		WebUI.executeJavaScript(
				"arguments[0].scrollIntoView({block: 'center', inline: 'nearest', behavior: 'instant'});",
				Arrays.asList(element))
		WebUI.click(target)
	}

	private void waitUntilShown(TestObject target, String step) {
		require(WebUI.waitForElementVisible(target, TIMEOUT), step)
	}

	private static void require(boolean reached, String step) {
		if (!reached) {
			KeywordUtil.markFailedAndStop(
					'TEST MECHANICS, not a billing result: could not ' + step +
					' within ' + TIMEOUT + ' seconds.')
		}
	}

	private void setDateField(String id, String isoDate) {
		WebUI.executeJavaScript(
				"var element = document.getElementById(arguments[0]);" +
				"element.value = arguments[1];" +
				"element.dispatchEvent(new Event('input', {bubbles: true}));" +
				"element.dispatchEvent(new Event('change', {bubbles: true}));",
				Arrays.asList(id, isoDate))
	}
}
