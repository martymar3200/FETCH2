@withdrawal

Feature: Withdrawal Page Functionality Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @FETCH-867 @FETCH-825 @regression
  Scenario: User should be able to verify Withdrawal Dashboard
    When user navigates to the Withdrawal Page
    Then user verifies Withdrawal dashboard column names

      | column         |
      | Job ID #       |
      | # of Items     |
      | Status         |
      | Date Created   |


  @regression @smoke
  Scenario: User should be able to create a Withdraw Job
    When user navigates to the Withdrawal Page
    And user clicks Create Withdraw Job
    Then user verifies "A Withdraw Job has been successfully created." alert msg
    And user verifies a Withdraw Job is created


  @assigned_user @smoke
  Scenario:  User should be able to change Assigned User within a Withdraw Job
    When user navigates to the Withdrawal Page
    And user clicks Create Withdraw Job
    Then user verifies "A Withdraw Job has been successfully created." alert msg
    When user clicks three dot menu next to Job Number
    Then user clicks Edit
    Then Assign User dropdown is clickable
    When user selects User from dropdown
    And Save Edits button is clickable
    And Cancel edits button is clickable
    Then user clicks Save Edits button
    And user verifies "The job has been updated." alert msg
    And user verifies the assigned user has been updated


  @regression @smoke
  Scenario: User should be able to delete a Withdraw Job
    When user navigates to the Withdrawal Page
    And user clicks on the created Withdraw Job
    And user clicks three dot menu next to Job Number
    And user clicks Delete Job
    And user confirms delete job action
    Then user verifies "The Withdraw Job has been canceled." alert msg


  @FETCH-867 @FETCH-825
  Scenario: User should be able to validate Withdraw Dashboard and Job Detail UI
    When user navigates to the Withdrawal Page
    And user clicks on Withdraw Job
    Then user verifies the Withdraw Job detail page is displayed
    When user clicks three dot menu next to Job Number
    Then Edit option is displayed
    And Delete Job option is displayed
    And Print Job option is displayed
    And View History option is displayed
    When user clicks three dots menu next to the Item Barcode in the table
    Then user verifies "Remove Item" option is displayed


  @FETCH-893 @FETCH-826
  Scenario: User should be able to create/complete Withdraw job
    When user clicks Accession on side navigation menu
    And user completes a new Non-Tray Accession Job
    When user navigates to the Verification Page
    And user navigates to the verification job
    And user saves Verification Job number
    Then user verifies item barcode
    When user clicks Complete Job button
    And user clicks Complete
    Then user verifies "The Job has been completed." msg
    When user clicks Shelving on side navigation menu
    Then user completes a Shelving Job
    When user navigates to the Withdrawal Page
    When user clicks Create Withdraw Job
    Then user verifies "A Withdraw Job has been successfully created." alert msg
    When user clicks Add Items
    Then user selects Manually Enter Barcode option
    When user enters Item Barcode with status IN
    Then user clicks Submit
    And user verifies "Successfully added an item to the Withdraw Job!" alert msg
    When user clicks three dot menu next to Job Number
    Then user verifies three dots menu options are displayed
    When user clicks three dots menu next to the Item Barcode in the table
    Then user verifies "Remove Item" option is displayed
    When user clicks Create Pick List job button
    Then user clicks the alert link
    And user clicks Retrieve Pick List
    Then user scans a Pick List Container
    And user verifies the item is retrieved
    When user clicks Complete Job
    And user clicks Complete
    Then user verifies "The Pick List Job has been completed." alert msg
    When user navigates to the Withdrawal Page
    And user navigates back to the Withdraw job
    Then user verifies the item status has changed to OUT
    When user clicks Withdraw Items button
    Then user verifies the item status has changed to WITHDRAWN


  @FETCH-1423 @FETCH-1141
  Scenario: User should be able to verify the Location Information is displayed on Withdraw Job page
    When user navigates to the Withdrawal Page
    And user clicks on Withdraw Job
    Then user verifies the Withdraw Job detail page is displayed
    And user verifies Items in Job column names

      | column              |
      | Shelf Barcode       |
      | Tray Barcode        |
      | Barcode             |
      | Owner               |
      | Item Status         |
      | Location            |


  @FETCH-1385 @FETCH-1143 @print
  Scenario: User should be able to verify Print Options for Withdraw Job Summary
    When user navigates to the Withdrawal Page
    Then user clicks on Withdraw Job
    When user clicks Withdraw Items button
    Then user verifies Withdraw&Print option is displayed
    When user clicks three dot menu next to Job Number
    And user clicks Print Job
    Then user is able to see a print window with a batch report


  @date_created
  Scenario: User should be able to verify Withdraw job created date
    When user navigates to the Withdrawal Page
    And user clicks Create Withdraw Job
    Then user verifies "A Withdraw Job has been successfully created." alert msg
    And user verifies a Withdraw Job is created
    And user verifies date created


  Scenario: User should be able to add to Withdraw job Items with status "Out"
    When user creates a Request
    And user creates a Pick List job
    And user clicks Retrieve Pick List
    Then user saves an item barcode
    When user scans a Pick List Container
    Then user verifies the item is retrieved
    When user clicks Complete Job
    And user clicks Complete
    Then user verifies "The Pick List Job has been completed." alert msg
    When user navigates to the Withdrawal Page
    When user clicks Create Withdraw Job
    And user clicks Add Items
    Then user selects Scan Items option
    When user scans an Item Barcode from a completed Pick List job
    And user verifies scanned barcode is displayed




