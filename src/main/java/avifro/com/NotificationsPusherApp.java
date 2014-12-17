package avifro.com;

import avifro.com.Entities.CloudStorageProviderEnum;
import avifro.com.Entities.MyTransfer;
import avifro.com.Services.*;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by avifro on 11/6/14.
 */
public class NotificationsPusherApp {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private PropertiesHandler propertiesHandler;
    private CloudStorageProvider cloudStorageProvider;
    private List<NotificationActionsService> notificationSenders;
    private MyTransferRepository myTransferRepository;

    // TODO Temp solution : needs to be stored in DB instead
    private List<MyTransfer> myActiveTransfers = new ArrayList<>();
    private long videoFolderId;
    private long transferFolderId;

    private static NotificationsPusherApp app;

    private NotificationsPusherApp() {}

    public static NotificationsPusherApp getInstance() {
        if (app == null) {
            app = new NotificationsPusherApp();
            app.initApp();
        }
        return app;
    }

    private void initApp() {
        propertiesHandler = PropertiesHandler.getInstance();
        NotificationsPusherApp app = NotificationsPusherApp.getInstance();

        // Setting up DB settings
        String dbHostName = propertiesHandler.getProperty(PropertiesHandler.DB_HOST_KEY, "localhost");
        System.out.println("Current DB host is: " + dbHostName);

        int dbPort = Integer.valueOf(propertiesHandler.getProperty(PropertiesHandler.DB_PORT_KEY, "27017"));
        try {
            String dbName = propertiesHandler.getProperty(PropertiesHandler.DB_NAME_KEY, "pushDownloadNotifications");
            MongoCredential credential = MongoCredential.createMongoCRCredential(propertiesHandler.getProperty(PropertiesHandler.DB_USER_NAME_KEY),
                                                                                 dbName,
                                                                                 propertiesHandler.getProperty(PropertiesHandler.DB_PASSWORD_KEY).toCharArray());
            MongoClient mongoClient = new MongoClient(new ServerAddress(dbHostName, dbPort), Arrays.asList(credential));
            DB db = mongoClient.getDB(dbName);
            MyTransferRepository myTransferRepository = new MyTransferRepository();
            myTransferRepository.setMongoDB(db);
            myTransferRepository.createCollection(propertiesHandler.getProperty(PropertiesHandler.DB_COLLECTION_KEY));
            app.setMyTransferRepository(myTransferRepository);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Couldn't connect to DB possibly because of unknown host: " + dbHostName);
        }

        initNotificationSendersList();
    }

    // Setting up notification actions services
    private void initNotificationSendersList() {
        //TODO needs to do it dynamically
        notificationSenders = new ArrayList<>(2);
        NotificationActionsService prowlActionsService = new ProwlActionsService(propertiesHandler.getProperty(PropertiesHandler.MY_APP_NAME_KEY),
                                                                                 propertiesHandler.getProperty(PropertiesHandler.PROWL_API_KEY));
        NotificationActionsService emailNotificationActionsService = new EmailActionsService(propertiesHandler.getProperty(PropertiesHandler.USER_NAME_KEY),
                                                                                             propertiesHandler.getProperty(PropertiesHandler.PASSWORD_KEY));
        notificationSenders.add(prowlActionsService);
        notificationSenders.add(emailNotificationActionsService);
    }

    public void setMyTransferRepository(MyTransferRepository myTransferRepository) {
        this.myTransferRepository = myTransferRepository;
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
        return cloudStorageProvider.getMyToken(propertiesHandler.getProperty(PropertiesHandler.USER_NAME_KEY),
                                               propertiesHandler.getProperty(PropertiesHandler.PASSWORD_KEY));
    }

    public void startApp(String token) {
        Validate.notBlank(token);

        if (videoFolderId == 0) {
            videoFolderId = cloudStorageProvider.findFolderIdByType(token, "video");
        }
        if (transferFolderId == 0) {
            transferFolderId = cloudStorageProvider.findFolderIdByType(token, "transfer");
        }

        List<MyTransfer> transfers;
        try {
            transfers = cloudStorageProvider.findMyTransfers(token);
        } catch (Exception e) {
            notificationSenders.forEach((n) -> n.sendNotification("Notifications Push app has been terminated because of an error", e.getMessage()));
            throw new RuntimeException(e);
        }

        pushNotifications(transfers);
        cloudStorageProvider.moveFolderContentToAnotherFolder(transferFolderId, videoFolderId, token);
    }

    private void pushNotifications(List<MyTransfer> myTransfers) {
        if (myTransfers.size() > 0) {
            PropertiesHandler propertiesHandler = PropertiesHandler.getInstance();
            String collectionDbName = propertiesHandler.getProperty("dbCollection");
            for (MyTransfer myTransfer : myTransfers) {
                switch (myTransfer.getStatus()) {
                    case "Pending" :
                    case "Downloading" :
                        if (!myActiveTransfers.contains(myTransfer) &&
                            !myTransferRepository.exists(collectionDbName, myTransfer.getFilename())) {
                            logger.info(myTransfer.getFilename() + " - new download has been started");
                            myActiveTransfers.add(myTransfer);
                            notificationSenders.forEach((n) -> n.sendNotification("New Download has been started", myTransfer.getFilename()));
                        }
                        break;
                    case "Encoded" :
                    case "Complete" :
                        if (!myTransferRepository.exists(collectionDbName, myTransfer.getFilename())) {
                            logger.info(myTransfer.getFilename() +  " - download has been finished");
                            // persist information in db
                            myTransferRepository.insertDoc(collectionDbName, myTransfer);
                            notificationSenders.forEach((n) -> n.sendNotification("Download is finished", myTransfer.getFilename()));
                        }
                        myActiveTransfers.remove(myTransfer);
                        break;
                }
            }
        }
    }

}
