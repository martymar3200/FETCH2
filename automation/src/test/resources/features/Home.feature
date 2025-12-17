@home

Feature: Home Page Functionality Validation

  Background:
    Given user navigates to FETCH Homepage


  @regression
  Scenario: User should be able to validate Homepage icons and tabs
    When user logs in as a tester1
    And user verifies FETCH logo is displayed
    Then the hamburger menu is clickable
    And the search bar is visible
    And the login button is clickable


  @FETCH-1365 @FETCH-1021
  Scenario: User should be able to validate Home Landing Page Implementation
    When user logs in as a tester1
    Then user verifies FETCH logo is displayed
    And user verifies the welcome message is displayed
    And user verifies the welcome message contains users name
    When user clicks logout button
    And user logs in as an admin
    Then user verifies FETCH logo is displayed
    And user verifies the welcome message is displayed
    And user verifies the welcome message contains users name
    When user clicks logout button
    And user logs in with invalid email
    Then user verifies "User not found for test@example.com" alert msg


  @regression @smoke
  Scenario: User should be able to validate side navigation tabs on Homepage
    When user logs in as a tester1
    Then user verifies side navigation tabs on Homepage

      | tabname      |
      | Accession    |
      | Verification |
      | Shelving     |
      | Request      |
      | Pick List    |
      | Refile       |
      | Withdrawal   |
      | Reports      |


  @FETCH-526 @FETCH-454 @active_links @regression @smoke
  Scenario: User should tell which Page/Section of the WebApp they are currently at
    When user logs in as a tester1
    And user clicks Accession on side navigation menu
    Then verify that Accession navigation link on side menu is highlighted
    When user clicks Verification on side navigation menu
    Then verify that Verification navigation link on side menu is highlighted
    When user clicks Shelving on side navigation menu
    Then verify that Shelving navigation link on side menu is highlighted
    When user clicks Request on side navigation menu
    Then verify that Request navigation link on side menu is highlighted
    When user clicks Pick List on side navigation menu
    Then verify that Pick List navigation link on side menu is highlighted
    When user clicks Refile on side navigation menu
    Then verify that Refile navigation link on side menu is highlighted
    When user clicks Withdrawal on side navigation menu
    Then verify that Withdrawal navigation link on side menu is highlighted
    When user clicks Reports on side navigation menu
    Then verify that Reports navigation link on side menu is highlighted


  @FETCH-694 @FETCH-548 @smoke
  Scenario: User should be able to verify successful login with valid credentials
    When user logs in as a tester1
    Then user should be able to verify account name on user dashboard


  @FETCH-694 @FETCH-548 @negative
  Scenario: User should see an Error Message when logs in with invalid credentials
    When user logs in with invalid email
    Then user verifies "User not found for test@example.com" alert msg


  @FETCH-765 @FETCH-670 @FETCH-1510 @FETCH-1308 @scanning @regression
  Scenario: User should be able to use Toggle Scanning Function
    When user logs in as a tester1
    Then user verifies barcode scanning is enabled
    When user switches off Toggle Barcode Scan
    Then user verifies barcode scanning is disabled


  @current_date
  Scenario: User should be able to check current date
    When user logs in as a tester1
    Then user verifies today date


  @FETCH-1172 @FETCH-967
  Scenario: User should be able to validate table columns on Shelving, Pick List and Withdrawal pages
    Then user logs in as a tester1
    When user clicks Shelving on side navigation menu
    And user verifies that completed jobs are not displayed
    Then user verifies columns displayed

      | column                 |
      | Job Number             |
      | # of Containers in Job |
      | Status                 |
      | Assigned User          |
      | Date Added             |
      | Last Updated           |

    When user clicks filter icon
    Then user verifies default filter options

      | option                 |
      | Created                |
      | Paused                 |
      | Running                |

    When user clicks Pick List on side navigation menu
    And user verifies that completed jobs are not displayed
    Then user verifies columns displayed

      | column                 |
      | Job Number             |
      | Building               |
      | # of Items in Job      |
      | Status                 |
      | Assigned User          |
      | Date Added             |
      | Last Updated           |

    When user clicks filter icon
    Then user verifies default filter options

      | option                 |
      | Created                |
      | Paused                 |

    When user clicks Withdrawal on side navigation menu
    And user verifies that completed jobs are not displayed
    Then user verifies columns displayed

      | column                 |
      | Job ID #               |
      | # of Items             |
      | Status                 |
      | Date Created           |


    When user clicks filter icon
    Then user verifies default filter options

      | option                 |
      | Created                |
      | Paused                 |
      | Running                |


  @exact_search
  Scenario: User should be able to validate Exact Search feature
    When user logs in as a tester1
    And user clicks search bar menu
    Then user verifies search menu options

    | option        |
    | Item          |
    | Tray          |
    | Shelf         |
    | Accession     |
    | Verification  |
    | Shelving      |
    | Request       |
    | Batch Request |
    | Picklist      |
    | Refile        |
    | Withdraw      |

    When user selects Item option
    And user searches Item barcode
    And user clicks on search result
    Then user verifies the overlay slide is visible
    When user clicks to see Item Request History
    Then user verifies Item Details page is displayed


  @FETCH-894 @FETCH-793 @advanced_search
  Scenario: User should be able to validate Advanced Search feature
    When user logs in as a tester1
    And user clicks search bar menu
    Then user verifies search menu options

      | option        |
      | Item          |
      | Tray          |
      | Shelf         |
      | Accession     |
      | Verification  |
      | Shelving      |
      | Request       |
      | Batch Request |
      | Picklist      |
      | Refile        |
      | Withdraw      |

    And user verifies Advanced Search button is displayed
    When user selects Item option
    And user clicks Advanced Search
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Tray option
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Shelf option
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Accession option
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Verification option
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Shelving option
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Request option
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Batch Request option
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Picklist option
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Refile option
    Then user verifies Advanced Search modal is displayed
    When user clicks search bar menu
    And user selects Withdraw option
    Then user verifies Advanced Search modal is displayed

































