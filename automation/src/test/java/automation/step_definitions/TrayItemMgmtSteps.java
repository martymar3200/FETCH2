package automation.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import automation.pages.TrayItemMgmtPage;
import automation.utilities.*;

import java.util.List;
import java.util.Map;

public class TrayItemMgmtSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    TrayItemMgmtPage trayMgmt = new TrayItemMgmtPage();
    Helper helper = new Helper();
    GenericHelper generic = new GenericHelper();
    WaitHelper wait = new WaitHelper();



    @Given("user navigates to Item Management Page")
    public void user_navigates_to_item_management_page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "trayItemMgmtURL"));
    }

    @When("user looks at the tray header")
    public void user_looks_at_the_tray_header() {
        generic.IsElementPresentQuick(By.cssSelector(".col > .text-h4"));
    }

    @Then("the name of tray is displayed")
    public void the_name_of_tray_is_displayed() {
        Helper.verifyElementDisplayed(trayMgmt.trayHeader);
        String expectedTrayHeader = "Tray Mctray Photograph Archive - 332";
        String actualTrayHeader = trayMgmt.trayHeader.getText();
        Assert.assertEquals("Tray Header verification failed",
                expectedTrayHeader, actualTrayHeader);
    }

    @Then("tray barcode is visible")
    public void tray_barcode_is_visible() {
        trayMgmt.trayBarcodeText.getText();
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
            Assert.assertEquals("Columnname verification failed",
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
            Assert.assertEquals("Labelname verification failed",
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
            Assert.assertEquals("Labelname verification failed",
                    expectedLabel, actualLabel);
            i++;
        }
    }

    @When("user clicks on item in the table")
    public void user_clicks_on_item_in_the_table() {
        trayMgmt.item1.click();
    }

    @Then("the overlay slide is visible")
    public void the_overlay_slide_is_visible() {
        WaitHelper.waitForVisibility(trayMgmt.sideOverlay, 3000);
    }

    @And("user verifies item in tray details on Overlay Slide")
    public void user_verifies_item_in_tray_details_on_overlay_slide(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedLabel = map.get("labelname");
            String actualLabel = trayMgmt.overlayItemsLabels.get(i).getText();
            Assert.assertEquals("Labelname verification failed",
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
            Assert.assertEquals("Labelname verification failed",
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

    @Then("the overlay slide is not visible")
    public void the_overlay_slide_is_not_visible() {
        Helper.verifyElementNotDisplayed(trayMgmt.sideOverlay);
    }


}
