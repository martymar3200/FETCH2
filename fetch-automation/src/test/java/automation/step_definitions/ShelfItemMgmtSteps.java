package automation.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import automation.pages.ShelfItemMgmtPage;
import automation.utilities.ConfigurationReader;
import automation.utilities.Driver;
import automation.utilities.Helper;

import java.util.List;
import java.util.Map;

public class ShelfItemMgmtSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    ShelfItemMgmtPage nonTrayMgmt = new ShelfItemMgmtPage();


    @Given("user navigates to Shelf Item Management Page")
    public void user_navigates_to_shelf_item_management_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "shelfItemMgmtURL"));
    }

    @Then("the name of shelf is displayed")
    public void the_name_of_shelf_is_displayed() {
        Helper.verifyElementDisplayed(nonTrayMgmt.shelfHeader);
    }

    @Then("user verifies shelf labels on Non-Trayed Items Management Page")
    public void user_verifies_shelf_labels_on_non_trayed_items_management_page(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("labelname");
            String actualLabel = nonTrayMgmt.shelfLabels.get(i).getText();
            Assert.assertEquals("Labelname verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @Then("user verifies non-trayed items labels on Non-Trayed Items Management Page")
    public void user_verifies_non_trayed_items_labels_on_on_trayed_items_management_page(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("labelname");
            String actualLabel = nonTrayMgmt.itemsLabels.get(i).getText();
            Assert.assertEquals("Labelname verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }



}

