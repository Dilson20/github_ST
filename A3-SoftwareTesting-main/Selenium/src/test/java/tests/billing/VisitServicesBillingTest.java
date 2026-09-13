package tests.billing;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
        servicesPage = new VisitFixtureFlow(driver, BASE_URL). createOwnerWithPetAndVisit();

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
    // TC-CAL-01..03 walk the loyalty-discount threshold: below it, exactly on it, above it.
    // Datasets are the three worked examples from the Requirements Summary, used verbatim.

    @Test
    @DisplayName("TC-CAL-01 (SCRUM-58) [FR-S1, FR-S4]: below threshold - no services still bills the 40.00 base fee")
    void tcCal01_belowThresholdWithNoServices() {
        // Covers FR-S1 (base visit fee is charged on every visit) and FR-S4 (no discount below
        // 150.00). Spec worked example 1: 40.00 / 0.00 / 3.20 / 43.20.
        // Shares its dataset with TC-CAL-04 by design - see the note on that test.
        assertTrue(servicesPage.selectedServiceNames().isEmpty(),
                "TC-CAL-01 [FR-S1]: this case must bill a visit with nothing selected, but found "
                        + servicesPage.selectedServiceNames());

        // The full breakdown is asserted, not just the total: the point of this case is that the
        // 40.00 base fee is present as the subtotal in its own right, with no service behind it.
        assertBreakdown("TC-CAL-01", "SCRUM-58", readBreakdown(), "40.00", "0.00", "3.20", "43.20");
    }

    @Test
    @DisplayName("TC-CAL-02 (SCRUM-59) [FR-S4 AC-04]: exactly at the 150.00 threshold the discount applies")
    void tcCal02_exactlyAtDiscountThreshold() {
        // Covers FR-S4 AC-04: the 10% discount applies at subtotal >= 150.00 (inclusive).
        // Spec worked example 2: Base + Microchipping + X-Ray = 150.00 / 15.00 / 10.80 / 145.80.
        addServices("Microchipping", "X-Ray");

        assertBreakdown("TC-CAL-02", "SCRUM-59", readBreakdown(), "150.00", "15.00", "10.80", "145.80");
    }

    @Test
    @DisplayName("TC-CAL-03 (SCRUM-60) [FR-S4, FR-S5]: above the threshold the discount and tax both scale")
    void tcCal03_aboveDiscountThreshold() {
        // Covers FR-S4 (discount above the threshold) and FR-S5 (tax on the post-discount amount).
        // Spec worked example 3: Base + Surgery = 240.00 / 24.00 / 17.28 / 233.28.
        addServices("Surgery");

        assertBreakdown("TC-CAL-03", "SCRUM-60", readBreakdown(), "240.00", "24.00", "17.28", "233.28");
    }

    // ------------------------------------------------------------------ SCRUM-94 / US-CAL-02
    @Test
    @DisplayName("TC-CAL-04 (SCRUM-95) [FR-S5 AC-01]: tax with no discount - control case")
    void tcCal04_taxWithNoDiscount() {
        // Covers FR-S5 AC-01/AC-05 for the no-discount equivalence class. Control case: with a
        // 0.00 discount the pre- and post-discount tax bases are identical, so a defect in the tax
        // base cannot show here. It anchors the comparison made by TC-CAL-05 and TC-CAL-06.
        // Spec worked example 1: 40.00 / 0.00 / 3.20 / 43.20. Dataset shared with TC-CAL-01.
        assertTrue(servicesPage.selectedServiceNames().isEmpty(),
                "TC-CAL-04 [FR-S1]: this case must bill a visit with nothing selected, but found "
                        + servicesPage.selectedServiceNames());

        Breakdown actual = readBreakdown();
        assertAll("TC-CAL-04 (SCRUM-95) tax base with no discount",
                () -> assertBreakdown("TC-CAL-04", "SCRUM-95", actual, "40.00", "0.00", "3.20", "43.20"),
                () -> assertTaxMatchesDisplayedPostDiscountAmount("TC-CAL-04", actual));
    }

    @Test
    @DisplayName("TC-CAL-05 (SCRUM-96) [FR-S5 AC-01]: tax with a discount is charged on the post-discount amount")
    void tcCal05_taxWithDiscount() {
        // Covers FR-S5 AC-01/AC-04/AC-05 for the discount equivalence class.
        // Spec worked example 2: 150.00 / 15.00 / 10.80 / 145.80. Dataset shared with TC-CAL-02.
        addServices("Microchipping", "X-Ray");

        Breakdown actual = readBreakdown();
        assertAll("TC-CAL-05 (SCRUM-96) tax base with a discount",
                () -> assertBreakdown("TC-CAL-05", "SCRUM-96", actual, "150.00", "15.00", "10.80", "145.80"),
                () -> assertTaxMatchesDisplayedPostDiscountAmount("TC-CAL-05", actual),
                () -> assertTaxIsNotChargedOnPreDiscountSubtotal("TC-CAL-05", actual));
    }

    @Test
    @DisplayName("TC-CAL-06 (SCRUM-97) [FR-S5 AC-01]: a large discount still moves the tax base")
    void tcCal06_taxWithLargeDiscount() {
        // Covers FR-S5 AC-01/AC-04/AC-05 with the largest discount of the three worked examples,
        // so any error in the tax base shows with the widest margin (17.28 vs 19.20).
        // Spec worked example 3: 240.00 / 24.00 / 17.28 / 233.28. Dataset shared with TC-CAL-03.
        addServices("Surgery");

        Breakdown actual = readBreakdown();
        assertAll("TC-CAL-06 (SCRUM-97) tax base with a large discount",
                () -> assertBreakdown("TC-CAL-06", "SCRUM-97", actual, "240.00", "24.00", "17.28", "233.28"),
                () -> assertTaxMatchesDisplayedPostDiscountAmount("TC-CAL-06", actual),
                () -> assertTaxIsNotChargedOnPreDiscountSubtotal("TC-CAL-06", actual));
    }

    // ------------------------------------------------------------------ SCRUM-113 / US-CAL-03

    @Test
    @DisplayName("TC-CAL-07 (SCRUM-115) [FR-S3]: Remove Single Service")
    void tcCal07_removeSingleServiceRecalculatesBreakdown() {
        addServices("Vaccination", "Dental Cleaning");
        Breakdown before = readBreakdown();

        servicesPage.removeService("Dental Cleaning");

        Breakdown after = readBreakdown();
        List<String> selectedAfter = servicesPage.selectedServiceNames();
        List<String> catalogueAfter = servicesPage.catalogueServiceNames();

        // Both stages are snapshotted before anything is asserted, so a wrong pre-removal figure
        // cannot abort the test and hide what the removal itself did.
        assertAll("TC-CAL-07 (SCRUM-115) removing Dental Cleaning from a 110.00 visit",
                () -> assertBreakdown("TC-CAL-07 before removal", "SCRUM-115", before,
                        "110.00", "0.00", "8.80", "118.80"),
                () -> assertFalse(selectedAfter.contains("Dental Cleaning"),
                        "TC-CAL-07 [FR-S3 AC-01]: the removed service must disappear from Selected Services"),
                () -> assertTrue(catalogueAfter.contains("Dental Cleaning"),
                        "TC-CAL-07 [FR-S1]: a removed service must return to the unselected catalogue"),
                () -> assertBreakdown("TC-CAL-07 after removing Dental Cleaning", "SCRUM-115", after,
                        "60.00", "0.00", "4.80", "64.80"));
    }

    // ------------------------------------------------------------------ SCRUM-114 / US-CAL-04

    @Test
    @DisplayName("TC-CAL-08 (SCRUM-116) [FR-S3]: Remove Multiple Service Sequentially")
    void tcCal08_removeMultipleServicesSequentially() {
        addServices("Vaccination", "Dental Cleaning", "X-Ray", "Microchipping");
        Breakdown before = readBreakdown();

        servicesPage.removeService("Microchipping");
        Breakdown afterFirst = readBreakdown();

        servicesPage.removeService("X-Ray");
        Breakdown afterSecond = readBreakdown();
        List<String> selectedAfterSecond = servicesPage.selectedServiceNames();

        String billForVaccinationAndDental = specTotalFor(
                BASE_VISIT_FEE.add(new BigDecimal("20.00")).add(new BigDecimal("50.00")));

        assertAll("TC-CAL-08 (SCRUM-116) sequential removals",
                () -> assertBreakdown("TC-CAL-08 before removals", "SCRUM-116", before,
                        "220.00", "22.00", "15.84", "213.84"),
                () -> assertBreakdown("TC-CAL-08 after removing Microchipping", "SCRUM-116", afterFirst,
                        "190.00", "19.00", "13.68", "184.68"),
                () -> assertBreakdown("TC-CAL-08 after also removing X-Ray", "SCRUM-116", afterSecond,
                        "110.00", "0.00", "8.80", "118.80"),
                // Selected Services comes back in a different order between page loads, so this
                // compares the set, not the sequence.
                () -> assertEquals(List.of("Dental Cleaning", "Vaccination"),
                        selectedAfterSecond.stream().sorted().toList(),
                        "TC-CAL-08 [AC-01]: only the kept services may remain selected"),
                () -> assertEquals(billForVaccinationAndDental, afterSecond.total(),
                        "TC-CAL-08 [AC-03]: the final bill must match a visit where only Vaccination "
                                + "and Dental Cleaning were ever added"));
    }

    @Test
    @DisplayName("TC-CAL-09 (SCRUM-117) [FR-S3/FR-S4]: Remove Service at Discount Threshold")
    void tcCal09_removeServiceAtDiscountThresholdDropsDiscount() {
        addServices("Microchipping", "X-Ray");
        Breakdown before = readBreakdown();

        servicesPage.removeService("X-Ray");

        Breakdown after = readBreakdown();
        List<String> selectedAfter = servicesPage.selectedServiceNames();

        assertAll("TC-CAL-09 (SCRUM-117) dropping from 150.00 to 70.00",
                () -> assertBreakdown("TC-CAL-09 before removal", "SCRUM-117", before,
                        "150.00", "15.00", "10.80", "145.80"),
                () -> assertFalse(selectedAfter.contains("X-Ray"),
                        "TC-CAL-09 [FR-S3 AC-01]: X-Ray must disappear from Selected Services, but found "
                                + selectedAfter),
                () -> assertBreakdown("TC-CAL-09 after removing X-Ray", "SCRUM-117", after,
                        "70.00", "0.00", "5.60", "75.60"));
    }

    @Test
    @DisplayName("TC-CAL-10 (SCRUM-118) [FR-S3/FR-S5]: Remove Service Above Discount Threshold")
    void tcCal10_removeServiceAboveDiscountThresholdRecalculates() {
        addServices("Surgery", "X-Ray");
        Breakdown before = readBreakdown();

        servicesPage.removeService("X-Ray");

        Breakdown after = readBreakdown();

        assertAll("TC-CAL-10 (SCRUM-118) dropping from 320.00 to 240.00, still above the threshold",
                () -> assertBreakdown("TC-CAL-10 before removal", "SCRUM-118", before,
                        "320.00", "32.00", "23.04", "311.04"),
                () -> assertBreakdown("TC-CAL-10 after removing X-Ray", "SCRUM-118", after,
                        "240.00", "24.00", "17.28", "233.28"),
                () -> assertNotEquals("32.00", after.discount(),
                        "TC-CAL-10 [AC-02]: the discount must be recomputed, not carried over from 320.00"));
    }

    // ------------------------------------------------------------------ helpers

    private void addServices(String... serviceNames) {
        for (String serviceName : serviceNames) {
            servicesPage.addService(serviceName);
        }

        // FR-S2 AC-01: adding a service updates the selected list.
        List<String> selected = servicesPage.selectedServiceNames();
        assertTrue(selected.containsAll(List.of(serviceNames)),
                "[FR-S2 AC-01]: every added service must appear in Selected Services, but found " + selected);
    }

    private record Breakdown(String subtotal, String discount, String tax, String total) {}

    private Breakdown readBreakdown() {
        return new Breakdown(servicesPage.subtotal(), servicesPage.discount(),
                servicesPage.tax(), servicesPage.total());
    }

    private static void assertBreakdown(String testCaseId, String jiraId, Breakdown actual,
                                        String expectedSubtotal, String expectedDiscount,
                                        String expectedTax, String expectedTotal) {
        assertAll(testCaseId + " (" + jiraId + ") billing breakdown for subtotal " + expectedSubtotal,
                () -> assertEquals(expectedSubtotal, actual.subtotal(),
                        testCaseId + " [FR-S1]: subtotal must be 40.00 base plus the selected service fees"),
                () -> assertEquals(expectedDiscount, actual.discount(),
                        testCaseId + " [FR-S4]: discount must be 10% when subtotal >= 150.00 (inclusive), else 0.00"),
                () -> assertEquals(expectedTax, actual.tax(),
                        testCaseId + " [FR-S5]: tax must be 8% of (subtotal - discount)"),
                () -> assertEquals(expectedTotal, actual.total(),
                        testCaseId + " [FR-S4 AC-04]: total must be (subtotal - discount) + tax"));

        assertEquals(expectedTotal, specTotalFor(money(expectedSubtotal)),
                testCaseId + ": expected total in the test data must match the specification rule");
    }

    /** FR-S5 AC-05: the figures on the page must be internally consistent with each other. */
    private static void assertTaxMatchesDisplayedPostDiscountAmount(String testCaseId, Breakdown actual) {
        BigDecimal postDiscount = money(actual.subtotal()).subtract(money(actual.discount()));
        assertEquals(format(pct(postDiscount, TAX_RATE)), actual.tax(),
                testCaseId + " [FR-S5 AC-05]: displayed tax must be 8% of the displayed post-discount amount");
    }

    /** FR-S5 AC-01, stated negatively: the tax base must not be the raw subtotal. */
    private static void assertTaxIsNotChargedOnPreDiscountSubtotal(String testCaseId, Breakdown actual) {
        assertNotEquals(format(pct(money(actual.subtotal()), TAX_RATE)), actual.tax(),
                testCaseId + " [FR-S5 AC-01]: tax must NOT be 8% of the pre-discount subtotal");
    }

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

        options.addArguments("--lang=en-US");
        options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "en-US,en"));

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
