package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class WithdrawalPage {

    WebDriver driver;

    public WithdrawalPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }


    @FindBy(css = ".q-table tbody tr")
    public List<WebElement> withdrawJobList;

    @FindBy(xpath = "//button[.='Create Withdraw Job']")
    public WebElement createWithdrawJob;

    @FindBy(css = "[class='info-display-number-box text-h4']")
    public WebElement withdrawJobNumber;

    @FindBy(css = "[class='text-body1 text-center outline text-highlight']")
    public WebElement jobStatus;

    @FindBy(xpath = "//button[.='Withdraw Items']")
    public WebElement withdrawItemsBtn;

    @FindBy(css = ".q-table th.text-left")
    public List<WebElement> withdrawalColumns;

    @FindBy(css = "[role='menuitem']")
    public List<WebElement> threeDotMenuOptions;

    @FindBy(xpath = "(//button[.='more_vert'])[2]")
    public WebElement threeDotNextToItemBarcode;

    @FindBy(xpath = "//td[.='Created']")
    public List<WebElement> createdJobs;

    @FindBy(xpath = "//button[.='Delete Job']")
    public WebElement confirmDeleteWithdrawJob;

    @FindBy(xpath = "//label[.=' Assigned User: ']//following-sibling::p")
    public WebElement assignedUser;

    @FindBy(xpath = "//*[.='Add Items']/../..")
    public WebElement addItemsBtn;

    @FindBy(xpath = "//div[.=' Scan Item(s) '][@role='menuitem']")
    public WebElement scanItemsOption;

    @FindBy(xpath = "//div[.=' Manually Enter Barcode '][@role='menuitem']")
    public WebElement manuallyEnterBarcodeOption;

    @FindBy(css = "[class='q-td text-left']:nth-child(4)")
    public WebElement itemBarcode1;

    @FindBy(css = "[placeholder='Please Enter Barcode']")
    public WebElement enterBarcodeField;

    @FindBy(xpath = "//button[.='Create Pick List Job']")
    public  WebElement createPickListJobFromWithdrawal;

    @FindBy(css = "td[class='q-td text-left']")
    public List<WebElement> withdrawJobColumnValues;

    @FindBy(css = "th.text-left")
    public List<WebElement> itemsInJobColumns;

    @FindBy(xpath = "(//button[.='Withdraw Items'])[2]")
    public WebElement confirmWithdrawItems;

    @FindBy(xpath = "//button[.='Withdraw & Print']")
    public WebElement withdrawAndPrint;

}
