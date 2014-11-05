package avifro.com.Services;

import avifro.com.ClientHelper;
import avifro.com.Entities.MyTransfer;
import com.google.gson.Gson;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by avifro on 11/1/14.
 */
public class HiveActionsService {

    private final static String CLIENT_TYPE_ATT = "Client-Type";
    private final static String WEB_CLIENT_TYPE_VALUE = "WEB";
    private final static String BROWSER_CLIENT_TYPE_VALUE = "Browser";

    private final static String CLIENT_VERSION_ATT = "Client-Version";
    private final static String CLIENT_VERSION_VALUE = "0.1";

    private final static String AUTHORIZATION_ATT = "Authorization";

    private final static String EMAIL_KEY = "email";
    private final static String PASSWORD_KEY = "password";

    private WebTarget rootTarget;
    private Properties properties = new Properties();

    public HiveActionsService(String rootUrl) {
        init(rootUrl);
    }

    private void init(String rootUrl) {
        ClientHelper helper = new ClientHelper();
        ClientBuilder builder = new JerseyClientBuilder();
        builder.sslContext(helper.getDefaultSSLContext());
        builder.hostnameVerifier(helper.getDefualtHostnameVerifier());
        Client client = builder.build();
        try {
            properties.load((getClass().getResourceAsStream("/settings.properties")));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read properties file");
        }
        rootTarget = client.target(rootUrl);
    }

    public String getMyToken() {
        WebTarget signInTarget = rootTarget.path("/user/sign-in/");
        signInTarget.queryParam(CLIENT_TYPE_ATT, WEB_CLIENT_TYPE_VALUE);
        signInTarget.queryParam(CLIENT_VERSION_ATT, CLIENT_VERSION_VALUE);

        Form form = new Form();
        form.param(EMAIL_KEY, properties.getProperty(EMAIL_KEY));
        form.param(PASSWORD_KEY, properties.getProperty(PASSWORD_KEY));


        Invocation.Builder invocation = signInTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocation.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        String data = response.readEntity(String.class);
        Gson gson = new Gson();
        Map jsonObject = (Map) gson.fromJson(data, Object.class);
        return (String)((Map)jsonObject.get("data")).get("token");
    }

    public List<MyTransfer> findTransfers(String token) {
        List<MyTransfer> myTransfers = new ArrayList<MyTransfer>();
        WebTarget getTransferListTarget = rootTarget.path("/hive/get/");
        Invocation.Builder invocation = getTransferListTarget.request(MediaType.APPLICATION_JSON);
        invocation.header(CLIENT_TYPE_ATT, BROWSER_CLIENT_TYPE_VALUE);
        invocation.header(CLIENT_VERSION_ATT, CLIENT_VERSION_VALUE);
        invocation.header(AUTHORIZATION_ATT, token);

        Response response = invocation.get();
        String data = response.readEntity(String.class);


        return myTransfers;
    }

}
