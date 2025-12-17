@accessibility

Feature: FETCH Accessibility Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  Scenario: Check Accessibility on Home Page
    Then verify Accessibility


  Scenario: Check Accessibility on Accession Page
    Given user navigates to the Accession Page
    Then verify Accessibility


  Scenario: Check Accessibility on Verification Page
    Given user navigates to the Verification Page
    Then verify Accessibility


  Scenario: Check Accessibility on Shelving Page
    Given user navigates to the Shelving Page
    Then verify Accessibility


  Scenario: Check Accessibility on Admin Dashboard
    Given user navigates to the Admin Page
    Then verify Accessibility


  Scenario: Verify Accessibility of Accession Process for Trayed Item
    Given user navigates to the Home Page
    When user clicks Accession on side navigation menu
    And user clicks Start Accession button
    And user selects Trayed Accession
    And user selects all required fields
    Then verify Accessibility


  Scenario: Verify Accessibility on Create new Shelf Modal
    Given user navigates to the Shelving Page
    When user clicks on Create Shelving Job button
    Then verify Accessibility


  Scenario: Verify Accessibility on Verification Job page
    Given user navigates to FETCH Homepage
    When user clicks Verification on side navigation menu
    And user selects a Verification Job
    Then verify Accessibility
