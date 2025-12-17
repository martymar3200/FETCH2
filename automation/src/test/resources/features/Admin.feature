@admin

Feature: Admin Page Functionality Validation

  Background:
    Given user navigates to FETCH Homepage
    And user logs in as a tester1


  @FETCH-527 @FETCH-319
  Scenario: User should be able to validate Location Manager links
    When user clicks Admin on side navigation menu
    When user clicks Location Manager Link
    And user verifies Location Manager dropdown options

      | option    |
      | Buildings |
      | Modules   |
      | Aisles    |
      | Ladders   |
      | Shelves   |


  Scenario: User should be able to validate Buildings Management page
    When user navigates to the Admin Page
    And user clicks Location Manager Link
    And user selects Building
    And user verifies table columns

      | column       |
      | Building     |
      | Created Date |
      | Last Updated |

    Then user verifies Add Building button is clickable


  Scenario: User should be able to validate Buildings Management page
    When user navigates to the Admin Page
    And user clicks Location Manager Link
    And user selects Building
    And user verifies table columns

      | column       |
      | Building     |
      | Created Date |
      | Last Updated |

    Then user verifies Add Building button is clickable


  @FETCH-819 @FETCH-547 @regression
  Scenario: User should be able to validate Group Management UI
    When user navigates to the Admin Page
    And user clicks Groups and Permissions
    Then user verifies the dashboard contains Groups
    When user clicks Add New Group
    And user enters Group Name
    And user clicks Submit
    Then user verifies a new Group is created
    When user clicks three dots menu next to a new created group
    Then user verifies all the options

      | option                    |
      | Edit Permissions          |
      | Add/Edit User(s) in Group |
      | Rename Group Name         |
      | Delete Group              |

    When user clicks Edit Permissions
    Then user verifies permissions tab names

      | name         |
      | ACCESSION    |
      | VERIFICATION |
      | SHELVING     |
      | REQUEST      |
      | PICKLIST     |
      | REFILE       |
      | WITHDRAW     |
      | REPORTING    |

    And user adds some permissions
    Then user clicks on Groups and Permissions breadcrumb link
    When user clicks three dots menu next to a new created group
    And user clicks Add Edit Users in Group
    And user selects User names
    Then user clicks Add Users
    When user clicks on User name
    Then user is able to delete User from Group
    And user closes modal
    When user clicks three dots menu next to a new created group
    And user clicks Rename Group Name
    And user renames Group
    Then user saves changes
    When user clicks three dots menu next to a new created group
    And user clicks Delete Group
    Then user is able to delete Group


  @FETCH-1102 @FETCH-821 @regression
  Scenario: User should be able to validate Size Class Management page
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Size Class Management link
    Then user verifies Size Class dashboard is displayed
    And user verifies table columns

      | column      |
      | Full Name   |
      | Short Name  |
      | Width (in)  |
      | Depth (in)  |
      | Height (in) |

    When user clicks Add Size Class
    Then user verifies a modal to add new record is displayed
    And user verifies Add Size Class button is disabled
    And user verifies Cancel button is enabled
    When user enters full name
    And user enters short name
    And user enters width
    And user enters depth
    And user enters height
    Then user verifies Add Size Class button is enabled
    When user clicks Add Size Class button
    Then user verifies "Successfully added a new Size Class." alert msg
    And user verifies that Size Class is created
    When user clicks three dots menu
    And user clicks to edit record
    Then user verifies a modal to edit the record is displayed
    When user updates full name
    And user clicks Update Size Class button
    Then user verifies "Successfully updated the Size Class." alert msg
    When user clicks three dots menu
    And user clicks to delete record
    And user verifies delete warning message
    Then user confirms delete size class action
    And user verifies "Successfully Deleted The Size Class." alert msg


  @FETCH-1102 @FETCH-821 @negative @regression
  Scenario: User should not be able to create Size Class with existing Name
    When user navigates to the Admin Page
    And user clicks List Configurations
    Then user clicks Size Class Management link
    When user clicks Add Size Class
    Then user verifies a modal to add new record is displayed
    When user enters existing in the system full name
    And user enters short name
    And user enters width
    And user enters depth
    And user enters height
    Then user clicks Add Size Class button
    And user verifies "Network Error" alert msg
    And user verifies that Size Class with existing full name is not created
    When user clicks Add Size Class
    Then user verifies a modal to add new record is displayed
    When user enters full name
    And user enters existing in the system short name
    And user enters width
    And user enters depth
    And user enters height
    Then user clicks Add Size Class button
    And user verifies "Network Error" alert msg
    And user verifies that Size Class with existing short name is not created


  @FETCH-1173 @FETCH-822 @regression
  Scenario: User should be able to validate Media Type Management page
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Media Type Management link
    Then user verifies Media Type dashboard is displayed
    And user verifies table columns

      | column      |
      | Name        |

    When user clicks Add Media Type
    Then user verifies a modal to add new record is displayed
    And user verifies Add Media Type button is disabled
    And user verifies Cancel button is enabled
    When user enters name
    And user verifies Add Media Type button is enabled
    When user clicks Add Media Type button
    Then user verifies "Successfully added a new Media Type." alert msg
    And user verifies that Media Type is created
    When user clicks three dots menu
    And user clicks to edit record
    Then user verifies a modal to edit the record is displayed
    When user updates name
    And user clicks Update Media Type button
    Then user verifies "Successfully updated the Media Type." alert msg
    When user clicks three dots menu
    And user clicks to delete record
    And user verifies delete warning message
    Then user confirms delete media type action
    And user verifies "Successfully Deleted The Media Type." alert msg


  @FETCH-1173 @FETCH-822 @negative @regression
  Scenario: User should not be able to create Media Type with existing Name
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Media Type Management link
    When user clicks Add Media Type
    Then user verifies a modal to add new record is displayed
    When user enters existing in the system name
    When user clicks Add Media Type button
    And user verifies alert message is displayed
    And user verifies that Media Type with existing name is not created


  @FETCH-1173 @FETCH-822 @negative
  Scenario: User should not be able to delete Media Type with associated records
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Media Type Management link
    When user clicks three dots menu next to a record with associated records
    And user clicks to delete record
    And user verifies delete warning message
    Then user confirms delete media type action
    And user verifies "Network Error" alert msg


  @FETCH-1176 @FETCH-820 @regression
  Scenario: User should be able to validate Owner Management page
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Owners Management link
    Then user verifies Owner dashboard is displayed
    And user verifies table columns

      | column         |
      | Owner Name     |
      | Parent Owner   |
      | Owner Tier     |

    When user clicks Add Owner
    Then user verifies a modal to add new record is displayed
    And user verifies Add Owner button is disabled
    And user verifies Cancel button is enabled
    When user selects Owner Tier
    And user selects Parent Owner
    When user enters Owner Name
    And user verifies Add Owner button is enabled
    When user clicks Add Owner button
    Then user verifies "Successfully added a new Owner." alert msg
    And user verifies that Owner is created
    When user clicks three dots menu
    And user clicks to edit record
    Then user verifies a modal to edit the record is displayed
    When user updates Owner Name
    And user clicks Update Owner button
    Then user verifies "Successfully updated the Owner." alert msg
    When user clicks three dots menu
    And user clicks to delete record
    And user verifies delete warning message
    Then user confirms delete owner action
    And user verifies "Successfully Deleted The Owner." alert msg


  @FETCH-1176 @FETCH-820 @negative @regression
  Scenario: User should not be able to create Owner with existing Owner Tier and Owner Name
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Owners Management link
    When user clicks Add Owner
    Then user verifies a modal to add new record is displayed
    When user enters existing in the system owner tier and owner name
    When user clicks Add Owner button
    And user verifies alert message is displayed
    And user verifies that Owner with existing owner tier and owner name is not created


  @FETCH-1176 @FETCH-820 @negative
  Scenario: User should not be able to delete Media Type with associated records
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Owners Management link
    When user clicks three dots menu next to a record with associated records
    And user clicks to delete record
    And user verifies delete warning message
    Then user confirms delete owner action
    And user verifies "Network Error" alert msg


  @FETCH-1174 @FETCH-1039 @regression
  Scenario: User should be able to validate Shelf Types Management page
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Shelf Type Management link
    Then user verifies Shelf Type dashboard is displayed
    And user verifies table columns

      | column         |
      | Shelf Type     |

    When user clicks Add Shelf Type
    Then user verifies a modal to add new record is displayed
    And user verifies Add Shelf Type button is disabled
    And user verifies Cancel button is enabled
    When user enters Shelf Type Name
    And user selects Size Class
    And user verifies Add Shelf Type button is enabled
    When user clicks Add Shelf Type button
    Then user verifies "Successfully added a new Shelf Type." alert msg
    And user verifies that Shelf Type is created
    When user clicks three dots menu
    And user clicks to edit record
    Then user verifies a modal to edit the record is displayed
    When user updates Shelf Type Name
    And user clicks Update Shelf Type button
    Then user verifies "Successfully updated the Shelf Type." alert msg
    When user clicks three dots menu
    And user clicks to delete record
    And user verifies delete warning message
    Then user confirms delete shelf type action
    And user verifies "\"Updated Test Shelf Type\" has been successfully deleted." alert msg


  @FETCH-1174 @FETCH-1039 @negative
  Scenario: User should not be able to delete Shelf Type with associated records
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Shelf Type Management link
    When user clicks three dots menu next to a record with associated records
    And user clicks to delete record
    And user verifies delete warning message
    Then user confirms delete shelf type action
    And user verifies alert msg contains "is in use and cannot be deleted."


  @FETCH-1174 @FETCH-1039 @regression
  Scenario: User should not be able to increase capacity of Shelf Type used by shelves
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Shelf Type Management link
    When user clicks three dots menu next to a record with associated records
    And user clicks to edit record
    Then user verifies a modal to edit the record is displayed
    When user increases capacity of Shelf Type used by shelves
    And user clicks Update Shelf Type button
    Then user verifies "Successfully updated the Shelf Type." alert msg


  @FETCH-1174 @FETCH-1039 @negative @regression
  Scenario: User should not be able to decrease capacity of Shelf Type used by shelves
    When user navigates to the Admin Page
    And user clicks List Configurations
    And user clicks Shelf Type Management link
    When user clicks three dots menu next to a record with associated records
    And user clicks to edit record
    Then user verifies a modal to edit the record is displayed
    When user decreases capacity of Shelf Type used by shelves
    And user clicks Update Shelf Type button
    Then user verifies alert msg contains "Cannot decrease capacity of Shelf Type"



    # feature_to_be_updated
