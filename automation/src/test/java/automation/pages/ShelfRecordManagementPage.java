package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class ShelfRecordManagementPage {


    WebDriver driver;

    public ShelfRecordManagementPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }


    @FindBy(css = "[class='barcode text-h4 text-center q-py-xs-sm q-py-sm-md']")
    public WebElement shelfBarcodeText;

    @FindBy(css = "label.shelf-details-label.text-h6")
    public List<WebElement> shelfDetailsLabels;

    @FindBy(css = "[class='shelf-details-label text-h6']")
    public List<WebElement> shelfLabels;

    @FindBy(css = ".q-table th")
    public List<WebElement> containersInShelfLabels;







}
