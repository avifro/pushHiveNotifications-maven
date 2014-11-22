package avifro.com;

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
            loadSystemEnvironment();
        }
        return propertiesHandler;
    }

    private static void loadSystemEnvironment() {
        properties = new Properties();
        properties.setProperty("dbHost", System.getenv("dbHost"));
        properties.setProperty("dbPort", System.getenv("dbPort"));
        properties.setProperty("dbName", System.getenv("dbName"));
        properties.setProperty("dbCollection", System.getenv("dbCollection"));
        properties.setProperty("userName", System.getenv("userName"));
        properties.setProperty("password", System.getenv("password"));
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

//    private static void loadSystemProperties() {
//        properties = System.getProperties();
//    }

//    private static void loadPropertiesFile() {
//        properties = new Properties();
//        try {
//            System.out.println("Current working directory: " + PropertiesHandler.class.getClassLoader().getResource("").getPath());
//            properties.load(PropertiesHandler.class.getClassLoader().getResourceAsStream("./settings.properties"));
//        } catch (IOException e) {
//            throw new RuntimeException("Couldn't read properties file");
//        }
//    }
}
