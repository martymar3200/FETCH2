package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

import java.util.List;

public class HomePage {

    WebDriver driver;

    public HomePage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = "[alt='FETCH LOGO']")
    public WebElement fetchLogo;

    @FindBy(css = "[aria-label='Menu Button']")
    public WebElement hamburgerMenu;

    @FindBy(css = "[type='search']")
    public WebElement searchBar;

    @FindBy(css = "[aria-label='searchBarMenu']")
    public WebElement searchBarMenu;

    @FindBy(css = "[aria-label='advancedSearchButton']")
    public WebElement advancedSearchBtn;

    @FindBy(xpath = "//button[.='Search']")
    public WebElement search;

    @FindBy(css = "tbody tr")
    public List<WebElement> searchResults;

    @FindBy(css = "[aria-label='UserMenu']")
    public WebElement userIcon;

    @FindBy(xpath = "//*[@class='demo']/li")
    public WebElement barCodeField;

    @FindBy(css = "[class='q-list nav-list'] [class='q-item__label']")
    public List<WebElement> allNavigationTabs;

    @FindBy(css = "a[class$='nav-active']")
    public WebElement highlightedLink;

    @FindBy(css = "[href='/accession']")
    public WebElement accessionLink;

    @FindBy(css = "[href='/verification']")
    public WebElement verificationLink;

    @FindBy(css = "[href='/shelving']")
    public WebElement shelvingLink;

    @FindBy(css = "[href='/request']")
    public WebElement requestLink;

    @FindBy(css = "[href='/picklist']")
    public WebElement picklistLink;

    @FindBy(css = "[href='/refile']")
    public WebElement refileLink;

    @FindBy(css = "[href='/withdrawal']")
    public WebElement withdrawalLink;

    @FindBy(css = "[href='/reports']")
    public WebElement reportsLink;

    @FindBy(css = "[href='/admin']")
    public WebElement adminLink;

    @FindBy(css = "[aria-label='barcodeToggle']")
    public WebElement toggleScan;

    @FindBy(xpath = "//button[.='Disable Scan']")
    public WebElement disableScan;

    @FindBy(css = "[class='q-banner__content col text-body2']")
    public WebElement scanningEnabledAlert;

    @FindBy(css = "button[class$='q-btn q-btn-item non-selectable no-outline q-btn--flat q-btn--rectangle text-white q-btn--actionable q-focusable q-hoverable text-body1']:nth-child(3)")
    public WebElement banner;

    @FindBy(css = "[class='nav-actions'] button")
    public WebElement loginButton;

    @FindBy(css = "[placeholder='Enter User Email']")
    public WebElement usernameField;

    @FindBy(css = "[aria-label='Internal Login']")
    public WebElement login;

    @FindBy(css = "h1.text-h6")
    public WebElement user;

    @FindBy(xpath = "//i[.='logout']/../../..")
    public WebElement logout;

    @FindBy(xpath = "")
    public WebElement errorMessage;

    @FindBy(css = "ul.demo >li")
    public List<WebElement> scannedBarcodes;

    @FindBy(xpath = "//button[.='SSO Login']")
    public WebElement ssoLogin;

    @FindBy(css = "[type='email']")
    public WebElement email;

    @FindBy(css = "[type='submit']")
    public WebElement next;

    @FindBy(css = "[type='password']")
    public WebElement password;

    @FindBy(css = "[aria-label='dismissAlert']")
    public WebElement cancelAlert;

    @FindBy(css = "[class='q-banner__content col text-body2']")
    public WebElement downloadBanner;

    @FindBy(xpath = "//button[.='Never']")
    public WebElement never;

    @FindBy(css = "thead th")
    public List<WebElement> tableColumns;

    @FindBy(css = "[aria-label='tableFilterOptions']")
    public WebElement filterIcon;

    @FindBy(xpath = "//div[@aria-checked='true']/../..")
    public List<WebElement> checkedFilterOptions;

    @FindBy(css = "[aria-label='searchMenuList'] [role='menuitem']")
    public List<WebElement> searchOptions;

    @FindBy(xpath = "//*[.='Item'][@role='menuitem']")
    public WebElement searchItem;

    @FindBy(xpath = "//*[.='Request'][@role='menuitem']")
    public WebElement searchRequest;

    @FindBy(xpath = "//*[.='Tray'][@role='menuitem']")
    public WebElement searchTray;

    @FindBy(xpath = "//*[.='Shelf'][@role='menuitem']")
    public WebElement searchShelf;

    @FindBy(xpath = "//*[.='Accession'][@role='menuitem']")
    public WebElement searchAccession;

    @FindBy(xpath = "//*[.='Verification'][@role='menuitem']")
    public WebElement searchVerification;

    @FindBy(xpath = "//*[.='Shelving'][@role='menuitem']")
    public WebElement searchShelving;

    @FindBy(xpath = "//*[.='Batch Request'][@role='menuitem']")
    public WebElement searchBathRequest;

    @FindBy(xpath = "//*[.='Picklist'][@role='menuitem']")
    public WebElement searchPicklist;

    @FindBy(xpath = "//*[.='Refile'][@role='menuitem']")
    public WebElement searchRefile;

    @FindBy(xpath = "//*[.='Withdraw'][@role='menuitem']")
    public WebElement searchWithdraw;

    @FindBy(css = "[class='q-list search-results-list'] div")
    public WebElement searchResult;

    @FindBy(tagName = "h1")
    public WebElement welcomeMessage;

    @FindBy(css = "h1.text-h6")
    public WebElement usersName;

    @FindBy(css = "h2[class='text-h6 text-bold']")
    public WebElement advancedSearchModal;



}
