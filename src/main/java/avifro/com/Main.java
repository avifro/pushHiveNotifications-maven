package avifro.com;

import avifro.com.Entities.MyTransfer;
import avifro.com.Services.HiveActions;
import net.sourceforge.prowl.api.DefaultProwlEvent;
import net.sourceforge.prowl.api.ProwlClient;
import net.sourceforge.prowl.api.ProwlEvent;
import net.sourceforge.prowl.exception.ProwlException;

import java.util.List;

public class Main {

    private static final String MY_KEY = "7d02edc37983dbb3b0ed705b94bd77f34411fdbd";
    private static final String MY_APP_NAME = "Avifro notification app";

    private static final String ROOT_HTTP_PATH = "https://api-beta.hive.im/api/";

    public static void main(String[] args) {
        ProwlClient prowlClient = new ProwlClient();
        ProwlEvent prowlEvent = new DefaultProwlEvent(
                MY_KEY, MY_APP_NAME, "Download completed",
                "It's just a test", 0);
        try {
            String message = prowlClient.pushEvent(prowlEvent);
            System.out.println(message);
        } catch (ProwlException e1) {
            e1.printStackTrace();
        }

        HiveActions hiveActions = new HiveActions(ROOT_HTTP_PATH);
        String token = hiveActions.getMyToken();
        List<MyTransfer> transfers = hiveActions.findTransfers(token);

    }

}
