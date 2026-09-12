# Pet Clinic Test Automation & Performance Suite (Assignment 03)

This repository contains the comprehensive automated testing and performance assessment suite for the **Pet Clinic Release 2.0 candidate**. The project covers three core testing streams: **Selenium WebDriver Automation**, **Behavior-Driven Development (BDD / Cucumber & Katalon Studio)**, and **JMeter Performance Testing**, fully mapped to Jira User Stories and Acceptance Criteria.

---

## 📁 Repository Structure

```text
.
├── .gitignore                      # Excludes target/, .DS_Store, *.log, *.jar, etc.
├── README.md                       # Project overview and execution instructions
├── Selenium/                       # [Stream A] Pure Selenium Automation (Maven Project)
│   ├── pom.xml                     # Maven configuration (Selenium 4.27.0 + JUnit 5 + WebDriverManager + Surefire)
│   ├── src/test/java/
│   │   ├── pages/                  # Page Object Model classes organized by feature
│   │   │   ├── common/             # Shared base classes (BasePage.java)
│   │   │   ├── pet/                # Pet form pages (PetFormPage.java)
│   │   │   ├── owner/              # Owner management pages (OwnerDetailsPage.java)
│   │   │   └── billing/            # Visit & billing pages (VisitServicesPage.java, VisitFixtureFlow.java)
│   │   └── tests/                  # Test classes organized by feature
│   │       ├── pet/                # Pet management tests (PetManagementTest.java)
│   │       ├── owner/              # Owner management tests (OwnerManagementTest.java, OwnerSearchPaginationTest.java)
│   │       ├── billing/            # Visit services & billing tests (VisitServicesBillingTest.java)
│   │       └── veterinarian/       # Vet directory tests (VeterinarianDirectoryTest.java)
│   └── reports/                    # Execution reports categorized by feature
│       ├── pet/                    # Pet test reports (XML, TXT)
│       ├── owner/                  # Owner test reports (XML, TXT)
│       ├── billing/                # Visit services & billing test reports (XML, TXT)
│       └── veterinarian/           # Veterinarian test reports (XML, TXT)
├── Katalon/                        # [Stream B-1] Katalon Studio BDD & Acceptance Testing Project
│   ├── demo.prj                    # Katalon Studio project descriptor
│   ├── Include/
│   │   ├── features/               # Gherkin feature files organized by feature
│   │   │   ├── pet/                # Pet management features (pet_management.feature)
│   │   │   └── billing/            # Visit services & billing features (VisitServicesAndBilling.feature)
│   │   └── scripts/groovy/steps/   # Step definitions organized by feature
│   │       ├── pet/                # PetStepDefinitions.groovy
│   │       └── billing/            # VisitServicesAndBillingSteps.groovy
│   ├── Test Cases/                 # Katalon Test Cases by feature (Pet, Owner, Billing)
│   ├── Test Suites/                # Test Suites (Pet/TS_PetBDD, Regression/)
│   ├── Data Files/                 # Excel test data matrices & .dat descriptors (BVA & EP)
│   └── Object Repository/          # UI test object locators
├── BDD/                            # [Stream B-2] Maven Cucumber BDD Project (Alternative CLI Engine)
│   ├── pom.xml                     # Maven configuration (Cucumber 7 + JUnit Platform + Selenium 4)
│   ├── src/test/
│   │   ├── resources/features/     # Gherkin feature files
│   │   └── java/
│   │       ├── pages/              # Page Object Model classes used by step definitions
│   │       ├── steps/              # Step definitions (PetStepDefinitions.java)
│   │       └── runners/            # Cucumber Suite Runner (CucumberTestRunner.java)
│   └── reports/                    # Cucumber execution reports (HTML, JSON, XML)
├── JMeter/                         # [Stream C] Performance & Load Testing (Apache JMeter)
│   ├── billing/                    # Feature-specific JMeter plans (Asm3_VisitServiceBilling.jmx)
│   ├── PetClinic_Performance_Test.jmx # Comprehensive performance test plan
│   ├── results.jtl                 # Raw test metrics in CSV format
│   ├── load_level_summary.csv      # Aggregated performance summary table
│   ├── PERFORMANCE_ANALYSIS.md     # Detailed performance analysis and evaluation report
│   ├── html-dashboard-20260901/    # Generated HTML visual dashboard report
│   └── jmeter.log                  # JMeter execution log
└── docs/                           # Documentation & Requirements
    ├── pet/                        # Pet traceability (PET_TRACEABILITY_MATRIX.md)
    ├── A3 Assignment Details-1.pdf # Assignment brief and rubric
    └── A3 Description.pdf          # Pet Clinic system specification
```

