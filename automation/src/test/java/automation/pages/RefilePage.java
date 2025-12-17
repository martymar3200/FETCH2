package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class RefilePage {

    WebDriver driver;

    public RefilePage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = ".q-table")
    public WebElement table;

    @FindBy(css = ".q-table th")
    public List<WebElement> refileTableColumnNames;

    @FindBy(xpath = "//div[@class='text-center']/../../..")
    public WebElement refileQueueBtn;

    @FindBy(xpath = "//button[.='Refile Job']")
    public WebElement refileJobBtn;

    @FindBy(css = "[class='q-list'] [role='menuitem']")
    public List<WebElement> refileDropdownOptions;

    @FindBy(xpath = "//button[.='Done']")
    public WebElement doneBtn;

    @FindBy(xpath = "//span[contains(text(),'Create Refile Job')]")
    public WebElement createRefileJobBtn;

    @FindBy(css = "[aria-label='CreateRefileJobMenu']")
    public WebElement createRefileJobMenu;

    @FindBy(css = "[id='alertText']")
    public WebElement alertText;

    @FindBy(css = "[id='alertText'] a")
    public WebElement createdJobLink;

    @FindBy(css = "p[class$='text-h4']")
    public WebElement refileJobNumber;

    @FindBy(css = "[class='q-card popup-modal']")
    public WebElement scanModal;

    @FindBy(css = "div.container-details label")
    public List<WebElement> scanModalLabels;

    @FindBy(xpath = "(//*[@class='q-td text-left'] )[2]")
    public WebElement trayBarcodeValue;

    @FindBy(css = "[class='q-table'] tbody tr")
    public List<WebElement> refileJobsList;

    @FindBy(css = "[class='q-list'] div.q-item__label")
    public List<WebElement> dropdownOptions;

    @FindBy(xpath = "//div[.='Edit Job Info'][@role='menuitem']")
    public WebElement editJobInfo;







}
