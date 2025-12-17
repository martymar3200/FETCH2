package automation.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import automation.pages.AccessionPage;
import automation.pages.AlertPage;
import automation.utilities.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlertSteps {


    AlertPage alert = new AlertPage();
    Helper helper = new Helper();
    AccessionPage accession = new AccessionPage();
    WaitHelper wait = new WaitHelper();


    @Given("user navigates to the testing link")
    public void user_navigates_to_the_testing_link() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "alertTestURL"));
    }

    @When("user clicks on the Show Generic Alert button")
    public void user_clicks_on_the_show_generic_alert_button() throws InterruptedException {
        helper.scrollIntoView(alert.genericAlert);
        alert.genericAlert.click();
        wait.hardWait(100);
    }

    @Then("user verifies UI alert on top of the screen is visible")
    public void user_verifies_ui_alert_on_top_of_the_screen_is_visible() {
        WaitHelper.waitForVisibility(alert.alertMsg, 3000);
        String msg = alert.alertMsg.getText();
        String expectedMsg = "This is a user generated error message\n" +
                "close";
        assertEquals(expectedMsg, msg);
    }

    @Then("user is able to cancel alert")
    public void user_is_able_to_cancel_alert() {
        helper.jSClick(alert.cancelGenAlert);
    }

    @When("user clicks on the Show Persistent Alert button")
    public void user_clicks_on_the_Show_Persistent_Alert_button() throws InterruptedException {
        helper.jSClick(alert.persistentAlert);
        wait.hardWait(100);
    }

    @Then("user verifies alert popup is visible")
    public void user_verifies_alert_popup_is_visible() {
        WaitHelper.waitForVisibility(alert.audioAlertMsg, 3000);
        String msg = alert.audioAlertMsg.getText();
        String expectedMsg = "This is a user generated error message with audio";
        assertEquals(expectedMsg, msg);
    }

    @Then("user is able to click cancel button")
    public void user_is_able_to_click_cancel_button() {
        alert.cancelPersistentAlert.click();
    }

    @When("user navigates to Accession Job for a Non-Trayed Item")
    public void user_navigates_to_accession_job_for_a_non_trayed_item() {
        alert.nonTrayedAccessionJob.click();
    }

    @When("user enters {string} barcode and clicks Submit button")
    public void user_enters_barcode_and_clicks_submit_button(String string) {
        accession.enterBarcodeField.sendKeys(string);
        accession.submitBtn.click();
    }

    @Then("user verifies {string} alert msg")
    public void user_verifies_alert_msg(String string) throws InterruptedException {
        WaitHelper.waitForVisibility(alert.toastMsg, 3000);
        assertEquals(string, alert.toastMsg.getText());
        WaitHelper.waitForClickability(alert.closeToastMsg,3000);
        alert.closeToastMsg.click();
        wait.hardWait(2000);
    }

    @Then("user verifies alert msg contains {string}")
    public void user_verifies_alert_msg_contains(String string) throws InterruptedException {
        WaitHelper.waitForVisibility(alert.toastMsg, 3000);
        assertTrue(alert.toastMsg.getText().contains(string));
        WaitHelper.waitForClickability(alert.closeToastMsg,3000);
        alert.closeToastMsg.click();
        wait.hardWait(2000);
    }

    @Then("user verifies alert message is displayed")
    public void user_verifies_alert_message_is_displayed() throws InterruptedException {
        WaitHelper.waitForVisibility(alert.toastMsg, 3000);
        System.out.println("Error message detail: " + alert.toastMsg.getText());
        Helper.verifyElementDisplayed(alert.toastMsg);
        WaitHelper.waitForClickability(alert.closeToastMsg,3000);
        alert.closeToastMsg.click();
        wait.hardWait(2000);
    }

    @When("user clicks Delete")
    public void user_clicks_delete() {
        WaitHelper.waitForClickability(accession.deleteBtn, 3000);
        helper.jSClick(accession.deleteBtn);
    }

    @When("user clicks Confirm")
    public void user_clicks_confirm()  {
        WaitHelper.waitForClickability(accession.confirmDelete, 2000);
        helper.jSClick(accession.confirmDelete);
    }

    @When("user clicks Delete Item")
    public void user_delete_item() {
        WaitHelper.waitForClickability(accession.deleteItem, 2000);
        helper.jSClick(accession.deleteItem);
    }

    @Then("user verifies {string}")
    public void user_verifies(String string) {
        WaitHelper.waitForVisibility(alert.completedAndMovedForVerificationMsg, 5000);
        assertEquals(string, alert.completedAndMovedForVerificationMsg.getText());
        WaitHelper.waitForClickability(alert.closeToastMsg,3000);
        alert.closeToastMsg.click();
    }

    @Then("user verifies {string} msg")
    public void user_verifies_msg(String string) {
        WaitHelper.waitForVisibility(alert.theJobHasBeenCompleted, 1000);
        assertEquals(string, alert.theJobHasBeenCompleted.getText());
        WaitHelper.waitForClickability(alert.closeToastMsg,3000);
        alert.closeToastMsg.click();
    }

    @Then("user verifies {string} notification")
    public void user_verifies_notification(String string) {
        WaitHelper.waitForVisibility(alert.jobCreated, 1000);
        assertEquals(string, alert.jobCreated.getText());
        WaitHelper.waitForClickability(alert.closeToastMsg,3000);
        alert.closeToastMsg.click();
    }

    @Then("user verifies {string} message")
    public void user_verifies_message(String string) {
        WaitHelper.waitForVisibility(alert.theContainerHasBeenUpdated, 1000);
        assertEquals(string, alert.theContainerHasBeenUpdated.getText());
        WaitHelper.waitForClickability(alert.closeToastMsg,3000);
        alert.closeToastMsg.click();
    }



}


