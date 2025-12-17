package automation.utilities;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class GenericHelper {

    private static final Logger oLog = LogManager.getLogger(GenericHelper.class);
    public GenericHelper( ) {
        oLog.debug("Helper : " + Driver.getInstance().getDriver().hashCode());
    }


    public WebElement getElement(By locator) {
        oLog.info(locator);
        if (IsElementPresentQuick(locator))
            return Driver.getInstance().getDriver().findElement(locator);

        try {
            throw new java.util.NoSuchElementException("Element Not Found : " + locator);
        } catch (RuntimeException re) {
            oLog.error(re);
            throw re;
        }
    }

    public WebElement getElementWithNull(By locator) {
        oLog.info(locator);
        try {
            return Driver.getInstance().getDriver().findElement(locator);
        } catch (java.util.NoSuchElementException e) {
            // Ignore
        }
        return null;
    }

    public boolean IsElementPresentQuick(By locator) {
        boolean flag = Driver.getInstance().getDriver().findElements(locator).size() >= 1;
        oLog.info(flag);
        return flag;
    }









}