package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class ShelvingPage {

    WebDriver driver;
    public ShelvingPage(){
        driver= Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }



    @FindBy(css = "button[class$='table-component-filter']")
    public WebElement filter;

    @FindBy(css = "[aria-label='tableRearrangeMenu']")
    public WebElement rearrangeDropdown;

    @FindBy(css = "[role='checkbox']")
    public List<WebElement> rearrangeDropdownOptions;

    @FindBy(css = "[class*='q-link cursor-pointer q-manual-focusable'][role='option']")
    public List<WebElement> allDropdownOptions;

    @FindBy(css = "[aria-label='createShelvingJobMenu']")
    public WebElement createShelvingJob;

    @FindBy(css = ".q-table th")
    public List<WebElement> shelfTableColumns;

    @FindBy(className = "text-h6")
    public List<WebElement> modalSections;

    @FindBy(className = "form-group-label")
    public List<WebElement> modalFields;

    @FindBy(xpath = "//button[.='Cancel']")
    public WebElement cancelBtn;

    @FindBy(css = "[placeholder='Select Owner']")
    public WebElement selectOwner;

    @FindBy(css = "[placeholder='Enter Shelf Number']")
    public WebElement enterShelfNumber;

    @FindBy(css = "[placeholder='Enter Shelf Width']")
    public WebElement enterShelfWidth;

    @FindBy(css = "[placeholder='Enter Shelf Height']")
    public WebElement enterShelfHeight;

    @FindBy(css = "[placeholder='Enter Shelf Depth']")
    public WebElement enterShelfDepth;

    @FindBy(css = "[placeholder='Select Type']")
    public WebElement selectType;

    @FindBy(xpath = "//div[.=' From Verification Job '][@role='menuitem']")
    public WebElement fromVerificationJob;

    @FindBy(xpath = "//button[.='Direct To Shelve']")
    public WebElement directToShelve;

    @FindBy(xpath = "//button[.='No']")
    public WebElement no;

    @FindBy(css = "[class$='q-btn--rectangle bg-white text-black q-btn--actionable q-focusable q-hoverable q-btn--no-uppercase']")
    public WebElement yes;

    @FindBy(css = "[aria-label='verificationJobSelect']")
    public WebElement selectByNumber;

    @FindBy(css = "[role='option']")
    public List<WebElement> verificationJobsList;

    @FindBy(xpath = "//button[.='Submit']")
    public WebElement submit;

    @FindBy(css = "p[class*='text-body1 outline text-highlight']")
    public WebElement shelvingJobStatus;

    @FindBy(id = "jobNumber")
    public WebElement jobNumber;

    @FindBy(css = "[class='q-card popup-modal']")
    public WebElement createShelvingJobModal;

    @FindBy(xpath = "(//button[.='more_vert'])[2]")
    public WebElement threeDotNextToContainer;

    @FindBy(xpath = "(//td[@class='q-td text-left'])[8]")
    public WebElement shelfNumber;

    @FindBy(xpath = "//div[@role='menu']")
    public WebElement editOrAssign;

    @FindBy(css = "[placeholder='Select Building']")
    public WebElement building;

    @FindBy(css = "div[class*='q-item q-item-type row no-wrap q-item--clickable q-link cursor-pointer q-manual-focusable']")
    public List<WebElement> buildings;

    @FindBy(xpath = "//td[.='Running']")
    public WebElement runningJob;

    @FindBy(xpath = "//td[.='Created']")
    public WebElement createdJob;

    @FindBy(css = "[aria-label='userSelect']")
    public WebElement assignedUserField;

    @FindBy(xpath = "//button[.='Save Edits']")
    public WebElement saveEdits;

    @FindBy(xpath = "//button[.='Cancel']")
    public WebElement cancelEdits;

    @FindBy(xpath = "//button[.='Right']")
    public WebElement rightSide;

    @FindBy(xpath = "//button[.='Execute Job']")
    public WebElement executeJob;

    @FindBy(xpath = "//*[contains(text(),'assigned container shelf')]")
    public WebElement assignedShelf;

    @FindBy(xpath = "//button[.='close']")
    public  WebElement closeMsg;

    @FindBy(css = "[class='text-bold text-nowrap text-positive']")
    public WebElement shelvedCheckMark;

    @FindBy(css = ".q-field__native [placeholder='Select Shelf']")
    public WebElement selectShelf;

    @FindBy(css = ".q-field__native [placeholder='Select Shelf Position']")
    public WebElement selectShelfPosition;

    @FindBy(xpath = "//button[.='Complete Job']")
    public WebElement completeJob;

    @FindBy(css = "[class$='q-item--clickable q-link cursor-pointer q-focusable q-hoverable']")
    public WebElement editLocation;

    @FindBy(xpath = "//button[.='Confirm']")
    public WebElement beAwareMsg;

    @FindBy(css = "[aria-label='barcodeInputDelay']")
    public WebElement inputDelay;

    @FindBy(xpath = "(//td[@class='q-td text-left'])[1]")
    public WebElement containerBarcode;

    @FindBy(xpath = "(//td[@class='q-td text-left'])[10]")
    public WebElement secondContainerBarcode;

    @FindBy(css = "[aria-label='barcodeToggle']")
    public WebElement toggleScan;

    @FindBy(xpath = "//button[.='Complete']")
    public WebElement complete;

    @FindBy(xpath = "//label[contains(text(),'Date Created')]//following-sibling::p")
    public WebElement dateCreated;

    @FindBy(css = "[class='q-table'] tbody tr")
    public List<WebElement> shelvingJobsList;





}
