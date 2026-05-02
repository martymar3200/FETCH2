package automation.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import automation.utilities.Driver;

public class AlertPage {

    WebDriver driver;

    public AlertPage() {
        driver = Driver.getInstance().getDriver();
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//button[.='Show Generic Alert']")
    public WebElement genericAlert;

    @FindBy(css = ".alert-notification")
    public WebElement alertMsg;

    @FindBy(css = "[aria-label='dismissAlert']")
    public WebElement cancelGenAlert;

    @FindBy(xpath = "(//span[.='Show Persistent Alert'])[1]")
    public WebElement persistentAlert;

    @FindBy(css = "[class='text-body1']")
    public WebElement audioAlertMsg;

    @FindBy(xpath = "(//span[.='Cancel'])[1]")
    public WebElement cancelPersistentAlert;

    @FindBy(css = "[class='q-banner__content col text-body2'] [class='text-body1']")
    public WebElement toastMsg;

    @FindBy(xpath = "//p[.='The Job has been completed and moved for verification.']")
    public WebElement completedAndMovedForVerificationMsg;

    @FindBy(xpath = "//p[.='The Job has been completed.']")
    public WebElement theJobHasBeenCompleted;

    @FindBy(xpath = "//p[.='The container has been updated.']")
    public WebElement theContainerHasBeenUpdated;

    @FindBy(xpath = "//*[contains(text(),'successfully created')]")
    public WebElement jobCreated;

    @FindBy(xpath = "//button[.='close']")
    public  WebElement closeToastMsg;

    @FindBy(xpath = "//div[.='Non-Trayed']")
    public WebElement nonTrayedAccessionJob;






}
