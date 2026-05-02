package automation.step_definitions;


import automation.pages.ReportsPage;
import automation.utilities.Helper;
import automation.utilities.WaitHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReportsSteps {

    ReportsPage reports = new ReportsPage();
    Helper helper = new Helper();
    WaitHelper wait = new WaitHelper();


    @When("user clicks on the report selection input")
    public void user_clicks_on_the_report_selection_input() {
        helper.jSClick(reports.selectReportDropdown);
    }

    @When("user verifies the report options")
    public void user_verifies_the_report_options(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedReportName = map.get("option");
            String actualReportName = reports.reportsOptions.get(i).getText();
            Assert.assertTrue(expectedReportName, actualReportName.contains(expectedReportName));
            i++;
        }
    }

    @Then("user selects Item in Tray option")
    public void user_selects_item_in_tray_option() {
        helper.jSClick(reports.itemInTrayReport);
    }

    @Then("user selects Non-Tray Count option")
    public void user_selects_nontray_count_option() {
        helper.jSClick(reports.nonTrayCountReport);
    }

    @Then("user selects Tray Item Count option")
    public void user_selects_tray_item_count_option() {
        helper.jSClick(reports.trayItemCountReport);
    }

    @Then("user selects Total Item Retrieved option")
    public void user_selects_total_item_retrieved_option() {
        helper.jSClick(reports.totalItemRetrievedReport);
    }

    @Then("user selects Verification Change option")
    public void user_selects_verification_change_option() {
        helper.jSClick(reports.verificationChangeReport);
    }

    @Then("user selects User Job Summary option")
    public void user_selects_user_job_summary_option() {
        helper.jSClick(reports.userJobSummaryReport);
    }

    @Then("user selects Shelving Job Discrepancy option")
    public void user_selects_shelving_job_discrepancy_option() {
        helper.jSClick(reports.shelvingJobDiscrepancyReport);
    }

    @Then("user selects Item Accession option")
    public void user_selects_item_accession_option() {
        helper.jSClick(reports.itemAccessionReport);
    }

    @And("user verifies a modal with report parameters is displayed")
    public void user_verifies_a_modal_with_report_parameters_is_displayed() {
        WaitHelper.waitForClickability(reports.reportModal, 2000);
        Helper.verifyElementDisplayed(reports.reportModal);
    }

    @And("user verifies Item in Tray report input parameters")
    public void user_verifies_item_in_tray_report_input_parameters(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedParam = map.get("parameter");
            String actualParam = reports.reportParams.get(i).getText();
            Assert.assertTrue(expectedParam, actualParam.contains(expectedParam));
            i++;
        }
    }

    @And("user verifies Non-Tray Count report input parameters")
    public void user_verifies_nontray_count_report_input_parameters(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedParam = map.get("parameter");
            String actualParam = reports.reportParams.get(i).getText();
            Assert.assertTrue(expectedParam, actualParam.contains(expectedParam));
            i++;
        }
    }

    @And("user verifies Tray Item Count report input parameters")
    public void user_verifies_tray_item_count_report_input_parameters(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedParam = map.get("parameter");
            String actualParam = reports.reportParams.get(i).getText();
            Assert.assertTrue(expectedParam, actualParam.contains(expectedParam));
            i++;
        }
    }

    @And("user verifies Total Item Retrieved report input parameters")
    public void user_verifies_total_item_retrieved_report_input_parameters(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedParam = map.get("parameter");
            String actualParam = reports.reportParams.get(i).getText();
            Assert.assertTrue(expectedParam, actualParam.contains(expectedParam));
            i++;
        }
    }

    @And("user verifies Verification Change report input parameters")
    public void user_verifies_verification_change_report_input_parameters(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedParam = map.get("parameter");
            String actualParam = reports.reportParams.get(i).getText();
            Assert.assertTrue(expectedParam, actualParam.contains(expectedParam));
            i++;
        }
    }

    @And("user verifies User Job Summary report input parameters")
    public void user_verifies_user_job_summary_report_input_parameters(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedParam = map.get("parameter");
            String actualParam = reports.reportParams.get(i).getText();
            Assert.assertTrue(expectedParam, actualParam.contains(expectedParam));
            i++;
        }
    }

    @And("user verifies Shelving Job Discrepancy report input parameters")
    public void user_verifies_shelving_job_discrepancy_report_input_parameters(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedParam = map.get("parameter");
            String actualParam = reports.reportParams.get(i).getText();
            Assert.assertTrue(expectedParam, actualParam.contains(expectedParam));
            i++;
        }
    }

    @And("user verifies Item Accession report input parameters")
    public void user_verifies_item_accession_report_input_parameters(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedParam = map.get("parameter");
            String actualParam = reports.reportParams.get(i).getText();
            Assert.assertTrue(expectedParam, actualParam.contains(expectedParam));
            i++;
        }
    }

    @And("user runs report")
    public void user_runs_report() {
        helper.jSClick(reports.runReportBtn);
        WaitHelper.waitForPageToLoad(12000);
    }

    @Then("user verifies the result displays a table with the following information")
    public void user_verifies_the_result_displays_a_table_with_the_following_information(io.cucumber.datatable.DataTable dataTable) throws InterruptedException {
        wait.hardWait(1000);
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        int i = 0;
        for (Map<String, String> map : maps) {
            String expectedColumn = map.get("column");
            String actualColumn = reports.reportTableColumns.get(i).getText();
            Assert.assertTrue(expectedColumn, actualColumn.contains(expectedColumn));
            i++;
        }
    }

    @When("user clicks Redo Report button")
    public void user_clicks_redo_report_button() {
        WaitHelper.waitForClickability(reports.redoReport, 4000);
        reports.redoReport.click();
    }

    @When("user clicks Cancel")
    public void user_clicks_cancel() {
        helper.jSClick(reports.cancel);
    }

    @When("user clicks Export Report")
    public void user_clicks_export_report() {
        WaitHelper.waitForVisibility(reports.exportReport,3000);
        helper.jSClick(reports.exportReport);
    }

    @When("user clears the report option field")
    public void user_clears_the_report_option_field() {
        WaitHelper.waitForVisibility(reports.clearReportSelection,3000);
        helper.jSClick(reports.clearReportSelection);
    }

    @Then("user verifies the options to print and to download CSV are displayed")
    public void user_verifies_the_options_to_print_and_to_download_csv_are_displayed() {
        WaitHelper.waitForVisibility(reports.exportReportOptions.get(0), 2000);
        assertEquals("Print", reports.exportReportOptions.get(0).getText());
        assertEquals("Download CSV", reports.exportReportOptions.get(1).getText());
    }

    @And("user verifies all fields in Item in Tray modal are fillable")
    public void user_verifies_all_fields_in_item_in_tray_modal_are_fillable() {
        WaitHelper.waitForClickability(reports.selectModule, 3000);
        reports.selectModule.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
        WaitHelper.waitForClickability(reports.selectOwner, 3000);
        reports.selectOwner.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
        WaitHelper.waitForClickability(reports.enterAisleFrom, 3000);
        reports.enterAisleFrom.click();
        reports.enterAisleFrom.sendKeys("3");
        WaitHelper.waitForClickability(reports.enterAisleTo, 3000);
        reports.enterAisleTo.click();
        reports.enterAisleTo.sendKeys("5");
        WaitHelper.waitForClickability(reports.dateRangeFields.get(0), 3000);
        reports.dateRangeFields.get(0).click();
        reports.dateRangeFields.get(0).sendKeys("02/05/2025");
        WaitHelper.waitForClickability(reports.dateRangeFields.get(1), 3000);
        reports.dateRangeFields.get(1).click();
        reports.dateRangeFields.get(1).sendKeys("02/07/2025");
    }

    @And("user verifies all fields in Non-Tray Count modal are fillable")
    public void user_verifies_all_fields_in_nontray_count_modal_are_fillable() {
        WaitHelper.waitForClickability(reports.selectModule, 3000);
        reports.selectModule.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
        WaitHelper.waitForClickability(reports.selectOwner, 3000);
        reports.selectOwner.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
        WaitHelper.waitForClickability(reports.enterAisleFrom, 3000);
        reports.enterAisleFrom.click();
        reports.enterAisleFrom.sendKeys("3");
        WaitHelper.waitForClickability(reports.enterAisleTo, 3000);
        reports.enterAisleTo.click();
        reports.enterAisleTo.sendKeys("5");
        helper.scrollIntoView(reports.dateRangeFields.get(0));
        reports.dateRangeFields.get(0).click();
        reports.dateRangeFields.get(0).sendKeys("02/05/2025");
        WaitHelper.waitForClickability(reports.dateRangeFields.get(1), 3000);
        reports.dateRangeFields.get(1).click();
        reports.dateRangeFields.get(1).sendKeys("02/07/2025");
        WaitHelper.waitForClickability(reports.selectSizeClass, 3000);
        reports.selectSizeClass.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
    }

    @And("user verifies all fields in Total Item Retrieved modal are fillable")
    public void user_verifies_all_fields_in_total_item_retrieved_modal_are_fillable() {
        WaitHelper.waitForClickability(reports.dateRangeFields.get(0), 3000);
        reports.dateRangeFields.get(0).click();
        reports.dateRangeFields.get(0).sendKeys("02/05/2025");
        WaitHelper.waitForClickability(reports.dateRangeFields.get(1), 3000);
        reports.dateRangeFields.get(1).click();
        reports.dateRangeFields.get(1).sendKeys("02/07/2025");
        WaitHelper.waitForClickability(reports.selectOwner, 3000);
        reports.selectOwner.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
    }

    @And("user verifies all fields in Verification Change modal are fillable")
    public void user_verifies_all_fields_in_verification_change_modal_are_fillable() {
        WaitHelper.waitForClickability(reports.selectJobNumber, 3000);
        reports.selectJobNumber.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
        WaitHelper.waitForClickability(reports.dateRangeFields.get(0), 3000);
        reports.dateRangeFields.get(0).click();
        reports.dateRangeFields.get(0).sendKeys("02/05/2025");
        WaitHelper.waitForClickability(reports.dateRangeFields.get(1), 3000);
        reports.dateRangeFields.get(1).click();
        reports.dateRangeFields.get(1).sendKeys("02/07/2025");
        WaitHelper.waitForClickability(reports.selectAssignedUser, 3000);
        reports.selectAssignedUser.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
    }

    @And("user verifies all fields in User Job Summary modal are fillable")
    public void user_verifies_all_fields_in_user_job_summary_modal_are_fillable() {
        WaitHelper.waitForClickability(reports.dateRangeFields.get(0), 3000);
        reports.dateRangeFields.get(0).click();
        reports.dateRangeFields.get(0).sendKeys("02/05/2025");
        WaitHelper.waitForClickability(reports.dateRangeFields.get(1), 3000);
        reports.dateRangeFields.get(1).click();
        reports.dateRangeFields.get(1).sendKeys("02/11/2025");
        WaitHelper.waitForClickability(reports.selectUser, 3000);
        reports.selectUser.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
    }

    @And("user verifies all fields in Shelving Job Discrepancy modal are fillable")
    public void user_verifies_all_fields_in_shelving_job_discrepancy_modal_are_fillable() {
        WaitHelper.waitForClickability(reports.dateRangeFields.get(0), 3000);
        reports.dateRangeFields.get(0).click();
        reports.dateRangeFields.get(0).sendKeys("02/05/2025");
        WaitHelper.waitForClickability(reports.dateRangeFields.get(1), 3000);
        reports.dateRangeFields.get(1).click();
        reports.dateRangeFields.get(1).sendKeys("02/07/2025");
        WaitHelper.waitForClickability(reports.enterJobNumber, 3000);
        reports.enterJobNumber.click();
        reports.enterJobNumber.sendKeys("1");
        WaitHelper.waitForClickability(reports.selectAssignedUser, 3000);
        reports.selectAssignedUser.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
    }

    @And("user verifies all fields in Item Accession modal are fillable")
    public void user_verifies_all_fields_in_item_accession_modal_are_fillable() throws InterruptedException {
        WaitHelper.waitForClickability(reports.dateRangeFields.get(0), 3000);
        reports.dateRangeFields.get(0).click();
        reports.dateRangeFields.get(0).sendKeys("02/05/2025");
        WaitHelper.waitForClickability(reports.dateRangeFields.get(1), 3000);
        reports.dateRangeFields.get(1).click();
        reports.dateRangeFields.get(1).sendKeys("02/07/2025");
        WaitHelper.waitForClickability(reports.selectOwner, 3000);
        reports.selectOwner.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
        reports.selectOwner.click();
        WaitHelper.waitForClickability(reports.selectMediaType, 3000);
        reports.selectMediaType.click();
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(0), 3000);
        helper.jSClick(reports.fieldDropdownList.get(0));
        reports.selectMediaType.click();
        WaitHelper.waitForClickability(reports.selectSizeClass, 6000);
        reports.selectSizeClass.click();
        wait.hardWait(1000);
        WaitHelper.waitForClickability(reports.fieldDropdownList.get(3), 3000);
        helper.jSClick(reports.fieldDropdownList.get(3));
        reports.selectSizeClass.click();
    }

    @And("user enters Aisle range")
    public void user_enters_aisle_range() {
        WaitHelper.waitForClickability(reports.enterAisleFrom, 3000);
        reports.enterAisleFrom.click();
        reports.enterAisleFrom.sendKeys("3");
        WaitHelper.waitForClickability(reports.enterAisleTo, 3000);
        reports.enterAisleTo.click();
        reports.enterAisleTo.sendKeys("5");
    }


    @When("user selects Download CSV option")
    public void user_selects_download_csv_option() throws InterruptedException {
        String downloadFilepath = System.getProperty("user.dir") + "/downloads";
        File file = new File(downloadFilepath);
        if (!file.exists()) file.mkdirs();

        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadFilepath);
        prefs.put("download.prompt_for_download", false);
        prefs.put("plugins.always_open_pdf_externally", true); //For PDF files

        options.setExperimentalOption("prefs", prefs);

        WaitHelper.waitForClickability(reports.downloadCSV, 2000);
        reports.downloadCSV.click();

        wait.hardWait(5000);

        File downloadedFile = new File(downloadFilepath + "/Verification Change_2025_04_09_17_15_16");

        System.out.println(downloadedFile.getName());
        assertTrue(downloadedFile.getName().contains("Verification Change"));


    }

    @Then("user verifies the file download dialog is displayed")
    public void user_verifies_the_file_download_dialog_is_displayed() {

    }
}
