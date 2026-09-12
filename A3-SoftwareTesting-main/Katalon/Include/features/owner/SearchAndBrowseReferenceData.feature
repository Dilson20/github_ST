Feature: Search and Browse Reference Data
  As a clinic receptionist
  I want to search for owners and browse the veterinarian
  So that I can find the information that I need quickly
  
  Scenario: Empty search returns all owners
  	Given the receptionist has more than one owner registered
  	When the receptionist searchs for owners without any lastname entered
  	Then all registered owners should be displayed as a paginated list
 
  Scenario: Searching for non-existent owners shows an error	
  	Given no owner is registered under the lastname "Nguyen"
  	When the receptionist searchs for an owner by that lastname 
  	Then a "has not been found" message should be displayed
  
  Scenario: Searching for matching only one owner navigates directly to owner detail page
  	Given only one owner is registered under the lastname "Franklin"
  	When the receptionist searchs for an owner by that lastname
  	Then the receptionsist should be navigated directly to that owner detail page
  
  Scenario: Searching for matching multiple owners navigates to owner pagination list
  	Given multiple owners are registered under the lastname "Davis"
  	When the receptionist searchs for an owner by that lastname
  	Then a owner pagination list is shown
  
  Scenario Outline: Searching is case-insensitive
  	Given multiple owners are registered under the lastname "Davis"
  	When the receptionist searchs for an owner using "<lastname>"
  	Then a owner pagination list is shown
  	
 	Examples: 
      | lastname |
      | Davis    |     
      | davis    |     
      
  Scenario: A veterinarian without specialty is still displayed
  	Given a veterinarian is registered with no assigned specialty
  	When the receptionist views the veterinarian directory
  	Then that veterinarian should appear in the list without an error


