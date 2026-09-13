package billing

import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

class VisitBillingSteps {

	private ClinicUi clinic = new ClinicUi()

	// -------------------------------------------------------------- lifecycle

	@Before
	void openTheClinicApplication() {
		WebUI.openBrowser('')
		WebUI.maximizeWindow()
	}

	@After
	void closeTheClinicApplication() {
		WebUI.closeBrowser()
	}

	@Given("a visit has been booked for an owner's pet")
	void a_visit_has_been_booked_for_an_owners_pet() {
		clinic.registerOwner()
		clinic.addPet()
		clinic.bookVisitOn(clinic.tomorrow())
		clinic.openManageServices()
	}

	@When("the receptionist adds the services {string} to the visit")
	void the_receptionist_adds_the_services_to_the_visit(String services) {
	    if (services.trim().equalsIgnoreCase('none')) {
	        KeywordUtil.markPassed('No services were added; the visit carries only the base visit fee.')
	        return
	    }
	    for (String service : services.split(',')) {
	        clinic.addService(service.trim())
	    }
	}

	@When("the receptionist removes the {string} service from the visit")
	void the_receptionist_removes_the_service_from_the_visit(String service) {
		clinic.removeService(service)
	}

	@Then("the billing screen, the owner's page and the pet's visit history all show a total of {string}")
	void every_screen_shows_the_same_total(String expected) {
		String billingScreen = clinic.billingFigure('Total')
		String ownersPage = clinic.ownerPageTotal()
		String visitHistory = clinic.visitHistoryTotal()
		clinic.returnToBillingScreen()
		if (billingScreen == expected && ownersPage == expected && visitHistory == expected) {
			KeywordUtil.markPassed('All three screens show ' + expected + ', as FR-S6 and the billing rule require.')
		} else if (billingScreen == ownersPage && ownersPage == visitHistory) {
			KeywordUtil.markFailedAndStop('All three screens agree on ' + billingScreen +
					' but the specification requires ' + expected + '.')
		} else {
			KeywordUtil.markFailedAndStop('FR-S6: the visit total is not identical across screens: billing screen ' +
					billingScreen + ", owner's page " + ownersPage + ", pet's visit history " + visitHistory +
					'. The specification requires ' + expected + '.')
		}
	}

	@Then("the billing screen shows a subtotal of {string}")
	void the_billing_screen_shows_a_subtotal_of(String expected) {
		verifyBillingFigure('Subtotal', expected)
	}

	@Then("the billing screen shows a discount of {string}")
	void the_billing_screen_shows_a_discount_of(String expected) {
		verifyBillingFigure('Discount', expected)
	}

	@Then("the billing screen shows tax of {string}")
	void the_billing_screen_shows_tax_of(String expected) {
		verifyBillingFigure('Tax', expected)
	}

	@Then("the billing screen shows a total of {string}")
	void the_billing_screen_shows_a_total_of(String expected) {
		verifyBillingFigure('Total', expected)
	}

	private void verifyBillingFigure(String label, String expected) {
		String actual = clinic.billingFigure(label)
		if (actual == expected) {
			KeywordUtil.markPassed(label + ' is ' + actual + ', as the billing rule requires.')
		} else {
			KeywordUtil.markFailedAndStop(
					label + ': the specification requires ' + expected +
					' but the application displayed ' + actual + '.')
		}
	}
}