#  @FETCH-527 @FETCH-319
#  Scenario: User should be able to see and edit shelving items of a Building
#    When user navigates to the Admin Page
#    And user clicks Location Manager Link
#    And user selects Building
#    Then user should see building's shelving items
#    When user clicks three-dots on the left side of the table
#    And user clicks Edit Shelf button
#    Then Edit Shelf modal is displayed
#    And user verifies fields on Edit Shelf modal
#
#      | fieldname              |
#      | Shelf Number           |
#      | Owner                  |
#      | Container Size         |
#      | Max Container Capacity |
#      | Shelf Width            |
#      | Shelf Height           |
#      | Shelf Depth            |
#      | Shelf Barcode          |
#
#    And user is able to edit all the fields
#    And Update button is clickable
#    And Cancel button is clickable

# feature_to_be_updated
#  @FETCH-527 @FETCH-319 @add_building
#  Scenario: User should be able to validate Add Building feature
#    When user navigates to the Admin Page
#    And user clicks Buildings
#    And user clicks Add New dropdown button
#    And user selects add Building option
#    Then user verifies popup modal is displayed
#    And Building field is displayed
#    And Create button is disabled
#    When user enters Building information
#    Then Create button is enabled
#    And user exits modal

# feature_to_be_updated
#  @FETCH-527 @FETCH-319 @add_module
#  Scenario: User should be able to validate Add Module feature
#    When user navigates to the Admin Page
#    And user clicks Buildings
#    And user clicks Add New dropdown button
#    And user selects add Module option
#    Then user verifies popup modal is displayed
#    And Building dropdown is clickable
#    And Module field is disabled
#    And Create button is disabled
#    When user selects Building from dropdown
#    Then Module field is enabled
#    When user enters Module information
#    Then Create button is enabled
#    And user exits modal

