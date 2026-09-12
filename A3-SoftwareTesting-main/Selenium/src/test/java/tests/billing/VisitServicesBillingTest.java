package tests.billing;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pages.billing.VisitFixtureFlow;
import pages.billing.VisitServicesPage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Visit Services and Billing - FR-S1..FR-S5 (SCRUM-93, SCRUM-94, SCRUM-113, SCRUM-114)")
public class VisitServicesBillingTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final BigDecimal BASE_VISIT_FEE = new BigDecimal("40.00");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("150.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private static WebDriver driver;
    private VisitServicesPage servicesPage;

    // ------------------------------------------------------------------ lifecycle

    @BeforeAll
    static void openBrowser() {
        driver = new ChromeDriver(buildChromeOptions());
    }

    @BeforeEach
    void createFreshVisitFixture() {
        servicesPage = new VisitFixtureFlow(driver, BASE_URL).createOwnerWithPetAndVisit();

        // Fixture precondition, and incidentally FR-S1: a visit with no services still carries
        // the 40.00 base fee. Also proves nothing leaked in from the previous test.
        assertAll("fresh visit baseline (FR-S1: base fee applies with no services selected)",
                () -> assertTrue(servicesPage.selectedServiceNames().isEmpty(),
                        "A new visit must start with no selected services"),
                () -> assertEquals("40.00", servicesPage.subtotal(),
                        "FR-S1: base visit fee 40.00 applies to every visit"),
                () -> assertEquals("43.20", servicesPage.total(),
                        "FR-S1: 40.00 + 8% tax = 43.20 on an empty visit"));
    }

    @AfterAll
    static void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ------------------------------------------------------------------ SCRUM-93 / US-CAL-01

    @ParameterizedTest(name = "{0} ({1}): subtotal {3} -> discount {4}, tax {5}, total {6}")
    @CsvSource({
            // testCaseId, jiraId,   services,                        subtotal, discount, tax,   total
            "TC-CAL-01, SCRUM-58, 'Vaccination|X-Ray',              140.00,   0.00,    11.20, 151.20",
            "TC-CAL-02, SCRUM-59, 'Microchipping|X-Ray',            150.00,   15.00,   10.80, 145.80",
            "TC-CAL-03, SCRUM-60, 'Dental Cleaning|X-Ray',          170.00,   17.00,   12.24, 165.24"
    })
    @DisplayName("TC-CAL-01/02/03 [FR-S4]: loyalty discount applies exactly at subtotal >= 150.00")
    void tcCal01to03_discountThresholdBoundary(String testCaseId, String jiraId, String services,
                                               String expectedSubtotal, String expectedDiscount,
                                               String expectedTax, String expectedTotal) {
        List<String> chosen = List.of(services.split("\\|"));
        chosen.forEach(servicesPage::addService);

        // FR-S2 AC-01: adding a service updates the selected list.
        assertTrue(servicesPage.selectedServiceNames().containsAll(chosen),
                testCaseId + " [FR-S2]: every added service must appear in Selected Services, but found "
                        + servicesPage.selectedServiceNames());

        assertAll(testCaseId + " (" + jiraId + ") billing breakdown for subtotal " + expectedSubtotal,
                () -> assertEquals(expectedSubtotal, servicesPage.subtotal(),
                        testCaseId + " [FR-S1]: subtotal must be 40.00 base plus the selected service fees"),
                () -> assertEquals(expectedDiscount, servicesPage.discount(),
                        testCaseId + " [FR-S4]: discount must be 10% when subtotal >= 150.00 (inclusive), else 0.00"),
                () -> assertEquals(expectedTax, servicesPage.tax(),
                        testCaseId + " [FR-S5]: tax must be 8% of (subtotal - discount)"),
                () -> assertEquals(expectedTotal, servicesPage.total(),
                        testCaseId + " [FR-S4 AC-04]: total must be (subtotal - discount) + tax"));

        // Cross-check the displayed figures against the rule computed independently here,
        // so the test fails even if the CSV row itself were mistyped.
        assertEquals(expectedTotal, specTotalFor(money(expectedSubtotal)),
                testCaseId + ": expected total in the test data must match the specification rule");
    }

    // ------------------------------------------------------------------ SCRUM-94 / US-CAL-02

    @ParameterizedTest(name = "{0} ({1}): subtotal {3}, discount {4} -> tax {5}, total {6}")
    @CsvSource({
            // testCaseId, jiraId,   services,                                subtotal, discount, tax,   total
            "TC-CAL-04, SCRUM-95, 'Vaccination',                            60.00,    0.00,    4.80,  64.80",
            "TC-CAL-05, SCRUM-96, 'Vaccination|Dental Cleaning|X-Ray',     190.00,   19.00,   13.68, 184.68",
            "TC-CAL-06, SCRUM-97, 'Surgery',                               240.00,   24.00,   17.28, 233.28"
    })
    @DisplayName("TC-CAL-04/05/06 [FR-S5]: tax is charged on the post-discount amount")
    void tcCal04to06_taxIsChargedOnPostDiscountAmount(String testCaseId, String jiraId, String services,
                                                      String expectedSubtotal, String expectedDiscount,
                                                      String expectedTax, String expectedTotal) {
        List.of(services.split("\\|")).forEach(servicesPage::addService);

        String actualSubtotal = servicesPage.subtotal();
        String actualDiscount = servicesPage.discount();

        assertAll(testCaseId + " (" + jiraId + ") tax base for subtotal " + expectedSubtotal,
                () -> assertEquals(expectedSubtotal, actualSubtotal,
                        testCaseId + " [FR-S1]: subtotal must be 40.00 base plus the selected service fees"),
                () -> assertEquals(expectedDiscount, actualDiscount,
                        testCaseId + " [FR-S4]: discount must be 10% of subtotal at or above 150.00"),
                () -> assertEquals(expectedTax, servicesPage.tax(),
                        testCaseId + " [FR-S5 AC-01]: tax must be 8% of (subtotal - discount)"),
                () -> assertEquals(expectedTotal, servicesPage.total(),
                        testCaseId + " [FR-S5 AC-04]: total must use the correctly calculated tax"));

        String taxRequiredByRule = format(pct(money(actualSubtotal).subtract(money(actualDiscount)), TAX_RATE));
        assertEquals(taxRequiredByRule, servicesPage.tax(),
                testCaseId + " [FR-S5 AC-05]: displayed tax must be 8% of the displayed post-discount amount");

        if (money(expectedDiscount).signum() > 0) {
            assertNotEquals(format(pct(money(actualSubtotal), TAX_RATE)), servicesPage.tax(),
                    testCaseId + " [FR-S5 AC-01]: tax must NOT be 8% of the pre-discount subtotal");
        }
    }

    // ------------------------------------------------------------------ SCRUM-113 / US-CAL-03

    @Test
    @DisplayName("TC-CAL-07 [FR-S3]: removing a single service recalculates the breakdown")
    void tcCal07_removeSingleServiceRecalculatesBreakdown() {
        servicesPage.addService("Vaccination").addService("X-Ray");
        assertEquals("151.20", servicesPage.total(),
                "TC-CAL-07 precondition: Vaccination + X-Ray must total 151.20 before removal");

        servicesPage.removeService("X-Ray");

        List<String> selectedAfter = servicesPage.selectedServiceNames();
        List<String> catalogueAfter = servicesPage.catalogueServiceNames();
        String subtotal = servicesPage.subtotal();
        String discount = servicesPage.discount();
        String tax = servicesPage.tax();
        String total = servicesPage.total();

        assertAll("TC-CAL-07 (SCRUM-115) state immediately after removing X-Ray",
                () -> assertFalse(selectedAfter.contains("X-Ray"),
                        "TC-CAL-07 [FR-S3 AC-01]: the removed service must disappear from Selected Services"),
                () -> assertTrue(catalogueAfter.contains("X-Ray"),
                        "TC-CAL-07 [FR-S1]: a removed service must return to the unselected catalogue"),
                () -> assertEquals("60.00", subtotal,
                        "TC-CAL-07 [FR-S3 AC-02]: subtotal must fall to 40.00 base + 20.00 Vaccination"),
                () -> assertEquals("0.00", discount,
                        "TC-CAL-07 [FR-S4]: 60.00 is below the 150.00 threshold, so no discount"),
                () -> assertEquals("4.80", tax,
                        "TC-CAL-07 [FR-S5]: tax must be 8% of 60.00"),
                () -> assertEquals("64.80", total,
                        "TC-CAL-07 [FR-S3 AC-04]: the recalculated total must be mathematically correct"));
    }

    // ------------------------------------------------------------------ SCRUM-114 / US-CAL-04

    @Test
    @DisplayName("TC-CAL-08 [FR-S3]: services can be removed one by one without cascading errors")
    void tcCal08_removeMultipleServicesSequentially() {
        servicesPage.addService("Vaccination").addService("Dental Cleaning").addService("X-Ray");

        servicesPage.removeService("X-Ray");
        String subtotalAfterFirst = servicesPage.subtotal();
        String taxAfterFirst = servicesPage.tax();
        String totalAfterFirst = servicesPage.total();

        servicesPage.removeService("Dental Cleaning");
        String subtotalAfterSecond = servicesPage.subtotal();
        String taxAfterSecond = servicesPage.tax();
        String totalAfterSecond = servicesPage.total();
        List<String> selectedAfterSecond = servicesPage.selectedServiceNames();

        // AC-03: the final bill must equal what it would have been had only Vaccination been
        // added - derived from the rule rather than restated as a literal.
        String billForVaccinationOnly = specTotalFor(BASE_VISIT_FEE.add(new BigDecimal("20.00")));

        assertAll("TC-CAL-08 (SCRUM-116) sequential removals",
                () -> assertEquals("110.00", subtotalAfterFirst,
                        "TC-CAL-08 [AC-02] after removal 1: 40.00 base + Vaccination 20.00 + Dental Cleaning 50.00"),
                () -> assertEquals("8.80", taxAfterFirst,
                        "TC-CAL-08 [FR-S5] after removal 1: tax must be 8% of 110.00"),
                () -> assertEquals("118.80", totalAfterFirst,
                        "TC-CAL-08 [AC-02] after removal 1: the removal must recalculate the total"),
                () -> assertEquals("60.00", subtotalAfterSecond,
                        "TC-CAL-08 [AC-02] after removal 2: 40.00 base + Vaccination 20.00"),
                () -> assertEquals("4.80", taxAfterSecond,
                        "TC-CAL-08 [FR-S5] after removal 2: tax must be 8% of 60.00"),
                () -> assertEquals("64.80", totalAfterSecond,
                        "TC-CAL-08 [AC-02] after removal 2: the second removal must recalculate without cascading errors"),
                () -> assertIterableEquals(List.of("Vaccination"), selectedAfterSecond,
                        "TC-CAL-08 [AC-01]: only the kept service may remain selected"),
                () -> assertEquals(billForVaccinationOnly, totalAfterSecond,
                        "TC-CAL-08 [AC-03]: the final bill must match a visit where only Vaccination was ever added"));
    }

    @Test
    @DisplayName("TC-CAL-09 [FR-S3/FR-S4]: removing at the threshold drops the discount")
    void tcCal09_removeServiceAtDiscountThresholdDropsDiscount() {
        servicesPage.addService("Microchipping").addService("X-Ray");

        servicesPage.removeService("Microchipping");

        List<String> selectedAfter = servicesPage.selectedServiceNames();
        String subtotal = servicesPage.subtotal();
        String discount = servicesPage.discount();
        String tax = servicesPage.tax();
        String total = servicesPage.total();

        assertAll("TC-CAL-09 (SCRUM-117) after dropping from 150.00 to 120.00",
                () -> assertFalse(selectedAfter.contains("Microchipping"),
                        "TC-CAL-09 [FR-S3 AC-01]: Microchipping must disappear from Selected Services"),
                () -> assertEquals("120.00", subtotal,
                        "TC-CAL-09 [AC-02]: 40.00 base + X-Ray 80.00"),
                () -> assertEquals("0.00", discount,
                        "TC-CAL-09 [FR-S4]: falling below 150.00 must remove the discount entirely"),
                () -> assertEquals("9.60", tax,
                        "TC-CAL-09 [FR-S5]: tax must be 8% of 120.00"),
                () -> assertEquals("129.60", total,
                        "TC-CAL-09 [AC-04]: the recalculated total must be mathematically correct"));
    }

    @Test
    @DisplayName("TC-CAL-10 [FR-S3/FR-S5]: removing above the threshold recalculates the discount")
    void tcCal10_removeServiceAboveDiscountThresholdRecalculates() {
        servicesPage.addService("Vaccination").addService("Dental Cleaning").addService("X-Ray");

        servicesPage.removeService("Vaccination");

        String subtotal = servicesPage.subtotal();
        String discount = servicesPage.discount();
        String tax = servicesPage.tax();
        String total = servicesPage.total();

        assertAll("TC-CAL-10 (SCRUM-118) after dropping from 190.00 to 170.00",
                () -> assertEquals("170.00", subtotal,
                        "TC-CAL-10 [AC-02]: 40.00 base + Dental Cleaning 50.00 + X-Ray 80.00"),
                () -> assertEquals("17.00", discount,
                        "TC-CAL-10 [FR-S4]: still above 150.00, so the discount must be 10% of 170.00"),
                () -> assertNotEquals("19.00", discount,
                        "TC-CAL-10 [AC-02]: the discount must be recomputed, not carried over from 190.00"),
                () -> assertEquals("12.24", tax,
                        "TC-CAL-10 [FR-S5]: tax must be 8% of the post-discount 153.00"),
                () -> assertEquals("165.24", total,
                        "TC-CAL-10 [AC-04]: the recalculated total must be mathematically correct"));
    }

    // ------------------------------------------------------------------ helpers

    private static BigDecimal money(String displayed) {
        assertTrue(displayed != null && displayed.matches("\\d+\\.\\d{2}"),
                "Money must display with exactly two decimal places, but was: " + displayed);
        return new BigDecimal(displayed);
    }

    private static String format(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static BigDecimal pct(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /** The specification's total for a given subtotal, used as an independent oracle. */
    private static String specTotalFor(BigDecimal subtotal) {
        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) >= 0
                ? pct(subtotal, DISCOUNT_RATE)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal postDiscount = subtotal.subtract(discount);
        return format(postDiscount.add(pct(postDiscount, TAX_RATE)));
    }

    private static ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");

        // The Billing Breakdown rows carry no id or data attribute, so VisitServicesPage has to
        // locate them by their visible <th> label. Those labels come from messages*.properties
        // and the jar ships de/es/fa/ko/pt/ru/tr bundles, so the locale is pinned to keep the
        // locators deterministic on any machine.
        options.addArguments("--lang=en-US");
        options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "en-US,en"));

        // ---- ENVIRONMENT-SPECIFIC: the only machine-dependent code in this suite ------------
        // One dev machine has /usr/bin/chromium and no google-chrome, where Selenium Manager
        // cannot resolve a browser on its own. Teammates running real Chrome match neither
        // branch and need no edit to this file. Override anywhere with:
        //     mvn test -Dchrome.binary=/path/to/chrome
        String explicitBinary = System.getProperty("chrome.binary");
        if (explicitBinary != null && !explicitBinary.isBlank()) {
            options.setBinary(explicitBinary);
        } else if (!Files.isExecutable(Path.of("/usr/bin/google-chrome"))
                && Files.isExecutable(Path.of("/usr/bin/chromium"))) {
            options.setBinary("/usr/bin/chromium");
        }
        // -------------------------------------------------------------------------------------
        return options;
    }
}
