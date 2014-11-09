package avifro.com.Services;

import avifro.com.ClientHelper;
import avifro.com.Entities.MyTransfer;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
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
    private ObjectMapper mapper = new ObjectMapper();

    public HiveActionsService(String rootUrl) {
        init(rootUrl);
    }

    private void init(String rootUrl) {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
        List<MyTransfer> myTransfers;
        WebTarget getTransferListTarget = rootTarget.path("/transfer/list/");
        Invocation.Builder invocation = buildInvocation(getTransferListTarget, token);
        Response response = invocation.get();
        String responseContent = response.readEntity(String.class);

        JSONArray jsonArray = JsonPath.read(responseContent, "$.data");
        try {
            myTransfers = mapper.readValue(jsonArray.toJSONString(), TypeFactory.collectionType(List.class, MyTransfer.class));
        } catch (IOException e) {
            throw new RuntimeException("Transfers JSON object couldn't be mapped! ", e);
        }
        return myTransfers;
    }

    private Invocation.Builder buildInvocation(WebTarget webTarget, String token) {
        Invocation.Builder invocation = webTarget.request(MediaType.APPLICATION_JSON);
        invocation.header(CLIENT_TYPE_ATT, BROWSER_CLIENT_TYPE_VALUE);
        invocation.header(CLIENT_VERSION_ATT, CLIENT_VERSION_VALUE);
        invocation.header(AUTHORIZATION_ATT, token);
        return invocation;
    }

}
