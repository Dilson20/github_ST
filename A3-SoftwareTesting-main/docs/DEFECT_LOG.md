# ASG3 Defect Log
> Run: 2026-09-10 | SUT: `A3-Testing-Site.jar` SHA-256 `7246e232292a4a24e72d9cb5b0b08d29b212951e8099106c540b405dd0a9f9eb` | Java 17.0.19 | Chrome 153.0.8010.36 | macOS Apple Silicon

---

## PC-201 — Pet management: future birth date accepted without validation

| Field | Value |
|---|---|
| **Defect ID** | PC-201 |
| **Jira Story** | PC-101 (wider pet management, AC-03) |
| **Priority** | High |
| **Severity** | High — invalid medical record data can be persisted |
| **Status** | Open |
| **Assigned to** | [Assign to responsible team member] |
| **Detected by** | `TC-PET-04` `testFutureBirthDateBehavior`, `@TC-PET-04 @defect`, QA-PET-04 |
| **Feature requirement** | System must reject pet birth dates set in the future (AC-03) |
| **Test data** | Owner ID 1; Pet name `FuturePup<timestamp>`; Birth date: tomorrow's date (dynamically computed via `LocalDate.now().plusDays(1)`) |
| **Expected** | Form remains on `/owners/1/pets/new`; validation error displayed; no pet created |
| **Actual** | SUT accepted the future date, redirected to `/owners/1`, and created the pet record |
| **Evidence** | `Selenium/reports/pet/TEST-tests.pet.PetManagementTest.xml` — `testFutureBirthDateBehavior` FAILED |
| **Reproducible** | Yes — deterministic via tomorrow's date; no data setup required beyond Owner ID 1 existing |
| **Retest result** | Not yet retested (open) |

---

## PC-202 — Owner search: case-insensitive last-name search not implemented

| Field | Value |
|---|---|
| **Defect ID** | PC-202 |
| **Jira Story** | PC-103 (Owner Search, FR-O4) |
| **Priority** | Medium |
| **Severity** | Medium — users must type exact case to find owners; degraded usability |
| **Status** | Open |
| **Assigned to** | [Assign to responsible team member] |
| **Detected by** | `frO4_ownerSearchShouldBeCaseInsensitive`, `searchIsCaseInsensitive` (lowercase row), QA-OWN-11/17 |
| **Feature requirement** | FR-O4: last-name search must be case-insensitive |
| **Test data** | Search term `davis` (lowercase); seed owner `Davis` exists in the default dataset |
| **Expected** | Owner list returns at least one result matching `Davis` |
| **Actual** | SUT returns "has not been found" error — no results for lowercase `davis` |
| **Evidence** | `Selenium/reports/owner/TEST-tests.owner.OwnerManagementTest.xml` — `frO4_...` FAILED; `TEST-tests.owner.OwnerSearchPaginationTest.xml` — `searchIsCaseInsensitive` FAILED |
| **Retest result** | Not yet retested (open) |

---

## PC-203 — Owner list pagination: shows 6 owners per page instead of 5

| Field | Value |
|---|---|
| **Defect ID** | PC-203 |
| **Jira Story** | PC-103 (Owner Pagination, FR-PG1) |
| **Priority** | Medium |
| **Severity** | Medium — page-size contract violated; affects downstream page navigation |
| **Status** | Open |
| **Assigned to** | [Assign to responsible team member] |
| **Detected by** | `frPg1_ownerListShouldShowFiveOwnersPerPage`, QA-OWN-12 |
| **Feature requirement** | FR-PG1: owner list must paginate at exactly 5 records per page |
| **Test data** | Navigate to `/owners?lastName=` (all owners); count `table tbody tr` |
| **Expected** | 5 rows in the table body |
| **Actual** | 6 rows — SUT does not limit to 5 |
| **Failure message** | `FR-PG1 requires 5 owners per page; the release candidate shows 6 instead.` |
| **Evidence** | `Selenium/reports/owner/TEST-tests.owner.OwnerManagementTest.xml` — `frPg1_...` FAILED |
| **Retest result** | Not yet retested (open) |

---

## PC-204 — Billing: discount not applied at exactly 150.00 subtotal; tax charged on pre-discount amount

