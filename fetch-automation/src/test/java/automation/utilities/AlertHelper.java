package automation.utilities;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;

import java.util.Iterator;
import java.util.Set;

public class AlertHelper {
    private static final Logger oLog = LogManager.getLogger(AlertHelper.class);

    public AlertHelper() {
        oLog.debug("AlertHelper : " + Driver.getInstance().getDriver().hashCode());
    }

    public Alert getAlert() {
        oLog.debug("");
        return Driver.getInstance().getDriver().switchTo().alert();
    }

    public void AcceptAlert() {
        oLog.info("");
        getAlert().accept();
    }

    public void DismissAlert() {
        oLog.info("");
        getAlert().dismiss();
    }

    public String getAlertText() {
        String text = getAlert().getText();
        oLog.info(text);
        return text;
    }

    public boolean isAlertPresent() {
        try {
            Driver.getInstance().getDriver().switchTo().alert();
            oLog.info("true");
            return true;
        } catch (NoAlertPresentException e) {
            // Ignore
            oLog.info("false");
            return false;
        }
    }

    public void AcceptAlertIfPresent() {
        if (!isAlertPresent())
            return;
        AcceptAlert();
        oLog.info("");
    }

    public void dismissAlertIfPresent() {

        if (!isAlertPresent())
            return;
        DismissAlert();
        oLog.info("");
    }

    public void AcceptPrompt(String text) {

        if (!isAlertPresent())
            return;

        Alert alert = getAlert();
        alert.sendKeys(text);
        alert.accept();
        oLog.info(text);
    }


    public void isBannerPresent() {
        String mainWindHandler = Driver.getInstance().getDriver().getWindowHandle();
        String subWinHandler = null;
            Set<String> allHandle = Driver.getInstance().getDriver().getWindowHandles();

            Iterator<String> iterator = allHandle.iterator();
            if(iterator.hasNext()) {
                subWinHandler = iterator.next();
                Driver.getInstance().getDriver().switchTo().window(subWinHandler);
                WebElement later = Driver.getInstance().getDriver().findElement(By.cssSelector("button[class$='q-btn q-btn-item non-selectable no-outline q-btn--flat q-btn--rectangle text-white q-btn--actionable q-focusable q-hoverable text-body1']:nth-child(2)"));
                later.click();
                Driver.getInstance().getDriver().switchTo().window(mainWindHandler);
                oLog.info("true");
            } else {
                Driver.getInstance().getDriver().getCurrentUrl();
                oLog.info("false");
            }

        }
    }


