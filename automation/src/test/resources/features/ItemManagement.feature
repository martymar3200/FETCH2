@item

Feature: Tray/Non-Tray Item Management Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @exact_search @item
  Scenario: User should be able to validate Item Exact Search feature
    Then user clicks search bar menu
    When user selects Item option
    And user searches Item barcode
    And user clicks on search result
    Then user verifies the overlay slide is visible
    When user clicks to see Item Request History
    Then user verifies Item Details page is displayed


  @FETCH-1255 @FETCH-1117 @FETCH-287 @FETCH-170
  Scenario: User should be able to validate Item Detail page
    When user navigates to Item Details page
    Then user verifies the page header
    And user verifies item barcode is visible
    And Rearrange dropdown is visible and clickable
    And user verifies Item information

      | label                  |
      | Tray Barcode           |
      | Shelf Barcode          |
      | Owner                  |
      | Status                 |
      | Media Type             |
      | Size Class             |
      | Accession Date         |
      | Shelved Date:          |
      | Last Requested Date:   |
      | Last Refile Date:      |
      | Withdrawal Date        |
      | Location               |

    And user verifies Request History columns

      | column               |
      | Request ID           |
      | External Request ID  |
      | Request Date         |

    When user clicks on Shelf Barcode
    Then user verifies Shelf Details page is displayed
    When user clicks on Container in Shelf
    Then user verifies Item Details page is displayed




