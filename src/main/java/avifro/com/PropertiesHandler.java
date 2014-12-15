package avifro.com;

import java.util.Properties;

/**
 * Created by avifro on 11/19/14.
 */
public class PropertiesHandler {

    public static final String DB_USER_NAME_KEY = "dbUserName";
    public static final String DB_PASSWORD_KEY = "dbPassword";
    public static final String DB_HOST_KEY = "dbHost";
    public static final String DB_PORT_KEY = "dbPort";
    public static final String DB_NAME_KEY = "dbName";
    public static final String DB_COLLECTION_KEY = "dbCollection";
    public static final String USER_NAME_KEY = "userName";
    public static final String PASSWORD_KEY = "password";
    public static final String MY_APP_NAME_KEY = "myAppName";
    public static final String PROWL_API_KEY = "myNotificationServiceKey";


    private static PropertiesHandler propertiesHandler;
    private static Properties properties;

    private PropertiesHandler() {}

    public static PropertiesHandler getInstance() {
        if (propertiesHandler == null) {
            propertiesHandler = new PropertiesHandler();
            loadSystemEnvironmentVariables();
        }
        return propertiesHandler;
    }

    private static void loadSystemEnvironmentVariables() {
        properties = new Properties();
        if (System.getenv("isProduction") != null) {
            properties.setProperty(DB_USER_NAME_KEY, System.getenv(DB_USER_NAME_KEY));
            properties.setProperty(DB_PASSWORD_KEY, System.getenv(DB_PASSWORD_KEY));
            properties.setProperty(DB_HOST_KEY, System.getenv(DB_HOST_KEY));
            properties.setProperty(DB_PORT_KEY, System.getenv(DB_PORT_KEY));
            properties.setProperty(DB_NAME_KEY, System.getenv(DB_NAME_KEY));
            properties.setProperty(DB_COLLECTION_KEY, System.getenv(DB_COLLECTION_KEY));
            properties.setProperty(USER_NAME_KEY, System.getenv(USER_NAME_KEY));
            properties.setProperty(PASSWORD_KEY, System.getenv(PASSWORD_KEY));
            properties.setProperty(MY_APP_NAME_KEY, System.getenv(MY_APP_NAME_KEY));
            properties.setProperty(PROWL_API_KEY, System.getenv(PROWL_API_KEY));
        }
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
