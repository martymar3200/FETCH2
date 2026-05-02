package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class TrayRecordManagementPage {

    WebDriver driver;

    public TrayRecordManagementPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = ".col > .text-h4")
    public WebElement pageHeader;

    @FindBy(css = "[class='tray-details-label text-h6']")
    public List<WebElement> trayLabels;

    @FindBy(css = "[class='barcode text-h4 text-center bg-color-green-light text-positive q-py-xs-sm q-py-sm-md']")
    public WebElement trayBarcodeText;

    @FindBy(xpath = "//div[.='Rearrange']")
    public WebElement rearrangeDropdown;

    @FindBy(css = ".q-virtual-scroll__content .q-item__label")
    public List<WebElement> rearrangeOptions;

    @FindBy(css = ".q-table th")
    public List<WebElement> itemsLabels;

    @FindBy(css = ".item-details label")
    public List<WebElement> overlayItemsLabels;

    @FindBy(css = "label.tray-details-label.text-h6")
    public List<WebElement> trayDetailsLabels;

    @FindBy(css = "[class='q-table'] th")
    public List<WebElement> itemsInTrayColumns;

    @FindBy(css = "td.q-td.text-left")
    public WebElement itemInTrayBarcode;

    @FindBy(css = "[class='item-details'] a")
    public WebElement trayBarcodeLink;

    @FindBy(css = "tbody > :nth-child(1)")
    public WebElement item1;

    @FindBy(xpath = "//*[contains(text(),'close')]")
    public WebElement closeBtn;

    @FindBy(css = ".q-page")
    public WebElement mainPage;

}