# feature_to_be_updated
#  @FETCH-527 @FETCH-319 @add_aisle
#  Scenario: User should be able to validate Add Aisle feature
#    When user navigates to the Admin Page
#    And user clicks Buildings
#    And user clicks Add New dropdown button
#    And user selects add Aisle option
#    Then user verifies popup modal is displayed
#    And Building dropdown is clickable
#    And Module field is disabled
#    And Aisle field is disabled
#    And Create button is disabled
#    When user selects Building from dropdown
#    Then Module field is enabled
#    And Aisle field is enabled
#    When user selects Module from dropdown
#    And user enters Aisle information
#    Then Create button is enabled
#    And user exits modal

# feature_to_be_updated
#  @FETCH-527 @FETCH-319 @add_ladder
#  Scenario: User should be able to validate Add Ladder feature
#    When user navigates to the Admin Page
#    And user clicks Buildings
#    And user clicks Add New dropdown button
#    And user selects add Ladder option
#    Then user verifies popup modal is displayed
#    And Building dropdown is clickable
#    And Module field is disabled
#    And Aisle field is disabled
#    And Ladder field is disabled
#    And Create button is disabled
#    And Side button is enabled
#    When user selects Building from dropdown
#    Then Module field is enabled
#    And user selects Module from dropdown
#    Then Aisle field is enabled
#    And user selects Aisle from dropdown
#    Then Ladder field is enabled
#    And user selects Side
#    And user enters Ladder information
#    Then Create button is enabled
#    And user exits modal

# feature_to_be_updated
#  @FETCH-527 @FETCH-319 @manual_LH
#  Scenario: User should be able to validate Location Hierarchy Manual creation
#    When user navigates to the Admin Page
#    And user clicks Buildings
#    And user clicks Location Hierarchy dropdown button
#    And user selects Manual option
#    And user verifies popup modal is displayed
#    When user selects Building from dropdown
#    And user selects Module from dropdown
#    And user selects Aisle from dropdown
#    And user selects Side
#    And user selects Ladder
#    Then Create button is enabled
#    And user exits modal

# feature_to_be_updated
#  @FETCH-527 @FETCH-319 @bulk_upload
#  Scenario: User should be able to validate Location Hierarchy Bulk Upload creation
#    When user navigates to the Admin Page
#    And user clicks Buildings
#    And user clicks Location Hierarchy dropdown button
#    And user selects Bulk Upload option
#    And user verifies popup modal is displayed
#    When user selects Building from dropdown
#    And user selects Module from dropdown
#    And user selects Aisle from dropdown
#    And user selects Side
#    And user selects Ladder
#    And user uploads file
#    Then Create button is enabled
#    And user exits modal




















