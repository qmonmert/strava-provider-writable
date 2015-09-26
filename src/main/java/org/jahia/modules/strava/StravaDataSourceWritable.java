package org.jahia.modules.strava;

import com.google.common.collect.Sets;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jahia.modules.external.ExternalData;
import org.jahia.modules.external.ExternalDataSource;
import org.jahia.modules.external.ExternalQuery;
import org.jahia.modules.external.query.QueryHelper;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by Quentin on 12/09/15.
 */
public class StravaDataSourceWritable implements ExternalDataSource, ExternalDataSource.Writable, ExternalDataSource.Searchable {

    // Strava API
    private static final String API_V3_UPLOADS    = "/api/v3/uploads";
    private static final String API_V3_ACTIVITIES = "/api/v3/athlete/activities";
    private static final String URL_STRAVA        = "www.strava.com";
    private static final String ACCES_TOKEN       = "access_token";
    private static final String PER_PAGE          = "per_page";
    private static final String HTTPS             = "https://";
    private static final String ACTIVITY_TYPE     = "activity_type";
    private static final String FILE              = "file";
    private static final String DATA_TYPE         = "data_type";
    private static final String AUTHORIZATION     = "Authorization";
    private static final String BEARER            = "Bearer";
    private static final String RUN               = "run";
    private static final String RIDE              = "ride";
    private static final String GPX               = "gpx";
    private static final Integer PORT_STRAVA      = 443;

    // Http client
    private HttpClient httpClient;
    private int HTTP_400 = 400;

    // Cache
    private EhCacheProvider ehCacheProvider;
    private Ehcache cache;
    private static final String CACHE_NAME              = "strava-cache";
    private static final String CACHE_STRAVA_ACTVITIES  = "cacheStravaActivities";

    // Utils
    private static final String ENCTYPE = "enctype";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";

    // Node types
    private static final String JNT_STRAVA_ACTIVITY = "jnt:stravaActivity";
    private static final String JNT_CONTENT_FOLDER  = "jnt:contentFolder";

    // Strava keys account
    private String apiKeyValue;
    private String apiKeyValuePost;

    // Properties : strava
    private static final String ID       = "id";
    private static final String NAME     = "name";
    private static final String DISTANCE = "distance";
    private static final String TYPE     = "type";
    private static final String FILENAME = "filename";

    // Properties : JCR
    private static final String ROOT  = "root";

    // Constants
    private static final String ACTIVITY             = "activity";
    private static final String NB_ACTIVITIES_LOADED = "20";

    // CONSTRUCTOR

    public StravaDataSourceWritable() {
        httpClient = new HttpClient();
    }

    // GETTERS AND SETTERS

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    public void setApiKeyValuePost(String apiKeyValuePost) {
        this.apiKeyValuePost = apiKeyValuePost;
    }

