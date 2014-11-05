package avifro.com.Services;

import net.sourceforge.prowl.api.DefaultProwlEvent;
import net.sourceforge.prowl.api.ProwlClient;
import net.sourceforge.prowl.api.ProwlEvent;
import net.sourceforge.prowl.exception.ProwlException;

/**
 * Created by avifro on 11/5/14.
 */
public class ProwlActionsService {

    private final String appName;
    private final String apiKey;

    public ProwlActionsService(String appName, String apiKey) {
        this.appName = appName;
        this.apiKey = apiKey;
    }

    public void sendNotification(String event, String description) {
        ProwlClient prowlClient = new ProwlClient();
        ProwlEvent prowlEvent = new DefaultProwlEvent( apiKey, appName, event, description, 0);
        try {
            String message = prowlClient.pushEvent(prowlEvent);
            System.out.println(message);
        } catch (ProwlException e1) {
            e1.printStackTrace();
        }
    }

}
