@shelving

Feature: Shelving Page Functionality Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @FETCH-313 @FETCH-165 @regression
  Scenario: User should be able to validate Shelf Management icons and tabs
    When user navigates to Shelving Page
    And user is on Shelving page
    Then the hamburger menu is clickable
    And the search bar is visible
    And the login button is clickable
    And Filter dropdown is clickable
    And Rearrange dropdown is clickable


  @FETCH-313 @FETCH-165
  Scenario: User should be able to select multiple options from Rearrange dropdown
    When user navigates to Shelving Page
    And user clicks on Rearrange dropdown
    Then user verifies dropdown options

      | optionname             |
      | Job Number             |
      | # of Containers in Job |
      | Status                 |
      | Assigned User          |
      | Date Added             |
      | Last Updated           |

    And user verifies dropdown checkboxes are clickable
    And user verifies dropdown options match shelf table column options


  @FETCH-313 @FETCH-165 @regression
  Scenario: User should be able to select multiple options from Rearrange Dropdown
    When user navigates to Shelving Page
    And user clicks on Rearrange dropdown
    Then user verifies all options are selected
    And user is able to deselect all the options
    And user selects options A, B and C from the dropdown
    Then selected options are displayed on the page


  @FETCH-313 @FETCH-165
  Scenario: User should be able to validate fields of Create Shelving Job From Verification modal
    When user navigates to Shelving Page
    And user clicks Create Shelving Job button
    And user selects From Verification Job option
    And user selects Yes
    Then a new modal with shelving location options along with the verification job selection is displayed
    And user verifies Create Shelving Job modal sections

      | section                           |
      | Assign Shelving Location?         |
      | Verification Job List:            |
      | Please Select Shelving Locations: |

    And user verifies Create Shelving Job modal dropdown fields

      | field                             |
      | Please Select Verification Job(s) |
      | Building                          |
      | Module                            |
      | Aisle                             |
      | Side                              |
      | Ladder                            |

    And cancel button is clickable
    And submit button is enabled and clickable


  @FETCH-360 @FETCH-336 @regression @smoke
  Scenario: User should be able to rearrange tables columns to their preferred order
    When user navigates to Shelving Page
    And user clicks on Rearrange dropdown
    And user unchecks menu items to their preferred order
    Then user verifies the Shelf Table column names


  @FETCH-648 @FETCH-380 @regression @smoke
  Scenario: User should be able to create a Shelving Job from Verification Jobs with Assigning Shelving Location
    When user clicks Accession on side navigation menu
    And user clicks Start Accession button
    Then user selects Non-Tray Accession
    When user selects all fields
    Then user clicks submit button
    And user verifies "An Accession Job has successfully been created." alert msg
    When user switches off Toggle Barcode Scan
    And user clicks Enter Barcode button
    And user enters second barcode and clicks Submit button
    When user clicks Complete Job button
    And user clicks Complete
    Then user verifies "The Job has been completed and moved for verification." alert msg
    And user switches ON Toggle Barcode Scan
    When user clicks Verification on side navigation menu
    And user navigates to the verification job
    And user saves Verification Job number
    Then user verifies item barcode
    When user clicks Complete Job button
    And user clicks Complete
    Then user verifies "The Job has been completed." msg
    When user clicks Shelving on side navigation menu
    And user clicks Create Shelving Job button
    And user selects From Verification Job option
    And user selects Yes
    Then user selects a created Verification Job
    And user selects a Building from Shelving Locations
    And user selects Module from dropdown
    And user selects Aisle from dropdown
    And user selects Right side
    And user selects Ladder
    Then submit button is enabled and clickable


  @print
  Scenario: User should be able to verify Print Options for Shelving Job Summary
    When user navigates to the Shelving Page
    Then user clicks on Shelving Job
    When user clicks three dot menu next to Job Number
    And user clicks Print Job
    Then user is able to see a print window with a batch report


