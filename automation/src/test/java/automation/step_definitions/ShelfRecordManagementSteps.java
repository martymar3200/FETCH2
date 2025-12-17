package automation.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import automation.pages.ShelfRecordManagementPage;
import automation.utilities.ConfigurationReader;
import automation.utilities.Driver;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ShelfRecordManagementSteps {


    ShelfRecordManagementPage shelfMgmt = new ShelfRecordManagementPage();


    @Given("user navigates to Shelf Details page")
    public void user_navigates_to_shelf_details_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "shelfDetailsPage"));
    }

    @Then("user verifies shelf barcode is visible")
    public void user_verifies_shelf_barcode_is_visible() {
        shelfMgmt.shelfBarcodeText.isDisplayed();
    }

    @And("user verifies Shelf information")
    public void user_verifies_tray_information(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("label");
            String actualLabel = shelfMgmt.shelfDetailsLabels.get(i).getText();
            assertEquals("Label verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @And("user verifies Containers in Shelf columns")
    public void user_verifies_containers_in_shelf_columns(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedColumn = map.get("column");
            String actualColumn = shelfMgmt.containersInShelfLabels.get(i).getText();
            assertEquals("Column verification failed",
                    expectedColumn, actualColumn);
            i++;
        }
    }
}







