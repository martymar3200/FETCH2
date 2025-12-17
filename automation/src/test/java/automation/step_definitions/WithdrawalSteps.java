package automation.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import automation.pages.WithdrawalPage;
import automation.utilities.ConfigurationReader;
import automation.utilities.Driver;
import automation.utilities.Helper;
import automation.utilities.WaitHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WithdrawalSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    WithdrawalPage withdrawal = new WithdrawalPage();
    VerificationSteps verificationSteps = new VerificationSteps();
    WaitHelper wait = new WaitHelper();


    @Given("user navigates to the Withdrawal Page")
    public void user_navigates_to_the_withdrawal_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "withdrawalURL"));
    }

    @When("user clicks Create Withdraw Job")
    public void user_clicks_create_withdraw_job() {
        WaitHelper.waitForVisibility(withdrawal.createWithdrawJob,3000);
        Helper.clickWithJS(withdrawal.createWithdrawJob);
    }

    @When("user clicks on Withdraw Job")
    public void user_clicks_on_withdraw_job() {
        withdrawal.createdJobs.get(0).click();
    }

    @When("user clicks on the created Withdraw Job")
    public void user_clicks_on_the_created_withdraw_job() {
        withdrawal.createdJobs.get(withdrawal.createdJobs.size() - 1).click();
    }

    @Then("user verifies a Withdraw Job is created")
    public void user_verifies_a_withdraw_job_is_created() {
        assertEquals("Withdraw Job is not created!", "Created", withdrawal.jobStatus.getText());
    }

    @Then("user verifies the Withdraw Job detail page is displayed")
    public void user_verifies_the_withdraw_job_detail_page_is_displayed() {
        Helper.verifyElementDisplayed(withdrawal.withdrawJobNumber);
        Helper.verifyElementEnabled(withdrawal.withdrawItemsBtn);
        assertEquals("Status of the Job is not created!", "Created", withdrawal.jobStatus.getText());
    }

    @Then("user verifies Withdrawal dashboard column names")
    public void user_verifies_withdrawal_dashboard_column_names(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedName = map.get("column");
            wait.hardWait(1000);
            String actualName = withdrawal.withdrawalColumns.get(i).getText();
            assertTrue(actualName.contains(expectedName));
            i++;
        }
    }

    @Then("user verifies Withdraw Job table column names")
    public void user_verifies_table_tab_names(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedName = map.get("column");
            wait.hardWait(1000);
            String actualName = withdrawal.withdrawalColumns.get(i).getText();
            assertEquals("Tab names verification failed",
                    expectedName, actualName);
            i++;
        }
    }

    @When("user verifies three dots menu options are displayed")
    public void user_verifies_Three_dots_menu_options_are_displayed() throws InterruptedException {
        assertEquals("Edit", withdrawal.threeDotMenuOptions.get(0).getText());
        assertEquals("Delete Job", withdrawal.threeDotMenuOptions.get(1).getText());
        assertEquals("View History", withdrawal.threeDotMenuOptions.get(2).getText());
        verificationSteps.user_clicks_three_dot_menu_next_to_job_number();
    }

    @When("user clicks three dots menu next to the Item Barcode in the table")
    public void user_clicks_three_dots_menu_next_to_the_item_barcode_in_the_table() throws InterruptedException {
        if(withdrawal.threeDotNextToItemBarcode.isDisplayed()) {
            WaitHelper.waitForVisibility(withdrawal.threeDotNextToItemBarcode, 4000);
            withdrawal.threeDotNextToItemBarcode.click();
            wait.hardWait(1000);
        }else {
            System.out.println("There are no items in the job");
        }
    }

    @Then("user verifies {string} option is displayed")
    public void user_verifies_option_is_displayed(String removeItem) {
        if(withdrawal.threeDotMenuOptions.get(0).isDisplayed()) {
            WaitHelper.waitForVisibility(withdrawal.threeDotMenuOptions.get(0), 1000);
            assertEquals(removeItem, withdrawal.threeDotMenuOptions.get(0).getText());
        } else {
            System.out.println("There are no items in the job");
        }
    }

    @When("user clicks Delete Job")
    public void user_clicks_delete_job() throws InterruptedException {
        withdrawal.threeDotMenuOptions.get(1).click();
        wait.hardWait(100);
    }

    @When("user confirms delete job action")
    public void user_confirms_delete_job_action() throws InterruptedException {
        Helper.clickWithJS(withdrawal.confirmDeleteWithdrawJob);
        wait.hardWait(100);
    }

    @Then("user verifies the assigned user has been updated")
    public void user_verifies_the_assigned_user_has_been_updated() {
        System.out.println(withdrawal.assignedUser.getText());
        assertEquals("Admin Istrator", withdrawal.assignedUser.getText());
    }

    @Then("user clicks Add Items")
    public void user_clicks_add_items() {
        WaitHelper.waitForVisibility(withdrawal.addItemsBtn,3000);
        withdrawal.addItemsBtn.click();
    }

    @And("user selects Manually Enter Barcode option")
    public void user_selects_manually_enter_barcode_option() {
        WaitHelper.waitForVisibility(withdrawal.manuallyEnterBarcodeOption,3000);
        withdrawal.manuallyEnterBarcodeOption.click();
    }

    @And("user selects Scan Items option")
    public void user_selects_scan_items_option() {
        WaitHelper.waitForVisibility(withdrawal.scanItemsOption,3000);
        withdrawal.scanItemsOption.click();
    }

    @And("user verifies scanned barcode is displayed")
    public void user_verifies_scanned_barcode_is_displayed() {
        WaitHelper.waitForVisibility(withdrawal.itemBarcode1,3000);
        assertEquals(PickListSteps.itemBarcode, withdrawal.itemBarcode1.getText());
    }

    @When("user enters Item Barcode with status IN")
    public void user_enters_item_barcode_with_status_in() {
        WaitHelper.waitForClickability(withdrawal.enterBarcodeField,3000);
        withdrawal.enterBarcodeField.click();
        withdrawal.enterBarcodeField.sendKeys(ShelvingSteps.itemBarcode);
    }

    @When("user navigates to the Withdraw job")
    public void user_navigates_to_the_withdraw_job() {
        driver.get("https://test.fetch.example.com/withdrawal/11");
    }

    @When("user navigates back to the Withdraw job")
    public void user_navigates_back_to_the_withdraw_job() {
        WaitHelper.waitForClickability(withdrawal.withdrawJobList.getLast(), 3000);
        withdrawal.withdrawJobList.getLast().click();
    }

    @When("user clicks Create Pick List job button")
    public void user_clicks_create_pick_list_job_button() {
        WaitHelper.waitForVisibility(withdrawal.createPickListJobFromWithdrawal,3000);
        withdrawal.createPickListJobFromWithdrawal.click();
    }

    @Then("user verifies the item status has changed to OUT")
    public void user_verifies_the_item_status_has_changed_to_out() {
        WaitHelper.waitForVisibility(withdrawal.withdrawJobColumnValues.getLast(),2000);
        String itemStatus = withdrawal.withdrawJobColumnValues.getLast().getText();
        assertEquals("Out", itemStatus);
    }

    @Then("user verifies the item status has changed to WITHDRAWN")
    public void user_verifies_the_item_status_has_changed_to_withdrawn() {
        WebElement withdrawnStatus = driver.findElement(By.cssSelector("td[class='q-td text-left bg-color-green-light']:nth-child(6)"));
        assertEquals("Withdrawn", withdrawnStatus.getText());
    }

    @When("user clicks Withdraw Items button")
    public void user_clicks_withdraw_items_button() {
        WaitHelper.waitForClickability(withdrawal.withdrawItemsBtn,2000);
        withdrawal.withdrawItemsBtn.click();
        WaitHelper.waitForClickability(withdrawal.confirmWithdrawItems,2000);
        withdrawal.confirmWithdrawItems.click();
    }

    @When("user verifies Withdraw&Print option is displayed")
    public void user_verifies_withdraw_and_print_option_is_displayed() {
        WaitHelper.waitForVisibility(withdrawal.withdrawAndPrint,2000);
        withdrawal.withdrawAndPrint.isDisplayed();
        WebElement cancel = driver.findElement(By.xpath("//button[.='Cancel']"));
        cancel.click();

    }

    @And("user verifies Items in Job column names")
    public void user_verifies_items_in_job_column_names(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedName = map.get("column");
            wait.hardWait(1000);
            String actualName = withdrawal.itemsInJobColumns.get(i).getText();
            assertTrue(actualName.contains(expectedName));
            i++;
        }
    }
}
