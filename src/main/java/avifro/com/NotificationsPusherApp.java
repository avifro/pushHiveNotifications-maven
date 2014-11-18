package avifro.com;

import avifro.com.Entities.CloudStorageProviderEnum;
import avifro.com.Entities.MyTransfer;
import avifro.com.Services.CloudStorageProvider;
import avifro.com.Services.HiveActionsService;
import avifro.com.Services.ProwlActionsService;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by avifro on 11/6/14.
 */
public class NotificationsPusherApp {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String USERNAME_KEY = "userName";
    private final static String PASSWORD_KEY = "password";

    private CloudStorageProvider cloudStorageProvider;
    private ProwlActionsService prowlActionsService;
    private MyTransferDbHelper myTransferDbHelper;

    // TODO Temp solution : needs to be stored in DB instead
    private List<MyTransfer> myActiveTransfers = new ArrayList<>();

    private static NotificationsPusherApp instance = new NotificationsPusherApp();

    private NotificationsPusherApp() {}

    public static NotificationsPusherApp getInstance() {
        return instance;
    }

    public void setMyTransferDbHelper(MyTransferDbHelper myTransferDbHelper) {
        this.myTransferDbHelper = myTransferDbHelper;
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
        Validate.notBlank(token);
        if (prowlActionsService == null) {
            prowlActionsService = new ProwlActionsService(myAppName, myNotificationServiceKey);
        }

        List<MyTransfer> transfers;
        try {
            transfers = cloudStorageProvider.findMyTransfers(token);
        } catch (Exception e) {
            prowlActionsService.sendNotification("Notifications Push app has been terminated because of an error", e.getMessage());
            throw new RuntimeException(e);
        }

        pushNotifications(transfers);
    }

    private void pushNotifications(List<MyTransfer> myTransfers) {
        if (myTransfers.size() > 0) {
            for (MyTransfer myTransfer : myTransfers) {
                switch (myTransfer.getStatus()) {
                    case "Pending" :
                    case "Downloading" :
                        if (!myActiveTransfers.contains(myTransfer)) {
                            logger.info(myTransfer.getFilename() +  " - new download has been started");
                            // TODO needs to be modified once it's moved to DB
                            myActiveTransfers.add(myTransfer);
                            prowlActionsService.sendNotification("New Download has been started", myTransfer.getFilename());
                        }
                        break;
                    case "Complete" :
                        logger.info(myTransfer.getFilename() +  " - download has been finished");
                        // persist information in db
                        myTransferDbHelper.insertDoc(myTransfer);
                        // TODO needs to be modified once it's moved to DB
                        myActiveTransfers.remove(myTransfer);
                        prowlActionsService.sendNotification("Download finished", myTransfer.getFilename());
                        break;
                }
            }
        }
    }


}
