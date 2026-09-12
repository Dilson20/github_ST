@US-VISIT-BILLING
Feature: Visit Services and Billing

  @TC-VISIT-01 @AC-VISIT-01 @negative @boundary
  Scenario Outline: Visit date boundary validation
    Given an owner has a pet with no recorded visits
    When I attempt to book a visit dated <offset> day(s) from today
    Then the booking should be <result>

    Examples:
      | offset | result   |
      | -1     | rejected |
      | 0      | rejected |
      | 1      | accepted |

  @TC-BILL-01 @AC-BILL-01 @data-driven
  Scenario Outline: Billing total calculated correctly for selected services
    Given a visit exists with no services selected
    When I add "<service1>" and "<service2>" to the visit
    Then the displayed total should be "<expectedTotal>"

    Examples:
    | service1      | service2       | expectedTotal |
    | Vaccination   | X-Ray          | 151.20        |
    | Microchipping | X-Ray          | 145.80        |
    | Surgery       |                | 233.28        |

  @TC-VISIT-02 @AC-VISIT-02 @negative
  Scenario: Reject a visit dated today
    Given an owner has a pet with no recorded visits
    When I attempt to book a visit dated today
    Then the visit should be rejected with a validation error
