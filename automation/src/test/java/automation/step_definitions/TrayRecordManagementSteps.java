package automation.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import automation.pages.TrayRecordManagementPage;
import automation.utilities.*;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrayRecordManagementSteps {

    TrayRecordManagementPage trayMgmt = new TrayRecordManagementPage();
    Helper helper = new Helper();
    WaitHelper wait = new WaitHelper();



    @Given("user navigates to Tray Details page")
    public void user_navigates_to_tray_details_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "trayDetailsPage"));
    }

    @When("user verifies the page header")
    public void user_verifies_the_page_header() {
       assertTrue( trayMgmt.pageHeader.getText().contains("Details"));
    }

    @Then("user verifies tray barcode is visible")
    public void user_verifies_tray_barcode_is_visible() {
        trayMgmt.trayBarcodeText.isDisplayed();
    }

    @Then("Rearrange dropdown is visible and clickable")
    public void rearrange_dropdown_is_visible_and_clickable() {
        Helper.verifyElementDisplayed(trayMgmt.rearrangeDropdown);
        WaitHelper.waitForClickability(trayMgmt.rearrangeDropdown, 10);
        trayMgmt.rearrangeDropdown.click();
    }

    @And("user verifies Rearrange dropdown options")
    public void user_verifies_rearrange_dropdown_options(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            Helper.isClickable(trayMgmt.rearrangeOptions.get(i));
            String expectedLabel = map.get("columnname");
            String actualLabel = trayMgmt.rearrangeOptions.get(i).getText();
            assertEquals("Columnname verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @Then("user verifies tray labels on Items Management Page")
    public void user_verifies_tray_details_labels_on_items_management_page(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("labelname");
            String actualLabel = trayMgmt.trayLabels.get(i).getText();
            assertEquals("Labelname verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @Then("user verifies items labels on Items Management Page")
    public void user_verifies_items_labels_on_items_management_page(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("labelname");
            String actualLabel = trayMgmt.itemsLabels.get(i).getText();
            assertEquals("Labelname verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @When("user clicks on item in the table")
    public void user_clicks_on_item_in_the_table() {
        trayMgmt.item1.click();
    }


    @And("user verifies item in tray details on Overlay Slide")
    public void user_verifies_item_in_tray_details_on_overlay_slide(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("labelname");
            String actualLabel = trayMgmt.overlayItemsLabels.get(i).getText();
            assertEquals("Labelname verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @Then("user verifies item details on Overlay Slide")
    public void user_verifies_item_details_on_overlay_slide(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("labelname");
            String actualLabel = trayMgmt.overlayItemsLabels.get(i).getText();
            assertEquals("Labelname verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @And("the x button is clickable")
    public void the_x_button_is_clickable() {
        Helper.isClickable(trayMgmt.closeBtn);
        helper.jSClick(trayMgmt.closeBtn);
    }

    @And("user clicks outside of overlay")
    public void user_clicks_outside_of_overlay() throws InterruptedException {
        wait.hardWait(1000);
        helper.jSClick(trayMgmt.mainPage);
    }

    @And("user verifies Tray information")
    public void user_verifies_tray_information(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("label");
            String actualLabel = trayMgmt.trayDetailsLabels.get(i).getText();
            assertEquals("Label verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @And("user verifies Items in Tray columns")
    public void user_verifies_items_in_tray_columns(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedColumn = map.get("column");
            String actualColumn = trayMgmt.itemsInTrayColumns.get(i).getText();
            assertEquals("Column verification failed",
                    expectedColumn, actualColumn);
            i++;
        }
    }

    @When("user clicks on Item in Tray")
    public void user_clicks_on_item_in_tray() {
        helper.jSClick(trayMgmt.itemInTrayBarcode);
    }

    @Then("user verifies Tray Item Details page is displayed")
    public void user_verifies_tray_item_details_page_is_displayed() throws InterruptedException {
        wait.hardWait(100);
        assertEquals("Tray Item Details", trayMgmt.pageHeader.getText());
    }

    @When("user clicks on Tray Barcode")
    public void user_clicks_on_tray_barcode() {
        helper.jSClick(trayMgmt.trayBarcodeLink);
    }

    @Then("user verifies Tray Details page is displayed")
    public void user_verifies_tray_details_page_is_displayed() throws InterruptedException {
        wait.hardWait(100);
        assertEquals("Tray Details", trayMgmt.pageHeader.getText());
    }
}
