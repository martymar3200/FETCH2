package automation.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import automation.pages.AdminPage;
import automation.pages.ShelvingPage;
import automation.utilities.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShelvingSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    ShelvingPage shelving = new ShelvingPage();
    AdminPage admin = new AdminPage();
    HomeSteps homeSteps = new HomeSteps();
    AccessionSteps accessionSteps = new AccessionSteps();
    Helper helper = new Helper();
    GenericHelper genHelp = new GenericHelper();
    WaitHelper wait = new WaitHelper();
    SelectHelper select = new SelectHelper();
    Actions actions = new Actions(driver);
    static String itemBarcode = "";


    @Given("user navigates to Shelving Page")
    public void user_navigates_to_shelving_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "shelvingURL"));
        WaitHelper.waitForPageToLoad(90);
    }

    @When("user is on Shelving page")
    public void user_is_on_shelving_page() {
        String actualURL = driver.getCurrentUrl();
        String expectedURL = "https://test.fetch.example.com/shelving";
        Assert.assertEquals("Shelving page URL failed",
                expectedURL, actualURL);
    }

    @Then("Filter dropdown is clickable")
    public void filter_dropdown_is_clickable() {
        Helper.isClickable(shelving.filter);
        shelving.filter.click();
        shelving.filter.click();
    }

    @Then("Rearrange dropdown is clickable")
    public void rearrange_dropdown_is_clickable() {
        Helper.isClickable(shelving.rearrangeDropdown);
        shelving.rearrangeDropdown.click();
        shelving.rearrangeDropdown.click();
    }

    @Then("Create Shelving Job button is clickable")
    public void create_shelving_job_button_is_clickable() {
        Helper.isClickable(shelving.createShelvingJob);
        shelving.createShelvingJob.click();
        shelving.closeMsg.click();
    }

    @When("user clicks on Rearrange dropdown")
    public void user_clicks_on_rearrange_dropdown() {
        helper.jSClick(shelving.rearrangeDropdown);
    }

    @Then("user verifies dropdown options")
    public void user_verifies_dropdown_options(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedOption = map.get("optionname");
            wait.hardWait(1000);
            String actualOption = shelving.allDropdownOptions.get(i).getText();
            Assert.assertEquals("Options names verification failed",
                    expectedOption, actualOption);
            i++;
        }
    }

    @Then("user verifies dropdown checkboxes are clickable")
    public void user_verifies_dropdown_checkboxes_are_clickable() {
        for (WebElement option : shelving.allDropdownOptions) {
            assertTrue("Checkbox is not clickable: " + option.isEnabled(), option.isDisplayed());
        }
    }

    @Then("user verifies dropdown options match shelf table column options")
    public void user_verifies_dropdown_options_match_shelf_table_column_options() {
        String shelfTableValue = "";
        String filterDropdownValue = "";
        for (WebElement option : shelving.shelfTableColumns) {
            WaitHelper.waitForVisibility(option, 2000);
            shelfTableValue = option.getText();
        }
        for (WebElement value : shelving.allDropdownOptions) {
            WaitHelper.waitForVisibility(value, 2000);
            filterDropdownValue = value.getText();
        }
        Assert.assertEquals("Options did not match!", filterDropdownValue, shelfTableValue);
    }

    @Then("user verifies all options are selected")
    public void user_verifies_all_options_are_selected() {
        for (WebElement checkbox : shelving.allDropdownOptions) {
            if (checkbox.getAttribute("aria-selected").equals("false")) {
                System.out.println("Not all the checkboxes are selected");
            }
        }
    }

    @Then("user is able to deselect all the options")
    public void user_is_able_to_deselect_all_the_options() {
        for (WebElement checkbox : shelving.allDropdownOptions) {
            select.selectCheckBox(checkbox, true);
        }
    }

    @Then("user selects options A, B and C from the dropdown")
    public void user_selects_options_a_b_and_c_from_the_dropdown() {
        for (WebElement checkbox : shelving.allDropdownOptions) {
            String checkboxValue = checkbox.getText();
            if (checkboxValue.equals("Job Number") || checkboxValue.equals("Status") ||
                    checkboxValue.equals("Date Added")) {
                checkbox.click();
            }
        }
    }

    @Then("selected options are displayed on the page")
    public void selected_options_are_displayed_on_the_page() {
        String checkboxValue = "";
        String shelfTableValue = "";
        for (WebElement checkbox : shelving.allDropdownOptions) {
            if (checkbox.getAttribute("aria-selected").equals("true")) {
                WaitHelper.waitForVisibility(checkbox, 2000);
                checkboxValue = checkbox.getText();
            }
        }
        for (int i = 0; i < shelving.shelfTableColumns.size(); i++) {
            shelfTableValue = shelving.shelfTableColumns.get(i).getText();
        }
        Assert.assertEquals("Selected options are not displayed!", checkboxValue, shelfTableValue);
    }

    @When("user clicks Create Shelving Job button")
    public void user_clicks_create_shelving_job_button() throws InterruptedException {
        wait.hardWait(3000);
        shelving.createShelvingJob.click();
    }

    @Then("user verifies Create Shelving Job modal sections")
    public void user_verifies_create_shelving_job_modal_sections(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 1;

        for (Map<String, String> map : maps) {
            String expectedOption = map.get("section");
            String actualOption = shelving.modalSections.get(i).getText();
            Assert.assertEquals("Section names verification failed",
                    expectedOption, actualOption);
            i++;
        }
    }

    @Then("user verifies Create Shelving Job modal dropdown fields")
    public void user_verifies_create_shelving_job_modal_dropdown_fields(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedOption = map.get("field");
            String actualOption = shelving.modalFields.get(i).getText();
            Assert.assertEquals("Dropdown field verification failed",
                    expectedOption, actualOption);
            i++;
        }
    }

    @Then("cancel button is clickable")
    public void cancel_button_is_clickable() {
        Helper.isClickable(shelving.cancelBtn);
    }

    @Then("submit button is enabled and clickable")
    public void submit_button_is_enabled_and_clickable() {
        Helper.verifyElementEnabled(shelving.submit);
        Helper.isClickable(shelving.submit);
    }

    @When("user unchecks menu items to their preferred order")
    public void user_unchecks_menu_items_to_their_preferred_order() throws InterruptedException {
        shelving.rearrangeDropdownOptions.get(1).click();
        wait.hardWait(100);
        shelving.rearrangeDropdownOptions.get(4).click();
        shelving.rearrangeDropdownOptions.get(5).click();
        wait.hardWait(100);
        shelving.rearrangeDropdown.click();
    }

    @Then("user verifies the Shelf Table column names")
    public void user_verifies_the_shelf_table_column_names() {
        String column = "";
        for (WebElement element : shelving.shelfTableColumns) {
            column = element.getText();
        }
        List<String> shelfColumns = new ArrayList<>(Arrays.asList("Job Number", "Status",
                "Assigned User"));
        if (shelfColumns.contains(column)) {
            System.out.println("Table has correct column names");
        } else {
            System.out.println("Table has wrong column names");
        }
    }

    @When("user enters a Shelf Number")
    public void user_enters_a_shelf_number() {
        shelving.enterShelfNumber.sendKeys("2");
        actions.sendKeys(Keys.TAB).build().perform();
    }

    @When("user enters a Shelf Width")
    public void user_enters_a_shelf_width() {
        shelving.enterShelfWidth.sendKeys("5");
        actions.sendKeys(Keys.TAB).build().perform();
    }

    @When("user enters a Shelf Height")
    public void user_enters_a_shelf_height() {
        shelving.enterShelfHeight.sendKeys("3");
        actions.sendKeys(Keys.TAB).build().perform();
    }

    @When("user enters a Shelf Depth")
    public void user_enters_a_shelf_depth() {
        shelving.enterShelfDepth.sendKeys("1");
        actions.sendKeys(Keys.TAB).build().perform();
    }

    @When("user selects a Container Type")
    public void user_selects_a_container_type() {
        shelving.selectType.click();
        genHelp.getElement(By.cssSelector("div [role='option']:nth-child(1)")).click();
    }

    @When("user clicks Create Shelf button")
    public void user_clicks_create_shelf_button() {
        shelving.submit.click();
    }

    @When("user selects From Verification Job option")
    public void user_selects_from_verification_job_option() throws InterruptedException {
        helper.jSClick(shelving.fromVerificationJob);
        wait.hardWait(1000);
    }

    @When("user selects No")
    public void user_selects_no() {
        helper.jSClick(shelving.no);
    }

    @Then("user selects a Building from Shelving Locations")
    public void user_selects_a_building_from_shelving_locations() throws InterruptedException {
        WaitHelper.waitForVisibility(shelving.building, 1000);
        shelving.building.click();
        helper.jSClick(shelving.buildings.get(1));
        wait.hardWait(2000);
    }

    @Then("user clicks Submit")
    public void user_clicks_submit() {
        WaitHelper.waitForClickability(shelving.submit,3000);
        shelving.submit.click();
    }

    @When("user selects Yes")
    public void user_selects_yes() {
        WaitHelper.waitForClickability(shelving.yes, 3000);
        shelving.yes.click();
    }

    @Then("a new modal with shelving location options along with the verification job selection is displayed")
    public void a_new_modal_with_shelving_location_options_along_with_the_verification_job_selection_is_displayed() {
        Helper.verifyElementDisplayed(shelving.createShelvingJobModal);
    }

    @When("user navigates to Shelving Job")
    public void user_navigates_to_shelving_job() {
        WebElement job = driver.findElement(By.xpath("//td[.='118']"));
        job.click();
    }

    @When("user clicks three dot menu next to a container")
    public void user_clicks_three_dot_menu_next_to_a_container() {
        helper.jSClick(shelving.threeDotNextToContainer);
    }

    @Then("user should see Edit Location option")
    public void user_should_see_edit_location_option() {
        WaitHelper.waitForVisibility(shelving.editOrAssign, 10);
        assertEquals("Edit Location", shelving.editOrAssign.getText());
    }

    @Then("user clicks Edit Location button")
    public void user_clicks_edit_location_button() {
        shelving.editOrAssign.click();
    }

    @When("user navigates to Shelving Job with Running Status")
    public void user_navigates_to_shelving_job_with_running_status() throws InterruptedException {
        if (shelving.runningJob.isDisplayed()) {
            helper.jSClick(shelving.runningJob);
        }
        wait.hardWait(1000);
    }

    @Then("Assign User dropdown is clickable")
    public void assign_user_dropdown_is_clickable() {
        Helper.isClickable(shelving.assignedUserField);
    }

    @Then("user selects User from dropdown")
    public void user_selects_user_from_dropdown() throws InterruptedException {
        WaitHelper.waitForClickability(shelving.assignedUserField, 3000);
        shelving.assignedUserField.click();
        shelving.assignedUserField.sendKeys("Admin");
        wait.hardWait(100);
        shelving.assignedUserField.sendKeys(Keys.ARROW_DOWN);
        shelving.assignedUserField.sendKeys(Keys.ENTER);
    }

    @Then("Save Edits button is clickable")
    public void save_edits_button_is_clickable() {
        Helper.isClickable(shelving.saveEdits);
    }

    @Then("Cancel edits button is clickable")
    public void cancel_edits_button_is_clickable() {
        Helper.isClickable(shelving.cancelEdits);
    }

    @Then("user clicks Save Edits button")
    public void user_clicks_Save_Edits_button() {
        WaitHelper.waitForClickability(shelving.saveEdits,3000);
        shelving.saveEdits.click();
    }

    @When("user selects Direct To Shelve option")
    public void user_selects_direct_to_shelve_option() {
        helper.jSClick(shelving.directToShelve);
    }

    @Then("user selects Right side")
    public void user_selects_right_side() throws InterruptedException {
        wait.hardWait(1000);
        shelving.rightSide.click();
    }

    @Then("user verifies the Status is {string}")
    public void user_verifies_the_status_is(String string) throws InterruptedException {
        Assert.assertEquals("Shelving Job status does not match! ", string, shelving.shelvingJobStatus.getText());
        wait.hardWait(1000);
    }

    @When("user clicks Execute Job")
    public void user_clicks_execute_job() throws InterruptedException {
        WaitHelper.waitForPageToLoad(90);
        WaitHelper.waitForVisibility(shelving.executeJob,5000);
        helper.jSClick(shelving.executeJob);
        wait.hardWait(2000);
        helper.jSClick(shelving.beAwareMsg);
        wait.hardWait(3000);
    }

    @Then("user selects a created Verification Job")
    public void user_selects_a_created_verification_job() throws InterruptedException {
        WaitHelper.waitForVisibility(shelving.selectByNumber, 7000);
        shelving.selectByNumber.click();
        wait.hardWait(7000);
        for (WebElement dropdownValue : shelving.verificationJobsList) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", dropdownValue);
            if (dropdownValue.getText().contains("Job #: " + VerificationSteps.verificationJobNumber)) {
                dropdownValue.click();
            }
        }
        shelving.selectByNumber.click();
        wait.hardWait(1000);
    }

    @Then("user selects verification job")
    public void user_selects_verification_job() throws InterruptedException {
        WaitHelper.fluentWait(shelving.selectByNumber, 1000);
        shelving.selectByNumber.click();
        WaitHelper.waitForClickability(shelving.verificationJobsList.get(0), 1000);
        shelving.verificationJobsList.get(0).click();
        shelving.selectByNumber.click();
        wait.hardWait(1000);
    }

    @And("user scans a Shelving Container")
    public void user_scans_a_shelving_container() throws InterruptedException {
        driver.findElement(By.tagName("body")).sendKeys(shelving.containerBarcode.getText());
        wait.hardWait(2000);
    }

    @And("user scans wrong Container")
    public void user_scans_wrong_container() throws InterruptedException {
        driver.findElement(By.tagName("body")).sendKeys("123");
        wait.hardWait(2000);
    }

    @And("user scans shelf to verify Container")
    public void user_scans_shelf_to_verify_container() throws InterruptedException {
        driver.findElement(By.tagName("body")).sendKeys("1");
        WaitHelper.waitForVisibility(shelving.assignedShelf, 1000);
        String shelf = shelving.assignedShelf.getText().substring(79, 84);
        System.out.println("Shelf barcode value is: " + shelf);
        shelving.closeMsg.click();
        wait.hardWait(1000);
        driver.findElement(By.tagName("body")).sendKeys("" + shelf + "");
        Assert.assertEquals("Shelved", shelving.shelvedCheckMark.getText());
    }

    @And("user verifies second Container if exists")
    public void user_verifies_second_container_if_exists() throws InterruptedException {
        List<WebElement> containerList = driver.findElements(By.cssSelector("[class='q-table'] tbody tr"));
        if (containerList.size() > 1) {
            driver.findElement(By.tagName("body")).sendKeys(shelving.containerBarcode.getText());
            wait.hardWait(2000);
            driver.findElement(By.tagName("body")).sendKeys("1");
            WaitHelper.waitForVisibility(shelving.assignedShelf, 1000);
            String shelf = shelving.assignedShelf.getText().substring(79, 85);
            System.out.println(shelf);
            shelving.closeMsg.click();
            wait.hardWait(1000);
            driver.findElement(By.tagName("body")).sendKeys("" + shelf + "");
            Assert.assertEquals("Shelved", shelving.shelvedCheckMark.getText());
        } else {
            System.out.println("Job has only one container");
        }
    }

    @And("user selects Shelf")
    public void user_selects_shelf() throws InterruptedException {
        helper.scrollToElement(shelving.selectShelf);
        WaitHelper.waitForClickability(shelving.selectShelf, 1000);
        shelving.selectShelf.click();
        wait.hardWait(1000);
        admin.modalFieldOptions.get(0).click();
    }

    @And("user selects Shelf Position")
    public void user_selects_shelf_position() throws InterruptedException {
        helper.scrollToElement(shelving.selectShelfPosition);
        WaitHelper.waitForClickability(shelving.selectShelfPosition, 1000);
        shelving.selectShelfPosition.click();
        wait.hardWait(1000);
        admin.modalFieldOptions.get(0).click();
        wait.hardWait(1000);
    }

    @When("user clicks Complete Job")
    public void user_clicks_complete_job() throws InterruptedException {
//        wait.hardWait(2000);
        WaitHelper.waitForVisibility(shelving.completeJob,3000);
        helper.jSClick(shelving.completeJob);
        wait.hardWait(2000);
    }

    @When("user clicks three dot menu next to Container")
    public void user_clicks_three_dot_menu_next_to_container() {
        helper.jSClick(shelving.threeDotNextToContainer);
    }

    @Then("user clicks Edit Location")
    public void user_clicks_edit_location() {
        helper.jSClick(shelving.editLocation);
    }

    @Then("user verifies Edit Location button is clickable")
    public void user_verifies_edit_location_button_is_clickable() {
        Helper.isClickable(shelving.editLocation);
        helper.jSClick(shelving.threeDotNextToContainer);
    }

    @When("user sets input delay and switches Toggle on")
    public void user_sets_input_delay_and_switches_toggle_on() throws InterruptedException {
        WebElement userIcon = driver.findElement(By.cssSelector("[aria-label='UserMenu']"));
        userIcon.click();
        wait.hardWait(2000);
        shelving.inputDelay.click();
        shelving.inputDelay.sendKeys(Keys.CONTROL + "a");
        shelving.inputDelay.sendKeys(Keys.DELETE);
        shelving.inputDelay.sendKeys("1.25");
        shelving.toggleScan.click();
        wait.hardWait(1000);
    }

    @And("user saves item barcode")
    public void user_saves_item_barcode() {
        itemBarcode = shelving.containerBarcode.getText();
        System.out.println("Item barcode is : " + itemBarcode);
    }

    @When("user completes a Shelving Job")
    public void user_completes_a_shelving_Job() throws InterruptedException {
        homeSteps.user_clicks_shelving_on_side_navigation_menu();
        user_clicks_create_shelving_job_button();
        user_selects_from_verification_job_option();
        user_selects_a_created_verification_job();
        user_selects_a_building_from_shelving_locations();
        user_clicks_submit();
        user_clicks_execute_job();
        user_scans_a_shelving_container();
        user_saves_item_barcode();
        user_scans_shelf_to_verify_container();
        user_clicks_complete_job();
        accessionSteps.user_clicks_complete();
    }

    @Then("user selects a Verification Job from the Verification Jobs List")
    public void user_selects_a_verification_job_from_the_verification_jobs_list() throws InterruptedException {
        shelving.selectByNumber.click();
        WaitHelper.waitForClickability(shelving.verificationJobsList.get(0), 1000);
        shelving.verificationJobsList.get(0).click();
        shelving.selectByNumber.click();
        wait.hardWait(1000);
    }

    @When("user clicks to select a job from Verification Job List")
    public void user_clicks_to_select_a_job_from_verification_job_list() {
        shelving.selectByNumber.click();
    }

    @Then("user verifies that the response includes all the metadata required")
    public void user_verifies_that_the_response_includes_all_the_metadata_required() {
        for (WebElement job : shelving.verificationJobsList) {

            if (job.getText().contains("Non-Tray")) {
                System.out.println("Non-Tray JOBS: " + job.getText());
                String jobNumber = job.getText().substring(0, 5);
                String items = job.getText().substring(24, 29);
                assertEquals("Job #", jobNumber);
                assertEquals("items", items);

            } else {
                System.out.println("Trayed JOBS: " + job.getText());
                String jobNumber = job.getText().substring(0, 5);
                String containers = job.getText().substring(22, 32);
                String items = job.getText().substring(36, 41);
                assertEquals("Job #", jobNumber);
                assertEquals("containers", containers);
                assertEquals("items", items);
            }
        }
    }

    @And("user verifies date created")
    public void user_verifies_date_created() {
        WaitHelper.waitForVisibility(shelving.dateCreated, 3000);
        String dateCreated = shelving.dateCreated.getText();
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
        String date = dateFormat.format(currentDate);
        System.out.println("Date created: "+ dateCreated + " Current date: " + date);
        assertEquals(date,dateCreated);
    }

    @Then("user clicks on Shelving Job")
    public void user_clicks_on_shelving_job() {
        shelving.shelvingJobsList.get(0).click();
    }

    @Then("user verifies a list of verification jobs is displayed")
    public void user_verifies_a_list_of_verification_jobs_is_displayed() {
        WaitHelper.waitForClickability(shelving.verificationJobsList.get(0), 1000);
        for (WebElement job : shelving.verificationJobsList) {
            job.isDisplayed();
            System.out.println("Completed Verification Jobs List: " + job.getText());
        }
    }

    @And("user verifies that the menu includes the correct data")
    public void user_verifies_that_the_menu_includes_the_correct_data() {
        String data = shelving.verificationJobsList.get(0).getText();
        String jobNumber = data.substring(0, 9);
        if (data.contains("Trayed")) {
            String itemsCount = data.substring(34);
            assertTrue(jobNumber.contains("Job #"));
            assertTrue(itemsCount.contains("items"));
        } else if (data.contains("Non-Tray")) {
            String nontrayItemsCount = data.substring(24);
            assertTrue(jobNumber.contains("Job #"));
            assertTrue(nontrayItemsCount.contains("items"));
        }
    }

    @And("user scans Shelf")
    public void user_scans_shelf() {
        driver.findElement(By.tagName("body")).sendKeys("100103");
    }


}











