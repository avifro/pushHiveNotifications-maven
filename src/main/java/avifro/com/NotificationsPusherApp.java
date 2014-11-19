package avifro.com;

import avifro.com.Entities.CloudStorageProviderEnum;
import avifro.com.Entities.MyTransfer;
import avifro.com.Services.CloudStorageProvider;
import avifro.com.Services.HiveActionsService;
import avifro.com.Services.ProwlActionsService;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
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

    private static NotificationsPusherApp app;

    private NotificationsPusherApp() {}

    public static NotificationsPusherApp getInstance() {
        if (app == null) {
            app = new NotificationsPusherApp();
            initApp();
        }
        return app;
    }

    private static void initApp() {
        PropertiesHandler propertiesHandler = PropertiesHandler.getInstance();
        NotificationsPusherApp app = NotificationsPusherApp.getInstance();
        String dbHostName = propertiesHandler.getProperty("dbHost", "localhost");
        int dbPort = Integer.valueOf(propertiesHandler.getProperty("dbPort", "27017"));
        try {
            MongoClient mongoClient = new MongoClient(dbHostName, dbPort);
            DB db = mongoClient.getDB(propertiesHandler.getProperty("dbName", "pushDownloadNotifications"));
            MyTransferDbHelper myTransferDbHelper = new MyTransferDbHelper();
            myTransferDbHelper.setMongoDB(db);
            myTransferDbHelper.createCollection(propertiesHandler.getProperty("dbCollection"));
            app.setMyTransferDbHelper(myTransferDbHelper);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Couldn't connect to DB possibly because of unknown host: " + dbHostName);
        }
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

        PropertiesHandler propertiesHandler = PropertiesHandler.getInstance();
        return cloudStorageProvider.getMyToken(propertiesHandler.getProperty(USERNAME_KEY), propertiesHandler.getProperty(PASSWORD_KEY));
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
            PropertiesHandler propertiesHandler = PropertiesHandler.getInstance();
            String collectionDbName = propertiesHandler.getProperty("dbCollection");
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
                        myTransferDbHelper.insertDoc(collectionDbName, myTransfer);
                        // TODO needs to be modified once it's moved to DB
                        myActiveTransfers.remove(myTransfer);
                        prowlActionsService.sendNotification("Download finished", myTransfer.getFilename());
                        break;
                }
            }
        }
    }


}
