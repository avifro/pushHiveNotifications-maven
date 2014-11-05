package avifro.com;

import avifro.com.Entities.MyTransfer;
import avifro.com.Services.HiveActionsService;
import avifro.com.Services.ProwlActionsService;

import java.util.List;

public class Main {

    private static final String MY_KEY = "7d02edc37983dbb3b0ed705b94bd77f34411fdbd";
    private static final String MY_APP_NAME = "Avifro notification app";

    private static final String ROOT_HTTP_PATH = "https://api-beta.hive.im/api/";

    public static void main(String[] args) {

        HiveActionsService hiveActionsService = new HiveActionsService(ROOT_HTTP_PATH);
        String token = hiveActionsService.getMyToken();
        List<MyTransfer> transfers = hiveActionsService.findTransfers(token);
        if (transfers.size() > 0) {
            for (MyTransfer transfer : transfers) {
                ProwlActionsService prowlActionsService = new ProwlActionsService(MY_APP_NAME, MY_KEY);
                prowlActionsService.sendNotification("Download started!", transfer.getName());
            }
        }
    }

}
