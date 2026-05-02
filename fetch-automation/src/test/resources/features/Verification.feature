@verification

Feature: Verification Page Functionality Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @FETCH-585 @FETCH-456 @trayed @next_tray
  Scenario: User should be able to validate Verification Job features for a Trayed Item
    Then user clicks Accession on side navigation menu
    When user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    And user selects Media Type
    And user clicks submit button
    Then user verifies "An Accession Job has successfully been created." alert msg
    And user scans Barcode
    And user scans the barcode of the item
    Then verify Add Tray button is activated
    And user clicks Add Tray button
    Then verify new modal Select Tray is displayed
    And user clicks add tray on the modal
    And the container is cleared out so a new tray can be scanned
    Then user scans another Barcode
    And user enters a second barcode by scanning
    When user clicks Complete Job button
    Then user clicks Complete
    And user clicks Verification on side navigation menu
    When user navigates to the verification job
    Then Tray container view is displayed
    And user scans Tray Barcode
    And user verifies the barcode by scanning
    And user clicks Next Tray button
    And user clicks on new tray on the modal
    Then the container is cleared out so a new tray can be scanned
    Then user scans second Tray Barcode
    And user verifies the barcode by scanning
    When user clicks Complete Job button
    And user clicks Complete
    Then user verifies "The Job has been completed." msg


  @FETCH-585 @FETCH-456 @FETCH-627 @FETCH-582 @nontrayed_verification @regression @smoke
  Scenario: User should be able to go through the Verification workflow process from start to finish for a Non-Trayed Job
    When user clicks Accession on side navigation menu
    Then user clicks Start Accession button
    When user selects Non-Tray Accession
    And user selects all fields
    Then user clicks submit button
    Then user verifies "An Accession Job has successfully been created." alert msg
    And user switches off Toggle Barcode Scan
    When user clicks Enter Barcode button
    And user enters the barcode and clicks Submit button
    When user clicks Complete Job button
    And user clicks Complete
    Then user verifies "The Job has been completed and moved for verification." alert msg
    Then user clicks Verification on side navigation menu
    When user navigates to the verification job
    Then user verifies non-trayed items container view is displayed
    And user clicks three dot menu next to Job Number
    And user clicks Edit
    And user edits Container Size field
#    And user edits Media Type field
    And user clicks Save Edits
    Then user verifies "The job has been updated." alert msg
    When user selects one of the barcodes in the table
    And user clicks Edit Barcode button
    And user edits the barcode and clicks Submit button
    Then user verifies "The item has been updated." alert msg
    And user clicks Enter Barcode button
    And user enters item barcode and clicks Submit button
    Then user confirms they want to add a new item to the job
    When user verifies second barcode
    When user selects one of the barcodes in the table
    And user clicks Delete
    And user clicks Confirm
    Then user verifies "The selected item(s) has been removed." alert msg
    When user clicks Pause Job button
    Then user verifies "Job Status has been updated to: Paused" alert msg
    When user clicks Resume Job button
    Then user verifies "Job Status has been updated to: Running" alert msg
    When user clicks Complete Job button
    And user clicks Complete
    Then user verifies "The Job has been completed." msg


  @FETCH-627 @FETCH-582 @trayed_verification @regression @smoke
  Scenario: User should be able to go through the Verification workflow process from start to finish for a Trayed Job
    Then user clicks Accession on side navigation menu
    When user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    And user selects Media Type
    And user clicks submit button
    And user scans Barcode
    And user scans the item barcode
    When user clicks Complete Job button
    Then user clicks Complete
    Then user verifies "The Job has been completed and moved for verification." alert msg
    When user clicks Verification on side navigation menu
    And user navigates to the verification job
    Then Tray container view is displayed
    And user scans Tray Barcode
    Then user verifies "The job has been updated." alert msg
    And user clicks three dot menu next to Job Number
    And user clicks Edit
    And user edits Media Type
    And user clicks Save Edits
    Then user verifies "Verification Container Has Been Updated" alert msg
    And user verifies the barcode
    When user clicks Enter Barcode button
    And user enters the barcode and clicks Submit button
    Then user confirms they want to add a new item to the job
    When user selects one of the barcodes in the table
    Then user verifies Enter Barcode button is changed to Edit Barcode
    And user clicks Edit Barcode button
    And user edits the barcode and clicks Submit button
    Then user verifies "The item has been updated." alert msg
    When user selects one of the barcodes in the table
    And user clicks Delete
    And user clicks Confirm
    Then user verifies "The selected item(s) has been removed." alert msg
    When user clicks Pause Job button
    Then user verifies "Job Status has been updated to: Paused" alert msg
    When user clicks Resume Job button
    Then user verifies "Job Status has been updated to: Running" alert msg
    When user clicks Complete Job button
    And user clicks Complete
    Then user verifies "The Job has been completed." msg


  @FETCH-1001 @FETCH-839 @FETCH-569 @FETCH-475 @print
  Scenario: User should be able to verify Print Options for Verification Job Summary
    When user navigates to the Verification Page
    And user navigates to the verification job link
    When user clicks three dot menu next to Job Number
    And user clicks Print Job
    Then user is able to see a print window with a batch report


  @FETCH-1002 @FETCH-887
  Scenario: User should be able to verify Edit/Complete Buttons Disabled for Completed Verification Job
    When user navigates to the Verification Page
    Then user verifies that completed jobs are not displayed
    When user navigates to the completed job using Top Search
    And user verifies all the action buttons are disabled
    When user selects one of the barcodes in the table
    Then user verifies Edit Barcode and Delete buttons are disabled
    And user scans "12345005555" barcode
    Then user verifies that barcode "12345005555" is not displayed
    When user clicks three dot menu next to Accession Job Number
    Then user verifies only Print Job option is enabled


  @FETCH-1466 @FETCH-1069 @cancel_verification
  Scenario: User should be able to cancel a Verification Job
    When user clicks Accession on side navigation menu
    And user completes a Non-Tray Accession Job
    When user navigates to the Verification Page
    And user selects a Verification Job
    Then user saves Verification Job number
    When user clicks three dot menu next to Job Number
    And user clicks Cancel Job
    Then user verifies warning message
    And user confirms verification job cancellation
    When user navigates to the Accession Page
    Then user verifies the status of the corresponding Accession job
    And user clicks three dot menu next to Accession Job Number
    And user clicks Cancel Job
    Then user verifies warning message
    And user confirms cancellation






















