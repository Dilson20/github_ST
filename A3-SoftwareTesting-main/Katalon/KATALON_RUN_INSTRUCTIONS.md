# Katalon BDD Execution Instructions

> **You must follow these steps manually in Katalon Studio to produce the rubric-required Katalon execution report.**  
> The Maven Cucumber report in `BDD/reports/` covers the same scenarios but is supplementary only.

---

## Prerequisites

1. Katalon Studio installed (version 9.x or later recommended).
2. SUT running: `java -jar A3-Testing-Site.jar` from the repository root.
3. Chrome or Chromium available on your machine.

---

## Step 1 — Open the Katalon project

1. Launch Katalon Studio.
2. **File → Open Project**.
3. Navigate to `<repo-root>/Katalon/` and select `demo.prj`.
4. Wait for the project to load (Dependencies will resolve automatically).

---

## Step 2 — Verify execution profiles

1. In the left panel, expand **Profiles**.
2. Open `default` — confirm `URL = http://localhost:8080`.
3. If the SUT is running on a different port, update the URL here.

---

## Step 3 — Execute TS_PetBDD

1. In the left panel, expand **Test Suites → Pet**.
2. Right-click `TS_PetBDD` → **Run**.
3. Select **Chrome** as the browser.
4. Wait for the run to complete.
5. **Expected outcome**:
   - TC-PET-01: PASSED
   - TC-PET-02 (×5 rows): PASSED
   - TC-PET-03: PASSED
   - TC-PET-04 `@defect`: **FAILED** (expected — SUT accepts future date; this is defect PC-201)
   - TC-PET-05: PASSED

> [!IMPORTANT]
> TC-PET-04 **must fail** in this run. Do not disable or skip it. The failing result is your evidence that defect PC-201 was detected by automation.

---

## Step 4 — Execute TS_BillingBDD

1. In the left panel, expand **Test Suites → Billing**.
2. Right-click `TS_BillingBDD` → **Run**.
3. Select **Chrome**.
4. Wait for the run to complete.
5. **Expected outcome**:
   - TC-VISIT-01 (offsets −1 and 0): **FAILED** if SUT rejects past/today dates — or PASSED if those are rejected (check feature: offset −1 and 0 → `rejected`; offset +1 → `accepted`). Adjust expectation based on SUT behaviour.
   - TC-BILL-01 (row 1 Microchipping + X-Ray → 145.80): **FAILED** (PC-204 — discount not applied at 150.00)
   - TC-BILL-01 (row 2 Surgery → 233.28): **FAILED** (PC-204 — tax on pre-discount base)
   - TC-VISIT-02 (today rejected): PASS or FAIL depending on SUT date validation.

---

## Step 5 — Export the execution report

1. After both test suites finish, Katalon auto-generates reports in `Katalon/Reports/`.
2. To export as PDF/HTML:
   - In the **Test Suite** result view, click **Export to HTML** (toolbar button).
3. Save the exported report to `Katalon/Reports/` using a timestamped name, e.g.:
   - `TS_PetBDD_2026-09-10.html`
   - `TS_BillingBDD_2026-09-10.html`

---

## Step 6 — Attach to Word and QAlity

1. **Word report Section 4.2** — copy the Katalon pass/fail summary table into the BDD section.
2. **QAlity** — attach the exported HTML report to the relevant test execution cycle:
   - Pet cycle → attach `TS_PetBDD_2026-09-10.html` + screenshots of TC-PET-04 failure.
   - Billing cycle → attach `TS_BillingBDD_2026-09-10.html` + screenshots of TC-BILL-01 failures.
3. **Jira defects** — attach TC-PET-04 failure screenshot to **PC-201**; TC-BILL-01 failure screenshots to **PC-204**.

---

## Troubleshooting

| Symptom | Solution |
|---|---|
| `Connection refused localhost:8080` | Start SUT: `java -jar A3-Testing-Site.jar` in a separate terminal |
| Chrome version mismatch | Katalon auto-manages ChromeDriver; if it fails, go to **Window → Katalon Studio Preferences → Katalon → Web UI** and click **Update WebDrivers** |
| Step `the booking should be rejected` always passes | Check the SUT — offset −1 (yesterday) and 0 (today) should be rejected. If SUT accepts them, this is an additional defect to document. |
| `TS_BillingBDD` cannot find Manage Services link | Ensure the SUT JAR version is `A3-Testing-Site.jar` SHA-256 `7246e232...` (run: `shasum -a 256 A3-Testing-Site.jar`) |