#  @FETCH-625 @FETCH-382 @FETCH-686 @FETCH-383
#  Scenario:  User should be able to change Shelving Address within a Shelving Job
#    When user navigates to Shelving Page
#    Then user clicks Accession on side navigation menu
#    And user clicks Start Accession button
#    Then user selects Non-Tray Accession
#    When user selects all fields
#    Then user clicks submit button
#    When user clicks Enter Barcode button
#    And user enters second barcode and clicks Submit button
#    When user clicks Complete Job button
#    And user clicks Complete
#    Then user navigates to the Verification Page
#    And user navigates to the verification job
#    And user saves Verification Job number
#    Then user verifies item barcode
#    When user clicks Complete Job button
#    And user clicks Complete
#    Then user verifies "The Job has been completed." msg
#    When user clicks Shelving on side navigation menu
#    And user clicks Create Shelving Job button
#    And user selects From Verification Job option
#    Then user selects a created Verification Job
#    And user selects a Building from Shelving Locations
#    And user clicks Submit
#    Then user verifies "A Shelving Job has been successfully created." notification
#    And user verifies the Status is "Created"
#    When user clicks Execute Job
#    Then user verifies the Status is "Running"
#    And user clicks three dot menu next to a container
#    Then user should see Edit Location option
#    And user clicks Edit Location button
#    And user selects Ladder
#    And user selects Shelf
#    And user selects Shelf Position
#    And user clicks Submit
#    Then user verifies "The container has been updated." message


  @FETCH-686 @FETCH-383 @assigned_user @regression
  Scenario:  User should be able to change Assigned User within a Shelving Job
    When user navigates to Shelving Page
    Then user navigates to Shelving Job with Running Status
    And user clicks three dot menu next to Job Number
    And user clicks Edit
    Then Assign User dropdown is clickable
    And user selects User from dropdown
    And Save Edits button is clickable
    And Cancel edits button is clickable
    Then user clicks Save Edits button


  @FETCH-686 @FETCH-383 @negative
  Scenario: User should receive an Error Message when the Wrong Container is scanned
    When user navigates to Shelving Page
    When user navigates to Shelving Job with Running Status
    And user scans wrong Container
    Then user verifies "The scanned container does not exist in this shelving job. Please try again." alert msg


  @FETCH-648 @FETCH-380 @FETCH-684 @FETCH-438 @FETCH-686 @FETCH-383 @regression @smoke
  Scenario: User should be able to verify the Shelving Process from Verification Job
    When user clicks Accession on side navigation menu
    When user completes a new Trayed Accession Job
    And user navigates to the Verification Page
    Then user completes a Tray Verification Job
    When user clicks Complete Job button
    And user clicks Complete
    Then user verifies "The Job has been completed." msg
    When user clicks Shelving on side navigation menu
    And user clicks Create Shelving Job button
    And user selects From Verification Job option
    Then user selects a created Verification Job
    And user selects a Building from Shelving Locations
    And user clicks Submit
    Then user verifies "A Shelving Job has been successfully created." notification
    And user verifies the Status is "Created"
    When user clicks Execute Job
    Then user verifies the Status is "Running"
    When user clicks three dot menu next to Container
    And user verifies Edit Location button is clickable
    And user scans a Shelving Container
    Then user scans shelf to verify Container
    And user verifies "The container has been updated." alert msg
    Then user verifies second Container if exists
    When user clicks Pause Job button
    Then user verifies "Job Status has been updated to: Paused" alert msg
    When user clicks Resume Job button
    Then user verifies "Job Status has been updated to: Running" alert msg
    When user clicks Complete Job
    And user clicks Complete
    Then user verifies "The Shelving Job has been completed." alert msg


  @FETCH-685 @FETCH-439
  Scenario: User should be able to create a Shelving Job from Direct to Shelve Method
    When user navigates to Shelving Page
    Then user clicks Create Shelving Job button
    And user selects Direct To Shelve option
    And user selects a Building from Shelving Locations
    And user clicks Submit
    Then user verifies "A Direct Shelving Job has been successfully created." notification


  @FETCH-1042 @FETCH-881
  Scenario: User should be able to verify Metadata on Create Shelving Job From Verification modal
    When user navigates to Shelving Page
    Then user clicks Create Shelving Job button
    And user selects From Verification Job option
    When user clicks to select a job from Verification Job List
    Then user verifies that the response includes all the metadata required


  @date_created
  Scenario: User should be able to verify Shelving job created date
    When user navigates to Shelving Page
    Then user clicks Create Shelving Job button
    And user selects From Verification Job option
    When user selects a Verification Job from the Verification Jobs List
    And user selects a Building from Shelving Locations
    And user clicks Submit
    Then user verifies "A Shelving Job has been successfully created." notification
    And user verifies date created


  @FETCH-1035 @FETCH-891
  Scenario: User should be able to validate Metadata to Modal Addition
    When user clicks Shelving on side navigation menu
    And user clicks Create Shelving Job button
    Then user selects From Verification Job option
    When user clicks to select a job from Verification Job List
    Then user verifies a list of verification jobs is displayed
    And user verifies that the menu includes the correct data












































