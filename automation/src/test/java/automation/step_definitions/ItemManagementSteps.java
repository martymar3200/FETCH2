package automation.step_definitions;

import automation.pages.ItemManagementPage;
import automation.utilities.ConfigurationReader;
import automation.utilities.Driver;
import automation.utilities.Helper;
import automation.utilities.WaitHelper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ItemManagementSteps {

    ItemManagementPage itemMgmt = new ItemManagementPage();
    Helper helper = new Helper();
    WaitHelper wait = new WaitHelper();


    @When("user navigates to Item Details page")
    public void user_navigates_to_item_details_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "itemManagementPage"));
    }

    @Then("user verifies item barcode is visible")
    public void user_verifies_item_barcode_is_visible() {
        itemMgmt.itemBarcodeText.isDisplayed();
    }

    @Then("user verifies Item information")
    public void user_verifies_item_information(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("label");
            String actualLabel = itemMgmt.itemsLabels.get(i).getText();
            assertEquals("Column verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }
    @Then("user verifies Request History columns")
    public void user_verifies_request_history_columns(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedColumn = map.get("column");
            String actualColumn = itemMgmt.requestHistoryLabels.get(i).getText();
            assertEquals("Column verification failed",
                    expectedColumn, actualColumn);
            i++;
        }
    }

    @When("user clicks on Shelf Barcode")
    public void user_clicks_on_shelf_barcode() {
        helper.jSClick(itemMgmt.shelfBarcodeLink);
    }

    @Then("user verifies Shelf Details page is displayed")
    public void user_verifies_shelf_details_page_is_displayed() throws InterruptedException {
        wait.hardWait(100);
        assertEquals("Shelf Details", itemMgmt.pageHeader.getText());
    }

    @When("user clicks on Container in Shelf")
    public void user_clicks_on_container_in_shelf() {
        helper.jSClick(itemMgmt.containerInShelfBarcode);
    }

    @Then("user verifies Item Details page is displayed")
    public void user_verifies_item_details_page_is_displayed()  {
        WaitHelper.waitForPageToLoad(90);
        assertTrue(itemMgmt.pageHeader.getText().contains("Item Details"));
    }

    @Then("user verifies the overlay slide is visible")
    public void user_verifies_the_overlay_slide_is_visible() {
        WaitHelper.waitForVisibility(itemMgmt.sideOverlay, 3000);
        itemMgmt.sideOverlay.isDisplayed();
    }

    @Then("the overlay slide is not visible")
    public void the_overlay_slide_is_not_visible() {
        Helper.verifyElementNotDisplayed(itemMgmt.sideOverlay);
    }

    @When("user clicks to see Item Request History")
    public void user_clicks_to_see_item_request_history() throws InterruptedException {
        helper.jSClick(itemMgmt.showItemRequestHistory);
        wait.hardWait(3000);
    }
}
