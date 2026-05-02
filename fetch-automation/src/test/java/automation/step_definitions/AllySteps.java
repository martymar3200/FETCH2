package automation.step_definitions;


import com.deque.axe.AXE;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import automation.utilities.ConfigurationReader;
import automation.utilities.Driver;


import java.net.URL;

public class AllySteps {

    WebDriver driver = Driver.getInstance().getDriver();
    private static final URL scriptURL = AllySteps.class.getResource("/axe.min.js");


    @Given("user navigates to the Home Page")
    public void user_navigates_to_the_Home_Page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "fetchURL"));
    }

    @Then("verify Accessibility")
    public void verify_accessibility() {
        JSONObject responseJson = new AXE.Builder(driver, scriptURL).analyze();
        JSONArray violations = responseJson.getJSONArray("violations");

        if (violations.length() == 0) {
            System.out.println("no errors");
        } else {
            AXE.writeResults("AllyTest", responseJson);
            Assert.assertTrue(AXE.report(violations), false);

        }
    }

    @Given("user navigates to the Shelving Page")
    public void user_navigates_to_the_Shelving_Page() {
        Driver.getInstance().getDriver().get(ConfigurationReader.getProperty("config.properties", "shelvingURL"));
    }


}

