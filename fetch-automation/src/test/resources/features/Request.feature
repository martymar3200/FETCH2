@request

Feature: Request Page Functionality Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @FETCH-748 @FETCH-606 @regression @smoke
  Scenario: User should be able to verify Front-End Layout of Request Dashboard
    When user navigates to the Request Page
    And user clicks Create button
    Then user verifies the dropdown options

      | option                    |
      | Add to Pick List          |
      | Create a Pick List        |
      | Create Manual Requests    |
      | Import Requests from File |

    And user selects Create Manual Requests option
    Then request job creation modal is displayed


  @FETCH-903 @FETCH-802 @regression
  Scenario: User should be able to verify Required Fields for Manual Request
    When user navigates to the Request Page
    And user clicks Create button
    And user selects Create Manual Requests option
    Then request job creation modal is displayed
    When user enters an item barcode
    And user enters Request ID
    Then submit button is enabled


  @FETCH-903 @FETCH-802 @negative
  Scenario: User should be able to verify Required Fields for Manual Request
    When user navigates to the Request Page
    And user clicks Create button
    And user selects Create Manual Requests option
    Then request job creation modal is displayed
    And user enters Requester Name
    And user selects Priority
    And user selects Request Type
    And user selects Delivery Location
    Then request submit button is disabled


  @manual_request @smoke
  Scenario: User should be able to create a Manual Request
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
    Then user switches off Toggle Barcode Scan
    When user completes a Shelving Job
    When user clicks Request on side navigation menu
    When user clicks Create button
    And user selects Create Manual Requests option
    Then request job creation modal is displayed
    When user enters shelved Item Barcode
    And user enters Request ID
    And user enters Requester Name
    And user selects Priority
    And user selects Request Type
    And user selects Delivery Location
    Then submit button is enabled
    And user clicks submit button
    And user verifies "Successfully created the request." alert msg


  @FETCH-903 @FETCH-802 @negative
  Scenario: User should be able to verify the Duplicate Item is not Added to Requests
    When user navigates to the Request Page
    And user has an Item present in request table
    And user clicks Create button
    And user selects Create Manual Requests option
    Then request job creation modal is displayed
    When user enters an existing in request table Item barcode
    And user enters Request ID
    Then submit button is enabled
    And user clicks submit button
    Then user verifies item already requested error msg


  @negative @manual_request
  Scenario: User should be able to verify Request Is Not Created When Incorrect Item barcode Is Entered
    When user navigates to the Request Page
    And user clicks Create button
    And user selects Create Manual Requests option
    Then request job creation modal is displayed
    When user enters an incorrect Item Barcode
    And user enters Request ID
    Then submit button is enabled
    And user clicks submit button
    Then user verifies "Barcode value 1234567890 not found" alert msg


  @FETCH-1418 @FETCH-1066
  Scenario: User should be able to edit a created Request
    When user navigates to the Request Page
    And user clicks on Request with status New
    Then user verifies Request Details page is displayed
    When user clicks three dots menu
    And user clicks Edit Request button
    Then request job creation modal is displayed
    And submit button is enabled
    And cancel button is enabled
    When user updates the request
    And user clicks submit button
    Then user verifies "Successfully updated the request." alert msg
    And user verifies the request has been updated


  @FETCH-1418 @FETCH-1066 @negative
  Scenario: User should not be able to edit a Request with Item status other than Requested
    When user navigates to the Request Page
    And user clicks on Request with status New
    Then user verifies Request Details page is displayed
    When user clicks three dots menu
    And user verifies Edit Request button is enabled
    When user clicks Request on side navigation menu
    And user clicks on Request with status other than New
    Then user verifies Request Details page is displayed
    When user clicks three dots menu
    And user verifies Edit Request button is disabled


  @date_created
  Scenario: User should be able to verify Pick List job created date
    When user navigates to the Request Page
    And user clicks Create button
    And user selects Create a Pick List option
    When user selects Building from dropdown
    And user clicks Submit
    Then user verifies options with checkboxes are displayed
    When user selects Request
    And user clicks Create Pick List
    Then user verifies the Pick List is created
    When user clicks the alert link
    Then user is able to see the Pick List dashboard
    And user verifies date created


  @FETCH-955 @FETCH-840
  Scenario: User should be able to verify Building is added to Request table
    When user navigates to the Request Page
    Then user verifies the Requests table column names

      | name                |
      | Request ID #        |
      | Request Type        |
      | Barcode             |
      | External Request ID |
      | Building            |
      | Requestor Name      |
      | Request Status      |
      | Priority            |
      | Media Type          |
      | Item Location       |
      | Delivery Location   |
      | Date Created        |


  @FETCH-1453 @FETCH-1315
  Scenario: User should be able to validate Request Details page
    When user navigates to the Request Page
    And user clicks on Request
    Then user verifies Request Details page is displayed
    When user clicks three dots menu
    And user verifies Edit Request button is displayed
    And user verifies Cancel Request button is displayed
    And user verifies Request information

      | label               |
      | Item Barcode        |
      | Request ID          |
      | External Request ID |
      | Request Status      |
      | Request Type        |
      | Priority            |
      | Requested Date      |
      | Requestor Name      |
      | Delivery Location:  |

    When user clicks search bar menu
    And user selects Request option
    And user clicks Advanced Search
    Then user clicks search
    When user clicks on Request
    Then user verifies Request Details page is displayed
    And user saves Request ID
    When user clicks search bar menu
    When user selects Request option
    And user searches Request ID
    And user clicks on search result
    Then user verifies Request Details page is displayed
    When user clicks on Item Barcode in Request
    Then user verifies Item Details page is displayed
    When user clicks on Request ID
    Then user verifies Request Details page is displayed
