---

## 🚀 Prerequisites

- **Java Development Kit (JDK)**: OpenJDK 17 or higher
- **Build Tool**: Apache Maven 3.8 or higher
- **Web Browser**: Google Chrome & compatible ChromeDriver (managed automatically via Selenium 4)
- **BDD Frameworks**: Katalon Studio (v9+ / v11+) or Maven CLI
- **Performance Tool**: Apache JMeter 5.6+

---

## 🛠️ Execution Guide

### 0. Start the System Under Test (SUT)
Ensure the Pet Clinic Release 2.0 application is running at `http://localhost:8080`:
```bash
java -jar A3-Testing-Site.jar
```

---

### 1. Execute Selenium Automated Tests
To run all Selenium WebDriver tests across all feature suites:
```bash
cd Selenium
mvn test
```
To run tests for a specific feature:
```bash
# Run Pet Management tests
mvn test -Dtest=PetManagementTest

# Run Owner Management tests (Search, Pagination, Create)
mvn test -Dtest=Owner*Test

# Run Visit Services and Billing tests
mvn test -Dtest=VisitServicesBillingTest

# Run Veterinarian Directory tests
mvn test -Dtest=VeterinarianDirectoryTest
```
- Test execution reports will be generated in `Selenium/target/surefire-reports/` and archived in `Selenium/reports/{pet, owner, billing, veterinarian}/`.

---

### 2. Execute BDD Acceptance Tests (Two Methods)

#### Method A: Using Katalon Studio (Recommended for Submission Reports)
1. Launch **Katalon Studio**.
2. Go to **File -> Open Project** and select `Katalon/demo.prj` located in this repository.
3. In the left-hand **Tests Explorer**:
   - Expand **Test Cases** -> Open **RunPetBDD** -> Click the green **Run (Chrome)** button.
   - Or expand **Test Suites** -> Open **TS_PetBDD** -> Click **Run (Chrome)** to execute and automatically compile suite reports.
4. After completion, view and export the execution report from the **Reports** directory in HTML or PDF format for the submission's `Exports/` folder.

#### Method B: Using Maven CLI (Cucumber Engine)
To run the Cucumber BDD suite directly from the command line:
```bash
cd BDD
mvn test
```
- Interactive HTML Report: `BDD/target/cucumber-reports/cucumber.html`
- JSON Metrics Report: `BDD/target/cucumber-reports/cucumber.json`

---

### 3. Execute JMeter Performance Tests
To run performance tests in non-GUI mode and output an HTML dashboard:
```bash
cd JMeter
jmeter -n -t PetClinic_Performance_Test.jmx -l results.jtl -e -o ./html-dashboard
```
To view the existing baseline performance dashboard:
```bash
open JMeter/html-dashboard-20260901/index.html
```

---

## 👥 Collaboration & Academic Integrity (Section 6.1)

In compliance with course guidelines:
1. **Private Repository**: This repository must remain **Private** throughout the semester to prevent collusion.
2. **Collaborator**: Add the course assessor as a collaborator upon creation: `honguyenphubao@gmail.com`.
3. **Individual Commits**: Each group member must commit their respective work under their own GitHub account as evidence for individual contribution assessment.
