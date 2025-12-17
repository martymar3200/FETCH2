@alert

Feature: Alert Notification Validation
  Background:
    Given user navigates to the testing link


  @FETCH-457 @FETCH-330
  Scenario: User is able to verify UI Alert Notification
    When user clicks on the Show Generic Alert button
    Then user verifies UI alert on top of the screen is visible
    And user is able to cancel alert
    When user clicks on the Show Persistent Alert button
    Then user verifies alert popup is visible
    And user is able to click cancel button

    @accessionJobAlerts
    Scenario: User is able to validate UI Alert Notifications on Accession Job Page
      And user logs in as a tester1
      When user clicks Accession on side navigation menu
      When user clicks Start Accession button
      And user selects Non-Tray Accession
      And user selects all fields
      And user clicks submit button
      Then user verifies "An Accession Job has successfully been created." alert msg
      When user switches off Toggle Barcode Scan
      And user clicks Enter Barcode button
      And user enters barcode and clicks Submit button
      When user selects one of the barcodes in the table
      And user clicks Delete
      And user clicks Delete Item
      Then user verifies "The selected item(s) has been removed." alert msg
      When user clicks three dot menu next to Accession Job Number
      And user clicks Cancel Job
      Then user verifies warning message
      And user confirms cancellation

