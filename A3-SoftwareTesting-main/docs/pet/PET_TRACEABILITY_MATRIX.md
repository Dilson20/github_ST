# Pet Management Traceability Matrix

> [NEW][UPDATED] This document follows the official Pet Management User Story and AC wording supplied for the assignment.
>
> Pet area scope: **one Jira User Story only**, linked to **AC-01 and AC-04**. AC-02, AC-03 and AC-05 remain covered by the wider Pet Management test set, but are not linked to this specific Story unless the team creates a separate Story for them.

## 1. Official Jira Story scope

### `PC-101` — Register and maintain pet records

**User story**

As a Pet Clinic receptionist / front-desk staff, I want to register new pets and update existing pet details for a registered pet owner, so that the clinic maintains accurate medical and identification records for all patient animals.

**Linked AC:** `AC-01`, `AC-04`

**Test type:** Functional / Positive / Smoke, with data-driven coverage

**Priority:** High

**Preconditions:**

- The Pet Clinic system is running at `http://localhost:8080`.
- A valid owner exists, for example Owner ID 1, George Franklin.

## 2. Primary traceability matrix for this Story

| Jira Story | Linked AC | QAlity test case | Automation test | BDD scenario/tag | Defect | Evidence / status |
|---|---|---|---|---|---|---|
| `PC-101` | `AC-01` Successful Registration | `QA-PET-01` Valid pet registration | `TC-PET-01` `testAddNewPetSuccess` | `@TC-PET-01 @AC-01 @smoke` valid registration | None | **PASSED** — 2026-09-10 run; verifies name, birth date and type on Owner Details. Report: `Selenium/reports/pet/TEST-tests.pet.PetManagementTest.xml` |
| `PC-101` | `AC-01`, `AC-04` Successful Registration + Pet Type Selection | `QA-PET-02` Data-driven valid species selection | `TC-PET-02` `testDataDrivenAddPet` `@ParameterizedTest` | `@TC-PET-02 @AC-01 @AC-04 @data-driven` with 5 examples | None | **PASSED** (all 5 rows) — covers cat, bird, hamster, snake and lizard. |

## 3. Wider Pet Management coverage

These tests are retained because they are part of the supplied Pet Management test set. They are not linked to `PC-101` because that Story explicitly links only `AC-01` and `AC-04`.

| Coverage item | AC | QAlity test case | Automation test | BDD tag | Defect link | Relationship to `PC-101` |
|---|---|---|---|---|---|---|
| Blank Name validation | `AC-02` | `QA-PET-03` | `TC-PET-03` `testAddPetWithBlankNameValidation` | `@TC-PET-03 @AC-02 @negative` | None confirmed | Wider Pet coverage; not linked to `PC-101`. |
| Future Birth Date validation | `AC-03` | `QA-PET-04` | `TC-PET-04` `testFutureBirthDateBehavior` | `@TC-PET-04 @AC-03 @defect` | `PC-201` BUG-PET-01 | Wider Pet coverage; not linked to `PC-101`. Defect triaged — see `docs/DEFECT_LOG.md`. |
| Update Existing Pet | `AC-05` | `QA-PET-05` | `TC-PET-05` `testEditExistingPet` | `@TC-PET-05 @AC-05 @regression` | None confirmed | Wider Pet coverage; not linked to `PC-101`. |

## 4. QAlity test case records

| QAlity ID | Linked Jira Story | AC | Preconditions | Test data | Expected result | Automation link |
|---|---|---|---|---|---|---|
| `QA-PET-01` | `PC-101` | `AC-01` | SUT running; Owner ID 1 exists | Name `Maximus`; Birth Date `2023-04-15`; Type `dog` | Pet is added under Owner 1 and Owner Details displays Maximus, dog and 2023-04-15. | `TC-PET-01`; `@TC-PET-01 @AC-01` |
| `QA-PET-02` | `PC-101` | `AC-01`, `AC-04` | SUT running; an existing owner is available | Whiskers/cat/2022-01-10; Tweety/bird/2023-03-15; Hammy/hamster/2022-11-20; Sly/snake/2021-08-05; Iggy/lizard/2020-05-12 | Each valid dropdown choice is accepted and saved without truncation or mapping errors. | `TC-PET-02`; `@TC-PET-02 @AC-01 @AC-04` |
| `QA-PET-03` | Not linked to `PC-101` | `AC-02` | User is on `/owners/1/pets/new` | Blank name; 2023-01-01; cat | Submission is prevented, form remains open and a required-field error is displayed. | `TC-PET-03`; `@TC-PET-03 @AC-02` |
| `QA-PET-04` | Not linked to `PC-101` | `AC-03` | User is on `/owners/1/pets/new` | Future date (tomorrow); FuturePup; dog | Submission is blocked with a birth-date error and no pet is created. **Defect `PC-201` open.** | `TC-PET-04`; `@TC-PET-04 @AC-03` |
| `QA-PET-05` | Not linked to `PC-101` | `AC-05` | Owner 1 has pet Leo | Rename to LeoUpdated; change type to bird | Updated name/type appear on Owner Details and existing visit history remains associated. | `TC-PET-05`; `@TC-PET-05 @AC-05` |

## 5. Defect record

| Defect ID | Jira Story / AC | Detected by | Expected result | Actual result | Severity | Status |
|---|---|---|---|---|---|---|
| `PC-201` (BUG-PET-01) | Story for `AC-03` (wider Pet Management) | `QA-PET-04`, `TC-PET-04`, `@TC-PET-04` | Future birth date rejected; form stays open; no pet created. | SUT **accepts** the future date, redirects to Owner Details and creates the pet. | **High** — invalid medical data persisted. | Open. Evidence: 2026-09-10 Surefire report `TEST-tests.pet.PetManagementTest.xml`. |

## 6. Evidence locations

| Evidence | Location |
|---|---|
| Selenium test class | `Selenium/src/test/java/tests/pet/PetManagementTest.java` |
| Page Objects | `Selenium/src/test/java/pages/pet/` and `pages/owner/` |
| BDD feature | `BDD/src/test/resources/features/pet_management.feature` |
| BDD step definitions | `BDD/src/test/java/steps/PetStepDefinitions.java` |
| Surefire XML report | `Selenium/reports/pet/TEST-tests.pet.PetManagementTest.xml` |
| Cucumber HTML report | `BDD/reports/cucumber.html` |

## 7. HD completion checklist

- [x] Created Pet Story `PC-101` linking only `AC-01`, `AC-04`.
- [x] Created `QA-PET-01` through `QA-PET-05` with preconditions, test data and expected results.
- [x] Fresh Surefire evidence (2026-09-10) with correct package `tests.pet.PetManagementTest`.
- [x] BDD Cucumber report covers Pet scenarios including `@defect` tag.
- [x] Defect `PC-201` (BUG-PET-01) created and triaged against AC-03.
- [x] Complete Story → AC → QAlity case → Automation → Defect traceability table above.