    public void setCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }

    // METHODS

    public void start() {
        // Init method defined in the bean : StravaDataSourceWritable
        try {
            if (!ehCacheProvider.getCacheManager().cacheExists(CACHE_NAME)) {
                ehCacheProvider.getCacheManager().addCache(CACHE_NAME);
            }
            cache = ehCacheProvider.getCacheManager().getCache(CACHE_NAME);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (CacheException e) {
            e.printStackTrace();
        }
    }

    // UTILS

    private JSONArray queryStrava(String path, String... params) throws RepositoryException {
        try {
            HttpsURL url = new HttpsURL(URL_STRAVA, PORT_STRAVA, path);
            // Params
            Map<String, String> m = new LinkedHashMap<>();
            m.put(ACCES_TOKEN, apiKeyValue);
            m.put(PER_PAGE, NB_ACTIVITIES_LOADED);
            url.setQuery(m.keySet().toArray(new String[m.size()]), m.values().toArray(new String[m.size()]));
            System.out.println("=> Start request strava-writable : " + url);
            GetMethod httpMethod = new GetMethod(url.toString());
            try {
                httpClient.executeMethod(httpMethod);
                cache.put(new Element(CACHE_STRAVA_ACTVITIES, new JSONArray(httpMethod.getResponseBodyAsString())));
                return new JSONArray(httpMethod.getResponseBodyAsString());
            } finally {
                httpMethod.releaseConnection();
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    private JSONArray getCacheStravaActivities(boolean deleteCache) throws RepositoryException {
        JSONArray activities;
        if (cache.get(CACHE_STRAVA_ACTVITIES) != null && !deleteCache) {
            activities = (JSONArray) cache.get(CACHE_STRAVA_ACTVITIES).getObjectValue();
        } else {
            activities = queryStrava(API_V3_ACTIVITIES);
        }
        return activities;
    }

    // IMPLEMENTS : ExternalDataSource

    @Override
    public List<String> getChildren(String path) throws RepositoryException {
        List<String> r = new ArrayList<>();
        if (path.equals("/")) {
            try {
                JSONArray activities = getCacheStravaActivities(false);
                for (int i = 1; i <= activities.length(); i++) {
                    JSONObject activity = (JSONObject) activities.get(i - 1);
                    r.add((i <= 9 ? "0" : "") + i + "-" + ACTIVITY + "-" + activity.get(ID));
                }
            } catch (JSONException e) {
                throw new RepositoryException(e);
            }
        }
        return r;
    }

    @Override
    public ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
        if (identifier.equals(ROOT)) {
            return new ExternalData(identifier, "/", JNT_CONTENT_FOLDER, new HashMap<String, String[]>());
        }
        Map<String, String[]> properties = new HashMap<>();
        String[] numActivity = identifier.split("-");
        if (numActivity.length == 3) {
            try {
                JSONArray activities = getCacheStravaActivities(false);
                JSONObject activity = (JSONObject) activities.get(Integer.parseInt(numActivity[0]) - 1);
                properties.put(NAME, new String[]{activity.getString(NAME)});
                properties.put(DISTANCE, new String[]{activity.getString(DISTANCE)});
                properties.put(TYPE, new String[]{activity.getString(TYPE)});
                ExternalData data = new ExternalData(identifier, "/" + identifier, JNT_STRAVA_ACTIVITY, properties);
                return data;
            } catch (Exception e) {
                throw new ItemNotFoundException(identifier);
            }
        } else {
            throw new ItemNotFoundException(identifier);
        }
    }

    @Override
    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        String[] splitPath = path.split("/");
        try {
            if (splitPath.length <= 1) {
                return getItemByIdentifier(ROOT);
            } else {
                return getItemByIdentifier(splitPath[1]);
            }
        } catch (ItemNotFoundException e) {
            throw new PathNotFoundException(e);
        }
    }

    @Override
    public Set<String> getSupportedNodeTypes() {
        return Sets.newHashSet(JNT_CONTENT_FOLDER, JNT_STRAVA_ACTIVITY);
    }

    @Override
    public boolean isSupportsHierarchicalIdentifiers() {
        return false;
    }

    @Override
    public boolean isSupportsUuid() {
        return false;
    }

    @Override
    public boolean itemExists(String path) {
        return false;
    }

    // Implements : ExternalDataSource.Searchable

    public List<String> search(ExternalQuery query) throws RepositoryException {
        List<String> results = new ArrayList<>();
        String nodeType = QueryHelper.getNodeType(query.getSource());
        if (NodeTypeRegistry.getInstance().getNodeType(JNT_STRAVA_ACTIVITY).isNodeType(nodeType)) {
            try {
                JSONArray activities = getCacheStravaActivities(false);
                for (int i = 1; i <= activities.length(); i++) {
                    JSONObject activity = (JSONObject) activities.get(i - 1);
                    results.add("/" + (i <= 9 ? "0" : "") + i + "-" + ACTIVITY + "-" + activity.get(ID));
                }
            } catch (JSONException e) {
                throw new RepositoryException(e);
            }
        }
        return results;
    }

    // Implements : ExternalDataSource.Writable

    @Override
    public void move(String oldPath, String newPath) throws RepositoryException {

    }

    @Override
    public void order(String path, List<String> children) throws RepositoryException {

    }

    @Override
    public void removeItemByPath(String path) throws RepositoryException {

    }

    @Override
    public void saveItem(ExternalData data) throws RepositoryException {
        // Var
        org.apache.http.client.HttpClient httpClient;
        HttpResponse response;
        int statusCode = 200;

        // Retrieve the path of the file to upload in Strava
        String filename = data.getProperties().get(FILENAME)[0];

        // Prepare the post
        HttpPost httpPost = new HttpPost(HTTPS + URL_STRAVA + API_V3_UPLOADS);
        httpPost.addHeader(AUTHORIZATION, BEARER + " " + apiKeyValuePost);
        httpPost.setHeader(ENCTYPE, MULTIPART_FORM_DATA);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            reqEntity.addPart(ACTIVITY_TYPE, new StringBody(RUN));
            reqEntity.addPart(DATA_TYPE, new StringBody(GPX));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        FileBody body = new FileBody(new File(filename));
        reqEntity.addPart(FILE, body);
        httpPost.setEntity(reqEntity);

        try {

            while (statusCode != HTTP_400) {

                httpClient = new DefaultHttpClient();

                // Execute
                response = httpClient.execute(httpPost);

                // Look the response
                HttpEntity respEntity = response.getEntity();
                if (respEntity != null) {
                    String content = EntityUtils.toString(respEntity);
                    System.out.println(content);
                }

                // Status code
                statusCode = response.getStatusLine().getStatusCode();

                // Consume
                EntityUtils.consumeQuietly(response.getEntity());
            }

            // Wait the upload of the file on Strava
            // Strava says : "The mean processing time is currently around 8 seconds"
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Empty cache
            getCacheStravaActivities(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