| Field | Value |
|---|---|
| **Defect ID** | PC-204 |
| **Jira Story** | PC-104 (Visit Services Billing, FR-S4, FR-S5) |
| **Priority** | High |
| **Severity** | High — patients are overcharged when subtotal reaches exactly the discount threshold |
| **Status** | Open |
| **Assigned to** | [Assign to responsible team member] |
| **Detected by** | TC-CAL-02 (subtotal 150), TC-CAL-03 (170), TC-CAL-05 (190), TC-CAL-06 (240); QA-BILL-02,03,05,06 |
| **Feature requirement** | FR-S4: 10% loyalty discount applies when subtotal ≥ 150.00 (inclusive). FR-S5: tax is 8% of post-discount amount. |
| **Root cause observed** | SUT appears to apply discount only when subtotal **strictly exceeds** 150.00 (i.e. `> 150` not `>= 150`). Tax appears to be applied to the pre-discount subtotal, not the post-discount amount. |
| **Test data examples** | |
| TC-CAL-02 | Microchipping + X-Ray → subtotal 150.00; expected discount 15.00; expected total 145.80 |
| TC-CAL-02 actual | Discount 0.00; total 162.00 (no discount applied at exactly 150) |
| TC-CAL-03 | Dental Cleaning + X-Ray → subtotal 170.00; expected tax 12.24 (8% of 153.00); expected total 165.24 |
| TC-CAL-03 actual | Tax 13.60 (8% of pre-discount 170.00); total 166.60 |
| **Evidence** | `Selenium/reports/billing/TEST-tests.billing.VisitServicesBillingTest.xml` — TC-CAL-02, TC-CAL-03, TC-CAL-05, TC-CAL-06 FAILED |
| **Retest result** | Not yet retested (open) |

---

## PC-205 — Billing: removing a service does not update subtotal, discount or tax

| Field | Value |
|---|---|
| **Defect ID** | PC-205 |
| **Jira Story** | PC-104 (Visit Services Billing, FR-S3) |
| **Priority** | High |
| **Severity** | High — customers cannot adjust services; displayed total is wrong after removal |
| **Status** | Open |
| **Assigned to** | [Assign to responsible team member] |
| **Detected by** | TC-CAL-07, TC-CAL-08, TC-CAL-09, TC-CAL-10; QA-BILL-07,08,09,10 |
| **Feature requirement** | FR-S3 AC-01: removed service disappears from Selected Services. FR-S3 AC-02: subtotal is recalculated immediately. FR-S1: removed service returns to catalogue. |
| **Root cause observed** | After clicking Remove, the removed service stays in the Selected Services table and the billing breakdown (subtotal / discount / tax / total) is not updated. The SUT appears to not process the removal server-side or not refresh the billing panel. |
| **Test data examples** | |
| TC-CAL-07 | Add Vaccination + X-Ray (subtotal 151.20); Remove X-Ray. Expected: subtotal 60.00, discount 0.00, total 64.80 |
| TC-CAL-07 actual | Subtotal remains 140.00, total remains 151.20; X-Ray still in Selected list; X-Ray NOT in catalogue |
| TC-CAL-09 | Add Microchipping + X-Ray (subtotal 150.00); Remove Microchipping. Expected: subtotal 120.00, discount 0.00, total 129.60 |
| TC-CAL-09 actual | Subtotal 150.00, discount 15.00, total 162.00 — removal had no effect |
| **Evidence** | `Selenium/reports/billing/TEST-tests.billing.VisitServicesBillingTest.xml` — TC-CAL-07, TC-CAL-08, TC-CAL-09, TC-CAL-10 FAILED |
| **Retest result** | Not yet retested (open) |

---

## PC-206 — Visit booking: past dates and today accepted without validation

| Field | Value |
|---|---|
| **Defect ID** | PC-206 |
| **Jira Story** | PC-104 (Visit Services Billing, AC-VISIT-01 / AC-VISIT-02) |
| **Priority** | High |
| **Severity** | High — past and same-day visits can be booked, creating invalid clinical scheduling records |
| **Status** | Open |
| **Assigned to** | [Assign to responsible team member] |
| **Detected by** | BDD `@TC-VISIT-01` (offset −1 and 0), `@TC-VISIT-02` (today), QA-BILL-11 |
| **Feature requirement** | AC-VISIT-01: visit dates in the past must be rejected. AC-VISIT-02: visit dated today must be rejected (future-only booking policy). |
| **Test data** | Owner/Pet/Visit fixture created fresh per scenario; dates set to `yesterday` (offset −1), `today` (offset 0), and `today` (standalone TC-VISIT-02) |
| **Expected** | Validation error displayed; browser stays on Add Visit form; no visit record created |
| **Actual** | SUT accepted past dates and today without any error; browser redirected to Owner Details page with visit saved. Example URL: `http://localhost:8080/owners/11/pets/14/visits/new` (visit was created) |
| **Failure messages** | `DEFECT: Visit should show a validation error for this date. URL: http://localhost:8080/owners/11/pets/14/visits/new` |
| **Evidence** | `BDD/reports/TEST-runners.CucumberTestRunner.xml` — Examples.Example #1.1 and #1.2 of TC-VISIT-01 FAILED; TC-VISIT-02 FAILED. `BDD/reports/cucumber.html` |
| **Note on offset +1** | Example #1.3 (tomorrow) correctly PASSED — SUT accepts future-dated visits as expected |
| **Retest result** | Not yet retested (open) |
