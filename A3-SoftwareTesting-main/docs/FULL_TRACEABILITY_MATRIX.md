# ASG3 Full Traceability Matrix — All Functional Areas
> Evidence run: 2026-09-10 | SUT: `A3-Testing-Site.jar` (SHA-256 `7246e232292a4a24e72d9cb5b0b08d29b212951e8099106c540b405dd0a9f9eb`) | Java 17.0.19 (Homebrew) | Chrome 153.0.8010.36 | macOS (Apple Silicon)

---

## Story → AC → QAlity Case → Automation → BDD → Defect (all 5 domains)

| Domain | Jira Story | AC | QAlity Case | Selenium TC | BDD Scenario | Defect | Run Status |
|---|---|---|---|---|---|---|---|
| **Pet** | `PC-101` | AC-01 | QA-PET-01 | TC-PET-01 `testAddNewPetSuccess` | `@TC-PET-01 @smoke` | — | ✅ PASS |
| **Pet** | `PC-101` | AC-01, AC-04 | QA-PET-02 | TC-PET-02 `testDataDrivenAddPet` (×5) | `@TC-PET-02 @data-driven` | — | ✅ PASS (all 5) |
| **Pet** | (wider) | AC-02 | QA-PET-03 | TC-PET-03 `testAddPetWithBlankNameValidation` | `@TC-PET-03 @negative` | — | ✅ PASS |
| **Pet** | (wider) | AC-03 | QA-PET-04 | TC-PET-04 `testFutureBirthDateBehavior` | `@TC-PET-04 @defect` | **PC-201** | ❌ FAIL — SUT accepted future date |
| **Pet** | (wider) | AC-05 | QA-PET-05 | TC-PET-05 `testEditExistingPet` | `@TC-PET-05 @regression` | — | ✅ PASS |
| **Owner** | `PC-102` | FR-O1 Add (valid) | QA-OWN-01 | TC-OWN-01 `tcOwn01_addOwnerWithValid10DigitTelephone` | — | — | ✅ PASS |
| **Owner** | `PC-102` | FR-O1 Add (boundary −1) | QA-OWN-02 | TC-OWN-02 `tcOwn02_addOwnerWith9DigitTelephoneRejected` | — | — | ✅ PASS |
| **Owner** | `PC-102` | FR-O1 Add (boundary +1) | QA-OWN-03 | TC-OWN-03 `tcOwn03_addOwnerWith11DigitTelephoneRejected` | — | — | ✅ PASS |
| **Owner** | `PC-102` | FR-O1 Add (non-digit) | QA-OWN-04 | TC-OWN-04 `tcOwn04_addOwnerWithNonDigitTelephoneRejected` | — | — | ✅ PASS |
| **Owner** | `PC-102` | FR-O1 Add (blank) | QA-OWN-05 | TC-OWN-05 `tcOwn05_addOwnerWithBlankTelephoneRejected` | — | — | ✅ PASS |
| **Owner** | `PC-102` | FR-O1 (data-driven) | QA-OWN-06 | TC-OWN-06 `tcOwn02to04_telephoneBoundaryValuesRejected` (×5) | — | — | ✅ PASS (all 5) |
| **Owner** | `PC-102` | FR-O2 Edit (valid) | QA-OWN-07 | TC-OWN-06 `tcOwn06_editOwnerWithValidDetails` | — | — | ✅ PASS |
| **Owner** | `PC-102` | FR-O2 Edit (blank) | QA-OWN-08 | TC-OWN-07 `tcOwn07_editOwnerWithBlankMandatoryFieldRejected` | — | — | ✅ PASS |
| **Owner** | `PC-102` | FR-O2 Edit (tel too short) | QA-OWN-09 | TC-OWN-08 `tcOwn08_editOwnerTelephoneTooShortRejected` | — | — | ✅ PASS |
| **Owner** | `PC-102` | FR-O2 Edit (tel too long) | QA-OWN-10 | TC-OWN-09 `tcOwn09_editOwnerTelephoneTooLongOrNonDigitRejected` | — | — | ✅ PASS |
| **Owner** | `PC-103` | FR-O4 Case-insensitive search | QA-OWN-11 | TC-OWN-10 `frO4_ownerSearchShouldBeCaseInsensitive` | — | **PC-202** | ❌ FAIL — lowercase 'davis' not found |
| **Owner** | `PC-103` | FR-PG1 Pagination (5/page) | QA-OWN-12 | TC-OWN-11 `frPg1_ownerListShouldShowFiveOwnersPerPage` | — | **PC-203** | ❌ FAIL — SUT shows 6 per page |
| **Owner** | `PC-103` | FR-O3 Search (empty) | QA-OWN-13 | `emptySearchReturnsAllOwners` | — | — | ✅ PASS |
| **Owner** | `PC-103` | FR-O3 Search (no match) | QA-OWN-14 | `searchWithNoMatchOwner` | — | — | ✅ PASS |
| **Owner** | `PC-103` | FR-O3 Search (one match) | QA-OWN-15 | `searchWithOnlyOneMatchOwner` | — | — | ✅ PASS |
| **Owner** | `PC-103` | FR-O3 Search (multi) | QA-OWN-16 | `searchWithMultipleMatchOwner` | — | — | ✅ PASS |
| **Owner** | `PC-103` | FR-O4 Case-insensitive (data-driven) | QA-OWN-17 | `searchIsCaseInsensitive` (Davis ✅, davis ❌) | — | **PC-202** | ❌ FAIL (lowercase) |
| **Owner** | `PC-103` | FR-PG1 Pagination click | QA-OWN-18 | `testOwnerListPagination` | — | — | ✅ PASS |
| **Billing** | `PC-104` | FR-S4 Discount boundary | QA-BILL-01 | TC-CAL-01 subtotal 140 (no discount) | `@TC-BILL-01` | — | ✅ PASS |
| **Billing** | `PC-104` | FR-S4 Discount boundary | QA-BILL-02 | TC-CAL-02 subtotal 150 (10% discount) | `@TC-BILL-01` | **PC-204** | ❌ FAIL — SUT gives 0% discount at exactly 150 |
| **Billing** | `PC-104` | FR-S5 Tax on post-discount | QA-BILL-03 | TC-CAL-03 subtotal 170 | — | **PC-204** | ❌ FAIL — tax computed on wrong base |
| **Billing** | `PC-104` | FR-S5 Tax | QA-BILL-04 | TC-CAL-04 subtotal 60 | — | — | ✅ PASS |
| **Billing** | `PC-104` | FR-S5 Tax (combo) | QA-BILL-05 | TC-CAL-05 subtotal 190 | — | **PC-204** | ❌ FAIL — tax computed on pre-discount base |
| **Billing** | `PC-104` | FR-S5 Tax (Surgery) | QA-BILL-06 | TC-CAL-06 subtotal 240 | — | **PC-204** | ❌ FAIL — tax computed on pre-discount base |
| **Billing** | `PC-104` | FR-S3 Remove service | QA-BILL-07 | TC-CAL-07 | — | **PC-205** | ❌ FAIL — service not removed from billing; subtotal not updated |
| **Billing** | `PC-104` | FR-S3 Remove multiple | QA-BILL-08 | TC-CAL-08 | — | **PC-205** | ❌ FAIL — subtotal not updated after removal |
| **Billing** | `PC-104` | FR-S3/FR-S4 Remove at threshold | QA-BILL-09 | TC-CAL-09 | — | **PC-205** | ❌ FAIL — subtotal/discount not updated |
| **Billing** | `PC-104` | FR-S3/FR-S5 Remove above threshold | QA-BILL-10 | TC-CAL-10 | — | **PC-205** | ❌ FAIL — stale values after removal |
| **Vet** | `PC-105` | FR-V1 List display | QA-VET-01 | `testVetListDisplayed` | — | — | ✅ PASS |
| **Vet** | `PC-105` | FR-V1 Specialties | QA-VET-02 | `testVetSpecialtiesDisplayed` | — | — | ✅ PASS |
| **Vet** | `PC-105` | FR-V1 Count | QA-VET-03 | `testVetCount` | — | — | ✅ PASS |
| **BDD Pet** | `PC-101` | AC-01 | — | — | TC-PET-01 `@smoke` | — | ✅ PASS (Cucumber) |
| **BDD Pet** | `PC-101` | AC-01, AC-04 | — | — | TC-PET-02 `@data-driven` (×5) | — | ✅ PASS |
| **BDD Pet** | wider | AC-02 | — | — | TC-PET-03 `@negative` | — | ✅ PASS |
| **BDD Pet** | wider | AC-03 | — | — | TC-PET-04 `@defect` | **PC-201** | ❌ FAIL (expected — defect test) |
| **BDD Pet** | wider | AC-05 | — | — | TC-PET-05 `@regression` | — | ✅ PASS |
| **BDD Billing** | `PC-104` | FR-S1/FR-S4/FR-S5 | — | — | TC-BILL-01 Billing total (×2 rows) | **PC-204** | See BDD run result |
| **BDD Visit** | `PC-104` | AC-VISIT-01 Date boundary | — | — | TC-VISIT-01 (offset −1, 0, +1) | — | See BDD run result |
| **BDD Visit** | `PC-104` | AC-VISIT-02 Today rejected | — | — | TC-VISIT-02 | — | See BDD run result |

