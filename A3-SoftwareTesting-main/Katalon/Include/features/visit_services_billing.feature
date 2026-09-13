@VisitServicesAndBilling
Feature: Visit Services and Billing

  As a clinic receptionist
  I want every visit priced from the services the pet received, and shown the same wherever it appears
  So that the owner is charged one consistent amount that follows the clinic's billing policy

  Background:
    Given a visit has been booked for an owner's pet

  @Scenario-1 @US-CAL-01 @US-CAL-02 @US-CAL-05 @FR-S2 @FR-S4 @FR-S5 @FR-S6
  Scenario Outline: A visit's total is identical on every screen that shows it
    When the receptionist adds the services "<services>" to the visit
    Then the billing screen, the owner's page and the pet's visit history all show a total of "<total>"
    And the billing screen shows a subtotal of "<subtotal>"
    And the billing screen shows a discount of "<discount>"
    And the billing screen shows tax of "<tax>"

    Examples:
      | services             | subtotal | discount | tax   | total  |
      | none                 | 40.00    | 0.00     | 3.20  | 43.20  |
      | Microchipping, X-Ray | 150.00   | 15.00    | 10.80 | 145.80 |
      | Surgery              | 240.00   | 24.00    | 17.28 | 233.28 |
      
  @Scenario-2 @US-CAL-03 @US-CAL-05 @FR-S2 @FR-S3 @FR-S6
  Scenario: Removing a service re-prices the visit on every screen straight away
    Given the receptionist adds the services "Vaccination, Dental Cleaning" to the visit
    When the receptionist removes the "Dental Cleaning" service from the visit
    Then the billing screen, the owner's page and the pet's visit history all show a total of "64.80"

  @Scenario-3 @US-CAL-04 @US-CAL-05 @FR-S2 @FR-S3 @FR-S6
  Scenario: Removing services one at a time re-prices the visit after every removal
    Given the receptionist adds the services "Vaccination, Dental Cleaning, X-Ray, Microchipping" to the visit
    Then the billing screen shows a subtotal of "220.00"
    And the billing screen shows a discount of "22.00"
    And the billing screen shows tax of "15.84"
    And the billing screen shows a total of "213.84"
    When the receptionist removes the "Microchipping" service from the visit
    Then the billing screen shows a subtotal of "190.00"
    And the billing screen shows a discount of "19.00"
    And the billing screen shows tax of "13.68"
    And the billing screen shows a total of "184.68"
    When the receptionist removes the "X-Ray" service from the visit
    Then the billing screen shows a subtotal of "110.00"
    And the billing screen shows a discount of "0.00"
    And the billing screen shows tax of "8.80"
    And the billing screen shows a total of "118.80"
