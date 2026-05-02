package automation.utilities;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class BrowserFactory {
    public static WebDriver createInstance() {


        WebDriver driver = null;

        try {
            if (driver == null) {
                if (System.getProperty("browser") == null) {
                    WebDriverManager.chromedriver().setup();
                    //hashmap to store key and value of config
                    HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
                    chromePrefs.put("profile.default_content_settings.popups", 0);
                    chromePrefs.put("download.default_directory", System.getProperty("user.dir") + "\\src\\test\\resources\\testData\\Downloads");

                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--ignore-ssl-errors=yes");
                    options.addArguments("--ignore-certificate-errors");
                    options.setExperimentalOption("prefs", chromePrefs);
//                    options.addArguments("--auto-open-devtools-for-tabs");
                    driver = new ChromeDriver(options);
                } else {
                    //if System.getProperty("browser") returns value then based on value Switch case will be executed
                    switch (System.getProperty("browser")) {
                        case "chrome-headless":
                            WebDriverManager.chromedriver().setup();
                            driver = new ChromeDriver(new ChromeOptions().addArguments("--headless=new"));
                            break;
                        case "chromeRemote":
                            HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
                            chromePrefs.put("download.default_directory", System.getProperty("user.dir") + "\\src\\test\\resources\\testData\\Downloads");
                            ChromeOptions chrOptions = new ChromeOptions();
                            chrOptions.addArguments("user-data-dir=C:\\Users\\Example User\\Downloads\\chrome-win64\\chrome-win64\\chrome.exe");
                            chrOptions.addArguments("--ignore-ssl-errors=yes");
                            chrOptions.addArguments("--ignore-certificate-errors");
                            chrOptions.setExperimentalOption("prefs", chromePrefs);
                            try {
                                driver = new RemoteWebDriver(new URL("http://54.224.48.204:4444/wd/hub"), chrOptions);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "firefox":
                            WebDriverManager.firefoxdriver().setup();
                            driver = new FirefoxDriver();
                            break;
                        case "firefox-headless":
                            WebDriverManager.firefoxdriver().setup();
                            driver = new FirefoxDriver(new FirefoxOptions().addArguments("--headless=new"));
                            break;
                        case "firefoxRemote":
                            FirefoxOptions firOptions = new FirefoxOptions();
                            try {
                                driver = new RemoteWebDriver(new URL("http://54.224.48.204:4444/wd/hub"), firOptions);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "ie":
                            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                                throw new WebDriverException("Your operating system does not support the requested browser");
                            }
                            WebDriverManager.iedriver().setup();
                            driver = new InternetExplorerDriver();
                            break;

                        case "edge":
                            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                                throw new WebDriverException("Your operating system does not support the requested browser");
                            }
                            WebDriverManager.edgedriver().setup();
                            System.setProperty("webdriver.edge.driver", "C:\\Users\\Example User\\Downloads\\edgedriver_win32\\msedgedriver.exe");
                            driver = new EdgeDriver();
                            break;

                        case "safari":
                            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                                throw new WebDriverException("Your operating system does not support the requested browser");
                            }
                            WebDriverManager.getInstance(SafariDriver.class).setup();
                            driver = new SafariDriver();
                            break;
                        default:
                            WebDriverManager.chromedriver().setup();
                            driver = new ChromeDriver();
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return driver;
    }

}