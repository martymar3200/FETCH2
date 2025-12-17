package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class VerificationPage {

    WebDriver driver;

    public VerificationPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//*[text()='Trayed']")
    public List<WebElement> trayedVerificationJobs;

    @FindBy(css = "[class='q-table'] tbody tr")
    public List<WebElement> verificationJobsList;

    @FindBy(xpath = "//*[text()='Non-Trayed']")
    public WebElement nonTrayedVerificationJob;

    @FindBy(xpath = "//*[text()='Trayed']")
    public List<WebElement> trayedJobList;

    @FindBy(xpath = "//*[text()='Non-Trayed']")
    public List<WebElement> nonTrayedJobList;

    @FindBy(css = "[class$='q-mb-md-xl q-mb-lg-none']")
    public WebElement scanTrayBox;

    @FindBy(xpath = "//*[@class='q-table']/tbody/tr")
    public List<WebElement> scannedVerificationItems;

    @FindBy(css = "[class='q-table'] td [role='checkbox']")
    public List<WebElement> scannedItemsCheckbox;

    @FindBy(xpath = "//button[.='Next Tray']")
    public WebElement nextTrayBtn;

    @FindBy(css = "button[class$='verification-next-tray-action full-width']")
    public List<WebElement> newTrays;

    @FindBy(xpath = "//*[.='Enter Barcode']/../..")
    public WebElement enterBarcodeBtn;

    @FindBy(css = "[placeholder='Please Enter Barcode']")
    public WebElement enterBarcodeField;

    @FindBy(xpath = "//button[.='Submit']")
    public WebElement submitBtn;

    @FindBy(xpath = "//span[.='Complete Job'] /../..")
    public WebElement completeJob;

    @FindBy(xpath = "(//button[.='more_vert'])[1]")
    public WebElement threeDot;

    @FindBy(xpath = "(//input[@class='q-field__input q-placeholder col'])[1]")
    public WebElement editOwnerField;

    @FindBy(xpath = "(//input[@class='q-field__input q-placeholder col'])[2]")
    public WebElement editContainerSizeField;

    @FindBy(css = "[aria-label='mediaTypeSelect']")
    public WebElement editMediaTypeField;

    @FindBy(css = "div[role='option']")
    public List<WebElement> editFieldOptions;

    @FindBy(xpath = "(//p[@class='outline'])[2]")
    public WebElement containerSizeValue;

    @FindBy(css = "[class='q-td text-left'] span")
    public List<WebElement> scannedItemList;

    @FindBy(css = "[class='text-bold text-positive']")
    public WebElement verifiedCheckMark;

    @FindBy(xpath = "//button[.='Add New Item']")
    public WebElement addNewItem;

    @FindBy(css = "[class*='text-highlight']")
    public List<WebElement> jobStatuses;

    @FindBy(xpath = "//button[.='Cancel Verification']")
    public WebElement confirmVerificationJobCancellation;


}
