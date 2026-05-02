package automation.utilities;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
public class ConfigurationReader {
    private static Properties properties;
    public static String getProperty(String fileName, String key) {
        try {
            //generating path to the specified file
            String path = "src/test/resources/"+ fileName;

            FileInputStream stream = new FileInputStream(path);
            properties = new Properties();
            // sending fileInputStream obj to properties constructor
            properties.load(stream);
            // closing()
            stream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key); }
}
