package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class TrayItemMgmtPage {

    WebDriver driver;

    public TrayItemMgmtPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = ".col > .text-h4")
    public WebElement trayHeader;

    @FindBy(css = "[class='tray-details-label text-h6']")
    public List<WebElement> trayLabels;

    @FindBy(css = "[class='barcode text-h4 text-center q-py-xs-md']")
    public WebElement trayBarcodeText;

    @FindBy(xpath = "//div[.='Rearrange']")
    public WebElement rearrangeDropdown;

    @FindBy(css = ".q-virtual-scroll__content .q-item__label")
    public List<WebElement> rearrangeOptions;

    @FindBy(css = ".q-table th")
    public List<WebElement> itemsLabels;

    @FindBy(css = ".item-details label")
    public List<WebElement> overlayItemsLabels;

    @FindBy(css = "tbody > :nth-child(1)")
    public WebElement item1;

    @FindBy(css = ".q-card")
    public WebElement sideOverlay;


    @FindBy(xpath = "//*[contains(text(),'close')]")
    public WebElement closeBtn;

    @FindBy(css = ".q-page")
    public WebElement mainPage;

}
