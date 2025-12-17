package automation.utilities;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SelectHelper extends  GenericHelper{
    private static final Logger oLog = LogManager.getLogger(SelectHelper.class);
    public SelectHelper() {
        oLog.debug("CheckBoxOrRadioButtonHelper : " + Driver.getInstance().getDriver().hashCode());
    }

    public void selectCheckBox(WebElement element, boolean check){
        if(check){
            if(!element.isSelected()){
                element.click();
            }
        } else {
            if(element.isSelected()){
                element.click();
            }
        }
    }

    public static void selectByVisibleText(WebElement element,String visibleText) {
        Select select = new Select(element);
        select.selectByVisibleText(visibleText);
        oLog.info("Locator : " + element + " Value : " + visibleText);
    }

    public void selectByValue(By locator,String value) {
        Select select = new Select(getElement(locator));
        select.selectByValue(value);
        oLog.info("Locator : " + locator + " Value : " + value);
    }

    public void selectByIndex(By locator,int index) {
        Select select = new Select(getElement(locator));
        select.selectByIndex(index);
        oLog.info("Locator : " + locator + " Index : " + index);
    }

    public String getSelectedValue(By locator) {
        oLog.info(locator);
        return getSelectedValue(getElement(locator));
    }

    public String getSelectedValue(WebElement element) {
        String value = new Select(element).getFirstSelectedOption().getText();
        oLog.info("WebELement : " + element + " Value : "+ value);
        return value;
    }


    public List<String> getAllDropDownValues(By locator) {
        Select select = new Select(getElement(locator));
        List<WebElement> elementList = select.getOptions();
        List<String> valueList = new LinkedList<String>();

        for (WebElement element : elementList) {
            oLog.info(element.getText());
            valueList.add(element.getText());
        }
        return valueList;
    }

    public static List<String> getDropdownOptionsText(WebElement dropdownElement) {
        List<String> optionsText = new ArrayList<>();
        Select dropdown = new Select(dropdownElement);
        List<WebElement> options = dropdown.getOptions();
        for(WebElement option: options) {
            optionsText.add(option.getText());
        }
        return optionsText;
    }

}