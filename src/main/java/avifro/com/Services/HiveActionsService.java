package avifro.com.Services;

import avifro.com.ClientHelper;
import avifro.com.Entities.MyTransfer;
import com.jayway.jsonpath.JsonPath;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by avifro on 11/1/14.
 */
public class HiveActionsService implements CloudStorageProvider {

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

    @Override
    public String getMyToken(String userName, String password) {
        WebTarget signInTarget = rootTarget.path("/user/sign-in/");
        signInTarget.queryParam(CLIENT_TYPE_ATT, WEB_CLIENT_TYPE_VALUE);
        signInTarget.queryParam(CLIENT_VERSION_ATT, CLIENT_VERSION_VALUE);

        Form form = new Form();
        form.param(EMAIL_KEY, userName);
        form.param(PASSWORD_KEY, password);

        Invocation.Builder invocation = signInTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocation.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        String data = response.readEntity(String.class);
        return JsonPath.read(data, "$.data.token");
    }

    @Override
    public List<MyTransfer> findMyTransfers(String token) {
        List<MyTransfer> myTransfers = new ArrayList<MyTransfer>();
        WebTarget getFirstLevelDirectoriesTarget = rootTarget.path("/hive/get/");
        Invocation.Builder invocation = buildInvocation(getFirstLevelDirectoriesTarget, token);
        Response response = invocation.get();
        String responseContent = response.readEntity(String.class);
        String transfersDirectoryId = extractTransfersDirectoryId(responseContent);

        WebTarget getTransferListTarget = rootTarget.path("/hive/get-children/");
        invocation = buildInvocation(getTransferListTarget, token);
        Form form = new Form();
        form.param("parentId", transfersDirectoryId);
        form.param("filter", "folder");
        form.param("order","dateModified");
        form.param("sort", "desc");

        response = invocation.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        responseContent = response.readEntity(String.class);


        return myTransfers;
    }

    private Invocation.Builder buildInvocation(WebTarget webTarget, String token) {
        Invocation.Builder invocation = webTarget.request(MediaType.APPLICATION_JSON);
        invocation.header(CLIENT_TYPE_ATT, BROWSER_CLIENT_TYPE_VALUE);
        invocation.header(CLIENT_VERSION_ATT, CLIENT_VERSION_VALUE);
        invocation.header(AUTHORIZATION_ATT, token);
        return invocation;
    }

    private String extractTransfersDirectoryId(String firstLevelFoldersResponse) {
        return JsonPath.read(firstLevelFoldersResponse, "$.data[?(@.type=='transfer')].id");
    }

}
