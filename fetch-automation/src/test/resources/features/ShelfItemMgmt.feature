@shelf

Feature: Shelf Item Management Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @FETCH-311 @FETCH-182
  Scenario: User should be able to validate Shelf Item Management Page icons and tabs
    When user navigates to Shelf Item Management Page
    And user looks at the header
    Then the name of shelf is displayed
    And the hamburger menu is clickable
    And the search bar is visible
    And the login button is clickable


  Scenario: User should be able to validate Page Labels
    When user navigates to Shelf Item Management Page
    Then user verifies shelf labels on Non-Trayed Items Management Page

      | labelname      |
      | Facility       |
      | Shelf Location |
      | Media Type     |
      | Container Type |
      | Accession Date |
      | Item Count     |
      | # of Items Out |
      | Delete Count   |


  Scenario: User should be able to validate Non-Trayed Items labels
    When user navigates to Shelf Item Management Page
    Then user verifies non-trayed items labels on Non-Trayed Items Management Page

      | labelname      |
      | Barcode        |
      | Media Type     |
      | Shelf Location |
      | Shelf Position |
      | Accession Date |
      | Subcollection  |
      | Container Type |


  Scenario: User should be able to validate Overlay Slide with Item's details
    When user navigates to Shelf Item Management Page
    And user clicks on item in the table
    Then the overlay slide is visible
    And user verifies item details on Overlay Slide

      | labelname       |
      | Barcode:        |
      | Media Type:     |
      | Dimensions:     |
      | Accession Date: |










