package automation.pages;

import automation.utilities.Driver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class ReportsPage {

    WebDriver driver;

    public ReportsPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }


    @FindBy(css = "[aria-label='reportSelect']")
    public WebElement selectReportDropdown;

    @FindBy(css = "div[role='option']")
    public List<WebElement> reportsOptions;

    @FindBy(css = "[class='q-card popup-modal']")
    public WebElement reportModal;

    @FindBy(xpath = "//div[.='Item in Tray']")
    public WebElement itemInTrayReport;

    @FindBy(xpath = "//div[.='Non-Tray Count']")
    public WebElement nonTrayCountReport;

    @FindBy(xpath = "//div[.='Tray/Item Count By Aisle']")
    public WebElement trayItemCountReport;

    @FindBy(xpath = "//div[.='Total Item Retrieved']")
    public WebElement totalItemRetrievedReport;

    @FindBy(xpath = "//div[.='Verification Change']")
    public WebElement verificationChangeReport;

    @FindBy(xpath = "//div[.='User Job Summary']")
    public WebElement userJobSummaryReport;

    @FindBy(xpath = "//div[.='Shelving Job Discrepancy']")
    public WebElement shelvingJobDiscrepancyReport;

    @FindBy(xpath = "//div[.='Item Accession']")
    public WebElement itemAccessionReport;

    @FindBy(css = "[class='form-group-label']")
    public List<WebElement> reportParams;

    @FindBy(xpath = "//button[.='Run Report']")
    public WebElement runReportBtn;

    @FindBy(css = "[class='text-left sortable']")
    public List<WebElement> reportTableColumns;

    @FindBy(xpath = "//button[.='Redo Report']")
    public WebElement redoReport;

    @FindBy(xpath = "//button[.='Cancel']")
    public WebElement cancel;

    @FindBy(xpath = "//*[.='Export Report']/../..")
    public WebElement exportReport;

    @FindBy(css = "[role='menuitem']")
    public List<WebElement> exportReportOptions;

    @FindBy(css = "div[class='q-virtual-scroll__content'] [role='option']")
    public List<WebElement> fieldDropdownList;

    @FindBy(xpath = "//div[@role='menuitem'] [.=' Download CSV ']")
    public WebElement downloadCSV;

    @FindBy(css = "[placeholder='Select Module']")
    public WebElement selectModule;

    @FindBy(css = "[placeholder='Select Owner']")
    public WebElement selectOwner;

    @FindBy(css = "[placeholder='Select Media Type']")
    public WebElement selectMediaType;

    @FindBy(css = "[placeholder='Enter Aisle (From)']")
    public WebElement enterAisleFrom;

    @FindBy(css = "[placeholder='Enter Aisle (To)']")
    public WebElement enterAisleTo;

    @FindBy(css = "[placeholder='Ex: MM/DD/YYYY']")
    public List<WebElement> dateRangeFields;

    @FindBy(css = "[placeholder='Select Size Class']")
    public WebElement selectSizeClass;

    @FindBy(css = "[placeholder='Select Job Number']")
    public WebElement selectJobNumber;

    @FindBy(css = "[placeholder='Select Assigned User']")
    public WebElement selectAssignedUser;

    @FindBy(css = "[placeholder='Select User']")
    public WebElement selectUser;

    @FindBy(css = "[placeholder='Enter Job Number']")
    public WebElement enterJobNumber;

    @FindBy(css = "[aria-label='Clear']")
    public WebElement clearReportSelection;


}
