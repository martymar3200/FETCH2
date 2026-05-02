package automation.utilities;

import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.*;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;


public class Helper {

    WebDriver driver = Driver.getInstance().getDriver();

    public static void hover(WebElement element) {
        Actions actions = new Actions(Driver.getInstance().getDriver());
        actions.moveToElement(element).perform();
    }

    public static List<String> getElementsText(List<WebElement> list) {
        List<String> elemTexts = new ArrayList<>();
        for (WebElement el : list) {
            elemTexts.add(el.getText());
        }
        return elemTexts;
    }

    public static List<String> getElementsText(By locator) {

        List<WebElement> elems = Driver.getInstance().getDriver().findElements(locator);
        List<String> elemTexts = new ArrayList<>();

        for (WebElement el : elems) {
            elemTexts.add(el.getText());
        }
        return elemTexts;
    }

    public static void verifyElementDisplayed(By by) {
        try {
            assertTrue("Element not visible: " + by, Driver.getInstance().getDriver().findElement(by).isDisplayed());
        } catch (NoSuchElementException e) {
            Assert.fail("Element not found: " + by);

        }
    }

    public static void verifyElementDisplayed(WebElement element) {
        try {
            assertTrue("Element not visible: " + element, element.isDisplayed());
        } catch (NoSuchElementException e) {
            Assert.fail("Element not found: " + element);

        }
    }

    public static void verifyElementEnabled(WebElement element) {
        if (element.isEnabled()) {
            System.out.println("Button is enabled");
        } else {
            System.out.println("Button is disabled");
        }
    }

    public static void verifyElementDisabled(WebElement element) {
        if (element.getDomAttribute("aria-disabled").equals("true")) {
            System.out.println("Button is disabled");
        } else {
            System.out.println("Button is enabled");
        }
    }

    public static void verifyButtonIsDisabled(WebElement element) {
        if (element.getDomAttribute("disabled").equals("true")) {
            System.out.println("Button is disabled");
        } else {
            System.out.println("Button is enabled");
        }
    }

    public WebElement selectRandomTextFromDropdown(Select select) {
        Random random = new Random();
        List<WebElement> weblist = select.getOptions();
        int optionIndex = 1 + random.nextInt(weblist.size() - 1);
        select.selectByIndex(optionIndex);
        return select.getFirstSelectedOption();
    }

    public static void clickWithJS(WebElement element) {
        ((JavascriptExecutor) Driver.getInstance().getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
        ((JavascriptExecutor) Driver.getInstance().getDriver()).executeScript("arguments[0].click();", element);
    }

    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) Driver.getInstance().getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void doubleClick(WebElement element) {
        new Actions(Driver.getInstance().getDriver()).doubleClick(element).build().perform();
    }

    public void setAttribute(WebElement element, String attributeName, String attributeValue) {
        ((JavascriptExecutor) Driver.getInstance().getDriver()).executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", element, attributeName, attributeValue);
    }

    public static boolean isClickable(WebElement element) {
        WebDriverWait wait = new WebDriverWait(Driver.getInstance().getDriver(), Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void scrollIntoView(WebElement element) {
        JavascriptExecutor jse = (JavascriptExecutor) Driver.getInstance().getDriver();
        jse.executeScript("arguments[0].scrollIntoView()", element);
    }

    public void scrollIntoViewAndClick(WebElement element) {
        scrollIntoView(element);
        element.click();
    }

    public void jSClick(WebElement element) {
        JavascriptExecutor jse = (JavascriptExecutor) Driver.getInstance().getDriver();
        jse.executeScript("arguments[0].click();", element);
    }

    public static boolean verifyElementNotDisplayed(WebElement element) {
        try {
            return (!element.isDisplayed());
        } catch (Exception e) {
            return false;
        }
    }

    public static int generateBarcodeNumber() {
        Random rand = new Random();
        int max = 999999, min = 100000;
        int number = rand.nextInt(max - min + 1) + min;

        return number;
    }

    public static String generateItemBarcode() {
        Random rand = new Random();
        long randomNumber = rand.nextLong(1000000000L, 10000000000L);
        String number = String.valueOf(randomNumber) + "A";

        return number;
    }

    public static String todayDate() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String date = dateFormat.format(currentDate);
        System.out.println("Today is: " + date);

        return date;
    }



}
