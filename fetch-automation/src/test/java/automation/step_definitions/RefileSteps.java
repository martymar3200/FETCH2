package automation.step_definitions;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import automation.pages.RefilePage;
import automation.utilities.ConfigurationReader;
import automation.utilities.Driver;
import automation.utilities.Helper;
import automation.utilities.WaitHelper;
import java.util.List;
import java.util.Map;


public class RefileSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    RefilePage refile = new RefilePage();
    AccessionSteps accessionSteps = new AccessionSteps();
    VerificationSteps verificationSteps = new VerificationSteps();
    HomeSteps homeSteps = new HomeSteps();
    ShelvingSteps shelvingSteps = new ShelvingSteps();
    RequestSteps requestSteps = new RequestSteps();
    AlertSteps alertSteps = new AlertSteps();
    Helper helper = new Helper();
    WaitHelper wait = new WaitHelper();
    static String trayBarcodeValue = "";


    @Given("user navigates to the Refile Page")
    public void user_navigates_to_the_refile_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "refileURL"));
        WaitHelper.waitForPageToLoad(9000);
    }

    @Then("user verifies Refile Job table is displayed")
    public void user_verifies_refile_job_table_is_displayed() {
        Helper.verifyElementDisplayed(refile.table);
    }

    @Then("user verifies Refile Job table column names")
    public void user_verifies_refile_job_table_column_names(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedName = map.get("name");
            String actualName = refile.refileTableColumnNames.get(i).getText();
            Assert.assertTrue("Column names verification failed",
                    actualName.contains(expectedName));
            i++;
        }
    }

    @When("user clicks Refile Queue")
    public void user_clicks_refile_queue() {
        WaitHelper.waitForClickability(refile.refileQueueBtn, 2000);
        helper.jSClick(refile.refileQueueBtn);
    }

    @Then("user verifies Refile Queue table is displayed")
    public void user_verifies_refile_queue_table_is_displayed() {
        Helper.verifyElementDisplayed(refile.table);
    }

    @Then("user verifies Refile Queue table column names")
    public void user_verifies_refile_queue_table_column_names(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedName = map.get("name");
            String actualName = refile.refileTableColumnNames.get(i).getText();
            Assert.assertEquals("Column names verification failed",
                    expectedName, actualName);
            i++;
        }
    }

    @When("user selects Add Item to Queue option")
    public void user_selects_add_item_to_queue_option() {
        WaitHelper.waitForClickability(refile.refileDropdownOptions.get(0), 3000);
        helper.jSClick(refile.refileDropdownOptions.get(0));
    }

    @When("user scans an Item Barcode from a completed Pick List job")
    public void user_scans_an_item_barcode_from_a_completed_pick_list_job() throws InterruptedException {
        driver.findElement(By.tagName("body")).sendKeys(PickListSteps.itemBarcode);
        wait.hardWait(2000);
        WaitHelper.waitForClickability(refile.doneBtn, 3000);
        refile.doneBtn.click();
    }

    @When("user scans incorrect Item Barcode")
    public void user_scans_incorrect_item_barcode() throws InterruptedException {
        driver.findElement(By.tagName("body")).sendKeys("12345");
        wait.hardWait(2000);
    }

    @When("user selects Create Refile Job option")
    public void user_selects_create_refile_job_option() {
        WaitHelper.waitForClickability(refile.refileDropdownOptions.get(2), 2000);
        helper.jSClick(refile.refileDropdownOptions.get(2));
    }

    @When("user clicks Create Refile Job")
    public void user_clicks_create_refile_job() {
        WaitHelper.waitForClickability(refile.createRefileJobBtn, 2000);
        helper.jSClick(refile.createRefileJobBtn);
    }

    @Then("user verifies the Refile Job is created")
    public void user_verifies_the_refile_job_is_created() {
        WaitHelper.waitForVisibility(refile.alertText, 3000);
        Assert.assertTrue(refile.alertText.getText().contains("Successfully created Refile Job #: "));
    }

    @Then("user verifies Tray Barcode is displayed")
    public void user_verifies_tray_barcode_is_displayed() {
        refile.trayBarcodeValue.isDisplayed();
        trayBarcodeValue = refile.trayBarcodeValue.getText();
    }

    @Then("user verifies scan modal is displayed")
    public void user_verifies_scan_modal_is_displayed() throws InterruptedException {
        Helper.verifyElementDisplayed(refile.scanModal);
        wait.hardWait(1000);
    }

    @Then("user verifies the information on the scan modal")
    public void user_verifies_the_information_on_the_scan_modal() {
        if (refile.scanModalLabels.get(1).getText().contains("Tray Barcode:")) {
            String value = driver.findElement(By.xpath("(//p[@class='text-body1'] )[5]")).getText();
            Assert.assertEquals(value, trayBarcodeValue);
        } else {
            System.out.println("This is a Non-Tray Item");
        }
    }

    @When("user clicks Create Refile Job menu")
    public void user_clicks_create_refile_job_menu() throws InterruptedException {
        wait.hardWait(3000);
        WaitHelper.fluentWait(refile.createRefileJobMenu, 5000);
        refile.createRefileJobMenu.click();
    }

    @When("user clicks on Refile Job")
    public void user_clicks_on_refile_job() {
        WaitHelper.waitForClickability(refile.refileJobsList.get(0), 2000);
        refile.refileJobsList.get(0).click();
    }

    @Then("user selects Add to Refile Job option")
    public void user_selects_add_to_refile_job_option() {
        WaitHelper.waitForClickability(refile.dropdownOptions.get(1), 2000);
        refile.dropdownOptions.get(1).click();
    }

    @When("user clicks Create Refile Job button")
    public void user_clicks_create_refile_job_btn() {
        WaitHelper.waitForClickability(refile.createRefileJobBtn, 3000);
        helper.jSClick(refile.createRefileJobBtn);
    }

    @Then("user is able to see the Refile Job dashboard")
    public void user_is_able_to_see_the_refile_job_dashboard() {
        WaitHelper.waitForVisibility(refile.refileJobNumber, 1000);
        String jobNumber = refile.refileJobNumber.getText();
        Assert.assertEquals(RequestSteps.createdJob, jobNumber);
    }

    @Then("user creates a Request")
    public void user_creates_a_Request() throws InterruptedException {
        homeSteps.user_clicks_accession_on_side_navigation_menu();
        accessionSteps.user_completes_a_non_tray_accession_job();
        verificationSteps.user_navigates_to_the_verification_page();
        verificationSteps.user_navigates_to_the_verification_job();
        verificationSteps.user_saves_verification_job_number();
        verificationSteps.user_verifies_item_barcode();
        accessionSteps.user_clicks_complete_job_button();
        accessionSteps.user_clicks_complete();
        alertSteps.user_verifies_msg("The Job has been completed.");
        homeSteps.user_clicks_shelving_on_side_navigation_menu();
        shelvingSteps.user_completes_a_shelving_Job();
        alertSteps.user_verifies_alert_msg("The Shelving Job has been completed.");
        homeSteps.user_clicks_request_on_side_navigation_menu();
        requestSteps.user_clicks_create_request_job_menu();
        requestSteps.user_selects_create_manual_requests_option();
        requestSteps.request_job_creation_modal_is_displayed();
        requestSteps.user_enters_shelved_item_barcode();
        requestSteps.user_enters_request_id();
        accessionSteps.submit_button_is_enabled();
        accessionSteps.user_clicks_submit_button();
        alertSteps.user_verifies_alert_msg("Successfully created the request.");
    }

    @When("user clicks Edit Job Info")
    public void user_clicks_edit_job_info() {
        helper.jSClick(refile.editJobInfo);
        }

}



