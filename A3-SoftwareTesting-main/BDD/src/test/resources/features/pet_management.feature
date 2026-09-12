Feature: Pet Management Lifecycle
  As a Pet Clinic receptionist / front-desk staff
  I want to register new pets and update existing pet details for a registered pet owner
  So that the clinic maintains accurate medical and identification records for all patient animals

  Background:
    Given the clinic application is running on "http://localhost:8080"
    And an existing owner with ID 1 is present in the system

  @TC-PET-01 @AC-01 @smoke
  Scenario: Successfully register a new pet with valid details
    Given I navigate to the pet registration form for owner 1
    When I submit pet details with name "Maximus", birth date "2023-04-15", and type "dog"
    # [IMPROVED] Verify the submitted species as part of the acceptance outcome.
    Then the pet "Maximus" with type "dog" should be successfully recorded under owner 1

  @TC-PET-02 @AC-01 @AC-04 @data-driven
  Scenario Outline: Add pets with diverse species and valid dates
    Given I navigate to the pet registration form for owner 1
    When I submit pet details with name "<petName>", birth date "<birthDate>", and type "<species>"
    Then the pet "<petName>" with type "<species>" should be successfully recorded under owner 1

    Examples:
      | petName  | birthDate   | species |
      | Whiskers | 2022-01-10  | cat     |
      | Tweety   | 2023-03-15  | bird    |
      | Hammy    | 2022-11-20  | hamster |
      | Sly      | 2021-08-05  | snake   |
      | Iggy     | 2020-05-12  | lizard  |

  @TC-PET-03 @AC-02 @negative @validation
  Scenario: Prevent pet registration when required name is missing
    Given I navigate to the pet registration form for owner 1
    When I submit pet details with name "" and type "cat"
    Then the system should reject the submission and display a validation error

  @TC-PET-04 @AC-03 @defect @business-rule
  # [IMPROVED] Keep the future date in Examples and assert the defect honestly.
  Scenario Outline: Reject pet registration when birth date is set in the future
    Given I navigate to the pet registration form for owner 1
    When I submit pet details with name "<petName>" and a future birth date "<futureDate>"
    Then the system should refuse the future birth date and prevent creation

    Examples:
      | petName   | futureDate |
      | FuturePup | tomorrow   |

  @TC-PET-05 @AC-05 @regression
  Scenario: Edit an existing pet and update its profile
    Given I navigate to the owner details page for owner 1
    When I edit the pet named "Leo" and change the name to "LeoUpdated" and type to "bird"
    Then the pet "LeoUpdated" with type "bird" should be successfully recorded under owner 1
