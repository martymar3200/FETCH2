package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class RequestPage {


    WebDriver driver;

    public RequestPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//*[.='Create']/../..")
    public WebElement createRequestJobMenu;

    @FindBy(css = "[class='q-list'] div.q-item__label")
    public List<WebElement> dropdownOptions;

    @FindBy(css = "[class='q-card popup-modal']")
    public WebElement modal;

    @FindBy(css = "[placeholder='Enter or Scan Item Barcode']")
    public WebElement itemBarcodeField;

    @FindBy(css = "[placeholder='Enter External Request Id']")
    public WebElement requestIDField;

    @FindBy(css = "[placeholder='Enter Requestor Name']")
    public WebElement requestorNameField;

    @FindBy(css = "[placeholder='Select Request Type']")
    public WebElement requestTypeField;

    @FindBy(css = "[placeholder='Select Priority']")
    public WebElement priorityField;

    @FindBy(css = "[role='option']")
    public List<WebElement> options;

    @FindBy(css = "[placeholder='Select Delivery Location']")
    public WebElement deliveryLocationField;

    @FindBy(css = "[class='request-item-details']")
    public List<WebElement> requestItemDetails;

    @FindBy(css = "[class='barcode text-h4 text-center']")
    public WebElement actualBarcode;

    @FindBy(css = "[role='checkbox']")
    public List<WebElement> optionsWithCheckboxes;

    @FindBy(xpath = "//span[contains(text(),'Create Pick List')]")
    public WebElement createPickListBtn;

    @FindBy(xpath = "//span[contains(text(),'Add To Pick List')]")
    public WebElement addToPickListBtn;

    @FindBy(css = "[id='alertText']")
    public WebElement alertText;

    @FindBy(css = "[id='alertText'] a")
    public WebElement createdJobLink;

    @FindBy(css = "[placeholder='Select Pick List Job']")
    public WebElement selectPickListJobDropdown;

    @FindBy(css = "[role='option']")
    public List<WebElement> dropdownList;

    @FindBy(xpath = "//button[.='Submit']")
    public WebElement submitRequest;

    @FindBy(xpath = "(//td[.='New']/../td)[3]")
    public WebElement firstItemBarcode;

    @FindBy(xpath = "//*[@class='q-table']/tbody/tr")
    public List<WebElement> requestList;

    @FindBy(xpath = "//td[.='New']")
    public List<WebElement> newRequestsList;

    @FindBy(xpath = "//td[.='InProgress']")
    public List<WebElement> inProgressRequestsList;

    @FindBy(xpath = "//td[.='Out']")
    public List<WebElement> outStatusRequestsList;

    @FindBy(xpath = "//td[.='PickList']")
    public List<WebElement> picklistStatusRequestsList;

    @FindBy(css = "[class='q-card request-item-content']")
    public WebElement overlay;

    @FindBy(xpath = "//*[.='Edit Request']")
    public WebElement editRequestBtn;

    @FindBy(xpath = "//*[.='Cancel Request']")
    public WebElement cancelRequestBtn;

    @FindBy(css = ".q-table th")
    public List<WebElement> requestsTableColumnNames;

    @FindBy(css = "h1.text-h4.text-bold")
    public WebElement requestDetailsPageHeader;

    @FindBy(css = "label.request-details-label.text-h6")
    public List<WebElement> requestDetailsLabels;

    @FindBy(css = "[class='request-details'] a")
    public WebElement itemBarcodeLink;

    @FindBy(css = "td.q-td.text-left")
    public WebElement requestIDLink;


}
