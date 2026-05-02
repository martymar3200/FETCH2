@shelf

Feature: Shelf Record Management Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @exact_search @shelf
  Scenario: User should be able to validate Shelf Exact Search feature
    Then user clicks search bar menu
    When user selects Shelf option
    And user searches Shelf barcode value
    And user clicks on search result
    Then user verifies Shelf Details page is displayed


  @FETCH-1288 @FETCH-1119 @FETCH-311 @FETCH-182
  Scenario: User should be able to validate Shelf Details page
    When user navigates to Shelf Details page
    Then user verifies the page header
    And user verifies shelf barcode is visible
    And Rearrange dropdown is visible and clickable
    And user verifies Shelf information

      | label               |
      | Shelf Number        |
      | Owner               |
      | Size Class          |
      | Created Date        |
      | Width               |
      | Height              |
      | Depth               |
      | Available Quantity  |
      | Used Quantity       |
      | Max Quantity        |
      | Shelf Location      |

    And user verifies Containers in Shelf columns

      | column  |
      | Barcode |

    When user clicks on Container in Shelf
    Then user verifies Item Details page is displayed
    When user clicks on Shelf Barcode
    Then user verifies Shelf Details page is displayed













