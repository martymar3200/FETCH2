package automation.pages;

import automation.utilities.Driver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class ItemManagementPage {

    WebDriver driver;

    public ItemManagementPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }


    @FindBy(css = ".col > .text-h4")
    public WebElement pageHeader;

    @FindBy(css = "[class='item-details-label text-h6']")
    public List<WebElement> itemsLabels;

    @FindBy(css = "[class='barcode text-h4 text-center bg-color-green-light text-positive q-py-xs-sm q-py-sm-md']")
    public WebElement itemBarcodeText;

    @FindBy(css = ".q-table th")
    public List<WebElement> requestHistoryLabels;

    @FindBy(css = "td.q-td.text-left")
    public WebElement containerInShelfBarcode;

    @FindBy(css = "[class='item-details'] a :nth-child(2)")
    public WebElement shelfBarcodeLink;

    @FindBy(css = ".q-card")
    public WebElement sideOverlay;

    @FindBy(xpath = "//button[.='Show Item Request History']")
    public WebElement showItemRequestHistory;
}
