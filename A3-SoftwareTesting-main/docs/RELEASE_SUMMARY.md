# ASG3 Release Summary — Sections 4–6
> **Copy this content into Word Section 4 (Test Execution Summary), Section 5 (Defect Summary) and Section 6 (Release Recommendation).**
> Run date: 2026-09-10 | SUT: `A3-Testing-Site.jar` (SHA-256 `7246e232292a4a24e72d9cb5b0b08d29b212951e8099106c540b405dd0a9f9eb`) | Java 17.0.19 | Chrome 153 | macOS Apple Silicon

---

## Section 4 — Test Execution Summary

### 4.1 Selenium / JUnit 5 test execution

| Domain | Test class | Total | Passed | Failed | Errors | Notes |
|---|---|---|---|---|---|---|
| Pet Management | `tests.pet.PetManagementTest` | 9 | 8 | 1 | 0 | TC-PET-04 FAIL: future birth date accepted (PC-201) |
| Billing / Visit Services | `tests.billing.VisitServicesBillingTest` | 10 | 2 | 8 | 0 | TC-CAL-02,03,05,06 (discount/tax, PC-204); TC-CAL-07,08,09,10 (remove, PC-205) |
| Owner Management | `tests.owner.OwnerManagementTest` | 16 | 15 | 1 | 0 | TC-OWN-10 FAIL: case-insensitive search (PC-202) |
| Owner Search / Pagination | `tests.owner.OwnerSearchPaginationTest` | 7 | 6 | 1 | 0 | `searchIsCaseInsensitive` lowercase case FAIL (PC-202); `frPg1_...` FAIL (PC-203) |
| Veterinarian Directory | `tests.veterinarian.VeterinarianDirectoryTest` | 3 | 3 | 0 | 0 | All passed |
| **TOTAL** | | **45** | **34** | **11** | **0** | |

> **Note**: the Owner pagination defect (`frPg1_...`) is also covered by `OwnerManagementTest`, giving 10 unique test-method failures across 8 distinct test methods, representing 5 distinct defects.

### 4.2 BDD / Cucumber test execution

| Feature | Scenarios | Passed | Failed | Errors | Notes |
|---|---|---|---|---|---|
| Pet Management (`pet_management.feature`) | 9 | 7 | 2 | 0 | TC-PET-04 `@defect` FAIL — PC-201 confirmed. TC-PET-05 FAIL — Leo not found after earlier scenarios added 7+ pets to Owner 1 (possible SUT max-pet limit; needs investigation). |
| Visit Services and Billing (`VisitServicesAndBilling.feature`) | 6 | 1 | 5 | 0 | TC-VISIT-01 offset +1: PASS. TC-VISIT-01 offsets −1 and 0: FAIL (PC-206 — past/today dates accepted). TC-VISIT-02 (today): FAIL (PC-206). TC-BILL-01 both rows: FAIL (PC-204). |
| **TOTAL** | **15** | **8** | **6** | **0** | 6 failures all trace to confirmed SUT defects PC-201, PC-204, PC-206 |

> Evidence: `BDD/reports/TEST-runners.CucumberTestRunner.xml` (15 scenarios, 6 failures) and `BDD/reports/cucumber.html` (1 MB rich report).

> **Katalon BDD execution**: See `Katalon/KATALON_RUN_INSTRUCTIONS.md` for steps to produce the Katalon Studio execution report.

### 4.3 JMeter performance test execution

| Virtual users | Samples | Mean response (ms) | Error rate | Throughput (req/s) |
|---|---|---|---|---|
| 1 | 59 | 29.37 | 0.00% | 3.03 |
| 10 | 487 | 10.99 | 0.00% | 24.74 |
| 25 | 1,200 | 8.63 | 0.00% | 61.47 |
| 50 | 2,427 | 4.79 | 0.00% | 123.17 |
| **Total** | **4,173** | — | **0.00%** | — |

All 4,173 samples passed response assertions. Source: `JMeter/results.jtl`.

---

## Section 5 — Defect Summary

| Defect ID | Area | FR / AC | Severity | Priority | Status | Detecting test |
|---|---|---|---|---|---|---|
| **PC-201** | Pet | AC-03 Future birth date | **HIGH** | High | Open | TC-PET-04, BDD `@defect` |
| **PC-202** | Owner Search | FR-O4 Case-insensitive | **MEDIUM** | Medium | Open | TC-OWN-10, `searchIsCaseInsensitive` |
| **PC-203** | Owner Pagination | FR-PG1 5 per page | **MEDIUM** | Medium | Open | `frPg1_...` |
| **PC-204** | Billing Discount/Tax | FR-S4 (at threshold), FR-S5 (tax base) | **HIGH** | High | Open | TC-CAL-02,03,05,06; BDD TC-BILL-01 |
| **PC-205** | Billing Remove | FR-S3 Recalculation | **HIGH** | High | Open | TC-CAL-07,08,09,10 |
| **PC-206** | Visit Date Validation | AC-VISIT-01 Past dates; AC-VISIT-02 Today | **HIGH** | High | Open | BDD TC-VISIT-01 (−1,0), TC-VISIT-02 |

