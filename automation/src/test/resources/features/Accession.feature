@accession

Feature: Accession Page Functionality Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @FETCH-314 @FETCH-249 @regression
  Scenario: User should be able to verify Front-End Layout for Accession Job of Trayed Items
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Trayed Accession
    Then user verifies required and optional fields on Start New Accession modal

      | fieldname             |
      | Owner (Required)      |
      | Media Type (Optional) |

    And Owner dropdown is clickable
    And Media Type field is clickable
    And back button is clickable
    And cancel button is enabled
    And submit button is disabled


  @FETCH-314 @FETCH-249 @regression
  Scenario: User should be able to verify Front-End Layout for Accession Job of Non-Trayed Items
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Non-Tray Accession
    Then user verifies required and optional fields on Start New Accession modal

      | fieldname                 |
      | Owner (Required)          |
      | Container Size (Optional) |
      | Media Type (Optional)     |

    And Owner dropdown is clickable
    And Container Size dropdown is clickable
    And Media Type field is clickable
    And back button is clickable
    And cancel button is enabled
    And submit button is disabled


  @FETCH-904 @FETCH-773 @regression @smoke
  Scenario: User should be able to create a new Accession Job when required field is selected
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    Then submit button is enabled and clickable
    And user clicks cancel button
    When user clicks Start Accession button
    And user selects Non-Tray Accession
    And user selects all required fields
    Then submit button is enabled


  @FETCH-904 @FETCH-773 @negative
  Scenario: User should not be able to create a new Accession Job if required field is not selected
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Trayed Accession
    And user selects Media Type
    Then submit button is disabled
    And user clicks cancel button
    When user clicks Start Accession button
    And user selects Non-Tray Accession
    And user selects Container Size
    And user selects Media Type
    Then submit button is disabled


  @FETCH-904 @FETCH-773 @regression
  Scenario: User should be able to see a New Scanned Item as a First Item in the table
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    Then user clicks submit button
    Then user scans Barcode
    And user enters barcode by scanning
    And user enters a second barcode by scanning
    Then user verifies that new added barcode is displayed first in the table
    When user clicks three dot menu next to Accession Job Number
    And user clicks Cancel Job
    Then user confirms cancellation


  @FETCH-358 @FETCH-194 @search @regression
  Scenario: User should be able to verify dropdown search functionality
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Trayed Accession
    And user types "<search_query>" in the Owner dropdown search field
    And Owner dropdown should display options related to search query
    Then user selects an option from the Owner dropdown
    And user types "<search_query>" in the Media Type dropdown search field
    And Media Type dropdown should display options related to search query
    Then user selects an option from the Media Type dropdown


  @FETCH-545 @FETCH-455 @add_tray
  Scenario: User should be able to Add Tray to Trayed Assession Job
    When user navigates to the Accession Page
    When user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    And user selects Media Type
    And user clicks submit button
    And user scans Barcode
    And user enters barcode by scanning
    Then user switches off Toggle Barcode Scan
    When user selects one of the barcodes in the table
    Then user verifies Enter Barcode button is changed to Edit Barcode
    And user clicks Edit Barcode button
    Then verify new modal allowing to edit the barcode is displayed
    And user edits the barcode and clicks submit button
    Then verify Add Tray button is activated
    And user clicks Add Tray button
    Then verify new modal Select Tray is displayed
    And user clicks add tray on the modal
    And the container is cleared out so a new tray can be scanned
    When user clicks three dot menu next to Accession Job Number
    And user clicks Cancel Job
    Then user confirms cancellation


  @FETCH-545 @FETCH-455 @FETCH-627 @FETCH-582 @nontrayed_accession @regression @smoke
  Scenario: User should be able to go through the Accession workflow process from start to finish for a Non-Trayed Job
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Non-Tray Accession
    And user selects all fields
    And user clicks submit button
    Then user verifies "An Accession Job has successfully been created." alert msg
    And user switches off Toggle Barcode Scan
    When user clicks Enter Barcode button
    And user enters barcode and clicks Submit button
    When user clicks Enter Barcode button
    Then user enters second barcode and clicks Submit button
    When user selects one of the barcodes in the table
    And user clicks Edit Barcode button
    And user edits the barcode and clicks submit button
    Then user verifies "The item has been updated." alert msg
    Then user verifies that edited barcode is displayed
    When user selects one of the barcodes in the table
    And user clicks Delete
    And user clicks Delete Item
    Then user verifies "The selected item(s) has been removed." alert msg
    When user clicks three dot menu next to Accession Job Number
    And user clicks Edit
    And user edits Container Size
    And user edits Media Type
    And user clicks Save Edits
    Then user verifies "The job has been updated." alert msg
    When user clicks Pause Job button
    Then user verifies "Job Status has been updated to: Paused" alert msg
    When user clicks Resume Job button
    Then user verifies "Job Status has been updated to: Running" alert msg
    And user clicks three dot menu next to Accession Job Number
    And user clicks Cancel Job
    Then user verifies warning message
    And user confirms cancellation


  @FETCH-627 @FETCH-582 @trayed_accession @regression @smoke
  Scenario: User should be able to go through the Accession workflow process from start to finish for a Trayed Job
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    Then user selects Media Type
    And user clicks submit button
    Then user verifies "An Accession Job has successfully been created." alert msg
    And user scans Barcode
    When user enters barcode by scanning
    And user verifies that scanned barcode is displayed
    Then user switches off Toggle Barcode Scan
    When user clicks Enter Barcode button
    And user enters barcode and clicks Submit button
    When user clicks three dot menu next to Accession Job Number
    And user clicks Edit
