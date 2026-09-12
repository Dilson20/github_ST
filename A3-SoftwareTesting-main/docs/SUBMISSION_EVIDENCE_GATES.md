# A3 submission evidence gates

This repository contains source code and draft evidence. Do not submit it until every gate below is satisfied with artefacts produced from the same release candidate and commit.

## 1. Reproducible Selenium evidence

1. Start the assigned SUT JAR and reset its in-memory data.
2. From `Selenium/`, run `mvn test`.
3. Keep the resulting `target/surefire-reports/` directory, including any intentional defect failures.
4. Archive that directory with the commit SHA, JAR SHA-256, run date, Java version, Chrome version and operating system.

Do not use the existing `Selenium/reports/` files as proof for the current source: their package names and outcomes do not consistently match `Selenium/src/test/java/`.

## 2. Katalon evidence

1. Execute both `TS_PetBDD` and `TS_BillingBDD` in Katalon against the same reset SUT.
2. Export the Katalon execution report, including the deliberately failing specification-contradiction scenario.
3. Attach the exported report and screenshots to the matching QAlity test run and Jira defect.

The Maven Cucumber report in `BDD/reports/` is supplementary only. It does not prove that the Katalon Billing feature was executed.

## 3. Jira and QAlity evidence

For every one of the five functional areas, export evidence of:

- a Jira story with specific acceptance criteria;
- a QAlity case linked to that story and acceptance criteria;
- an executed cycle with its final status;
- each defect's expected requirement, exact actual value, severity rationale, priority, owner, triage status, detecting test and screenshot; and
- a complete Story -> AC -> QAlity case -> automation -> defect traceability table.

Replace all `TO CREATE`, `TO ASSIGN` and candidate-only labels before submission.

## 4. Report consistency gate

The Word report, Surefire report, Katalon report, Jira export, QAlity export and JMeter data must state the same run date, build identifier and pass/fail counts. The release recommendation must be recalculated from those artefacts; do not claim a two-failure run if the supplied Surefire XML lists more failures.

## 5. Remaining non-code deliverables

- Complete Word report sections 4, 5, 6.2-6.5 and 8 with the real exported evidence.
- Include an AI-use declaration and a critique of one substantive contribution with two runtime/specification-verified failures and resulting changes.
- Add the client-facing slide deck and ensure each member presents their assigned stream.