Full defect details including test data, expected vs actual, and evidence file references: see `docs/DEFECT_LOG.md`.

---

## Section 6 — Release Assessment and Recommendation

### 6.1 Exit criteria definition

| # | Exit criterion | Measurable threshold |
|---|---|---|
| EC-1 | All Selenium tests pass or intentional defect tests fail for documented reasons | 0 unintentional failures |
| EC-2 | No HIGH or CRITICAL open defects in billing functionality | 0 open HIGH billing defects |
| EC-3 | No HIGH or CRITICAL open defects in pet data integrity | 0 open HIGH pet data defects |
| EC-4 | JMeter performance: 0% error rate at up to 50 concurrent users | Error rate = 0.00% |
| EC-5 | BDD Katalon execution report available for all test suites | Katalon report exported |
| EC-6 | Full traceability: every AC has ≥1 linked QAlity case, ≥1 automation result | No unlinked ACs |

### 6.2 Exit criteria evaluation

| # | Criterion | Status | Evidence |
|---|---|---|---|
| EC-1 | All Selenium tests pass | ❌ **NOT MET** | 11 test-method failures: TC-PET-04 (PC-201), TC-CAL-02,03,05,06 (PC-204), TC-CAL-07,08,09,10 (PC-205), TC-OWN-10 (PC-202), `frPg1_...` (PC-203) |
| EC-2 | No open HIGH billing defects | ❌ **NOT MET** | PC-204 (discount/tax), PC-205 (service removal), PC-206 (visit date) — all HIGH severity and open |
| EC-3 | No open HIGH pet data defects | ❌ **NOT MET** | PC-201 (future date accepted) HIGH severity and open |
| EC-4 | JMeter 0% error rate at 50 users | ✅ **MET** | `results.jtl`: 4,173 samples, 0.00% error rate |
| EC-5 | Katalon execution report | ⚠️ **PENDING** | Katalon Studio manual run required — see `Katalon/KATALON_RUN_INSTRUCTIONS.md` |
| EC-6 | Full traceability | ✅ **MET** | All ACs linked — see `docs/FULL_TRACEABILITY_MATRIX.md` |

### 6.3 Release recommendation

> **RECOMMENDATION: DO NOT RELEASE — hold for defect resolution.**

Three exit criteria are not met. Defects PC-204 and PC-205 are **HIGH severity billing defects** that directly affect customer-facing invoice correctness:

- **PC-204**: patients whose subtotal is exactly $150.00 are not given the 10% loyalty discount they are entitled to; patients with subtotals above $150 are charged tax on the pre-discount amount — both result in overcharging.
- **PC-205**: once a service is added to a visit, it cannot be removed; the billing total is permanently wrong — preventing legitimate corrections before checkout.
- **PC-201**: future-dated pet birth dates are silently accepted, corrupting medical records.

A release with these defects would expose the clinic to financial disputes and data integrity failures. **Release should be blocked until PC-204 and PC-205 are resolved and re-tested. PC-201 should also be resolved before release.**

PC-202 (case-insensitive search) and PC-203 (pagination) are MEDIUM severity and could be accepted as deferred improvements in a point release if the HIGH defects are resolved, subject to business sign-off.

### 6.4 Risk register

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Billing overcharge complaints | **HIGH** (defect confirmed) | **HIGH** | Block release until PC-204 + PC-205 fixed |
| Pet data integrity errors | **HIGH** (defect confirmed) | **HIGH** | Block release until PC-201 fixed |
| Search usability complaints | Medium | Medium | Defer to patch release after HIGH defects resolved |
| Performance under production load | Low (localhost tested only) | High | Repeat with SUT and JMeter on separate machines before production |

### 6.5 Outstanding items before release

1. **Fix and re-test PC-204** (billing discount boundary + tax base). Retest TC-CAL-01 through TC-CAL-06 and all BDD billing scenarios.
2. **Fix and re-test PC-205** (service removal recalculation). Retest TC-CAL-07 through TC-CAL-10.
3. **Fix and re-test PC-201** (future pet birth date). Retest TC-PET-04 and BDD `@defect` scenario — expect it to PASS after fix.
4. **Obtain Katalon execution report** for `TS_PetBDD` and `TS_BillingBDD` (see `Katalon/KATALON_RUN_INSTRUCTIONS.md`).
5. **Update this summary** with re-test results and revised pass/fail counts before final submission.
6. **Performance regression test** with separate load-generator and SUT machines before claiming production readiness.
