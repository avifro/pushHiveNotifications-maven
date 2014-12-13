package avifro.com.Services;

import avifro.com.ClientHelper;
import avifro.com.Entities.MyFile;
import avifro.com.Entities.MyTransfer;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * Created by avifro on 11/1/14.
 */
public class HiveActionsService implements CloudStorageProvider {

    private Logger logger = LoggerFactory.getLogger(getClass());


    private final static String CLIENT_TYPE_ATT = "Client-Type";
    private final static String WEB_CLIENT_TYPE_VALUE = "WEB";
    private final static String BROWSER_CLIENT_TYPE_VALUE = "Browser";

    private final static String CLIENT_VERSION_ATT = "Client-Version";
    private final static String CLIENT_VERSION_VALUE = "0.1";

    private final static String AUTHORIZATION_ATT = "Authorization";

    private final static String EMAIL_KEY = "email";
    private final static String PASSWORD_KEY = "password";

    private final static String FOLDER_ID_KEY = "parentId";
    private final static String VIDEO_FOLDER_ID_KEY = "parent";
    private final static String VIDEO_ID_KEY = "hiveId";

    private WebTarget rootTarget;
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
    public List<MyFile> findFilesByFolderId(long folderId, String token) {
        List<MyFile> myFiles;
        WebTarget getTransferListTarget = rootTarget.path("/hive/get-children/");
        Invocation.Builder invocation = buildInvocation(getTransferListTarget, token);

        Form form = new Form();
        form.param(FOLDER_ID_KEY, String.valueOf(folderId));

        Response response = invocation.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        String responseContent = response.readEntity(String.class);

        JSONArray jsonArray = JsonPath.read(responseContent, "$.data");
        try {
            myFiles = mapper.readValue(jsonArray.toJSONString(), TypeFactory.collectionType(List.class, MyFile.class));
        } catch (IOException e) {
            throw new RuntimeException("Transfers JSON object couldn't be mapped! ", e);
        }
        return myFiles;
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

    @Override
    public long findFolderIdByType(String token, String type) {
        WebTarget getTransferListTarget = rootTarget.path("/hive/get/");
        Invocation.Builder invocation = buildInvocation(getTransferListTarget, token);
        Response response = invocation.get();
        String responseContent = response.readEntity(String.class);
        String videoFolderId = (String)((JSONArray)JsonPath.read(responseContent, "$.data[?(@.type=='" + type + "')].id")).get(0);
        return Long.valueOf(videoFolderId);
    }

    @Override
    public void moveFolderContentToAnotherFolder(long sourceFolderId, long destinationFolderId, String token) {
        WebTarget moveToTarget = rootTarget.path("hive/move/");
        Invocation.Builder invocation = buildInvocation(moveToTarget, token);

        List<MyFile> completedTransfers = findFilesByFolderId(sourceFolderId, token);

        for (MyFile myFile : completedTransfers) {
            Form form = new Form();
            form.param(VIDEO_ID_KEY, String.valueOf(myFile.getStorageProviderId()));
            form.param(VIDEO_FOLDER_ID_KEY, String.valueOf(destinationFolderId));

            Response response = invocation.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
            String data = response.readEntity(String.class);
            if (!JsonPath.read(data, "$.status").equals("success")) {
                logger.warn("Couldn't move movie with id " + destinationFolderId + " to videos folder");
            }
        }
    }

    private Invocation.Builder buildInvocation(WebTarget webTarget, String token) {
        Invocation.Builder invocation = webTarget.request(MediaType.APPLICATION_JSON);
        invocation.header(CLIENT_TYPE_ATT, BROWSER_CLIENT_TYPE_VALUE);
        invocation.header(CLIENT_VERSION_ATT, CLIENT_VERSION_VALUE);
        invocation.header(AUTHORIZATION_ATT, token);
        return invocation;
    }

}
