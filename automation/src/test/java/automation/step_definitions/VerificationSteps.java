package automation.step_definitions;

import automation.pages.AlertPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.*;
import automation.pages.AccessionPage;
import automation.pages.VerificationPage;
import automation.utilities.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class VerificationSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    VerificationPage verification = new VerificationPage();
    AccessionPage accession = new AccessionPage();
    AccessionSteps accessionSteps = new AccessionSteps();
    AlertPage alert = new AlertPage();
    Helper helper = new Helper();
    WaitHelper wait = new WaitHelper();
    HomeSteps homeSteps = new HomeSteps();
    ThreadLocalRandom random = ThreadLocalRandom.current();
    long random01 = random.nextLong(10000000000L, 100000000000L);
    long random02 = random.nextLong(10000000000L, 100000000000L);
    long scanned01 = random.nextLong(10000000000L, 100000000000L);
    long scanned02 = random.nextLong(10000000000L, 100000000000L);
    long scanned03 = random.nextLong(10000000000L, 100000000000L);
    static String verificationJobNumber = "";
    static String trayedItemBarcode = "";


    @Given("user navigates to the Verification Page")
    public void user_navigates_to_the_verification_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "verificationURL"));
        WaitHelper.waitForPageToLoad(90);
    }

    @When("user clicks on Verification Job for a Trayed Item")
    public void user_clicks_on_verification_job_for_a_trayed_item() throws InterruptedException {
        helper.scrollIntoView(verification.trayedVerificationJobs.get(verification.trayedVerificationJobs.size() - 1));
        wait.hardWait(1000);
        verification.trayedVerificationJobs.get(verification.trayedVerificationJobs.size() - 1).click();
    }

    @Then("Tray container view is displayed")
    public void tray_container_view_is_displayed() {
        assertEquals("Please Scan Tray", verification.scanTrayBox.getText());
    }

    @Then("user scans a Tray")
    public void user_scans_a_tray() throws InterruptedException {
        driver.findElement(By.tagName("body")).sendKeys("EL222222");
        wait.hardWait(1000);
    }

    @Then("user verifies the entered barcode is displayed")
    public void user_verifies_the_entered_barcode_is_displayed() {
        int index = verification.scannedVerificationItems.size();
        assertEquals("Entered Barcode is not displayed!", verification.scannedVerificationItems.get(index - 1), AccessionSteps.entered1);
    }

    @Then("verify the updated barcode is displayed")
    public void verify_the_updated_barcode_is_displayed() {
        int index = verification.scannedVerificationItems.size();
        assertEquals("Updated Barcode is not displayed!", verification.scannedVerificationItems.get(index - 1).getText(), "54321");
    }

    @Then("user deselects the edited barcode")
    public void user_deselects_the_edited_barcode() {
        int index = verification.scannedItemsCheckbox.size();
        verification.scannedItemsCheckbox.get(index - 1).click();
    }

    @Then("verify Next Tray button is activated")
    public void verify_next_tray_button_is_activated() {
        helper.scrollIntoView(verification.nextTrayBtn);
        assertTrue(verification.nextTrayBtn.isEnabled());
    }

    @Then("user clicks Next Tray button")
    public void user_clicks_next_tray_button() {
        helper.scrollIntoView(verification.nextTrayBtn);
        helper.jSClick(verification.nextTrayBtn);
    }

    @Then("user clicks on new tray on the modal")
    public void user_clicks_on_new_tray_on_the_modal() {
        WaitHelper.waitForClickability(verification.newTrays.get(0),2000);
        verification.newTrays.get(0).click();
    }

    @When("user clicks on Verification Job for a Non-Trayed Item")
    public void user_clicks_on_verification_Job_for_a_non_trayed_item() {
        verification.nonTrayedVerificationJob.click();
    }

    @Then("user verifies non-trayed items container view is displayed")
    public void user_verifies_non_trayed_items_container_view_is_displayed() {
        assertEquals("Please Scan Non Tray", verification.scanTrayBox.getText());
    }

    @When("user verifies second barcode")
    public void user_verifies_second_barcode() throws InterruptedException {
        WaitHelper.waitForClickability(verification.enterBarcodeBtn,9000);
        helper.scrollIntoView(verification.enterBarcodeBtn);
        helper.jSClick(verification.enterBarcodeBtn);
        WaitHelper.waitForClickability(verification.enterBarcodeField, 1000);
        verification.enterBarcodeField.sendKeys(verification.scannedVerificationItems.get(0).getText());
        verification.submitBtn.click();
        wait.hardWait(2000);
    }

    @When("user verifies the barcode")
    public void user_verifies_the_barcode() throws InterruptedException {
        homeSteps.user_switches_off_barcode_scan();
        driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL, Keys.HOME);
        helper.scrollIntoView(verification.enterBarcodeBtn);
        helper.jSClick(verification.enterBarcodeBtn);
        verification.enterBarcodeField.sendKeys(verification.scannedVerificationItems.get(0).getText());
        verification.submitBtn.click();
        if (!verification.verifiedCheckMark.getText().equals("Item Verified")) {
            verification.enterBarcodeBtn.click();
            verification.enterBarcodeField.sendKeys(verification.scannedVerificationItems.get(0).getText());
            verification.submitBtn.click();
        } else {
            System.out.println(verification.verifiedCheckMark.getText().equals("Item Verified"));
        }
        wait.hardWait(2000);
    }

    @When("user verifies item barcode")
    public void user_verifies_item_barcode() throws InterruptedException {
        homeSteps.user_switches_off_barcode_scan();
        WaitHelper.waitForClickability(verification.enterBarcodeBtn, 3000);
        verification.enterBarcodeBtn.click();
        verification.enterBarcodeField.sendKeys(verification.scannedVerificationItems.get(0).getText());
        verification.submitBtn.click();
        wait.hardWait(2000);
        WaitHelper.waitForClickability(verification.enterBarcodeBtn, 5000);
        verification.enterBarcodeBtn.click();
        verification.enterBarcodeField.sendKeys(verification.scannedVerificationItems.get(0).getText());
        WaitHelper.waitForClickability(verification.submitBtn,5000);
        verification.submitBtn.click();
        wait.hardWait(2000);
    }

    @When("user verifies the barcode by scanning")
    public void user_verifies_the_barcode_by_scanning() throws InterruptedException {
        WaitHelper.waitForVisibility(verification.scannedVerificationItems.get(0),7000);
        driver.findElement(By.tagName("body")).sendKeys(verification.scannedVerificationItems.get(0).getText());
        wait.hardWait(2000);
    }

    @Then("user verifies Complete Job button is activated")
    public void user_verifies_complete_job_button_is_activated() {
        assertTrue(verification.completeJob.isEnabled());
    }

    @When("user navigates to the verification job link")
    public void user_navigates_to_the_verification_job_link() {
        verification.verificationJobsList.getLast().click();
    }

    @And("user verifies barcode")
    public void user_verifies_barcode() throws InterruptedException {
        wait.hardWait(2000);
        driver.findElement(By.tagName("body")).sendKeys("!000000988989!");
    }

    @When("user clicks most recent Verification Job for a Trayed Item")
    public void user_clicks_most_recent_verification_job_for_a_trayed_item() throws InterruptedException {
        helper.scrollIntoView(verification.nonTrayedJobList.getLast());
        helper.jSClick(verification.trayedJobList.get(verification.trayedJobList.size() - 1));
        wait.hardWait(1000);
    }

    @When("user clicks most recent Verification Job for a Non-Trayed Item")
    public void user_clicks_most_recent_verification_job_for_a_non_trayed_item() throws InterruptedException {
        helper.scrollIntoView(verification.nonTrayedJobList.getLast());
        helper.jSClick(verification.nonTrayedJobList.get(verification.nonTrayedJobList.size() - 1));
        wait.hardWait(1000);
    }

    @When("user clicks three dot menu next to Job Number")
    public void user_clicks_three_dot_menu_next_to_job_number() throws InterruptedException {
        WaitHelper.waitForVisibility(verification.threeDot,3000);
        verification.threeDot.click();
        wait.hardWait(1000);
    }

    @When("user edits Container Size field")
    public void user_edits_container_size_field() throws InterruptedException {
        WaitHelper.waitForClickability(verification.editContainerSizeField,5000);
        helper.scrollIntoView(verification.editContainerSizeField);
        verification.editContainerSizeField.click();
        verification.editContainerSizeField.sendKeys(Keys.CONTROL + "a");
        verification.editContainerSizeField.sendKeys(Keys.DELETE);
        wait.hardWait(600);
        WaitHelper.waitForVisibility(verification.editFieldOptions.get(6), 2000);
        verification.editFieldOptions.get(6).click();
        wait.hardWait(1000);
    }

    @When("user edits Media Type field")
    public void user_edits_media_type_field() throws InterruptedException {
        wait.handleStaleElement(By.cssSelector("[aria-label='mediaTypeSelect']"),5,3000);
        verification.editMediaTypeField.click();
        WaitHelper.waitForVisibility(verification.editFieldOptions.get(4),2000);
        verification.editFieldOptions.get(4).click();
    }

    @Then("user enters the barcode and clicks Submit button")
    public void user_enters_the_barcode_and_clicks_submit_button() {
        WaitHelper.waitForClickability(verification.enterBarcodeField,2000);
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys(Long.toString(random01));
        helper.jSClick(accession.submitBtn);
    }

    @Then("user confirms they want to add a new item to the job")
    public void user_confirms_they_want_to_add_a_new_item_to_the_job() throws InterruptedException {
        WaitHelper.waitForClickability(verification.addNewItem, 2000);
        verification.addNewItem.click();
        wait.hardWait(3000);
    }

    @Then("user edits the barcode and clicks Submit button")
    public void user_edits_the_barcode_and_clicks_submit_button() {
        accession.enterBarcodeField.sendKeys(Keys.CONTROL + "a");
        accession.enterBarcodeField.sendKeys(Keys.DELETE);
        accession.enterBarcodeField.sendKeys(Long.toString(random02));
        accession.submitBtn.click();
    }

    @Then("verify the edited barcode is displayed")
    public void verify_the_edited_barcode_is_displayed() {
        WaitHelper.waitForVisibility(verification.scannedItemList.get(verification.scannedItemList.size() - 1), 2000);
        assertEquals("Edited Barcode is not displayed!", Long.toString(random02), verification.scannedItemList.get(verification.scannedItemList.size() - 1).getText());
    }

    @And("user scans item barcode")
    public void user_scans_item_barcode() {
        WaitHelper.waitForVisibility(verification.containerSizeValue,3000);
        driver.findElement(By.tagName("body")).sendKeys("" + scanned01 + "");
    }

    @And("user scans the barcode of the item")
    public void user_scans_the_barcode_of_the_item() throws InterruptedException {
        wait.hardWait(2000);
        driver.findElement(By.tagName("body")).sendKeys("" + scanned02 + "");
        wait.hardWait(2000);
    }

    @And("user scans the item barcode")
    public void user_scans_the_item_barcode() throws InterruptedException {
        wait.hardWait(1000);
        driver.findElement(By.tagName("body")).sendKeys("" + scanned03 + "");
    }

    @When("user scans Tray Barcode")
    public void user_scans_tray_barcode() throws InterruptedException {
        wait.hardWait(1000);
        driver.findElement(By.tagName("body")).sendKeys(AccessionSteps.generatedTray);
    }

    @When("user scans second Tray Barcode")
    public void user_scans_second_tray_barcode() throws InterruptedException {
        wait.hardWait(1000);
        driver.findElement(By.tagName("body")).sendKeys(AccessionSteps.generatedTray2);
    }

    @And("user saves Verification Job number")
    public void user_saves_verification_job_number() {
        WebElement vJobNumber = driver.findElement(By.xpath("//h1[@class='text-h4 text-bold']"));
        verificationJobNumber = vJobNumber.getText().substring(5).trim();
        System.out.println("Verification Job Number: " + verificationJobNumber);
    }

    @And("user saves Trayed Item barcode")
    public void user_saves_trayed_item_barcode() {
        trayedItemBarcode = verification.scannedVerificationItems.get(0).getText().substring(0, 11);
        System.out.println("Trayed Item barcode: " + trayedItemBarcode);
    }

    @Then("user clicks a completed Accession Job")
    public void user_clicks_a_completed_accession_job() {
        for (WebElement job : verification.verificationJobsList) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", job);
            if (job.getText().equals(AccessionSteps.accessionJobNumber)) {
                job.click();
            }
        }
    }

    @And("user completes a Non-Tray Verification Job")
    public void user_creates_a_non_tray_verification_job() throws InterruptedException {
        user_navigates_to_the_verification_page();
        user_navigates_to_the_verification_job();
        user_saves_verification_job_number();
        user_verifies_the_barcode();
    }

    @And("user completes a Tray Verification Job")
    public void user_creates_a_Tray_Verification_Job() throws InterruptedException {
        user_navigates_to_the_verification_job();
        wait.hardWait(2000);
        user_saves_verification_job_number();
        wait.hardWait(1000);
        user_scans_tray_barcode();
        wait.hardWait(1000);
        user_verifies_the_barcode_by_scanning();
        wait.hardWait(2000);
    }

    @Then("user completes a Verification Job with a specific Owner and Size Class")
    public void user_creates_a_verification_job_with_a_specific_owner_and_size_class() throws InterruptedException {
        user_navigates_to_the_verification_page();
        user_navigates_to_the_verification_job();
        user_saves_verification_job_number();
        wait.hardWait(1000);
        driver.findElement(By.tagName("body")).sendKeys(AccessionSteps.generatedTrayForBHSizeClass);
        user_verifies_the_barcode_by_scanning();
        accessionSteps.user_clicks_complete_job_button();
        accessionSteps.user_clicks_complete();
    }

    @When("user navigates to the verification job")
    public void user_navigates_to_the_verification_job() {

      List<WebElement> overlays = driver.findElements(By.cssSelector("[class='overlay']"));
      for(WebElement overlay: overlays) {
          if(overlay.isDisplayed()) {
              System.out.println("Overlay is visible and possibly blocking the click");
          }
      }
      verification.verificationJobsList.getLast().click();
    }

    @When("user verifies that completed jobs are not displayed")
    public void user_verifies_that_completed_jobs_are_not_displayed() {
        for (WebElement status : verification.jobStatuses) {
            assertNotEquals("Completed", status.getText());
        }
    }

    @And("user confirms verification job cancellation")
    public void user_confirms_verification_job_cancellation() throws InterruptedException {
        helper.jSClick(verification.confirmVerificationJobCancellation);
        wait.hardWait(2000);
        alert.closeToastMsg.click();
    }


}
