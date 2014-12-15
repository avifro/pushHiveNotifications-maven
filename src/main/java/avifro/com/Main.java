package avifro.com;

import avifro.com.Entities.CloudStorageProviderEnum;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String ROOT_HTTP_PATH = "https://api-beta.hive.im/api/";


    public static void main(String[] args) {
        NotificationsPusherApp app = NotificationsPusherApp.getInstance();
        String token = app.signIn(CloudStorageProviderEnum.HIVE, ROOT_HTTP_PATH);
        
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            System.out.println("Checking for new\\finished downloads...");
            app.startApp(token);} , 0, 10, TimeUnit.MINUTES);
    }

}
