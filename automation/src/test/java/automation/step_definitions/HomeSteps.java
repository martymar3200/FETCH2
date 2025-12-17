package automation.step_definitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.*;
import automation.pages.HomePage;
import automation.utilities.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HomeSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    HomePage home = new HomePage();
    Helper helper = new Helper();
    static WaitHelper wait = new WaitHelper();


    @Given("user navigates to FETCH Homepage")
    public void user_navigates_to_FETCH_Homepage() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "fetchURL"));
        WaitHelper.waitForPageToLoad(90);
    }

    @When("user verifies FETCH logo is displayed")
    public void user_verifies_fetch_logo_is_displayed() {
        home.fetchLogo.isDisplayed();
    }

    @Then("the hamburger menu is clickable")
    public void hamburger_menu_is_clickable() {
        Helper.isClickable(home.hamburgerMenu);
    }

    @Then("the search bar is visible")
    public void the_search_bar_is_visible() {
        Helper.verifyElementDisplayed(home.searchBar);
    }

    @Then("the login button is clickable")
    public void login_button_is_clickable() {
        Helper.isClickable(home.loginButton);
        home.loginButton.click();
        home.loginButton.click();
    }

    @Then("Scanned Bar Codes field is visible")
    public void scanned_bar_codes_field_is_visible() {
        Helper.verifyElementDisplayed(home.barCodeField);
    }

    @Then("user verifies side navigation tabs on Homepage")
    public void user_verifies_side_navigation_tabs_on_homepage(DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedTabname = map.get("tabname");
            String actualTabname = home.allNavigationTabs.get(i).getText();
            Assert.assertEquals("Tabname verification failed",
                    expectedTabname, actualTabname);
            i++;
        }
    }

    @When("user clicks Accession on side navigation menu")
    public void user_clicks_accession_on_side_navigation_menu() {
        helper.jSClick(home.accessionLink);
    }

    @Then("verify that Accession navigation link on side menu is highlighted")
    public void verify_that_accession_navigation_link_on_side_menu_is_highlighted() {
        assertEquals("Link not highlighted", "Accession", home.highlightedLink.getText());
    }

    @When("user clicks Verification on side navigation menu")
    public void user_clicks_verification_on_side_navigation_menu() throws InterruptedException {
        helper.jSClick(home.verificationLink);
        wait.hardWait(1000);
    }

    @Then("verify that Verification navigation link on side menu is highlighted")
    public void verify_that_verification_navigation_link_on_side_menu_is_highlighted() {
        assertTrue(home.highlightedLink.getText().contains("Verification"));
    }

    @When("user clicks Shelving on side navigation menu")
    public void user_clicks_shelving_on_side_navigation_menu() throws InterruptedException {
        Helper.clickWithJS(home.shelvingLink);
        wait.hardWait(1000);
    }

    @Then("verify that Shelving navigation link on side menu is highlighted")
    public void verify_that_shelving_navigation_link_on_side_menu_is_highlighted() {
        assertTrue(home.highlightedLink.getText().contains("Shelving"));
    }

    @When("user clicks Request on side navigation menu")
    public void user_clicks_request_on_side_navigation_menu() throws InterruptedException {
        home.requestLink.click();
        wait.hardWait(1000);
    }

    @Then("verify that Request navigation link on side menu is highlighted")
    public void verify_that_request_navigation_link_on_side_menu_is_highlighted() {
        assertTrue(home.highlightedLink.getText().contains("Request"));
    }

    @When("user clicks Pick List on side navigation menu")
    public void user_clicks_pick_List_on_side_navigation_menu() throws InterruptedException {
        home.picklistLink.click();
        wait.hardWait(1000);
    }

    @Then("verify that Pick List navigation link on side menu is highlighted")
    public void verify_that_pick_List_navigation_link_on_side_menu_is_highlighted() {
        assertTrue(home.highlightedLink.getText().contains("Pick List"));
    }

    @When("user clicks Withdrawal on side navigation menu")
    public void user_clicks_withdrawal_on_side_navigation_menu() throws InterruptedException {
        home.withdrawalLink.click();
        wait.hardWait(1000);
    }

    @Then("verify that Withdrawal navigation link on side menu is highlighted")
    public void verify_that_withdrawal_navigation_link_on_side_menu_is_highlighted() {
        assertTrue(home.highlightedLink.getText().contains("Withdrawal"));
    }

    @When("user clicks Reports on side navigation menu")
    public void user_clicks_reports_on_side_navigation_menu() throws InterruptedException {
        home.reportsLink.click();
        wait.hardWait(1000);
    }

    @Then("verify that Reports navigation link on side menu is highlighted")
    public void verify_that_reports_navigation_link_on_side_menu_is_highlighted() {
        assertTrue(home.highlightedLink.getText().contains("Reports"));
    }

    @When("user clicks Refile on side navigation menu")
    public void user_clicks_refile_on_side_navigation_menu() throws InterruptedException {
        home.refileLink.click();
        wait.hardWait(1000);
    }

    @Then("verify that Refile navigation link on side menu is highlighted")
    public void verify_that_refile_navigation_link_on_side_menu_is_highlighted() {
        assertTrue(home.highlightedLink.getText().contains("Refile"));
    }

    @When("user clicks Admin on side navigation menu")
    public void user_clicks_admin_on_side_navigation_menu() throws InterruptedException {
        Helper.clickWithJS(home.adminLink);
        Thread.sleep(1000);
    }

    @When("user logs in as a tester1")
    public void user_logs_in_as_a_tester1() {
        home.loginButton.click();
        home.usernameField.sendKeys("tester1@example.com");

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try {
                home.login.click();
                new WebDriverWait((Driver.getInstance().getDriver()), Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[type='search']")));
                System.out.println("Login is successful on attempt " + attempt);
                return;//exit
            } catch (Exception e) {
                System.out.println("Login attempt " + attempt + " failed Error: " + e.getMessage());
                if (attempt == maxAttempts) {
                    throw new RuntimeException("Failed to login after " + maxAttempts + " attempts", e);
                }
            }
        }
    }

    @And("user logs in as an admin")
    public void user_logs_in_as_an_admin() throws InterruptedException {
        home.loginButton.click();
        wait.hardWait(1000);
        home.usernameField.sendKeys("admin@example.com");
        WaitHelper.waitForClickability(home.login, 20);
        home.login.click();
        wait.hardWait(2000);
    }


    @When("user logs in with invalid email")
    public void user_logs_in_with_invalid_email() throws InterruptedException {
        home.loginButton.click();
        wait.hardWait(1000);
        home.usernameField.sendKeys("test@example.com");
        WaitHelper.waitForClickability(home.login, 20);
        home.login.click();
        wait.hardWait(2000);
    }

    @Then("user should be able to verify account name on user dashboard")
    public void user_should_be_able_to_verify_account_name_on_user_dashboard() {
        WebElement userIcon = driver.findElement(By.cssSelector("[aria-label='UserMenu']"));
        userIcon.click();
        WaitHelper.waitForClickability(home.user, 1000);
        String actualAccountName = home.user.getText();
        Assert.assertEquals("Account name is not verified!", "Tester One", actualAccountName);
    }

    @Then("user validates {string} error message")
    public void user_validates_error_message(String expectedErrorMessage) {
        String actualErrorMessage = home.errorMessage.getText();
        Assert.assertEquals("Error message validation failed!", expectedErrorMessage, actualErrorMessage);
    }

    @When("user clicks logout button")
    public void user_clicks_logout_button() {
        WaitHelper.waitForClickability(home.logout, 20);
        home.logout.click();
    }

    @Then("login button is not enabled")
    public void login_button_is_not_enabled() {
        Assert.assertFalse(home.login.isEnabled());
    }

    @Then("user verifies the scanned barcode is displayed")
    public void user_verifies_the_scanned_barcode_is_displayed() {
        assertEquals("Scanned Barcode is not displayed!", home.scannedBarcodes.get(home.scannedBarcodes.size() - 1).getText(), Long.toString(AccessionSteps.scanned1));
    }

    @When("user switches ON Toggle Barcode Scan")
    public void user_switches_on_toggle_barcode_scan() {
        if (home.userIcon.isDisplayed()) {
            WaitHelper.waitForClickability(home.userIcon, 3000);
            home.userIcon.click();
            WaitHelper.waitForClickability(home.toggleScan, 3000);
            home.toggleScan.click();
            home.userIcon.click();
        }
        if (!home.userIcon.isDisplayed()) {
            driver.navigate().refresh();
            WaitHelper.waitForClickability(home.userIcon, 3000);
            home.userIcon.click();
            WaitHelper.waitForClickability(home.toggleScan, 3000);
            home.toggleScan.click();
            home.userIcon.click();
        }
    }

    @When("user switches off Toggle Barcode Scan")
    public void user_switches_off_barcode_scan() throws InterruptedException {
        WaitHelper.waitForClickability(home.userIcon, 3000);
        helper.jSClick(home.userIcon);
        if (!home.toggleScan.isEnabled()) {
            home.toggleScan.click();
            home.userIcon.click();
        } else if (home.toggleScan.isEnabled()) {
            home.toggleScan.click();
            home.userIcon.click();
        }
        wait.hardWait(1000);
    }

    @Then("user verifies barcode scanning is enabled")
    public void user_verifies_barcode_scanning_is_enabled() {
        helper.jSClick(home.userIcon);
        assertTrue(home.toggleScan.isEnabled());
        helper.jSClick(home.userIcon);
    }

    @Then("user verifies barcode scanning is disabled")
    public void user_verifies_barcode_scanning_is_disabled() {
        Assert.assertEquals("Barcode scanning is disabled.", home.scanningEnabledAlert.getText());
    }

    @Then("user verifies today date")
    public void user_verifies_today_date() {
        Helper.todayDate();
    }

    @Then("user verifies columns displayed")
    public void user_verifies_columns_displayed(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedColumn = map.get("column");
            wait.hardWait(1000);
            String actualColumn = home.tableColumns.get(i).getText();
            Assert.assertEquals("Column name verification failed",
                    expectedColumn, actualColumn);
            i++;
        }
    }

    @When("user clicks filter icon")
    public void userClicksFilterIcon() {
        WaitHelper.waitForVisibility(home.filterIcon, 2000);
        home.filterIcon.click();
    }

    @Then("user verifies default filter options")
    public void user_verifies_default_filter_options(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedOption = map.get("option");

            String actualOption = home.checkedFilterOptions.get(i).getText();
            Assert.assertEquals("Filter options verification failed",
                    expectedOption, actualOption);
            i++;
        }
    }

    @And("user clicks search bar menu")
    public void user_clicks_search_bar_menu() {
        helper.jSClick(home.searchBarMenu);
    }

    @And("user clicks Advanced Search")
    public void user_clicks_advanced_search() {
        helper.jSClick(home.advancedSearchBtn);
    }

    @Then("user verifies search menu options")
    public void user_verifies_search_menu_options(io.cucumber.datatable.DataTable dataTable) {
        WaitHelper.waitForVisibility(home.searchOptions.get(4), 2000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedOption = map.get("option");
            String actualOption = home.searchOptions.get(i).getText();
            Assert.assertEquals("Options verification failed",
                    expectedOption, actualOption);
            i++;
        }
    }

    @When("user selects Item option")
    public void user_selects_item_option() {
        helper.jSClick(home.searchItem);
    }

    @When("user selects Request option")
    public void user_selects_request_option() {
        helper.jSClick(home.searchRequest);
    }

    @When("user searches Item barcode")
    public void user_searches_item_barcode() {
        helper.jSClick(home.searchBar);
        home.searchBar.sendKeys("00001983386");
        home.searchBar.sendKeys(Keys.ENTER);
    }

    @When("user clicks on search result")
    public void user_clicks_on_search_result() {
        WaitHelper.waitForClickability(home.searchResult, 3000);
        helper.jSClick(home.searchResult);
    }

    @When("user selects Tray option")
    public void user_selects_tray_option() {
        helper.jSClick(home.searchTray);
    }

    @When("user searches Tray barcode")
    public void user_searches_tray_barcode() {
        helper.jSClick(home.searchBar);
        home.searchBar.sendKeys("AH00026");
        home.searchBar.sendKeys(Keys.ENTER);
    }

    @When("user selects Shelf option")
    public void user_selects_shelf_option() {
        helper.jSClick(home.searchShelf);
    }

    @When("user searches Shelf barcode value")
    public void user_searches_shelf_barcode_value() {
        helper.jSClick(home.searchBar);
        home.searchBar.sendKeys("02816");
        home.searchBar.sendKeys(Keys.ENTER);
    }

    @And("user verifies the welcome message is displayed")
    public void user_verifies_the_welcome_message_is_displayed() {
        home.welcomeMessage.isDisplayed();
    }

    @And("user verifies the welcome message contains users name")
    public void user_verifies_the_welcome_message_contains_users_name() {
        helper.jSClick(home.userIcon);
        WaitHelper.waitForVisibility(home.usersName, 3000);
        String name = home.usersName.getText();
        String welcomeMessageName = home.welcomeMessage.getText().substring(9, home.welcomeMessage.getText().indexOf('!'));
        assertEquals(name, welcomeMessageName);
    }

    @Then("user clicks search")
    public void user_clicks_search() {
        helper.jSClick(home.search);
    }

    @When("user verifies Advanced Search button is displayed")
    public void user_verifies_advanced_search_button_is_displayed() {
        Helper.verifyElementDisplayed(home.advancedSearchBtn);
    }

    @And("user verifies Advanced Search modal is displayed")
    public void user_verifies_advanced_search_modal_is_displayed() {
        WaitHelper.waitForVisibility(home.advancedSearchModal, 3000);
        assertTrue(home.advancedSearchModal.getText().contains("Advanced")&& home.advancedSearchModal.getText().contains("Search"));
    }

    @When("user selects Accession option")
    public void user_selects_accession_option() {
        helper.jSClick(home.searchAccession);
    }

    @When("user selects Verification option")
    public void user_selects_verification_option() {
        helper.jSClick(home.searchVerification);
    }

    @When("user selects Shelving option")
    public void user_selects_shelving_option() {
        helper.jSClick(home.searchShelving);
    }

    @When("user selects Batch Request option")
    public void user_selects_batch_request_option() {
        helper.jSClick(home.searchBathRequest);
    }

    @When("user selects Picklist option")
    public void user_selects_picklist_option() {
        helper.jSClick(home.searchPicklist);
    }

    @When("user selects Refile option")
    public void user_selects_refile_option() {
        helper.jSClick(home.searchRefile);
    }

    @When("user selects Withdraw option")
    public void user_selects_withdraw_option() {
        helper.jSClick(home.searchWithdraw);
    }



}