#    And user edits Media Type
    And user clicks Save Edits
    Then user verifies "The tray has been updated." alert msg
    When user selects one of the barcodes in the table
    Then user verifies Enter Barcode button is changed to Edit Barcode
    When user clicks Edit Barcode button
    And user edits the barcode and clicks submit button
    Then user verifies "The item has been updated." alert msg
    When user selects one of the barcodes in the table
    And user clicks Delete
    And user clicks Delete Item
    Then user verifies "The selected item(s) has been removed." alert msg
    When user clicks Pause Job button
    Then user verifies "Job Status has been updated to: Paused" alert msg
    When user clicks Resume Job button
    Then user verifies "Job Status has been updated to: Running" alert msg
    And user clicks three dot menu next to Accession Job Number
    And user clicks Cancel Job
    Then user verifies warning message
    And user confirms cancellation


  @FETCH-586 @FETCH-474 @complete&print
  Scenario: User should be able to validate Accession Job Batch Sheet Template Creation
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Non-Tray Accession
    And user selects all fields
    Then user clicks submit button
    Then user verifies "An Accession Job has successfully been created." alert msg
    When user switches off Toggle Barcode Scan
    And user clicks Enter Barcode button
    Then user enters item barcode
    When user clicks Complete Job button
    And user clicks Complete&Print button
    Then user is able to see a print window with a batch report


  @FETCH-917 @FETCH-735 @delete_tray @regression
  Scenario: User should be able to delete Tray from Accession job
    When user navigates to the Accession Page
    When user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    Then user selects Media Type
    And user clicks submit button
    Then user verifies "An Accession Job has successfully been created." alert msg
    And user scans Barcode
    When user enters barcode by scanning
    Then user switches off Toggle Barcode Scan
    When user clicks three dot menu next to Accession Job Number
    When user clicks Edit Tray Barcode
    And user edits Tray Barcode
    Then user submits the change
    Then user verifies "The tray has been updated." alert msg
    And user clicks three dot menu next to Accession Job Number
    Then user clicks Delete Tray
    And user verifies delete tray warning message
    Then user confirms delete tray action


  @FETCH-917 @FETCH-735 @cancel_accession
  Scenario: User should be able to cancel an Accession Job
    When user navigates to the Accession Page
    And user selects an Accession Job
    And user clicks three dot menu next to Accession Job Number
    And user clicks Cancel Job
    Then user verifies warning message
    And user confirms cancellation


  @FETCH-1001 @FETCH-839 @print
  Scenario: User should be able to verify Print Options for Accession Job Summary
    When user navigates to the Accession Page
    Then user selects an Accession Job
    When user clicks three dot menu next to Accession Job Number
    And user clicks Print Job
    Then user is able to see a print window with a batch report


  @FETCH-1000 @FETCH-886
  Scenario: User should be able to verify Duplicate Barcodes Restriction for Trayed/Non-Tray Accession
    When user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Non-Tray Accession
    And user selects all fields
    And user clicks submit button
    Then user verifies "An Accession Job has successfully been created." alert msg
    And user saves Accession Job number
    And user scans "12345000055" barcode
    Then user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    And user clicks submit button
    Then user verifies "An Accession Job has successfully been created." alert msg
    And user scans Barcode
    And user scans "12345000055" barcode
    Then user verifies error alert modal is displayed
    When user navigates to the Accession Page
    Then user navigates to the created earlier accession job
    When user selects one of the barcodes in the table
    And user clicks Delete
    And user clicks Delete Item
    Then user verifies "The selected item(s) has been removed." alert msg
    Then user navigates to the Accession Page
    And user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    And user clicks submit button
    Then user verifies "An Accession Job has successfully been created." alert msg
    And user scans Barcode
    And user scans "12345000055" barcode
    Then user verifies that barcode "12345000055" is displayed
    When user scans "12345000055" barcode
    Then user verifies the barcode is not displayed twice
    Then user clicks three dot menu next to Accession Job Number
    And user clicks Cancel Job
    Then user confirms cancellation


  @FETCH-1002 @FETCH-887
  Scenario: User should be able to verify Edit/Complete Buttons Disabled for Completed Accession Job
    When user navigates to the Accession Page
    Then user navigates to the completed job using Top Search
    And user verifies all the action buttons are disabled
    When user selects one of the barcodes in the table
    Then user verifies Edit Barcode and Delete buttons are disabled
    And user scans "12345005555" barcode
    Then user verifies that barcode "12345005555" is not displayed
    When user clicks three dot menu next to Accession Job Number
    Then user verifies only Print Job option is enabled


  @FETCH-1073 @FETCH-914 @regression
  Scenario: User should be able to enforce Item Barcode Rules for Invalid Barcode Types
    When user navigates to the Accession Page
    Then user creates an Accession Job
    And user saves Accession Job number
    Then user switches off Toggle Barcode Scan
    When user enters an Item barcode of invalid type
    Then user verifies "Barcode value is invalid for Item barcode rules." error alert is displayed
    And user verifies the Item barcode is not added
    When user enters an existing in the system Tray barcode
    Then user verifies "The scanned barcode exists but is not an "Item" barcode! Please try again." error alert is displayed
    And user verifies the Tray barcode is not added
    Then user clicks Complete Job button
    And user clicks Complete
    When user clicks Verification on side navigation menu
    Then user navigates to the verification job
    When user switches ON Toggle Barcode Scan
    And user scans Tray Barcode
    Then user verifies "The job has been updated." alert msg
    When user switches off Toggle Barcode Scan
    And user enters an Item barcode of invalid type
    Then user verifies "Barcode value is invalid for Item barcode rules." error alert is displayed
    And user verifies the Item barcode is not added
    When user enters an existing in the system Tray barcode
    Then user verifies "The scanned barcode exists but is not an "Item" barcode! Please try again." error alert is displayed
    And user verifies the Tray barcode is not added































































