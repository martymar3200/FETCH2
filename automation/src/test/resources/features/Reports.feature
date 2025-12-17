@reports

Feature: Reports Page Functionality Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @FETCH-894 @FETCH-793
  Scenario: User should be able to validate Reports dashboard
    When user clicks Reports on side navigation menu
    And user clicks on the report selection input
    Then user verifies the report options

      | option                    |
      | Item Accession            |
      | Item in Tray              |
      | Non-Tray Count            |
      | Open Locations            |
      | Refile Discrepancy        |
      | Shelving Job Discrepancy  |
      | Shelving Move Discrepancy |
      | Total Item Retrieved      |
      | Tray/Item Count By Aisle  |
      | User Job Summary          |
      | Verification Change       |

    Then user selects Item Accession option
    And user verifies a modal with report parameters is displayed
    Then user runs report
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed
    When user clears the report option field
    And user clicks on the report selection input
    Then user selects Item in Tray option
    And user verifies a modal with report parameters is displayed
    When user selects Building from dropdown
    Then user runs report
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed
    When user clears the report option field
    And user clicks on the report selection input
    Then user selects Non-Tray Count option
    And user verifies a modal with report parameters is displayed
    When user selects Building from dropdown
    Then user runs report
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed



  @FETCH-1245 @FETCH-927
  Scenario: User should be able to run Item in Tray report
    When user clicks Reports on side navigation menu
    And user clicks on the report selection input
    Then user selects Item in Tray option
    And user verifies a modal with report parameters is displayed
    And user verifies Item in Tray report input parameters

      | parameter           |
      | Building (Required) |
      | Module              |
      | Owner               |
      | Aisle (From)        |
      | Aisle (To)          |
      | Date (From)         |
      | Date (To)           |

    When user selects Building from dropdown
    And user runs report
    Then user verifies the result displays a table with the following information

      | column           |
      | Size Class       |
      | Total Tray Count |
      | Total Item Count |

    When user clicks Redo Report button
    And user verifies a modal with report parameters is displayed
    And user verifies all fields in Item in Tray modal are fillable
    Then user clicks Cancel
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed


  @FETCH-1270 @FETCH-926
  Scenario: User should be able to run Non-Tray Count report
    When user clicks Reports on side navigation menu
    And user clicks on the report selection input
    Then user selects Non-Tray Count option
    And user verifies a modal with report parameters is displayed
    And user verifies Non-Tray Count report input parameters

      | parameter           |
      | Building (Required) |
      | Module              |
      | Owner               |
      | Aisle (From)        |
      | Aisle (To)          |
      | Date (From)         |
      | Date (To)           |
      | Size Class          |

    When user selects Building from dropdown
    And user runs report
    Then user verifies the result displays a table with the following information

      | column               |
      | Size Class           |
      | # of Non-Tray Items  |

    When user clicks Redo Report button
    And user verifies a modal with report parameters is displayed
    And user verifies all fields in Non-Tray Count modal are fillable
    Then user clicks Cancel
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed


  @FETCH-1247 @FETCH-925
  Scenario: User should be able to run Tray/Item Count report
    When user clicks Reports on side navigation menu
    And user clicks on the report selection input
    Then user selects Tray Item Count option
    And user verifies a modal with report parameters is displayed
    And user verifies Tray Item Count report input parameters

      | parameter           |
      | Building (Required) |
      | Aisle (From)        |
      | Aisle (To)          |

    When user selects Building from dropdown
    And user enters Aisle range
    And user runs report
    Then user verifies the result displays a table with the following information

      | column                 |
      | Aisle Number           |
      | Shelf Count            |
      | # of Trays             |
      | # of Tray Items        |
      | # of Non-Tray Items    |
      | Total Items            |

    When user clicks Redo Report button
    And user verifies a modal with report parameters is displayed
    Then user clicks Cancel
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed


  @FETCH-1278 @FETCH-922
  Scenario: User should be able to run Total Item Retrieved report
    When user clicks Reports on side navigation menu
    And user clicks on the report selection input
    Then user selects Total Item Retrieved option
    And user verifies a modal with report parameters is displayed
    And user verifies Total Item Retrieved report input parameters

      | parameter           |
      | Date (From)         |
      | Date (To)           |
      | Owner               |

    When user runs report
    Then user verifies the result displays a table with the following information

      | column                       |
      | Owner                        |
      | Total Item Retrieval Count   |
      | Max Retrieval Count          |

    When user clicks Redo Report button
    And user verifies a modal with report parameters is displayed
    And user verifies all fields in Total Item Retrieved modal are fillable
    Then user clicks Cancel
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed


  @FETCH-1286 @FETCH-921
  Scenario: User should be able to run Verification Change report
    When user clicks Reports on side navigation menu
    And user clicks on the report selection input
    Then user selects Verification Change option
    And user verifies a modal with report parameters is displayed
    And user verifies Verification Change report input parameters

      | parameter           |
      | Job Number          |
      | Date (From)         |
      | Date (To)           |
      | Assigned User       |

    When user runs report
    Then user verifies the result displays a table with the following information

      | column                       |
      | Verification Job #           |
      | Completed Date               |
      | Completed By                 |
      | Item Barcode                 |
      | Tray Barcode                 |
      | Action                       |

