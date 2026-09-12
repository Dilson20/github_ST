# ASG3 Test Run Metadata

## Run date: 2026-09-10

### Environment

| Item | Value |
|---|---|
| **Date** | 2026-09-10 |
| **SUT** | `A3-Testing-Site.jar` |
| **JAR SHA-256** | `7246e232292a4a24e72d9cb5b0b08d29b212951e8099106c540b405dd0a9f9eb` |
| **Java version** | OpenJDK 17.0.19 (Homebrew) |
| **Browser** | Google Chrome 153.0.8010.36 |
| **OS** | macOS (Apple Silicon, arm64) |
| **Selenium** | 4.20.0 |
| **Cucumber** | 7.16.1 |
| **JUnit** | 5.10.2 |

### Selenium / JUnit 5 results

```
mvn test -f Selenium/pom.xml
Tests run: 45, Failures: 11*, Errors: 0, Skipped: 0
* Note: 11 test-method failures represent 10 unique defect assertions across 5 distinct defects
```

| Report file | Package name | Tests | Failures |
|---|---|---|---|
| `Selenium/reports/pet/TEST-tests.pet.PetManagementTest.xml` | `tests.pet.PetManagementTest` | 9 | 1 |
| `Selenium/reports/billing/TEST-tests.billing.VisitServicesBillingTest.xml` | `tests.billing.VisitServicesBillingTest` | 10 | 8 |
| `Selenium/reports/owner/TEST-tests.owner.OwnerManagementTest.xml` | `tests.owner.OwnerManagementTest` | 16 | 1 |
| `Selenium/reports/owner/TEST-tests.owner.OwnerSearchPaginationTest.xml` | `tests.owner.OwnerSearchPaginationTest` | 7 | 1 |
| `Selenium/reports/veterinarian/TEST-tests.veterinarian.VeterinarianDirectoryTest.xml` | `tests.veterinarian.VeterinarianDirectoryTest` | 3 | 0 |

### BDD / Cucumber results

```
mvn test -f BDD/pom.xml
Tests run: 15, Failures: 6, Errors: 0, Skipped: 0
```

| Report file | Scenarios | Passed | Failed |
|---|---|---|---|
| `BDD/reports/TEST-runners.CucumberTestRunner.xml` | 15 | 9 | 6 |
| `BDD/reports/cucumber.html` | — | — | — |
| `BDD/reports/cucumber.json` | — | — | — |

All 6 BDD failures trace to confirmed SUT defects: PC-201, PC-204, PC-206.

### JMeter results

```
JMeter/results.jtl
Total samples: 4,173
Error rate: 0.00%
```

### Katalon results

Katalon Studio manual run required. See `Katalon/KATALON_RUN_INSTRUCTIONS.md`.
