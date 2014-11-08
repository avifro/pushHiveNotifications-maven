package avifro.com;

import avifro.com.Entities.CloudStorageProviderEnum;
import avifro.com.Entities.MyTransfer;
import avifro.com.Services.CloudStorageProvider;
import avifro.com.Services.HiveActionsService;
import avifro.com.Services.ProwlActionsService;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by avifro on 11/6/14.
 */
public class NotificationsPusherApp {

    private final static String USERNAME_KEY = "userName";
    private final static String PASSWORD_KEY = "password";

    private static NotificationsPusherApp instance = new NotificationsPusherApp();
    private CloudStorageProvider cloudStorageProvider;
    private ProwlActionsService prowlActionsService;

    private NotificationsPusherApp() {}

    public static NotificationsPusherApp getInstance() {
        return instance;
    }

    public String signIn(CloudStorageProviderEnum type, String rootHttpPath) {
        switch (type) {
            case HIVE:
                cloudStorageProvider = new HiveActionsService(rootHttpPath);
                break;
            case DROPBOX:
             // not supported yet
        }

        Properties properties = new Properties();
        try {
            properties.load((getClass().getResourceAsStream("/settings.properties")));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read properties file");
        }

        return cloudStorageProvider.getMyToken(properties.getProperty(USERNAME_KEY), properties.getProperty(PASSWORD_KEY));
    }

    public void startApp(String token, String myNotificationServiceKey, String myAppName) {

        if (prowlActionsService == null) {
            prowlActionsService = new ProwlActionsService(myAppName, myNotificationServiceKey);
        }

        List<MyTransfer> transfers = cloudStorageProvider.findMyTransfers(token);
        if (transfers.size() > 0) {
            for (MyTransfer transfer : transfers) {
                prowlActionsService.sendNotification("Download started!", transfer.getName());
            }
        }
    }

}
