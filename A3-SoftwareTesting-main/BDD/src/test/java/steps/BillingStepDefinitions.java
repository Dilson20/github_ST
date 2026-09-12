package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pages.BillingFixtureFlow;
import pages.BillingServicesPage;
import pages.VisitBookingPage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for
 *   BDD/src/test/resources/features/billing/VisitServicesAndBilling.feature
 *
 * Covers three scenarios:
 *   @TC-VISIT-01  Visit date boundary validation (Scenario Outline, offsets -1/0/+1)
 *   @TC-BILL-01   Billing total for selected services (Scenario Outline, data-driven)
 *   @TC-VISIT-02  Reject a visit dated today (standalone Scenario)
 *
 * All three scenarios share the same WebDriver instance (per-Scenario lifecycle
 * via @Before / @After), which Cucumber JUnit Platform Engine manages.
 */
public class BillingStepDefinitions {

    private static final String BASE_URL = "http://localhost:8080";

    private WebDriver          driver;
    private BillingServicesPage servicesPage;
    private VisitBookingPage    visitBookingPage;

    // ------------------------------------------------------------------ lifecycle

    @Before("@US-VISIT-BILLING")
    public void openBrowser() {
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--remote-allow-origins=*");
        opts.addArguments("--window-size=1920,1080");
        // Pin locale so billing labels are deterministic regardless of machine setting.
        opts.addArguments("--lang=en-US");
        opts.setExperimentalOption("prefs", java.util.Map.of("intl.accept_languages", "en-US,en"));
        driver = new ChromeDriver(opts);
    }

    @After("@US-VISIT-BILLING")
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    // ------------------------------------------------------------------ @TC-VISIT-01 / @TC-VISIT-02

    @Given("an owner has a pet with no recorded visits")
    public void an_owner_has_a_pet_with_no_recorded_visits() {
        // Create a fresh owner/pet pair and open the Add Visit form.
        visitBookingPage = new BillingFixtureFlow(driver, BASE_URL)
                .createOwnerPetAndOpenVisitForm();
    }

    @When("I attempt to book a visit dated {int} day\\(s\\) from today")
    public void i_attempt_to_book_a_visit_dated_offset_days_from_today(int offset) {
        String isoDate = LocalDate.now().plusDays(offset).toString();
        visitBookingPage.setDate(isoDate)
                        .setDescription("BDD date-boundary test offset=" + offset);
        // Submit and let the Then step decide pass/fail — do not block here
        visitBookingPage.submitNoWait();
    }

    @When("I attempt to book a visit dated today")
    public void i_attempt_to_book_a_visit_dated_today() {
        String today = LocalDate.now().toString();
        visitBookingPage.setDate(today)
                        .setDescription("BDD same-day boundary test");
        visitBookingPage.submitNoWait();
    }

    @Then("the booking should be {word}")
    public void the_booking_should_be(String expectedOutcome) {
        // Give SUT a short moment to redirect or show error
        try { Thread.sleep(1500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

        String currentUrl = driver.getCurrentUrl();
        boolean redirectedToOwnerDetail = currentUrl.matches(".*/owners/\\d+(?:;[^/?]*)?/?$");
        boolean hasError = visitBookingPage.isRejected();

        if ("rejected".equalsIgnoreCase(expectedOutcome)) {
            // Expect to remain on the visit form with a validation error
            assertFalse(redirectedToOwnerDetail,
                    "DEFECT: Visit with past/today date should be REJECTED but SUT accepted it. URL: " + currentUrl);
            assertTrue(hasError,
                    "DEFECT: Visit should show a validation error for this date. URL: " + currentUrl);
        } else {
            // "accepted" — the SUT should have saved the visit
            assertTrue(redirectedToOwnerDetail,
                    "Visit with a future date should be ACCEPTED but SUT rejected it. URL: " + currentUrl);
        }
    }

    @Then("the visit should be rejected with a validation error")
    public void the_visit_should_be_rejected_with_a_validation_error() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        String currentUrl = driver.getCurrentUrl();
        boolean redirected = currentUrl.matches(".*/owners/\\d+(?:;[^/?]*)?/?$");
        assertFalse(redirected,
                "DEFECT: A visit dated today should be REJECTED but SUT redirected to: " + currentUrl);
        assertTrue(visitBookingPage.isRejected(),
                "A visit dated today should show a validation error. URL: " + currentUrl);
    }

    // ------------------------------------------------------------------ @TC-BILL-01

    @Given("a visit exists with no services selected")
    public void a_visit_exists_with_no_services_selected() {
        servicesPage = new BillingFixtureFlow(driver, BASE_URL)
                .createOwnerPetVisitAndOpenServices();
        // Verify precondition: no services and base fee present.
        assertTrue(servicesPage.selectedServiceNames().isEmpty(),
                "A fresh visit must start with no selected services, " +
                "but found: " + servicesPage.selectedServiceNames());
        assertEquals("40.00", servicesPage.subtotal(),
                "FR-S1: base visit fee 40.00 must apply before any service is added");
    }

    @When("I add {string} and {string} to the visit")
    public void i_add_services_to_the_visit(String service1, String service2) {
        // service1 should match the SUT catalogue name exactly (e.g. "X-Ray" not "XRay")
        if (service1 != null && !service1.isBlank()) {
            servicesPage.addService(service1);
        }
        if (service2 != null && !service2.isBlank()) {
            servicesPage.addService(service2);
        }
    }

    @Then("the displayed total should be {string}")
    public void the_displayed_total_should_be(String expectedTotal) {
        assertEquals(expectedTotal, servicesPage.total(),
                "FR-S4/FR-S5: displayed billing total must match the specification");
    }
}
