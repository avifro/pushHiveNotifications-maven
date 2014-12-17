package avifro.com.Services;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avifro on 12/14/14.
 */
public class EmailActionsService implements NotificationActionsService {

    private String username;
    private String password;

    public EmailActionsService(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void sendNotification(String event, String description) {
        Email email = new SimpleEmail();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator(username, password));
        email.setSSLOnConnect(true);

        // TODO - Get emails addresses from db
        List<String> recipients = new ArrayList<>();
        recipients.add("avifro@gmail.com");

        try {
            for (String recipient : recipients) {
                email.addTo(recipient);
            }
            email.setFrom("avifro@gmail.com");
            email.setSubject("TestMail");
            email.setMsg("This is a test mail ... :-)");
            email.send();
        } catch (EmailException e) {
            throw new RuntimeException("Couldn't send email notification", e);
        }
    }
}
