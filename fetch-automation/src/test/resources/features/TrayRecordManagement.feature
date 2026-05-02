@tray

Feature: Tray Record Management Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @exact_search @tray
  Scenario: User should be able to validate Tray Exact Search feature
    Then user clicks search bar menu
    When user selects Tray option
    And user searches Tray barcode
    And user clicks on search result
    Then user verifies Tray Details page is displayed


  @FETCH-1271 @FETCH-1118 @FETCH-287 @FETCH-170
  Scenario: User should be able to validate Tray Details page
    When user navigates to Tray Details page
    Then user verifies the page header
    And user verifies tray barcode is visible
    And Rearrange dropdown is visible and clickable
    And user verifies Tray information

      | label          |
      | Shelf Barcode  |
      | Owner          |
      | Media Type     |
      | Size Class     |
      | Accession Date |
      | Shelved Date   |
      | Location       |

    And user verifies Items in Tray columns

      | column  |
      | Barcode |
      | Status  |

    When user clicks on Item in Tray
    Then user verifies Tray Item Details page is displayed
    When user clicks on Tray Barcode
    Then user verifies Tray Details page is displayed


#  @FETCH-287 @FETCH-170
#  Scenario: User should be able to validate Tray Labels
#    When user navigates to Item Management Page
#    Then user verifies tray labels on Items Management Page
#
#      | labelname       |
#      | Facility        |
#      | Shelf Location  |
#      | Media Type      |
#      | Container Type  |
#      | Accession Date  |
#      | Shelved Date    |
#      | Withdrawal Date |
#      | Item Count      |
#      | # of Items Out  |
#      | Delete Count    |
#
#
#  @FETCH-298 @FETCH-180
#  Scenario: User should be able to validate Items in Tray labels
#    When user navigates to Item Management Page
#    Then user verifies items labels on Items Management Page
#
#      | labelname          |
#      | Barcode            |
#      | Media Type         |
#      | Size Class         |
#      | Temporary Location |
#      | Permanent Location |
#      | Volume             |
#      | Arrival Date       |
#      | Accession Date     |
#      | Withdrawal Date    |
#      | Container Type     |
#
