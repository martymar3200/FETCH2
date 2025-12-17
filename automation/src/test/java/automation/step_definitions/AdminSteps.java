package automation.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.*;
import automation.pages.AdminPage;
import automation.utilities.ConfigurationReader;
import automation.utilities.Driver;
import automation.utilities.Helper;
import automation.utilities.WaitHelper;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AdminSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    AdminPage admin = new AdminPage();
    Helper helper = new Helper();
    WaitHelper wait = new WaitHelper();
    AlertSteps alertSteps = new AlertSteps();


    @Given("user navigates to the Admin Page")
    public void user_navigates_to_the_admin_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "adminURL"));
    }

    @When("user is on the Admin Page")
    public void user_is_on_the_admin_page() {
        String actualURL = driver.getCurrentUrl();
        String expectedURL = "https://test.fetch.example.com/admin";
        assertEquals("Admin Page URL failed", expectedURL, actualURL);
    }

    @Then("user verifies the Admin Dashboard contains links")
    public void user_verifies_the_admin_dashboard_contains_links() {
        for (WebElement link : admin.adminPageLinks) {
            assertTrue(link.isDisplayed());
        }
    }

    @When("user clicks Buildings")
    public void user_clicks_buildings() throws InterruptedException {
        WaitHelper.waitForClickability(admin.buildingsLink, 2000);
        helper.jSClick(admin.buildingsLink);
        wait.hardWait(100);
    }

    @When("user selects Buildings")
    public void user_selects_buildings() throws InterruptedException {
        WaitHelper.waitForClickability(admin.lmDropdownLinks.get(0), 2000);
        helper.jSClick(admin.lmDropdownLinks.get(0));
        wait.hardWait(100);
    }

    @When("user clicks Location Manager Link")
    public void user_clicks_location_manager_Link() throws InterruptedException {
        WaitHelper.waitForClickability(admin.locationManagerLink, 2000);
        helper.jSClick(admin.locationManagerLink);
        wait.hardWait(100);
    }

    @Then("user verifies the Admin Dashboard contains Buildings")
    public void user_verifies_the_admin_dashboard_contains_buildings() {
        int count = 0;
        for (WebElement building : admin.buildings) {
            count++;
            assertTrue(building.isDisplayed());
            System.out.println("Displayed Building " + count + " : " + building.getText());
        }
    }

    @Then("Add New dropdown is displayed and clickable")
    public void add_new_dropdown_is_displayed_and_clickable() {
        Helper.verifyElementDisplayed(admin.addNew);
        Helper.isClickable(admin.addNew);
    }

    @Then("user verifies Location Manager dropdown options")
    public void user_verifies_location_manager_dropdown_options(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedField = map.get("option");
            String actualField = admin.lmDropdownLinks.get(i).getText();
            Assert.assertTrue(expectedField, actualField.contains(expectedField));
            i++;
        }
    }

    @Then("Location Hierarchy dropdown is displayed and clickable")
    public void location_hierarchy_dropdown_is_displayed_and_clickable() {
        Helper.verifyElementDisplayed(admin.locationHierarchy);
        Helper.isClickable(admin.locationHierarchy);
    }

    @Then("user verifies Location Hierarchy dropdown options")
    public void user_verifies_location_hierarchy_dropdown_options(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        helper.jSClick(admin.locationHierarchy);
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;

        for (Map<String, String> map : maps) {
            String expectedField = map.get("option");
            String actualField = admin.locHierOptions.get(i).getText();
            Assert.assertEquals("Options name verification failed", expectedField, actualField);
            i++;
        }
    }

    @When("user selects Building")
    public void user_selects_building() {
        helper.jSClick(admin.lmDropdownLinks.get(0));
    }

    @Then("user should see building's shelving items")
    public void user_should_see_building_s_shelving_items() {
        int count = 0;
        for (WebElement item : admin.shelvingItems) {
            count++;
            assertTrue(item.isDisplayed());
            System.out.println("Displayed Shelving Item " + count + " : " + item.getText());
        }
    }

    @When("user clicks three-dots on the left side of the table")
    public void user_clicks_three_dots_on_the_left_side_of_the_table() {
        admin.threeDots.get(0).click();
    }

    @When("user clicks Edit Shelf button")
    public void user_clicks_edit_shelf_button() {
        helper.jSClick(admin.editBtn);
    }

    @Then("Edit Shelf modal is displayed")
    public void edit_shelf_modal_is_displayed() {
        WaitHelper.waitForVisibility(admin.editShelfModal, 1000);
        Helper.verifyElementDisplayed(admin.editShelfModal);
    }

    @Then("user verifies fields on Edit Shelf modal")
    public void user_verifies_fields_on_edit_shelf_modal(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;

        for (Map<String, String> map : maps) {
            String expectedField = map.get("fieldname");
            wait.hardWait(1000);
            String actualField = admin.editShelfFields.get(i).getText();
            Assert.assertEquals("Fields name verification failed", expectedField, actualField);
            i++;
        }
    }

    @Then("user is able to edit all the fields")
    public void user_is_able_to_edit_all_the_fields() {
        admin.editShelfNumber.sendKeys(Keys.CONTROL + "a");
        admin.editShelfNumber.sendKeys(Keys.DELETE);
        admin.editShelfNumber.sendKeys("2");

        admin.editMaxCapacity.sendKeys(Keys.CONTROL + "a");
        admin.editMaxCapacity.sendKeys(Keys.DELETE);
        admin.editMaxCapacity.sendKeys("17");

        admin.editWidth.sendKeys(Keys.CONTROL + "a");
        admin.editWidth.sendKeys(Keys.DELETE);
        admin.editWidth.sendKeys("10");

        admin.editHeight.sendKeys(Keys.CONTROL + "a");
        admin.editHeight.sendKeys(Keys.DELETE);
        admin.editHeight.sendKeys("2");

        admin.editDepth.sendKeys(Keys.CONTROL + "a");
        admin.editDepth.sendKeys(Keys.DELETE);
        admin.editDepth.sendKeys("20");

        helper.jSClick(admin.editBarcode);
        admin.editBarcode.clear();
    }

    @Then("Update button is clickable")
    public void update_button_is_clickable() {
        Helper.isClickable(admin.update);
    }

    @Then("Cancel button is clickable")
    public void cancel_button_is_clickable() {
        Helper.isClickable(admin.cancelUpdate);
    }

    @When("user clicks Add New dropdown button")
    public void user_clicks_add_new_dropdown_button() {
        helper.jSClick(admin.addNew);
    }

    @When("user selects add Building option")
    public void user_selects_add_building_option() {
        admin.addNewOptions.get(0).click();
    }

    @Then("user verifies popup modal is displayed")
    public void user_verifies_popup_modal_is_displayed() {
        WaitHelper.waitForVisibility(admin.popUpModal, 1000);
        Helper.verifyElementDisplayed(admin.popUpModal);
    }

    @Then("Building field is displayed")
    public void building_field_is_displayed() {
        Helper.verifyElementDisplayed(admin.buildingField);
    }

    @Then("Create button is disabled")
    public void create_button_is_disabled() {
        assertFalse(admin.createBtn.isEnabled());
    }

    @When("user enters Building information")
    public void user_enters_building_information() {
        admin.buildingField.sendKeys("Cabin Brunch");
    }

    @Then("Create button is enabled")
    public void create_button_is_enabled() {
        assertTrue(admin.createBtn.isEnabled());
    }

    @Then("user exits modal")
    public void user_exits_modal() {
        helper.jSClick(admin.cancelBtn);
    }

    @When("user selects add Module option")
    public void user_selects_add_module_option() {
        admin.addNewOptions.get(1).click();
    }

    @Then("Building dropdown is clickable")
    public void building_dropdown_is_clickable() {
        Helper.isClickable(admin.selectBuilding);
    }

    @Then("Module field is disabled")
    public void module_field_is_disabled() {
        Assert.assertEquals("true", admin.moduleField.getAttribute("disabled"));
    }

    @When("user selects Building from dropdown")
    public void user_selects_building_from_dropdown() {
        WaitHelper.waitForClickability(admin.selectBuilding,3000);
        admin.selectBuilding.click();
        WaitHelper.waitForClickability(admin.fieldDropdwnList.get(1),3000);
        helper.jSClick(admin.fieldDropdwnList.get(1));
    }

    @Then("Module field is enabled")
    public void module_field_is_enabled() {
        Helper.isClickable(admin.moduleField);
    }

    @When("user enters Module information")
    public void user_enters_module_information() {
        helper.jSClick(admin.moduleField);
        admin.moduleField.sendKeys("Module 1");
    }

    @When("user selects add Aisle option")
    public void user_selects_add_aisle_option() throws InterruptedException {
        admin.addNewOptions.get(2).click();
        wait.hardWait(1000);
    }

    @Then("Aisle field is disabled")
    public void aisle_field_is_disabled() {
        assertFalse(admin.aisleField.isEnabled());
    }

    @Then("Aisle field is enabled")
    public void aisle_field_is_enabled() {
        assertTrue(admin.aisleField.isEnabled());
    }

    @When("user selects Module from dropdown")
    public void user_selects_module_from_dropdown() throws InterruptedException {
        wait.hardWait(1000);
        wait.handleStaleElement(By.cssSelector(".q-field__native [placeholder='Select Module']"), 4, 1000);
        admin.selectModule.click();
        admin.fieldDropdwnList.get(0).click();
    }

    @When("user enters Aisle information")
    public void user_enters_aisle_information() {
        helper.jSClick(admin.aisleField);
        admin.aisleField.sendKeys("3");
    }

    @When("user selects add Ladder option")
    public void user_selects_add_ladder_option() {
        admin.addNewOptions.get(3).click();
    }

    @Then("Ladder field is disabled")
    public void ladder_field_is_disabled() {
        assertFalse(admin.ladderField.isEnabled());
    }

    @Then("Side button is enabled")
    public void side_button_is_enabled() {
        assertTrue(admin.sideBtnLeft.isEnabled());
    }

    @Then("user selects Aisle from dropdown")
    public void user_selects_aisle_from_dropdown() throws InterruptedException {
        wait.hardWait(1000);
        wait.handleStaleElement(By.cssSelector(".q-field__native [placeholder='Select Aisle']"), 4, 1000);
        admin.selectAisle.click();
        admin.fieldDropdwnList.get(0).click();
    }

    @Then("Ladder field is enabled")
    public void ladder_field_is_enabled() {
        assertTrue(admin.ladderField.isEnabled());
    }

    @Then("user enters Ladder information")
    public void user_enters_ladder_information() {
        admin.ladderField.click();
        admin.ladderField.sendKeys("7");
    }

    @Then("user selects Side")
    public void user_selects_side() {
        helper.jSClick(admin.sideBtnRight);
    }

    @When("user clicks Location Hierarchy dropdown button")
    public void user_clicks_location_hierarchy_dropdown_button() {
        helper.jSClick(admin.locationHierarchy);
    }

    @When("user selects Manual option")
    public void user_selects_manual_option() {
        admin.locHierOptions.get(1).click();
    }

    @When("user selects Ladder")
    public void user_selects_ladder() throws InterruptedException {
        helper.scrollToElement(admin.selectLadder);
        WaitHelper.waitForClickability(admin.selectLadder, 1000);
        admin.selectLadder.click();
        wait.hardWait(1000);
        admin.modalFieldOptions.get(0).click();
    }

    @When("user selects Bulk Upload option")
    public void user_selects_bulk_upload_option() {
        admin.locHierOptions.get(0).click();
    }

    @When("user uploads file")
    public void user_uploads_file() throws InterruptedException {
        String filePath = System.getProperty("user.dir") + "/src/test/resources/testData/fileUpload/document.xlsx";
        admin.upload.sendKeys(filePath);
        Thread.sleep(3000);

        String expectedFileName = "document.xlsx";
        String actualFileName = admin.uploadedFile.getText();
        Assert.assertEquals("Uploaded file verification failed!", expectedFileName, actualFileName);
        Thread.sleep(5000);
    }

    @When("user clicks Groups and Permissions")
    public void user_clicks_groups_and_permissions() {
        helper.jSClick(admin.groupsAndPermissionsLink);
    }

    @Then("user verifies the dashboard contains Groups")
    public void user_verifies_the_dashboard_contains_groups() throws InterruptedException {
        wait.hardWait(100);
        int count = 0;
        for (WebElement group : admin.groups) {
            count++;
            assertTrue(group.isDisplayed());
            System.out.println("Displayed Groups " + count + " : " + group.getText());
        }
    }

    @When("user clicks Add New Group")
    public void user_clicks_add_new_group() {
        helper.jSClick(admin.addNewGroup);
    }

    @When("user enters Group Name")
    public void user_enters_Group_Name() {
        helper.jSClick(admin.enterGroupNameField);
        admin.enterGroupNameField.sendKeys("Test");
    }

    @Then("user verifies a new Group is created")
    public void user_verifies_a_new_group_is_created() {
        for (WebElement group : admin.groups) {
            if (group.getText().equals("Test")) {
                assertTrue(group.isDisplayed());
            }
        }
    }

    @Then("user closes alert message")
    public void user_closes_alert_message() {
        alertSteps.alert.closeToastMsg.click();
    }

    @When("user clicks three dots menu")
    public void user_clicks_three_dots_menu() {
        WaitHelper.waitForVisibility(admin.threeDots.getLast(),3000);
        helper.jSClick(admin.threeDotsMenu.getLast());
    }

    @When("user clicks three dots menu next to a new created group")
    public void user_clicks_three_dots_menu_next_to_a_nw_created_group() {
       WebElement threeDots = driver.findElement(By.xpath("//*[.='Test']/parent::div/button | //*[.='Renamed Test']/parent::div/button"));
       threeDots.click();
    }

    @When("user clicks three dots menu next to a record with associated records")
    public void user_clicks_three_dots_menu_next_a_record_with_associated_records() {
        WaitHelper.waitForVisibility(admin.threeDots.getFirst(),2000);
        helper.jSClick(admin.threeDotsMenu.getFirst());
    }

    @Then("user verifies all the options")
    public void user_verifies_all_the_options(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedField = map.get("option");
            String actualField = admin.menuOptions.get(i).getText();
            Assert.assertEquals("Options name verification failed", expectedField, actualField);
            i++;
        }
    }

    @When("user clicks Edit Permissions")
    public void user_clicks_edit_permissions() {
        helper.jSClick(admin.menuOptions.getFirst());
    }

    @Then("user verifies permissions tab names")
    public void user_verifies_permissions_tab_names(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedField = map.get("name");
            String actualField = admin.tabNames.get(i).getText();
            Assert.assertEquals("Tab Name verification failed", expectedField, actualField);
            i++;
        }
    }

    @Then("user verifies table columns")
    public void user_verifies_table_columns(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 1;
        for (Map<String, String> map : maps) {
            String expectedField = map.get("column");
            String actualField = admin.tableColumns.get(i).getText();
            Assert.assertTrue(actualField.contains(expectedField));
            i++;
        }
    }

    @When("user adds some permissions")
    public void user_adds_some_permissions() throws InterruptedException {
        helper.jSClick(admin.no.getFirst());
        wait.hardWait(1000);
        helper.jSClick(admin.yes.get(1));
        wait.hardWait(1000);
        WebElement verifTab = driver.findElement(By.cssSelector("[role='tab']:nth-child(2)"));
        helper.jSClick(verifTab);
        wait.hardWait(1000);
        helper.jSClick(admin.yes.getFirst());
    }

    @When("user clicks on Groups and Permissions breadcrumb link")
    public void user_clicks_on_groups_and_permissions_breadcrumb_link() {
        WebElement groupsAndPermissionsBreadcrumbLink = driver.findElement(By.xpath("//a[.='Groups & Permissions']"));
        groupsAndPermissionsBreadcrumbLink.click();
    }

    @When("user clicks Add Edit Users in Group")
    public void user_clicks_add_edit_users_in_group() {
        helper.jSClick(admin.menuOptions.get(1));
    }

    @When("user selects User names")
    public void user_selects_user_names() {
        admin.selectUserToAddField.click();
        helper.jSClick(admin.usersList.get(4));
        helper.jSClick(admin.usersList.get(5));
        helper.jSClick(admin.selectUserToAddField);
    }

    @When("user clicks Add Users")
    public void user_clicks_add_users() throws InterruptedException {
        helper.jSClick(admin.addUsersBtn);
        wait.hardWait(1000);
    }

    @When("user clicks on User name")
    public void user_clicks_on_user_name() throws InterruptedException {
        WaitHelper.waitForClickability(admin.groupUserTabs.getFirst(), 100);
        admin.groupUserTabs.get(0).click();
        wait.hardWait(1000);
    }

    @When("user is able to delete User from Group")
    public void user_is_able_to_delete_user_from_group() {
        helper.jSClick(admin.deleteUserFromGroupBtn);
    }

    @When("user closes modal")
    public void user_closes_modal() throws InterruptedException {
        helper.jSClick(admin.closeModal);
        wait.hardWait(2000);
    }

    @When("user clicks Rename Group Name")
    public void user_clicks_rename_group_name() {
        helper.jSClick(admin.menuOptions.get(2));
    }

    @When("user renames Group")
    public void user_renames_group() throws InterruptedException {
        helper.jSClick(admin.enterGroupNameField);
        admin.enterGroupNameField.sendKeys(Keys.CONTROL + "a");
        admin.enterGroupNameField.sendKeys(Keys.DELETE);
        admin.enterGroupNameField.sendKeys("Renamed Test");
        wait.hardWait(1000);
    }

    @When("user saves changes")
    public void user_saves_changes() throws InterruptedException {
        helper.jSClick(admin.renameGroupBtn);
        wait.hardWait(1000);
    }

    @When("user clicks Delete Group")
    public void user_clicks_delete_group() throws InterruptedException {
        helper.jSClick(admin.menuOptions.getLast());
        wait.hardWait(1000);
    }

    @Then("user is able to delete Group")
    public void user_is_able_to_delete_group() {
        helper.jSClick(admin.confirmDeleteGroup);
    }

    @Then("user verifies Add Building button is clickable")
    public void user_verifies_add_building_button_is_clickable() {
        Helper.isClickable(admin.addBuildingBtn);
    }

    @When("user clicks List Configurations")
    public void user_clicks_list_configurations() {
        WaitHelper.waitForClickability(admin.listConfigurationsLink,2000);
        helper.jSClick(admin.listConfigurationsLink);
    }

    @When("user clicks Owners Management link")
    public void user_clicks_owners_management_link() {
        WaitHelper.waitForClickability(admin.ownersManagementLink,2000);
        helper.jSClick(admin.ownersManagementLink);
    }

    @When("user clicks Media Type Management link")
    public void user_clicks_media_type_management_link() {
        WaitHelper.waitForClickability(admin.mediaTypeManagementLink,2000);
        helper.jSClick(admin.mediaTypeManagementLink);
    }

    @When("user clicks Size Class Management link")
    public void user_clicks_size_class_management_link() {
        WaitHelper.waitForClickability(admin.sizeClassManagementLink,2000);
        helper.jSClick(admin.sizeClassManagementLink);
    }

    @When("user clicks Shelf Type Management link")
    public void user_clicks_shelf_type_management_link() {
        WaitHelper.waitForClickability(admin.shelfTypeManagementLink,2000);
        helper.jSClick(admin.shelfTypeManagementLink);
    }

    @Then("user verifies Media Type dashboard is displayed")
    public void user_verifies_media_type_dashboard_is_displayed() {
        assertEquals("Media Type", admin.pageHeader.getText());
        Helper.verifyElementDisplayed(admin.addMediaType);
    }

    @Then("user verifies Size Class dashboard is displayed")
    public void user_verifies_size_class_dashboard_is_displayed() {
        assertEquals("Size Class", admin.pageHeader.getText());
        Helper.verifyElementDisplayed(admin.addSizeClass);
    }

    @Then("user verifies Owner dashboard is displayed")
    public void user_verifies_owner_dashboard_is_displayed() {
        assertEquals("Owner", admin.pageHeader.getText());
        Helper.verifyElementDisplayed(admin.addOwner);
    }

    @Then("user verifies Shelf Type dashboard is displayed")
    public void user_verifies_shelf_type_dashboard_is_displayed() {
        assertEquals("Shelf Type", admin.pageHeader.getText());
        Helper.verifyElementDisplayed(admin.addShelfType);
    }

    @When("user clicks Add Media Type")
    public void user_clicks_add_media_type() throws InterruptedException {
        WaitHelper.waitForClickability(admin.addMediaType, 1000);
        admin.addMediaType.click();
        wait.hardWait(1000);
    }

    @When("user clicks Add Size Class")
    public void user_clicks_add_size_class() throws InterruptedException {
        WaitHelper.waitForClickability(admin.addSizeClass, 1000);
        admin.addSizeClass.click();
        wait.hardWait(1000);
    }

    @When("user clicks Add Owner")
    public void user_clicks_add_owner() throws InterruptedException {
        WaitHelper.waitForClickability(admin.addOwner, 1000);
        admin.addOwner.click();
        wait.hardWait(1000);
    }

    @When("user clicks Add Shelf Type")
    public void user_clicks_add_shelf_type() throws InterruptedException {
        WaitHelper.waitForClickability(admin.addShelfType, 1000);
        admin.addShelfType.click();
        wait.hardWait(1000);
    }

    @Then("user verifies a modal to add new record is displayed")
    public void user_verifies_a_modal_to_add_new_record_is_displayed() {
        WaitHelper.waitForVisibility(admin.popUpModal, 1000);
        Helper.verifyElementDisplayed(admin.popUpModal);
        assertTrue(admin.modalHeader.getText().contains("Add New"));
    }

    @Then("user verifies a modal to edit the record is displayed")
    public void user_verifies_a_modal_to_edit_the_record_is_displayed() {
        WaitHelper.waitForVisibility(admin.popUpModal, 1000);
        Helper.verifyElementDisplayed(admin.popUpModal);
        assertTrue(admin.modalHeader.getText().contains("Edit"));
    }

    @And("user verifies Add Size Class button is disabled")
    public void user_verifies_add_size_class_button_is_disabled() {
        WaitHelper.waitForVisibility(admin.addSizeClassButton, 1000);
        Helper.verifyButtonIsDisabled(admin.addSizeClassButton);
    }

    @And("user verifies Add Media Type button is disabled")
    public void user_verifies_add_media_type_button_is_disabled() {
        WaitHelper.waitForVisibility(admin.addMediaTypeButton, 1000);
        Helper.verifyButtonIsDisabled(admin.addMediaTypeButton);
    }

    @And("user verifies Add Owner button is disabled")
    public void user_verifies_add_owner_button_is_disabled() {
        WaitHelper.waitForVisibility(admin.addOwnerButton, 1000);
        Helper.verifyButtonIsDisabled(admin.addOwnerButton);
    }

    @And("user verifies Add Shelf Type button is disabled")
    public void user_verifies_add_shelf_type_button_is_disabled() {
        WaitHelper.waitForVisibility(admin.addShelfTypeButton, 1000);
        Helper.verifyButtonIsDisabled(admin.addShelfTypeButton);
    }

    @And("user verifies Cancel button is enabled")
    public void user_verifies_cancel_button_is_enabled() {
        WaitHelper.waitForVisibility(admin.cancelButton, 1000);
        Helper.verifyElementEnabled(admin.cancelButton);
    }

    @When("user enters full name")
    public void user_enters_full_name() throws InterruptedException {
        admin.fullname.click();
        admin.fullname.sendKeys("Test Size Class");
        wait.hardWait(100);
    }

    @When("user updates full name")
    public void user_updates_full_name() throws InterruptedException {
        admin.fullname.click();
        admin.fullname.sendKeys(Keys.CONTROL + "a");
        admin.fullname.sendKeys(Keys.DELETE);
        admin.fullname.sendKeys("Updated Size Class");
        wait.hardWait(100);
    }

    @And("user enters short name")
    public void user_enters_short_name() throws InterruptedException {
        admin.shortname.click();
        admin.shortname.sendKeys("TSC");
        wait.hardWait(100);
    }

    @And("user enters width")
    public void user_enters_width() throws InterruptedException {
        admin.width.click();
        admin.width.sendKeys("40");
        wait.hardWait(100);
    }

    @And("user enters depth")
    public void user_enters_depth() throws InterruptedException {
        admin.depth.click();
        admin.depth.sendKeys("27");
        wait.hardWait(100);
    }

    @And("user enters height")
    public void user_enters_height() throws InterruptedException {
        admin.height.click();
        admin.height.sendKeys("15.70");
        wait.hardWait(100);
    }

    @Then("user verifies Add Size Class button is enabled")
    public void user_verifies_add_size_class_button_is_enabled() {
        WaitHelper.waitForVisibility(admin.addSizeClassButton, 1000);
        Helper.verifyElementEnabled(admin.addSizeClassButton);
    }

    @Then("user verifies Add Media Type button is enabled")
    public void user_verifies_add_media_type_button_is_enabled() {
        WaitHelper.waitForVisibility(admin.addMediaTypeButton, 1000);
        Helper.verifyElementEnabled(admin.addMediaTypeButton);
    }

    @Then("user verifies Add Owner button is enabled")
    public void user_verifies_add_owner_button_is_enabled() {
        WaitHelper.waitForVisibility(admin.addOwnerButton, 1000);
        Helper.verifyElementEnabled(admin.addOwnerButton);
    }

    @And("user verifies Add Shelf Type button is enabled")
    public void user_verifies_add_shelf_type_button_is_enabled() {
        WaitHelper.waitForVisibility(admin.addShelfTypeButton, 1000);
        Helper.verifyElementEnabled(admin.addShelfTypeButton);
    }

    @And("user verifies that Size Class is created")
    public void user_verifies_that_size_class_is_created() {
        helper.scrollIntoView(admin.sizeClassList.get(admin.sizeClassList.size()-1));
        String addedSizeClass = "Test Size Class";
        assertTrue(admin.sizeClassList.get(admin.sizeClassList.size()-1).getText().contains(addedSizeClass));
    }

    @And("user verifies that Media Type is created")
    public void user_verifies_that_media_type_is_created() {
        helper.scrollIntoView(admin.mediaTypeList.get(admin.mediaTypeList.size()-1));
        String addedMediaType = "Test Media Type";
        assertTrue(admin.mediaTypeList.get(admin.mediaTypeList.size()-1).getText().contains(addedMediaType));
    }

    @And("user verifies that Owner is created")
    public void user_verifies_that_owner_is_created() {
        helper.scrollIntoView(admin.ownerList.get(admin.ownerList.size()-1));
        String addedOwner = "Test Owner";
        assertTrue(admin.ownerList.get(admin.ownerList.size()-1).getText().contains(addedOwner));
    }

    @And("user verifies that Shelf Type is created")
    public void user_verifies_that_shelf_type_is_created() {
        helper.scrollIntoView(admin.shelfTypeList.get(admin.shelfTypeList.size()-1));
        String addedShelfType = "Test Shelf Type";
        assertTrue(admin.shelfTypeList.get(admin.shelfTypeList.size()-1).getText().contains(addedShelfType));
    }

    @When("user clicks to edit record")
    public void user_clicks_to_edit_record() throws InterruptedException {
        helper.jSClick(admin.menuOptions.get(0));
        wait.hardWait(1000);
    }

    @When("user clicks to delete record")
    public void user_clicks_to_delete_record() throws InterruptedException {
        helper.jSClick(admin.menuOptions.getLast());
        wait.hardWait(1000);
    }

    @When("user clicks Add Size Class button")
    public void user_clicks_add_size_class_button() {
        admin.addSizeClassButton.click();
    }

    @When("user clicks Add Media Type button")
    public void user_clicks_add_media_type_button() {
        admin.addMediaTypeButton.click();
    }

    @When("user clicks Add Owner button")
    public void user_clicks_add_owner_button() {
        admin.addOwnerButton.click();
    }

    @When("user clicks Add Shelf Type button")
    public void user_clicks_add_shelf_type_button() {
        admin.addShelfTypeButton.click();
    }

    @When("user clicks Update Size Class button")
    public void user_clicks_update_size_class_button() {
        WaitHelper.waitForVisibility(admin.updateSizeClassButton,2000);
        admin.updateSizeClassButton.click();
    }

    @When("user clicks Update Media Type button")
    public void user_clicks_update_media_type_button() {
        WaitHelper.waitForVisibility(admin.updateMediaTypeButton,2000);
        admin.updateMediaTypeButton.click();
    }

    @When("user clicks Update Owner button")
    public void user_clicks_update_owner_button() {
        WaitHelper.waitForVisibility(admin.updateOwnerButton,2000);
        admin.updateOwnerButton.click();
    }

    @When("user clicks Update Shelf Type button")
    public void user_clicks_update_shelf_type_button() {
        WaitHelper.waitForVisibility(admin.updateShelfTypeButton,4000);
        admin.updateShelfTypeButton.click();
    }

    @Then("user verifies delete warning message")
    public void user_verifies_delete_warning_message() {
        WaitHelper.waitForVisibility(admin.warningMsg, 1000);
        assertTrue(admin.warningMsg.getText().contains("Are you sure you want to delete"));
    }

    @And("user confirms delete size class action")
    public void user_confirms_delete_size_class_action() {
        helper.jSClick(admin.confirmDeleteSizeClass);
    }

    @And("user confirms delete media type action")
    public void user_confirms_delete_media_type_action() {
        helper.jSClick(admin.confirmDeleteMediaType);
    }

    @And("user confirms delete owner action")
    public void user_confirms_delete_owner_action() {
        helper.jSClick(admin.confirmDeleteOwner);
    }

    @And("user confirms delete shelf type action")
    public void user_confirms_delete_shelf_type_action() {
        helper.jSClick(admin.confirmDeleteShelfType);
    }

    @When("user enters existing in the system full name")
    public void user_enters_existing_in_the_system_full_name()  {
        Helper.clickWithJS(admin.fullname);
        admin.fullname.sendKeys("Flat2");
    }

    @When("user enters existing in the system short name")
    public void user_enters_existing_in_the_system_short_name()  {
        Helper.clickWithJS(admin.shortname);
        admin.shortname.sendKeys("Flat2");
    }

    @And("user verifies that Size Class with existing full name is not created")
    public void user_verifies_that_size_class_with_existing_full_name_is_not_created()  {
        WaitHelper.fluentWait(admin.sizeClassList.get(admin.sizeClassList.size()-1),1000);
        assertFalse(admin.sizeClassList.get(admin.sizeClassList.size()-1).getText().contains("Flat2"));
    }

    @And("user verifies that Size Class with existing short name is not created")
    public void user_verifies_that_size_class_with_existing_short_name_is_not_created()  {
        WaitHelper.fluentWait(admin.sizeClassList.get(admin.sizeClassList.size()-1),1000);
        assertFalse(admin.sizeClassList.get(admin.sizeClassList.size()-1).getText().contains("Flat2"));
    }

    @When("user enters name")
    public void user_enters_name() throws InterruptedException {
        admin.name.click();
        admin.name.sendKeys("Test Media Type");
        wait.hardWait(100);
    }

    @When("user updates name")
    public void user_updates_name() throws InterruptedException {
        admin.name.click();
        admin.name.sendKeys(Keys.CONTROL + "a");
        admin.name.sendKeys(Keys.DELETE);
        admin.name.sendKeys("Updated Test Media Type");
        wait.hardWait(100);
    }

    @When("user enters existing in the system name")
    public void user_enters_existing_in_the_system_name()  {
        Helper.clickWithJS(admin.name);
        admin.name.sendKeys("Microfilm");
    }

    @And("user verifies that Media Type with existing name is not created")
    public void user_verifies_that_media_type_with_existing_name_is_not_created()  {
        WaitHelper.fluentWait(admin.mediaTypeList.get(admin.mediaTypeList.size()-1),1000);
        assertFalse(admin.mediaTypeList.get(admin.mediaTypeList.size()-1).getText().contains("DVD"));
    }

    @When("user selects Owner Tier")
    public void user_selects_owner_tier() {
        WaitHelper.waitForVisibility(admin.selectOwnerTier,2000);
        admin.selectOwnerTier.click();
        admin.dropdownOptions.get(1).click();
    }

    @When("user selects Parent Owner")
    public void user_selects_parent_owner() {
        WaitHelper.waitForVisibility(admin.selectParentOwner,2000);
        admin.selectParentOwner.click();
        admin.dropdownOptions.get(0).click();
    }

    @When("user enters Owner Name")
    public void user_enters_owner_name() throws InterruptedException {
        WaitHelper.waitForClickability(admin.ownerName,2000);
        admin.ownerName.click();
        admin.ownerName.sendKeys("Test Owner");
        wait.hardWait(100);
    }

    @When("user updates Owner Name")
    public void user_updates_owner_name() throws InterruptedException {
        WaitHelper.waitForClickability(admin.ownerName,2000);
        admin.ownerName.click();
        admin.ownerName.sendKeys(Keys.CONTROL + "a");
        admin.ownerName.sendKeys(Keys.DELETE);
        admin.ownerName.sendKeys("Updated Test Owner");
        wait.hardWait(100);
    }

    @When("user enters existing in the system owner tier and owner name")
    public void user_enters_existing_in_the_system_owner_tier_and_owner_name()  {
        WaitHelper.waitForVisibility(admin.selectOwnerTier,2000);
        admin.selectOwnerTier.click();
        admin.dropdownOptions.getLast().click();
        Helper.clickWithJS(admin.ownerName);
        admin.ownerName.sendKeys("LC");
    }

    @And("user verifies that Owner with existing owner tier and owner name is not created")
    public void user_verifies_that_owner_with_existing_owner_tier_and_owner_name_is_not_created()  {
       WaitHelper.fluentWait(admin.ownerList.getLast(),1000);
        assertEquals(15, admin.ownerList.size());
    }

    @When("user enters Shelf Type Name")
    public void user_enters_shelf_type_name() throws InterruptedException {
        admin.shelfTypeName.click();
        admin.shelfTypeName.sendKeys("Test Shelf Type");
        wait.hardWait(100);
    }

    @And("user selects Size Class")
    public void user_selects_size_class() {
        admin.selectSizeClass.click();
        admin.dropdownOptions.get(2).click();
        admin.dropdownOptions.get(3).click();
        admin.dropdownOptions.get(4).click();
    }

    @When("user updates Shelf Type Name")
    public void user_updates_shelf_type_name() throws InterruptedException {
        WaitHelper.waitForClickability(admin.shelfTypeName,2000);
        admin.shelfTypeName.click();
        admin.shelfTypeName.sendKeys(Keys.CONTROL + "a");
        admin.shelfTypeName.sendKeys(Keys.DELETE);
        admin.shelfTypeName.sendKeys("Updated Test Shelf Type");
        wait.hardWait(100);
    }

    @When("user decreases capacity of Shelf Type used by shelves")
    public void user_decreases_capacity_of_shelf_type_used_by_shelves() throws InterruptedException {
        WaitHelper.waitForClickability(admin.expandMaxCapacity,2000);
        admin.expandMaxCapacity.click();
        WaitHelper.waitForVisibility(admin.capacity.get(0),2000);
        admin.capacity.get(0).click();
        admin.capacity.get(0).sendKeys(Keys.CONTROL + "a");
        admin.capacity.get(0).sendKeys(Keys.DELETE);
        admin.capacity.get(0).sendKeys("5");
        WaitHelper.waitForClickability(admin.collapseMaxCapacity,2000);
        admin.collapseMaxCapacity.click();
        wait.hardWait(100);
    }

    @When("user increases capacity of Shelf Type used by shelves")
    public void user_increases_capacity_of_shelf_type_used_by_shelves() throws InterruptedException {
        WaitHelper.waitForClickability(admin.expandMaxCapacity,5000);
        admin.expandMaxCapacity.click();
        WaitHelper.waitForVisibility(admin.capacity.get(1),2000);
        admin.capacity.get(1).click();
        admin.capacity.get(1).sendKeys(Keys.CONTROL + "a");
        admin.capacity.get(1).sendKeys(Keys.DELETE);
        admin.capacity.get(1).sendKeys("15");
        WaitHelper.waitForClickability(admin.collapseMaxCapacity,2000);
        admin.collapseMaxCapacity.click();
        wait.hardWait(100);
    }

}
