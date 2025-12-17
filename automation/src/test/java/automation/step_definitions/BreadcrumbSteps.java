package automation.step_definitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import automation.pages.HomePage;
import automation.pages.VerificationPage;
import automation.utilities.*;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BreadcrumbSteps {

    WebDriver driver = Driver.getInstance().getDriver();
    VerificationPage verification = new VerificationPage();
    WaitHelper wait = new WaitHelper();
    HomePage home = new HomePage();


    @Then("user should see the corresponding breadcrumbs")
    public void user_should_see_the_corresponding_breadcrumbs(io.cucumber.datatable.DataTable dataTable) {
       List<WebElement> breadcrumbs = driver.findElements(By.className("breadcrumb-items"));
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedField = map.get("breadcrumb");
            String actualField = breadcrumbs.get(i).getText();
            assertTrue(actualField.contains(expectedField));
            i++;
        }
    }


    @Then("user should see the following breadcrumbs")
    public void user_should_see_the_following_breadcrumbs() {
        List<WebElement> breadcrumbs = driver.findElements(By.className("breadcrumb-items"));

        for (WebElement breadcrumb : breadcrumbs) {
            String actualField = breadcrumb.getText();
            WebElement jobNumber = driver.findElement(By.cssSelector("[class='text-h4 text-bold']"));
            String accessionJobNumber = jobNumber.getText().substring(5).trim();
            assertTrue(actualField.contains("Home") || actualField.contains("Accession") || actualField.contains(accessionJobNumber));
        }
    }

    @Then("user selects a Verification Job")
    public void user_selects_a_Verification_Job() throws InterruptedException {
        verification.verificationJobsList.get(verification.verificationJobsList.size() - 1).click();
        wait.hardWait(2000);
    }

    @When("user clicks on Accession breadcrumb link")
    public void user_clicks_on_Accesssion_breadcrumb_link() {
        WebElement accessionBreadcrumbLink = driver.findElement(By.xpath("//a[.='Accession']"));
        accessionBreadcrumbLink.click();
    }

    @Then("user should navigate to Accession page")
    public void user_should_navigate_to_Verification_page() {
        assertEquals("https://test.fetch.example.com/accession", driver.getCurrentUrl());
    }

    @When("user clicks on Home breadcrumb link")
    public void user_clicks_on_Home_breadcrumb_link() {
        WebElement homeBreadcrumbLink = driver.findElement(By.xpath("//*[contains(text(),'Home')]"));
        homeBreadcrumbLink.click();
    }

    @When("user clicks on the banner")
    public void user_clicks_on_the_banner() {
        home.banner.click();
    }

    @Then("user should navigate to the Home page")
    public void user_should_navigate_to_the_Home_page() {
        assertTrue(driver.getCurrentUrl().contains("fetch.example.com/"));
    }

    @When("user clicks on Admin breadcrumb link")
    public void user_clicks_on_Admin_breadcrumb_link() {
        WebElement adminBreadcrumbLink = driver.findElement(By.xpath("//a[.='Admin']"));
        adminBreadcrumbLink.click();
    }

    @Then("user should navigate to the Admin page")
    public void user_should_navigate_to_the_Admin_page() {
        assertEquals("https://test.fetch.example.com/admin", driver.getCurrentUrl());
    }

    @Then("user is able to click X to cancel alert")
    public void user_is_able_to_click_x_to_cancel_alert() {
       Helper.clickWithJS(home.cancelAlert);
    }
}
