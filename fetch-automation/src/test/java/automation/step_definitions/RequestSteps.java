package automation.step_definitions;

import automation.pages.HomePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.*;
import automation.pages.AlertPage;
import automation.pages.PickListPage;
import automation.pages.RequestPage;
import automation.utilities.*;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RequestSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    RequestPage request = new RequestPage();
    Helper helper = new Helper();
    WaitHelper wait = new WaitHelper();
    AlertPage alert = new AlertPage();
    PickListPage picklist = new PickListPage();
    AdminSteps adminSteps = new AdminSteps();
    HomePage home = new HomePage();
    ShelvingSteps shelvingSteps = new ShelvingSteps();
    static String requestBarcode = "";
    static String createdJob = "";
    static String requestID = "";


    @When("user navigates to the Request Page")
    public void user_navigates_to_the_request_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "requestURL"));
        WaitHelper.waitForPageToLoad(90);
    }

    @When("user clicks Create Request Job menu")
    public void user_clicks_create_request_job_menu() {
        WaitHelper.waitForClickability(request.createRequestJobMenu, 3000);
        request.createRequestJobMenu.click();
    }

    @When("user clicks Create button")
    public void user_clicks_create_button() {
        WaitHelper.waitForClickability(request.createRequestJobMenu, 5000);
        request.createRequestJobMenu.click();
    }

    @Then("user verifies the dropdown options")
    public void user_verifies_the_dropdown_options(io.cucumber.datatable.DataTable dataTable) {
        WaitHelper.waitForVisibility(request.dropdownOptions.get(0), 10);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("option");
            String actualLabel = request.dropdownOptions.get(i).getText();
            assertEquals("Options name verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @Then("user selects Create Manual Requests option")
    public void user_selects_create_manual_requests_option() {
        WaitHelper.waitForClickability(request.dropdownOptions.get(2), 3000);
        request.dropdownOptions.get(2).click();
    }

    @Then("request job creation modal is displayed")
    public void request_job_creation_modal_is_displayed() {
        WaitHelper.waitForVisibility(request.modal, 10);
        Helper.verifyElementDisplayed(request.modal);
    }

    @Then("request submit button is disabled")
    public void request_submit_button_is_disabled() {
        helper.scrollIntoView(request.submitRequest);
        Assert.assertFalse(request.submitRequest.isEnabled());
    }

    @When("user enters an incorrect Item Barcode")
    public void user_enters_an_incorrect_item_barcode() {
        request.itemBarcodeField.click();
        request.itemBarcodeField.sendKeys("1234567890");
    }

    @When("user enters an item barcode")
    public void user_enters_an_item_barcode() {
        request.itemBarcodeField.click();
        request.itemBarcodeField.sendKeys("11223344550");
    }

    @When("user enters shelved Item Barcode")
    public void user_enters_shelved_item_barcode() {
        request.itemBarcodeField.click();
        request.itemBarcodeField.sendKeys(ShelvingSteps.itemBarcode);
    }

    @When("user enters a Trayed Item Barcode from an existing Shelving Job")
    public void user_enters_a_trayed_item_barcode_from_an_existing_shelving_job() {
        request.itemBarcodeField.click();
        request.itemBarcodeField.sendKeys(VerificationSteps.trayedItemBarcode);
    }

    @When("user enters Request ID")
    public void user_enters_request_id() {
        request.requestIDField.click();
        request.requestIDField.sendKeys("111");
    }

    @When("user enters Requester Name")
    public void user_enters_request_name() {
        request.requestorNameField.click();
        request.requestorNameField.sendKeys("Totu");
    }

    @When("user selects Request Type")
    public void user_selects_request_type() {
        request.requestTypeField.click();
        request.options.get(2).click();
    }

    @When("user selects Priority")
    public void user_selects_priority() {
        request.priorityField.click();
        request.options.get(1).click();
    }

    @When("user selects Delivery Location")
    public void user_selects_delivery_location() throws InterruptedException {
        helper.scrollIntoView(request.deliveryLocationField);
        request.deliveryLocationField.click();
        wait.hardWait(2000);
        request.options.get(1).click();
    }

    @Then("user clicks on created Request")
    public void user_clicks_on_created_request() {
        for (WebElement request : request.requestList) {
            try{
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", request);
                if (request.getText().contains("Totu") && request.getText().contains(ShelvingSteps.itemBarcode)) {
                    request.click();
                }

            }catch (Exception e){
                System.out.println("Request " + request + " not visible.");
            }
        }
    }

    @Then("user verifies Request details on Overlay Slide")
    public void user_verifies_request_details_on_overlay_slide() {
        WaitHelper.waitForVisibility(request.requestItemDetails.get(0), 3000);
        String actualBarcode = request.actualBarcode.getText();
        Assert.assertEquals(ShelvingSteps.itemBarcode, actualBarcode);
    }

    @When("user selects Create a Pick List option")
    public void user_selects_create_a_pick_list_option() {
        helper.jSClick(request.dropdownOptions.get(1));
    }

    @Then("user verifies options with checkboxes are displayed")
    public void user_verifies_options_with_checkboxes_are_displayed() {
        Helper.verifyElementDisplayed(request.optionsWithCheckboxes.get(0));
        if(request.optionsWithCheckboxes.size() >= 3){
            System.out.println("Number of Requests: " + request.optionsWithCheckboxes.size());
        } else {
            System.out.println("Number of Requests is not sufficient for this test");
        }
    }

    @When("user selects Request")
    public void user_selects_request() {
        helper.jSClick(request.optionsWithCheckboxes.get(1));
    }

    @When("user selects Requests")
    public void user_selects_requests() {
        helper.jSClick(request.optionsWithCheckboxes.get(1));
        helper.jSClick(request.optionsWithCheckboxes.get(2));
    }

    @When("user clicks Create Pick List")
    public void user_clicks_create_pick_list() throws InterruptedException {
        helper.jSClick(request.createPickListBtn);
        wait.hardWait(1000);
    }

    @Then("user verifies the Pick List is created")
    public void user_verifies_the_pick_list_is_created() {
        WaitHelper.waitForVisibility(request.alertText, 1000);
        Assert.assertTrue(request.alertText.getText().contains("Successfully created Pick List #: "));
    }

    @When("user clicks the alert link")
    public void user_clicks_the_alert_link() throws InterruptedException {
        createdJob = request.createdJobLink.getText();
        helper.jSClick(request.createdJobLink);
        wait.hardWait(1000);
    }

    @Then("user is able to see the Pick List dashboard")
    public void user_is_able_to_see_the_pick_list_dashboard() {
        String picklistJobNumber = picklist.picklistJobNumber.getText();
        Assert.assertEquals(picklistJobNumber, createdJob);
    }

    @When("user selects Add to Pick List option")
    public void user_selects_add_to_pick_list_option() {
        helper.jSClick(request.dropdownOptions.get(0));
    }

    @When("user selects Pick List from dropdown")
    public void user_selects_pick_list_from_dropdown() throws InterruptedException {
        request.selectPickListJobDropdown.click();
        request.dropdownList.get(0).click();
        wait.hardWait(1000);
    }

    @When("user clicks Pick List jobs dropdown")
    public void user_clicks_pick_list_jobs_dropdown() throws InterruptedException {
        WaitHelper.waitForClickability(request.selectPickListJobDropdown,2000);
        request.selectPickListJobDropdown.click();
        wait.hardWait(1000);
    }

    @Then("user verifies only Inactive Pick List jobs are displayed")
    public void user_verifies_only_inactive_pick_list_jobs_are_displayed() {
        assertEquals(PickListSteps.inactiveJobsCount, request.dropdownList.size());
    }
    @When("user clicks Add to Pick List")
    public void user_clicks_add_to_pick_list() throws InterruptedException {
        helper.jSClick(request.addToPickListBtn);
        wait.hardWait(1000);
    }

    @Then("user verifies items are added to the Pick List")
    public void user_verifies_items_are_added_to_the_pick_list() {
        WaitHelper.waitForVisibility(request.alertText, 1000);
        assertTrue(request.alertText.getText().contains("Successfully added items to Pick List #: "));
    }

    @When("user has an Item present in request table")
    public void user_has_an_item_present_in_request_table() {
        requestBarcode = request.firstItemBarcode.getText();
        System.out.println("Request Item Barcode is: " + requestBarcode);
    }

    @When("user enters an existing in request table Item barcode")
    public void user_enters_an_existing_in_request_table_item_barcode() {
        request.itemBarcodeField.click();
        request.itemBarcodeField.sendKeys(requestBarcode);
    }

    @Then("user verifies item already requested error msg")
    public void user_verifies_item_already_requested_error_msg() throws InterruptedException {
        WaitHelper.waitForVisibility(alert.toastMsg, 1000);
        assertTrue(alert.toastMsg.getText().toLowerCase().contains("item is already requested"));
        alert.closeToastMsg.click();
        wait.hardWait(3000);
    }

    @When("user creates a Pick List job")
    public void user_creates_a_Pick_List_job() throws InterruptedException {
        user_navigates_to_the_request_page();
        user_clicks_create_request_job_menu();
        user_selects_create_a_pick_list_option();
        adminSteps.user_selects_building_from_dropdown();
        shelvingSteps.user_clicks_submit();
        user_verifies_options_with_checkboxes_are_displayed();
        user_selects_request();
        user_clicks_create_pick_list();
        user_verifies_the_pick_list_is_created();
        user_clicks_the_alert_link();
        user_is_able_to_see_the_pick_list_dashboard();
    }

    @When("user clicks running job")
    public void user_clicks_running_job() {
        picklist.runningJob.click();
    }

    @And("user clicks on Request with status New")
    public void user_clicks_on_request_with_status_new() throws InterruptedException {
        helper.jSClick(request.newRequestsList.get(0));
        wait.hardWait(1000);
    }

    @And("user clicks on Request with status other than New")
    public void user_clicks_on_request_with_status_other_than_new() throws InterruptedException {
        helper.jSClick(request.inProgressRequestsList.get(0));
        wait.hardWait(1000);
    }

    @Then("user verifies an overlay slide is displayed")
    public void user_verifies_an_overlay_slide_is_displayed() {
        WaitHelper.waitForVisibility(request.overlay,2000);
        Helper.verifyElementDisplayed(request.overlay);
    }

    @And("user verifies Edit Request button is displayed")
    public void user_verifies_edit_request_button_is_displayed() {
        WaitHelper.waitForVisibility(request.editRequestBtn,2000);
        Helper.verifyElementDisplayed(request.editRequestBtn);
    }

    @And("user verifies Edit Request button is not displayed")
    public void user_verifies_edit_request_button_is_not_displayed() {
        Helper.verifyElementNotDisplayed(request.editRequestBtn);
    }

    @And("user verifies Cancel Request button is displayed")
    public void user_verifies_delete_request_button_is_displayed() {
        WaitHelper.waitForVisibility(request.cancelRequestBtn,2000);
        Helper.verifyElementDisplayed(request.cancelRequestBtn);
    }

    @When("user clicks Edit Request button")
    public void user_clicks_edit_request_button() {
        helper.jSClick(request.editRequestBtn);
    }

    @When("user updates the request")
    public void user_updates_the_request() {
        request.requestorNameField.click();
        request.requestorNameField.sendKeys(Keys.CONTROL + "a");
        request.requestorNameField.sendKeys(Keys.DELETE);
        request.requestorNameField.sendKeys("Updated Requestor Name");
    }

    @And("user verifies the request has been updated")
    public void user_verifies_the_request_has_been_updated() {
        WebElement requestorName = driver.findElement(By.xpath("(//p[@class='request-details-text'])[6]"));
        assertEquals("Updated Requestor Name", requestorName.getText());
    }

    @When("user clicks on the Request with status other than Requested")
    public void user_clicks_on_the_request_with_status_other_than_requested() {
        helper.jSClick(request.picklistStatusRequestsList.get(0));
    }

    @Then("user verifies the Requests table column names")
    public void user_verifies_the_requests_table_column_names(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedName = map.get("name");
            String actualName = request.requestsTableColumnNames.get(i).getText();
            Assert.assertTrue(expectedName.contains(actualName));
            i++;
        }
    }

    @Then("user verifies Request Details page is displayed")
    public void user_verifies_request_details_page_is_displayed() {
        WaitHelper.waitForVisibility(request.requestDetailsPageHeader,3000);
        assertTrue(request.requestDetailsPageHeader.getText().contains("Request Item"));
    }

    @And("user verifies Edit Request button is enabled")
    public void user_verifies_edit_request_button_is_enabled() {
        Helper.verifyElementEnabled(request.editRequestBtn);
    }

    @And("user verifies Edit Request button is disabled")
    public void user_verifies_edit_request_button_is_disabled() {
        String classAttribute = request.editRequestBtn.getAttribute("class");
        assertTrue(classAttribute.contains("disabled"));
    }

    @And("user clicks on Request")
    public void user_clicks_on_request() throws InterruptedException {
        WaitHelper.waitForClickability(request.requestList.get(0),2000);
        request.requestList.get(0).click();
        wait.hardWait(1000);
    }

    @And("user verifies Request information")
    public void user_verifies_request_information(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("label");
            String actualLabel = request.requestDetailsLabels.get(i).getText();
            assertEquals("Label verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @And("user saves Request ID")
    public void user_saves_request_ID() {
        requestID = request.requestDetailsPageHeader.getText().substring(14);
        System.out.println(requestID);
    }

    @And("user searches Request ID")
    public void user_searches_request_ID() {
        helper.jSClick(home.searchBar);
        home.searchBar.sendKeys(requestID);
        home.searchBar.sendKeys(Keys.ENTER);
    }

    @When("user clicks on Item Barcode in Request")
    public void user_clicks_on_item_barcode_in_request() {
        helper.jSClick(request.itemBarcodeLink);
    }

    @When("user clicks on Request ID")
    public void user_clicks_onRequestID() throws InterruptedException {
        helper.scrollIntoView(request.requestIDLink);
        request.requestIDLink.click();
    }
}