---

## Defect summary

| Defect ID | Area | AC / FR | Failing Test(s) | Severity | Status |
|---|---|---|---|---|---|
| **PC-201** | Pet | AC-03 Future date | TC-PET-04, BDD `@TC-PET-04` | HIGH | Open |
| **PC-202** | Owner Search | FR-O4 Case-insensitive | TC-OWN-10, `searchIsCaseInsensitive` (lowercase) | MEDIUM | Open |
| **PC-203** | Owner Pagination | FR-PG1 5-per-page | TC-OWN-11 `frPg1_...` | MEDIUM | Open |
| **PC-204** | Billing Discount | FR-S4 Discount at exactly 150 / FR-S5 Tax base | TC-CAL-02,03,05,06 | HIGH | Open |
| **PC-205** | Billing Remove | FR-S3 Remove service recalculation | TC-CAL-07,08,09,10 | HIGH | Open |

---

## Evidence file index

| Evidence | Path |
|---|---|
| Selenium Pet report | `Selenium/reports/pet/TEST-tests.pet.PetManagementTest.xml` |
| Selenium Billing report | `Selenium/reports/billing/TEST-tests.billing.VisitServicesBillingTest.xml` |
| Selenium Owner report | `Selenium/reports/owner/TEST-tests.owner.OwnerManagementTest.xml` |
| Selenium Pagination report | `Selenium/reports/owner/TEST-tests.owner.OwnerSearchPaginationTest.xml` |
| Selenium Vet report | `Selenium/reports/veterinarian/TEST-tests.veterinarian.VeterinarianDirectoryTest.xml` |
| BDD Cucumber HTML | `BDD/reports/cucumber.html` |
| BDD Cucumber JSON | `BDD/reports/cucumber.json` |
| JMeter JTL | `JMeter/results.jtl` |
| JMeter load summary | `JMeter/load_level_summary.csv` |
| Katalon execution report | `Katalon/Reports/` (to be added after Katalon run — see `Katalon/KATALON_RUN_INSTRUCTIONS.md`) |
