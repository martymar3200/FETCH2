package automation.pages;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class PickListPage {

    WebDriver driver;

    public PickListPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = ".q-table th")
    public List<WebElement> picklistTableColumnNames;

    @FindBy(css = "[class='info-display-number-box text-h4']")
    public WebElement picklistJobNumber;

    @FindBy(xpath = "//div[@role='menuitem'][.='Edit']")
    public WebElement editJob;

    @FindBy(xpath = "//div[@role='menuitem'][.='Delete Job']")
    public WebElement deleteJob;

    @FindBy(xpath = "//div[@role='menuitem'][.='Print Job']")
    public WebElement printJob;

    @FindBy(xpath = "//div[@role='menuitem'][.='View History']")
    public WebElement viewHistory;

    @FindBy(css = "th.text-left.sortable")
    public List<WebElement> itemsInJobTableColumnNames;

    @FindBy(xpath = "//td[.='Running']")
    public WebElement runningJob;

    @FindBy(css = "tbody tr")
    public List<WebElement> picklistJobs;

    @FindBy(xpath = "//button[.='Retrieve Pick List']")
    public WebElement retrievePickList;

    @FindBy(xpath = "//td[.='Retrieved  ']")
    public WebElement retrievedItem;

    @FindBy(xpath = "//td[@class='q-td text-left']")
    public WebElement containerBarcode;

    @FindBy(css = "[role='menu']")
    public WebElement revertItemToQueue;

    @FindBy(css = "tbody tr")
    public List<WebElement> picklistTableRows;

    @FindBy(tagName = "td")
    public List<WebElement> picklistTableColumns;
}
