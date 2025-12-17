@breadcrumb

Feature: Breadcrumb Functionality Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1

  @FETCH-533 @FETCH-472 @regression
  Scenario: User should be able to validate Breadcrumb Navigation
    When user clicks Accession on side navigation menu
    Then user should see the corresponding breadcrumbs

      | breadcrumb |
      | Home       |
      | Accession  |

    And user clicks Start Accession button
    And user selects Non-Tray Accession
    And user selects all required fields
    And user clicks submit button
    Then user should see the following breadcrumbs
    When user clicks three dot menu next to Accession Job Number
    And user clicks Cancel Job
    Then user verifies warning message
    And user confirms cancellation
    Then user is able to click X to cancel alert
    When user clicks on Home breadcrumb link
    Then user should navigate to the Home page