#    When user clicks Redo Report button
#    And user verifies a modal with report parameters is displayed
#    And user verifies all fields in Verification Change modal are fillable
#    Then user clicks Cancel
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed
    When user selects Download CSV option
    Then user verifies the file download dialog is displayed


  @FETCH-1276 @FETCH-923
  Scenario: User should be able to run User Job Summary report
    When user clicks Reports on side navigation menu
    And user clicks on the report selection input
    Then user selects User Job Summary option
    And user verifies a modal with report parameters is displayed
    And user verifies User Job Summary report input parameters

      | parameter           |
      | Date (From)         |
      | Date (To)           |
      | User                |

    When user runs report
    Then user verifies the result displays a table with the following information

      | column                |
      | User Name             |
      | Job Type              |
      | Total Items Processed |

    When user clicks Redo Report button
    And user verifies a modal with report parameters is displayed
    And user verifies all fields in User Job Summary modal are fillable
    Then user clicks Cancel
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed


  @FETCH-1192 @FETCH-920
  Scenario: User should be able to run Shelving Job Discrepancy report
    When user clicks Reports on side navigation menu
    And user clicks on the report selection input
    Then user selects Shelving Job Discrepancy option
    And user verifies a modal with report parameters is displayed
    And user verifies Shelving Job Discrepancy report input parameters

      | parameter           |
      | Date (From)         |
      | Date (To)           |
      | Job Number          |
      | Assigned User       |

    When user runs report
    Then user verifies the result displays a table with the following information

      | column                     |
      | Shelving Job #             |
      | Assigned User              |
      | Tray / Non-Tray Barcode    |
      | Size Class                 |
      | Owner                      |
      | Assigned Location          |
      | Pre-Assigned Location      |
      | Error Type                 |

    When user clicks Redo Report button
    And user verifies a modal with report parameters is displayed
    And user verifies all fields in Shelving Job Discrepancy modal are fillable
    Then user clicks Cancel
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed


  @FETCH-1109 @FETCH-919 @FETCH-1268 @FETCH-1151
  Scenario: User should be able to run Item Accession report
    When user clicks Reports on side navigation menu
    And user clicks on the report selection input
    Then user selects Item Accession option
    And user verifies a modal with report parameters is displayed
    And user verifies Item Accession report input parameters

      | parameter                |
      | Accession Date (From)    |
      | Accession Date (To)      |
      | Owner                    |
      | Media Type               |
      | Size Class               |

    When user runs report
    Then user verifies the result displays a table with the following information

      | column                     |
      | Year                       |
      | Month                      |
      | Owner                      |
      | Media Type                 |
      | Size Class                 |
      | Total Accessioned Count    |

    When user clicks Redo Report button
    And user verifies a modal with report parameters is displayed
    And user verifies all fields in Item Accession modal are fillable
    Then user clicks Cancel
    When user clicks Export Report
    Then user verifies the options to print and to download CSV are displayed
















