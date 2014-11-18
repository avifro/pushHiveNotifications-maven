package avifro.com;

import avifro.com.Entities.CloudStorageProviderEnum;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class Main {

    private static final String MY_NOTIFICATION_SERVICE_KEY = "7d02edc37983dbb3b0ed705b94bd77f34411fdbd";
    private static final String MY_APP_NAME = "Avifro notification app";

    private static final String ROOT_HTTP_PATH = "https://api-beta.hive.im/api/";


    public static void main(String[] args) {
        NotificationsPusherApp app = initApp();
        String token = app.signIn(CloudStorageProviderEnum.HIVE, ROOT_HTTP_PATH);
        while (true) {
            try {
                System.out.println("Checking for new\\ finished downloads...");
                app.startApp(token, MY_NOTIFICATION_SERVICE_KEY, MY_APP_NAME);
                Thread.sleep(3600000);
            } catch (InterruptedException e) {
                //Do nothing
            }
        }
    }

    private static NotificationsPusherApp initApp() {
        final String DB_HOST_NAME = "localhost";
        NotificationsPusherApp app = NotificationsPusherApp.getInstance();
        try {
            MongoClient mongoClient = new MongoClient(DB_HOST_NAME);
            DB db = mongoClient.getDB("pushDownloadNotifications");
            MyTransferDbHelper myTransferDbHelper = new MyTransferDbHelper();
            myTransferDbHelper.setMongoDB(db);
            myTransferDbHelper.createDownloadsCollection();
            app.setMyTransferDbHelper(myTransferDbHelper);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Couldn't connect to DB possibly because of unknown host: " + DB_HOST_NAME);
        }
        return app;
    }

}
