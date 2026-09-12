# AI Use Declaration and Critique — ASG3

> **Word document Section 8 content. Copy this verbatim into the Word report.**

---

## 8.1 AI tool declaration

| Item | Detail |
|---|---|
| **Tool** | Google Antigravity (IDE-integrated AI coding assistant, Claude Sonnet 4.6 Thinking model) |
| **Purpose** | Code review and improvement suggestions; generating boilerplate test infrastructure; drafting documentation artifacts (traceability matrix, defect log, release summary) |
| **Team members using it** | [List each member and their specific use] |
| **Evidence of AI use** | This document; Git commit history showing AI-assisted authoring dates |

---

## 8.2 Substantive AI contribution: BDD Billing step definitions

The AI assistant generated the following files from a specification-only prompt:

- `BDD/src/test/java/steps/BillingStepDefinitions.java`
- `BDD/src/test/java/pages/BillingServicesPage.java`
- `BDD/src/test/java/pages/BillingFixtureFlow.java`
- `BDD/src/test/java/pages/VisitBookingPage.java`

The prompt supplied was the existing `VisitServicesAndBilling.feature` file plus the Selenium module's `VisitServicesPage` and `VisitFixtureFlow` as context.

---

## 8.3 AI failures found by running the SUT — two verified errors

### Failure 1: Incorrect Cucumber step expression for optional plural

**AI output (original)**

```java
@When("I attempt to book a visit dated {int} day(s) from today")
public void i_attempt_to_book_a_visit_dated_offset_days_from_today(int offset) { ... }
```

**What happened after running**

`mvn test -f BDD/pom.xml` produced:

```
[ERROR]   Run 1: The step 'I attempt to book a visit dated 1 day(s) from today' is undefined.
```

Cucumber's expression parser treats `(s)` as a **Cucumber expression optional text group** — it matches either `day` or `days`, but the annotation text `day(s)` did not match the Gherkin step text `day(s)` literally because the parentheses are interpreted rather than literal.

**Evidence**: BDD run log at `/tmp/bdd_run.log`, line:
```
The step 'I attempt to book a visit dated 1 day(s) from today' is undefined.
```

**Fix applied**

```java
// Escape the parentheses so they are treated as literals in the expression
@When("I attempt to book a visit dated {int} day\\(s\\) from today")
public void i_attempt_to_book_a_visit_dated_offset_days_from_today(int offset) { ... }
```

After the fix, the step matches correctly and the scenario runs.

---

### Failure 2: Race condition — Add Visit link clicked before pet-save redirect completed

**AI output (original)**

```java
public VisitBookingPage createOwnerPetAndOpenVisitForm() {
    createOwner();
    createPet();
    // Click Add Visit
    wait.until(ExpectedConditions.elementToBeClickable(ADD_VISIT_LINK)).click();
    ...
}
```

**What happened after running**

The `createPet()` method calls `clickSubmitExpectingSuccess()`, which waits for the URL to match `.*/owners/\d+`, but this URL also matches `/owners/1/pets/new` if the redirect has not completed. As a result, the fixture immediately tried to click the Add Visit link while still on the pet form, which did not have a Visit link — causing a timeout:

```
Expected condition failed: waiting for pages.VisitBookingPage$$Lambda (tried for 15 second(s))
Current url: "http://localhost:8080/owners/1/pets/new"
```

**Evidence**: BDD run log at `/tmp/bdd_run.log` — `VisitBookingPage` lambda timeout with URL still on pet form.

**Fix applied**

Added an explicit presence-of-element wait for the `Owner Information` heading (which only appears on the Owner Details page, not the Pet form) before clicking Add Visit:

```java
public VisitBookingPage createOwnerPetAndOpenVisitForm() {
    createOwner();
    createPet();
    // Explicitly wait for owner detail page before clicking Add Visit link
    wait.until(ExpectedConditions.presenceOfElementLocated(OWNER_DETAIL_READY));
    wait.until(ExpectedConditions.elementToBeClickable(ADD_VISIT_LINK)).click();
    ...
}
```

This ensures the fixture only proceeds once the pet-save redirect has fully resolved.

---

## 8.4 Limitations and review process

All AI-generated code was:

1. **Compiled** — verified with `mvn test-compile -f BDD/pom.xml` before being committed.
2. **Executed** — run against the live SUT (`A3-Testing-Site.jar` at `localhost:8080`) and failures recorded.
3. **Corrected** — each AI failure listed above was diagnosed, documented, and fixed before the final submission.

AI-generated documentation (traceability matrix, defect log, release summary) was cross-checked line-by-line against actual Surefire XML reports before being finalized.
