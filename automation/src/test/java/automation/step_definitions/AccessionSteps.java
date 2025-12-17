package automation.step_definitions;


import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import automation.pages.AlertPage;
import automation.pages.VerificationPage;
import automation.utilities.ConfigurationReader;
import org.openqa.selenium.support.ui.WebDriverWait;
import automation.pages.AccessionPage;
import automation.utilities.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class AccessionSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    AccessionPage accession = new AccessionPage();
    VerificationPage verification = new VerificationPage();
    AlertPage alert = new AlertPage();
    Helper helper = new Helper();
    WaitHelper wait = new WaitHelper();
    HomeSteps homeSteps = new HomeSteps();
    AlertSteps alertSteps = new AlertSteps();
    static ThreadLocalRandom random = ThreadLocalRandom.current();
    static long scanned1 = random.nextLong(10000000000L, 100000000000L);
    static long scanned2 = random.nextLong(10000000000L, 100000000000L);
    static long scanned3 = random.nextLong(10000000000L, 100000000000L);
    static long scanned4 = random.nextLong(10000000000L, 100000000000L);
    static long entered1 = random.nextLong(10000000000L, 100000000000L);
    static long entered2 = random.nextLong(10000000000L, 100000000000L);
    static long entered3 = random.nextLong(10000000000L, 100000000000L);
    static long entered4 = random.nextLong(10000000000L, 100000000000L);
    static long entered5 = random.nextLong(10000000000L, 100000000000L);
    static long edited1 = random.nextLong(10000000000L, 100000000000L);
    static long itemBarcode = random.nextLong(10000000000L, 100000000000L);
    static String editedTrayBarcode = "";
    static String generatedTray = "";
    static String generatedTray2 = "";
    static String generatedTrayForBHSizeClass = "";
    static String accessionJobNumber = "";


    @Given("user navigates to the Accession Page")
    public void user_navigates_to_the_accession_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "accessionURL"));
        WaitHelper.waitForPageToLoad(900);
    }

    @When("user clicks Start Accession button")
    public void user_clicks_start_accession_button() {
        WaitHelper.waitForClickability(accession.startAccessionBtn, 1000);
        accession.startAccessionBtn.click();
    }

    @When("user selects Trayed Accession")
    public void user_selects_trayed_accession() {
        helper.jSClick(accession.trayedAccession);
    }

    @And("user selects Non-Tray Accession")
    public void user_selects_non_tray_accession() {
        helper.jSClick(accession.nonTrayAccession);
    }

    @And("user saves Accession Job number")
    public void user_saves_accession_job_number() {
        WebElement vJobNumber = driver.findElement(By.cssSelector("[class='text-h4 text-bold']"));
        accessionJobNumber = vJobNumber.getText().substring(5).trim();
        System.out.println("Accession Job Number: " + accessionJobNumber);
    }

    @Then("user verifies required and optional fields on Start New Accession modal")
    public void user_verifies_required_and_optional_fields_on_start_new_accession_modal(io.cucumber.datatable.DataTable dataTable) {
        WaitHelper.waitForVisibility(accession.newAccessionFields.get(0), 10);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("fieldname");
            String actualLabel = accession.newAccessionFields.get(i).getText();
            assertEquals("Fieldname verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @When("user selects all required fields")
    public void user_selects_all_required_fields() throws InterruptedException {
        WaitHelper.waitForClickability(accession.ownerField, 100);
        accession.ownerField.click();
        wait.hardWait(1000);
        accession.ownerFieldOptions.get(2).click();
        wait.hardWait(100);
    }

    @When("user selects all fields")
    public void user_selects_all_fields() throws InterruptedException {
        WaitHelper.waitForClickability(accession.ownerField, 1000);
        accession.ownerField.click();
        WaitHelper.waitForVisibility(accession.ownerFieldOptions.get(2), 1000);
        accession.ownerFieldOptions.get(2).click();
        wait.hardWait(1000);
        accession.containerSizeField.click();
        WaitHelper.waitForVisibility(accession.containerOptions.get(5), 4000);
        accession.containerOptions.get(5).click();
        wait.hardWait(1000);
        accession.mediaTypeField.click();
        WaitHelper.waitForVisibility(accession.mediaOptions.get(1), 2000);
        accession.mediaOptions.get(1).click();
    }

    @Then("Owner dropdown is clickable")
    public void owner_dropdown_is_clickable() {
        Helper.isClickable(accession.ownerField);
    }

    @Then("Container Size dropdown is clickable")
    public void container_size_dropdown_is_clickable() {
        Helper.isClickable(accession.containerSizeField);
    }

    @Then("Media Type field is clickable")
    public void media_type_field_is_clickable() {
        Helper.isClickable(accession.mediaTypeField);
    }

    @Then("submit button is disabled")
    public void submit_button_is_disabled() {
        Helper.verifyElementDisabled(accession.submit);
    }

    @Then("submit button is enabled")
    public void submit_button_is_enabled() {
        Helper.verifyElementEnabled(accession.submit);
    }

    @When("back button is clickable")
    public void back_button_is_clickable() {
        Helper.isClickable(accession.backBtn);
    }

    @When("cancel button is enabled")
    public void cancel_button_is_enabled() {
        Helper.verifyElementEnabled(accession.cancelBtn);
    }

    @And("user clicks cancel button")
    public void user_clicks_cancel_button() {
        helper.jSClick(accession.cancelBtn);
    }

    @When("user clicks submit button")
    public void user_clicks_submit_button() throws InterruptedException {
        helper.jSClick(accession.submit);
        wait.hardWait(1000);
    }

    @When("user submits the change")
    public void user_submits_the_change() throws InterruptedException {
        helper.jSClick(accession.submitBtn);
        wait.hardWait(2000);
    }

    @When("user scans Barcode")
    public void user_scans_barcode() throws InterruptedException {
        wait.hardWait(2000);
        generatedTray = "DL" + Helper.generateBarcodeNumber();
        driver.findElement(By.tagName("body")).sendKeys(generatedTray);
        wait.hardWait(2000);
    }

    @When("user scans another Barcode")
    public void user_scans_another_barcode() throws InterruptedException {
        wait.hardWait(2000);
        generatedTray2 = "DH" + Helper.generateBarcodeNumber();
        driver.findElement(By.tagName("body")).sendKeys(generatedTray2);
    }

    @When("user is able to edit Container Size and Media Type fields of the panel")
    public void user_is_able_to_edit_container_size_and_media_type_fields_of_the_panel() {
        helper.jSClick(accession.editContainerSize);
        helper.jSClick(accession.csField);
        accession.containerOptions.get(3).click();
        helper.jSClick(accession.editMediaType);
        accession.mtField.click();
        accession.mediaOptions.get(1).click();
    }

    @When("user is able to cancel edits")
    public void user_is_able_to_cancel_edits() {
        helper.jSClick(accession.cancelEdit);
    }

    @When("user is able to save edits")
    public void user_is_able_to_save_edits() {
        helper.jSClick(accession.saveEdits);
    }

    @When("Add Item button is enabled and clickable")
    public void add_item_button_is_enabled_and_clickable() {
        Helper.verifyElementEnabled(accession.addItem);
        Helper.isClickable(accession.addItem);
    }

    @When("Pause Job button is enabled and clickable")
    public void pause_job_button_is_enabled_and_clickable() {
        Helper.verifyElementEnabled(accession.pauseJob);
        Helper.isClickable(accession.pauseJob);
    }

    @When("Complete Job button is enabled and clickable")
    public void complete_job_button_is_enabled_and_clickable() {
        Helper.verifyElementEnabled(accession.completeJob);
        Helper.isClickable(accession.completeJob);
    }

    @When("user checks an Item")
    public void user_checks_an_item() {
        accession.scanItemCheckbox.get(1).click();
    }

    @Then("Delete button is enabled")
    public void delete_button_is_enabled() {
        Helper.verifyElementEnabled(accession.delete);
    }

    @When("user clicks Pause Job button")
    public void user_clicks_pause_job_button() {
        driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL, Keys.HOME);
        helper.jSClick(accession.pauseJob);
    }

    @Then("Add Item, Delete and Complete Job buttons are disabled")
    public void add_item_delete_and_complete_job_buttons_are_disabled() {
        System.out.println("Add Item button is disabled: " + accession.addItem.getDomAttribute("disabled"));
        System.out.println("Delete button is disabled: " + accession.delete.getDomAttribute("disabled"));
        WebElement completeBtn = driver.findElement(By.cssSelector(":nth-child(2) > .text-positive"));
        System.out.println("Complete Job button is disabled: " + completeBtn.getDomAttribute("disabled"));
    }

    @When("user types {string} in the Owner dropdown search field")
    public void user_types_in_the_owner_dropdown_search_field() throws InterruptedException {
        String owner = "geog";
        accession.ownerField.click();
        accession.ownerField.sendKeys(owner);
        wait.hardWait(1000);
    }

    @Then("Owner dropdown should display options related to search query")
    public void Owner_dropdown_should_display_options_related_to_search_query() {
        for (WebElement option : accession.ownerFieldOptions) {
            assertTrue(option.getText().toLowerCase().contains("geog"));
        }
    }

    @Then("user selects an option from the Owner dropdown")
    public void user_selects_an_option_from_the_owner_dropdown() {
        accession.ownerField.sendKeys(Keys.ARROW_DOWN);
        accession.ownerField.sendKeys(Keys.ENTER);
    }

    @Then("user types {string} in the Media Type dropdown search field")
    public void user_types_in_the_media_type_dropdown_search_field() throws InterruptedException {
        String mediaType = "boo";
        accession.mediaTypeField.click();
        accession.mediaTypeField.sendKeys(mediaType);
        wait.hardWait(2000);
    }

    @Then("Media Type dropdown should display options related to search query")
    public void media_type_dropdown_should_display_options_related_to_search_query() {
        for (WebElement option : accession.mediaOptions) {
            assertTrue(option.getText().toLowerCase().contains("boo"));
        }
    }

    @Then("user selects an option from the Media Type dropdown")
    public void user_selects_an_option_from_the_media_type_dropdown() {
        accession.mediaTypeField.sendKeys(Keys.ARROW_DOWN);
        accession.mediaTypeField.sendKeys(Keys.ENTER);
    }

    @Then("user selects an option from the Container Size dropdown")
    public void user_selects_an_option_from_the_container_size_dropdown() throws InterruptedException {
        accession.containerSizeField.sendKeys(Keys.ARROW_DOWN);
        accession.containerSizeField.sendKeys(Keys.ENTER);
        wait.hardWait(2000);
    }

    @Then("when user clicks Delete button")
    public void when_user_clicks_delete_button() {
        helper.jSClick(accession.delete);
    }

    @Then("verify a modal confirming delete action appears")
    public void verify_a_modal_confirming_delete_action_appears() throws InterruptedException {
        WaitHelper.waitForVisibility(accession.modal, 1000);
        assertEquals("Are you sure you want to delete selected items?", accession.modal.getText());
        accession.cancelModal.click();
        wait.hardWait(1000);
    }

    @When("user clicks Complete Job button")
    public void user_clicks_complete_job_button() throws InterruptedException {
        WaitHelper.waitForVisibility(accession.completeJob, 3000);
        helper.jSClick(accession.completeJob);
        wait.hardWait(2000);
    }

    @Then("verify a modal confirming complete job action appears")
    public void verify_a_modal_confirming_complete_job_action_appears() {
        WaitHelper.waitForVisibility(accession.modal, 3000);
        assertEquals("Are you sure you want to complete the job?", accession.modal.getText());
    }

    @Then("verify that Enter Barcode button is enabled")
    public void verify_that_enter_barcode_button_is_enabled() {
        Helper.verifyElementEnabled(accession.enterBarcodeBtn);
    }

    @Then("user clicks Enter Barcode button")
    public void user_clicks_enter_barcode_button() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0,0)");
        WaitHelper.waitForClickability(accession.enterBarcodeBtn, 3000);
        helper.jSClick(accession.enterBarcodeBtn);
        wait.hardWait(2000);
    }

    @Then("verify a modal with manual barcode entry is displayed")
    public void verify_a_modal_with_manual_barcode_entry_is_displayed() {
        WaitHelper.waitForVisibility(accession.popupModal, 1000);
        accession.popupModal.isDisplayed();
    }

    @Then("user enters barcode and clicks Submit button")
    public void user_enters_barcode_and_clicks_submit_button() throws InterruptedException {
        WaitHelper.waitForVisibility(accession.enterBarcodeField,3000);
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys(Long.toString(entered1));
        helper.jSClick(accession.submitBtn);
        wait.hardWait(2000);
    }

    @Then("user enters second barcode and clicks Submit button")
    public void user_enters_second_barcode_and_clicks_submit_button() throws InterruptedException {
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys(Long.toString(entered2));
        helper.jSClick(accession.submitBtn);
        wait.hardWait(1000);
    }

    @Then("user enters another barcode and clicks Submit button")
    public void user_enters_another_barcode_and_clicks_submit_button() throws InterruptedException {
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys(Long.toString(entered3));
        WaitHelper.waitForVisibility(accession.submitBtn, 3000);
        accession.submitBtn.click();
        wait.hardWait(1000);
    }

    @Then("user enters non-tray item barcode and clicks Submit button")
    public void user_enters_non_tray_item_barcode_and_clicks_submit_button() throws InterruptedException {
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys(Long.toString(entered5));
        WaitHelper.waitForVisibility(accession.submitBtn, 3000);
        accession.submitBtn.click();
        wait.hardWait(1000);
    }

    @Then("user enters item barcode and clicks Submit button")
    public void user_enters_item_barcode_and_clicks_submit_button() throws InterruptedException {
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys(Long.toString(entered4));
        helper.jSClick(accession.submitBtn);
        wait.hardWait(1000);
    }

    @Then("user enters item barcode")
    public void user_enters_item_barcode() throws InterruptedException {
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys(Long.toString(itemBarcode));
        helper.jSClick(accession.submitBtn);
        wait.hardWait(1000);
    }

    @When("user selects one of the barcodes in the table")
    public void user_selects_one_of_the_barcodes_in_the_table() {
        Helper.clickWithJS(accession.scannedItemCheckbox);
    }

    @When("user selects another barcode in the table")
    public void user_selects_another_barcode_in_the_table() {
        Helper.clickWithJS(accession.scannedItemCheckbox);
    }

    @Then("user verifies Enter Barcode button is changed to Edit Barcode")
    public void user_verifies_enter_barcode_button_is_changed_to_edit_barcode() {
        assertTrue(accession.enterBarcodeBtn.getText().contains("Edit Barcode"));
    }

    @Then("user clicks Edit Barcode button")
    public void user_clicks_edit_barcode_button() {
        WaitHelper.waitForClickability(accession.editBarcodeBtn,3000);
        Helper.clickWithJS(accession.editBarcodeBtn);
    }

    @Then("verify new modal allowing to edit the barcode is displayed")
    public void verify_new_modal_allowing_to_edit_the_barcode_is_displayed() {
        WaitHelper.waitForVisibility(accession.popupModal, 1000);
        accession.popupModal.isDisplayed();
    }

    @Then("user edits the barcode and clicks submit button")
    public void user_edits_the_barcode_and_clicks_submit_button() throws InterruptedException {
        accession.enterBarcodeField.sendKeys(Keys.CONTROL + "a");
        accession.enterBarcodeField.sendKeys(Keys.DELETE);
        accession.enterBarcodeField.sendKeys(Long.toString(edited1));
        accession.submitBtn.click();
        wait.hardWait(3000);
    }

    @Then("user verifies that edited barcode is displayed")
    public void user_verifies_that_edited_barcode_is_displayed() throws InterruptedException {
        wait.hardWait(1000);
        WaitHelper.waitForVisibility(accession.scannedItemList.get(0), 5000);
        assertEquals("Edited Barcode is not displayed!", Long.toString(edited1), accession.scannedItemList.get(0).getText());
    }

    @Then("verify Add Tray button is activated")
    public void verify_add_tray_button_is_activated() {
        WaitHelper.waitForVisibility(accession.addTrayBtn, 2000);
        assertTrue(accession.addTrayBtn.isEnabled());
    }

    @Then("user clicks Add Tray button")
    public void user_clicks_add_tray_button() {
//        driver.manage().window().fullscreen();
        WaitHelper.waitForVisibility(accession.addTrayBtn, 4000);
        helper.jSClick(accession.addTrayBtn);
    }

    @Then("verify new modal Select Tray is displayed")
    public void verify_new_modal_select_tray_is_displayed() {
        WaitHelper.waitForVisibility(accession.popupModal, 100);
        assertTrue(accession.popupModal.isDisplayed());
    }

    @Then("user clicks add tray on the modal")
    public void user_clicks_add_tray_on_the_modal() {
        WaitHelper.waitForClickability(accession.addTrayModalBtn, 100);
        accession.addTrayModalBtn.click();
    }

    @Then("verify {string} alert message is displayed")
    public void verify_alert_message_is_displayed(String alertMessage) {
        assertTrue(accession.alertMsg.getText().contains(alertMessage));
    }

    @Then("the container is cleared out so a new tray can be scanned")
    public void the_container_is_cleared_out_so_a_new_tray_can_be_scanned() {
        assertEquals("Please Scan Tray", accession.scanTrayField.getText());
    }

    @When("user clicks Complete&Print button")
    public void user_clicks_complete_print_button() {
        helper.jSClick(accession.completeAndprint);
    }

    @Then("user is able to see a print window with a batch report")
    public void user_is_able_to_see_a_print_window_with_a_batch_report() throws InterruptedException {
        WebDriverWait wait1 = new WebDriverWait(driver, Duration.ofSeconds(1000));
        wait1.until(ExpectedConditions.numberOfWindowsToBe(2));
        if (driver.getWindowHandles().size() > 1) {
            System.out.println("Print window is displayed");
        } else {
            System.out.println("Print window is not displayed");
        }
        wait.hardWait(1000);
    }

    @And("user selects Media Type")
    public void user_selects_media_type() throws InterruptedException {
        accession.mediaTypeField.click();
        wait.hardWait(1000);
        accession.mediaOptions.get(1).click();
    }

    @And("user enters barcode by scanning")
    public void user_enters_barcode_by_scanning() throws InterruptedException {
        wait.hardWait(3000);
        driver.findElement(By.tagName("body")).sendKeys("" + scanned1 + "");
        wait.hardWait(2000);
    }

    @And("user enters a new barcode by scanning")
    public void user_enters_a_new_barcode_by_scanning() throws InterruptedException {
        wait.hardWait(1000);
        driver.findElement(By.tagName("body")).sendKeys("" + scanned3 + "");
        wait.hardWait(2000);
    }

    @And("user enters a second barcode by scanning")
    public void user_enters_a_second_barcode_by_scanning() throws InterruptedException {
        wait.hardWait(2000);
        driver.findElement(By.tagName("body")).sendKeys("" + scanned2 + "");
        wait.hardWait(2000);
    }

    @Then("user verifies that new added barcode is displayed first in the table")
    public void user_verifies_that_new_added_barcode_is_displayed_first_in_the_table() {
        assertEquals("Barcode is not displayed first in the table!", Long.toString(scanned2), verification.scannedItemList.get(0).getText());
    }

    @Then("user verifies that scanned barcode is displayed")
    public void user_verifies_that_scanned_barcode_is_displayed() {
        assertEquals("Scanned Barcode is not displayed!", Long.toString(scanned1), verification.scannedItemList.get(verification.scannedItemList.size() - 1).getText());
    }

    @And("user selects Container Size")
    public void user_selects_container_size() {
        accession.containerSizeField.click();
        accession.containerOptions.get(2).click();
    }

    @When("user clicks three dot menu next to Accession Job Number")
    public void user_clicks_three_dot_menu_next_to_accession_job_number() throws InterruptedException {
        helper.jSClick(accession.threeDot);
        wait.hardWait(1000);
    }

    @When("user clicks Edit")
    public void user_clicks_edit() {
        helper.jSClick(accession.editAccessionJob);
    }

    @When("user edits Container Size")
    public void user_edits_container_size() throws InterruptedException {
        helper.scrollIntoView(accession.csField);
        accession.csField.click();
        accession.csField.sendKeys(Keys.CONTROL + "a");
        accession.csField.sendKeys(Keys.DELETE);
        wait.hardWait(1000);
        WaitHelper.waitForVisibility(accession.editFieldOptions.get(5), 6000);
        accession.editFieldOptions.get(5).click();
        wait.hardWait(1000);
    }

    @When("user edits Media Type")
    public void user_edits_media_type() throws InterruptedException {
        helper.scrollIntoView(accession.mtField);
        accession.mtField.click();
        accession.mtField.sendKeys(Keys.CONTROL + "a");
        accession.mtField.sendKeys(Keys.DELETE);
        wait.hardWait(700);
        WaitHelper.waitForVisibility(accession.editFieldOptions.get(1), 3000);
        accession.editFieldOptions.get(1).click();
    }

    @When("user clicks Save Edits")
    public void user_clicks_save_edits() {
        WaitHelper.waitForClickability(accession.saveEdits, 3000);
        accession.saveEdits.click();
    }

    @When("user clicks Resume Job button")
    public void user_clicks_resume_job_button() {
        WaitHelper.waitForClickability(accession.resumeJob, 3000);
        helper.jSClick(accession.resumeJob);
    }

    @When("user clicks Complete")
    public void user_clicks_complete() throws InterruptedException {
        WaitHelper.waitForClickability(accession.complete, 3000);
        helper.jSClick(accession.complete);
        wait.hardWait(4000);
    }

    @Then("Enter Barcode button is enabled")
    public void enter_barcode_button_is_enabled() {
        Helper.verifyElementEnabled(accession.enterBarcodeBtn);
    }

    @When("user selects an Accession Job")
    public void user_selects_an_accession_job() {
        accession.accessionJobsList.get(accession.accessionJobsList.size() - 1).click();
    }

    @And("user clicks Cancel Job")
    public void user_clicks_cancel_job() {
        helper.jSClick(accession.cancelJob);
    }

    @Then("user verifies warning message")
    public void user_verifies_warning_message() {
        WaitHelper.waitForVisibility(accession.warningMsg, 1000);
        assertTrue(accession.warningMsg.getText().contains("Are you sure you want to cancel"));
    }

    @And("user confirms cancellation")
    public void user_confirms_cancellation() throws InterruptedException {
        helper.jSClick(accession.confirmCancellation);
        wait.hardWait(2000);
        alert.closeToastMsg.click();
    }

    @And("user clicks Delete Tray")
    public void user_clicks_delete_tray() {
        helper.jSClick(accession.deleteTray);
    }

    @And("user clicks Edit Tray Barcode")
    public void user_clicks_edit_tray_barcode() {
        helper.jSClick(accession.editTrayBarcode);
    }

    @And("user edits Tray Barcode")
    public void user_edits_tray_barcode() throws InterruptedException {
        helper.jSClick(accession.editTrayBarcodeField);
        accession.editTrayBarcodeField.sendKeys(Keys.CONTROL + "a");
        accession.editTrayBarcodeField.sendKeys(Keys.DELETE);
        editedTrayBarcode = "BL" + Helper.generateBarcodeNumber();
        accession.editTrayBarcodeField.sendKeys(editedTrayBarcode);
        wait.hardWait(1000);
    }

    @Then("user verifies delete tray warning message")
    public void user_verifies_delete_tray_warning_message() {
        WaitHelper.waitForVisibility(accession.modal, 1000);
        assertTrue(accession.warningMsg.getText().contains("Are you sure you want to delete the tray? Warning: All associated tray items will be deleted."));
    }

    @And("user confirms delete tray action")
    public void user_confirms_delete_tray_action() throws InterruptedException {
        helper.jSClick(accession.confirmDeleteTray);
        wait.hardWait(2000);
        alert.closeToastMsg.click();
    }

    @When("user completes a Non-Tray Accession Job")
    public void user_completes_a_non_tray_accession_job() throws InterruptedException {
        user_clicks_start_accession_button();
        user_selects_non_tray_accession();
        user_selects_all_fields();
        user_clicks_submit_button();
        alertSteps.user_verifies_alert_msg("An Accession Job has successfully been created.");
        homeSteps.user_switches_off_barcode_scan();
        user_clicks_enter_barcode_button();
        WaitHelper.waitForVisibility(accession.enterBarcodeField, 2000);
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys(Helper.generateItemBarcode());
        helper.jSClick(accession.submitBtn);
        wait.hardWait(1000);

        user_clicks_complete_job_button();
        user_clicks_complete();
    }

    @When("user completes Non-Tray Accession Job")
    public void user_completes_non_tray_accession_job() throws InterruptedException {
        user_clicks_start_accession_button();
        user_selects_non_tray_accession();
        WaitHelper.waitForClickability(accession.ownerField, 100);
        accession.ownerField.click();
        WaitHelper.waitForVisibility(accession.ownerFieldOptions.get(0), 100);
        accession.ownerFieldOptions.get(0).click();
        accession.containerSizeField.click();
        helper.jSClick(accession.containerOptions.get(2));
        accession.mediaTypeField.click();
        wait.hardWait(1000);
        WaitHelper.waitForClickability(accession.mediaTypeField, 100);
        accession.mediaOptions.get(5).click();
        user_clicks_submit_button();
        user_clicks_enter_barcode_button();
        user_enters_non_tray_item_barcode_and_clicks_submit_button();
        user_clicks_complete_job_button();
        user_clicks_complete();
    }

    @When("user completes a new Non-Tray Accession Job")
    public void user_completes_a_new_non_tray_accession_Job() throws InterruptedException {
        user_clicks_start_accession_button();
        user_selects_non_tray_accession();
        user_selects_all_fields();
        user_clicks_submit_button();
        alertSteps.user_verifies_alert_msg("An Accession Job has successfully been created.");
        homeSteps.user_switches_on_toggle_barcode_scan();
        user_clicks_enter_barcode_button();
        user_enters_another_barcode_and_clicks_submit_button();
        user_clicks_complete_job_button();
        user_clicks_complete();
    }

    @When("user completes a Trayed Accession Job")
    public void user_completes_a_trayed_accession_job() throws InterruptedException {
        user_clicks_start_accession_button();
        user_selects_trayed_accession();
        user_selects_all_required_fields();
        user_selects_media_type();
        user_clicks_submit_button();
        user_scans_barcode();
        wait.hardWait(1000);
        user_enters_a_new_barcode_by_scanning();
        user_clicks_complete_job_button();
        user_clicks_complete();
        wait.hardWait(2000);
    }

    @When("user completes a new Trayed Accession Job")
    public void user_completes_a_new_trayed_accession_job() throws InterruptedException {
        user_clicks_start_accession_button();
        user_selects_trayed_accession();
        user_selects_all_required_fields();
        user_selects_media_type();
        user_clicks_submit_button();
        user_scans_barcode();
//        user_enters_barcode_by_scanning();
        user_enters_a_second_barcode_by_scanning();
        user_clicks_complete_job_button();
        user_clicks_complete();
    }

    @And("user clicks Print Job")
    public void user_clicks_print_job() {
        helper.jSClick(accession.printJob);
    }

    @And("user scans {string} barcode")
    public void user_scans_barcode(String barcode) throws InterruptedException {
        wait.hardWait(1000);
        driver.findElement(By.tagName("body")).sendKeys(barcode);
        wait.hardWait(2000);
    }

    @And("user verifies error alert modal is displayed")
    public void user_verifies_error_alert_modal_is_displayed() throws InterruptedException {
        WaitHelper.waitForVisibility(accession.alertModal, 1000);
        String message = "already exists";
        assertTrue(accession.alertModal.getText().contains(message));
        wait.hardWait(1000);
        accession.cancelModal.click();
    }

    @Then("user verifies that barcode {string} is displayed")
    public void user_verifies_that_barcode_is_displayed(String barcode) {
        assertEquals("Scanned Barcode is not displayed!", barcode, verification.scannedItemList.get(verification.scannedItemList.size() - 1).getText());
    }

    @Then("user verifies the barcode is not displayed twice")
    public void user_verifies_the_barcode_is_not_displayed_twice() {
        int size = verification.scannedItemList.size();
        assertEquals(1, size);
    }

    @Then("user navigates to the completed job using Top Search")
    public void user_navigates_to_the_completed_job_using_top_search() {
        Helper.clickWithJS(accession.searchBar);
        accession.searchBar.sendKeys("1");
        accession.searchBar.sendKeys(Keys.ENTER);
        Helper.clickWithJS(accession.completedJob);
    }

    @Then("user verifies all the action buttons are disabled")
    public void user_verifies_all_the_action_button_are_disabled() {
        assertFalse(accession.enterBarcodeBtn.isEnabled() && accession.deleteBtn.isEnabled()
                && accession.completeJob.isEnabled() && accession.pauseJob.isEnabled());
    }

    @Then("user verifies that barcode {string} is not displayed")
    public void user_verifies_that_barcode_is_not_displayed(String barcode) {
        assertFalse(verification.scannedItemList.get(verification.scannedItemList.size() - 1).getText().contains(barcode));
    }

    @Then("user verifies Edit Barcode and Delete buttons are disabled")
    public void user_verifies_edit_barcode_and_delete_buttons_are_disabled() {
        assertFalse(accession.enterBarcodeBtn.isEnabled() && accession.deleteBtn.isEnabled());
    }

    @Then("user verifies only Print Job option is enabled")
    public void user_verifies_only_print_job_option_is_enabled() {
        for (WebElement option : accession.disabledMenuItems) {
            System.out.println(option.getText() + " option is disabled");
        }

        System.out.println(accession.printJob.getText() + " option is enabled");
    }

    @Then("user creates an Accession Job")
    public void user_creates_an_accession_job() throws InterruptedException {
        user_clicks_start_accession_button();
        user_selects_trayed_accession();
        user_selects_all_required_fields();
        user_selects_media_type();
        user_clicks_submit_button();
        user_scans_barcode();
        user_enters_barcode_by_scanning();
    }

    @Then("user completes an Accession Job with a specific Owner and Size Class")
    public void user_creates_an_accession_job_with_a_specific_owner_and_size_class() throws InterruptedException {
        user_navigates_to_the_accession_page();
        user_clicks_start_accession_button();
        user_selects_trayed_accession();
        WaitHelper.waitForClickability(accession.ownerField, 10);
        accession.ownerField.click();
        wait.hardWait(100);
        accession.ownerFieldOptions.get(4).click();
        wait.hardWait(100);
        user_selects_media_type();
        user_clicks_submit_button();
        wait.hardWait(2000);
        generatedTrayForBHSizeClass = "BH" + Helper.generateBarcodeNumber();
        driver.findElement(By.tagName("body")).sendKeys(generatedTrayForBHSizeClass);
        wait.hardWait(1000);
        driver.findElement(By.tagName("body")).sendKeys("" + scanned4 + "");
        wait.hardWait(2000);
        user_clicks_complete_job_button();
        user_clicks_complete();
    }

    @When("user enters an Item barcode of invalid type")
    public void user_enters_an_item_barcode_of_invalid_type() throws InterruptedException {
        user_clicks_enter_barcode_button();
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys("1234567");
        helper.jSClick(accession.submitBtn);
        wait.hardWait(1000);
    }

    @Then("user verifies {string} error alert is displayed")
    public void user_verifies_an_error_alert_is_displayed(String alertText) {
        Helper.verifyElementDisplayed(accession.alertModal);
        assertEquals(alertText, accession.alertModal.getText());
        Helper.clickWithJS(accession.cancelModal);
    }

    @Then("user verifies the Item barcode is not added")
    public void user_verifies_the_item_barcode_is_not_added() {
        WaitHelper.waitForVisibility(accession.scannedItemList.get(0), 500);
        assertFalse(accession.scannedItemList.get(0).getText().contains("1234567"));
    }

    @When("user enters an existing in the system Tray barcode")
    public void user_enters_an_existing_in_the_system_tray_barcode() throws InterruptedException {
        user_clicks_enter_barcode_button();
        accession.enterBarcodeField.click();
        accession.enterBarcodeField.sendKeys(generatedTray);
        helper.jSClick(accession.submitBtn);
        wait.hardWait(1000);
    }

    @Then("user verifies the Tray barcode is not added")
    public void user_verifies_the_tray_barcode_is_not_added() {
        WaitHelper.waitForVisibility(accession.scannedItemList.get(0), 1000);
        assertFalse(accession.scannedItemList.get(0).getText().contains(generatedTray));
    }

    @Then("user verifies {string}Item{string} error alert is displayed")
    public void user_verifies_item_error_alert_is_displayed(String arg0, String arg1) {
        Helper.verifyElementDisplayed(accession.alertModal);
        assertTrue(accession.alertModal.getText().contains(arg0));
        Helper.clickWithJS(accession.cancelModal);
    }

    @Then("user navigates to the created earlier accession job")
    public void user_navigates_to_the_created_earlier_accession_job() {
        for (WebElement job : accession.accessionJobsList) {
            if (job.getText().contains(accessionJobNumber)) {
                job.click();
            }
        }
    }

    @And("user verifies the status of the corresponding Accession job")
    public void user_verifies_the_status_of_the_corresponding_accessionJob() throws InterruptedException {
        wait.hardWait(1000);
        for (WebElement job : accession.accessionJobsList) {
            if (job.getText().contains(VerificationSteps.verificationJobNumber)) {
                assertTrue(job.getText().contains("Paused"));
                job.click();
                wait.hardWait(1000);
            }
        }
    }


}

