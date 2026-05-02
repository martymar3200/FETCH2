package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class AccessionPage {

    WebDriver driver;

    public AccessionPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }


    @FindBy(xpath = "//button[.='Start Accession']")
    public WebElement startAccessionBtn;

    @FindBy(css = ".column > :nth-child(2) > .q-btn__content")
    public WebElement trayedAccession;

    @FindBy(xpath = "//button[.='Non-Tray Accession']")
    public WebElement nonTrayAccession;

    @FindBy(css = "[placeholder='Select Owner']")
    public WebElement ownerField;

    @FindBy(css = "[placeholder='Select Size Class']")
    public WebElement containerSizeField;

    @FindBy(css = ".q-virtual-scroll__content [role='option']")
    public List<WebElement> ownerFieldOptions;

    @FindBy(css = "[placeholder='Select Media Type']")
    public WebElement mediaTypeField;

    @FindBy(className = "form-group-label")
    public List<WebElement> newAccessionFields;

    @FindBy(css = ".q-pb-none > .q-btn--rectangle > .q-btn__content")
    public WebElement backBtn;

    @FindBy(css = ".q-btn--outline > .q-btn__content")
    public WebElement cancelBtn;

    @FindBy(xpath = "//button[.='Submit']")
    public WebElement submit;

    @FindBy(css = ":nth-child(3) > .text-h6 > .q-btn")
    public WebElement editContainerSize;

    @FindBy(css = ":nth-child(4) > .text-h6 > .q-btn")
    public WebElement editMediaType;

    @FindBy(css = ".q-pl-xs-xs > .q-btn > .q-btn__content")
    public WebElement cancelEdit;

    @FindBy(xpath = "(//input[@class='q-field__input q-placeholder col'])[1]")
    public WebElement csField;

    @FindBy(css = "[role='option']")
    public List<WebElement> containerOptions;

    @FindBy(css = "[aria-label='mediaTypeSelect']")
    public WebElement mtField;

    @FindBy(css = ".q-virtual-scroll__content span")
    public List<WebElement> mediaOptions;

    @FindBy(css = ".q-pr-xs-xs > .q-btn")
    public WebElement saveEdits;

    @FindBy(css = ".no-wrap > .bg-accent")
    public WebElement addItem;

    @FindBy(xpath = "//*[.='Pause Job']")
    public WebElement pauseJob;

    @FindBy(xpath = "//*[.='Resume Job']/../..")
    public WebElement resumeJob;

    @FindBy(css = ".q-mb-xs-lg > :nth-child(2) > .q-btn--unelevated")
    public WebElement completeJob;

    @FindBy(css = "[class='q-table--col-auto-width']")
    public List<WebElement> scanItemCheckbox;

    @FindBy(css = ".no-wrap > .bg-negative")
    public WebElement delete;

    @FindBy(css = "[class='q-card__section q-card__section--vert']>p")
    public WebElement modal;

    @FindBy(css = "[class='q-card popup-modal']")
    public WebElement popupModal;

    @FindBy(css = "[aria-label='dismissAlert']")
    public WebElement cancelModal;

    @FindBy(css = "button[class$='q-btn--no-uppercase btn-no-wrap text-body1 q-mr-sm-md'][type='button']")
    public WebElement enterBarcodeBtn;

    @FindBy(xpath = "//*[.='Edit Barcode']/../..")
    public WebElement editBarcodeBtn;

    @FindBy(css = "[placeholder='Please Enter Barcode']")
    public WebElement enterBarcodeField;

    @FindBy(xpath = "//span[.='Add Tray (1)']/../..")
    public WebElement addTrayBtn;

    @FindBy(css = "button[class$='btn-dashed btn-no-wrap text-body1 full-width']")
    public WebElement addTrayModalBtn;

    @FindBy(xpath = "//button[.='Submit']")
    public WebElement submitBtn;

    @FindBy(css = "[class='q-td text-left'] span")
    public List<WebElement> scannedItemList;

    @FindBy(css = "td.q-table--col-auto-width [role='checkbox']")
    public WebElement scannedItemCheckbox;

    @FindBy(css = "div[class='q-banner row items-center q-banner--dense rounded-borders alert-banner text-positive bg-color-green-light']")
    public WebElement alertMsg;

    @FindBy(xpath = "(//div[.='Please Scan Tray'])[2]")
    public WebElement scanTrayField;

    @FindBy(xpath = "//button[.='Complete & Print']")
    public WebElement completeAndprint;

    @FindBy(xpath = "//*[.='Delete']/../..")
    public WebElement deleteBtn;

    @FindBy(xpath = "//button[.='Confirm']")
    public WebElement confirmDelete;

    @FindBy(xpath = "//button[.='Delete Item(s)']")
    public WebElement deleteItem;

    @FindBy(xpath = "//i[.='more_vert']")
    public WebElement threeDot;

    @FindBy(xpath = "//*[.='Edit'][@role='menuitem']")
    public WebElement editAccessionJob;

    @FindBy(xpath = "//div[.='Cancel Job']")
    public WebElement cancelJob;

    @FindBy(xpath = "(//div[.='Print Job'])[1]")
    public WebElement printJob;

    @FindBy(xpath = "(//div[@role='menuitem'])[3]")
    public WebElement editTrayBarcode;

    @FindBy(xpath = "(//div[@role='menuitem'])[4]")
    public WebElement deleteTray;

    @FindBy(css = "div[role='option']")
    public List<WebElement> editFieldOptions;

    @FindBy(xpath = "//button[.='Complete']")
    public WebElement complete;

    @FindBy(css = "[class='q-card__section q-card__section--vert']")
    public WebElement warningMsg;

    @FindBy(xpath = "//button[.='Cancel Job']")
    public WebElement confirmCancellation;

    @FindBy(xpath = "//button[.='Delete Tray']")
    public WebElement confirmDeleteTray;

    @FindBy(css = "[placeholder='Please Enter Tray Barcode']")
    public WebElement editTrayBarcodeField;

    @FindBy(css = "[class='q-table'] tbody tr")
    public List<WebElement> accessionJobsList;

    @FindBy(css = "[id='alertText']")
    public WebElement alertModal;

    @FindBy(css = "[aria-label='SearchBar']")
    public WebElement searchBar;

    @FindBy(xpath = "//div[contains(text(),'Completed')]")
    public WebElement completedJob;

    @FindBy(css = "div[class*='disabled']")
    public List<WebElement> disabledMenuItems;






}
