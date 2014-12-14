package avifro.com;

import avifro.com.Entities.CloudStorageProviderEnum;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String MY_NOTIFICATION_SERVICE_KEY = "7d02edc37983dbb3b0ed705b94bd77f34411fdbd";
    private static final String MY_APP_NAME = "Avifro notification app";

    private static final String ROOT_HTTP_PATH = "https://api-beta.hive.im/api/";


    public static void main(String[] args) {
        NotificationsPusherApp app = NotificationsPusherApp.getInstance();
        String token = app.signIn(CloudStorageProviderEnum.HIVE, ROOT_HTTP_PATH);
        
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            System.out.println("Checking for new\\finished downloads...");
            app.startApp(token, MY_NOTIFICATION_SERVICE_KEY, MY_APP_NAME);} , 0, 10, TimeUnit.MINUTES);
    }

}
