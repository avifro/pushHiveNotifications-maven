package avifro.com;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by avifro on 11/19/14.
 */
public class PropertiesHandler {

    private static PropertiesHandler propertiesHandler;
    private static Properties properties;

    private PropertiesHandler() {}

    public static PropertiesHandler getInstance() {
        if (propertiesHandler == null) {
            propertiesHandler = new PropertiesHandler();
            loadPropertiesFile();
        }
        return propertiesHandler;
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        String value = null;
        if (properties != null) {
            value = properties.getProperty(key, defaultValue);
        }
        return value;
    }

    private static void loadPropertiesFile() {
        properties = new Properties();
        try {
            System.out.println("Current working directory: " + PropertiesHandler.class.getClassLoader().getResource("").getPath());
            properties.load(PropertiesHandler.class.getClassLoader().getResourceAsStream("./settings.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read properties file");
        }
    }
}
