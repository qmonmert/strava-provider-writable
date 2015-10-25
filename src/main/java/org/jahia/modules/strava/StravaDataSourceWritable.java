package org.jahia.modules.strava;

import com.google.common.collect.Sets;
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
import org.apache.log4j.Logger;
import org.jahia.modules.external.ExternalData;
import org.jahia.modules.external.ExternalDataSource;
import org.jahia.modules.external.ExternalQuery;
import org.jahia.modules.external.query.QueryHelper;
import org.jahia.modules.strava.utils.StravaUtils;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.File;
import java.util.*;

/**
 * Created by Quentin on 12/09/15.
 */
public class StravaDataSourceWritable implements ExternalDataSource, ExternalDataSource.Writable, ExternalDataSource.Searchable {

    // Logger
    private static final Logger LOGGER = Logger.getLogger(StravaDataSourceWritable.class);

    // Strava API
    private static final String API_V3_UPLOADS    = "/api/v3/uploads";
    private static final String API_V3_ACTIVITIES = "/api/v3/athlete/activities";
    private static final String URL_STRAVA        = "www.strava.com";
    private static final String ACCES_TOKEN       = "access_token";
    private static final String PER_PAGE          = "per_page";
    private static final String HTTPS             = "https://";
    private static final String FILE              = "file";
    private static final String DATA_TYPE         = "data_type";
    private static final String GPX               = "gpx";
    private static final String TCX               = "tcx";
    private static final String FIT               = "fit";
    private static final String AUTHORIZATION     = "Authorization";
    private static final String BEARER            = "Bearer";
    private static final String ACTIVITY_READY    = "Your activity is ready.";
    private static final Integer PORT_STRAVA      = 443;

    // Http client
    private HttpClient httpClient;

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
    private static final String ID          = "id";
    private static final String NAME        = "name";
    private static final String DISTANCE    = "distance";
    private static final String TYPE        = "type";
    private static final String MOVING_TIME = "moving_time";
    private static final String START_DATE  = "start_date";
    private static final String FILENAME    = "filename";

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
        } catch (Exception e) {
            LOGGER.error("Error with the cache : " + e.getMessage());
        }
    }

    // UTILS

    /**
     * Execute a GET on the strava API : https://www.strava.com:443/ + path + params
     * Example :
     *      - https://www.strava.com:443/api/v3/athlete/activities?access_token=xxx&per_page=20
     */
    private JSONArray queryStrava(String path, String... params) throws RepositoryException {
        try {
            HttpsURL url = new HttpsURL(URL_STRAVA, PORT_STRAVA, path);
            // Params
            Map<String, String> m = new LinkedHashMap<>();
            m.put(ACCES_TOKEN, apiKeyValue);
            m.put(PER_PAGE, NB_ACTIVITIES_LOADED);
            url.setQuery(m.keySet().toArray(new String[m.size()]), m.values().toArray(new String[m.size()]));
            LOGGER.info("GET : " + url);
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

    /**
     * Execute a GET on the strava API : https://www.strava.com:443/ + path
     *  with a header : -H "Authorization: Bearer ${apiKeyValuePost}"
     * Example :
     *      - https://www.strava.com:443//api/v3/uploads/987654321
     */
    private JSONObject queryStravaJSONObject(String path) throws RepositoryException {
        try {
            HttpsURL url = new HttpsURL(URL_STRAVA, PORT_STRAVA, path);
            LOGGER.info("GET : " + url);
            GetMethod httpMethod = new GetMethod(url.toString());
            httpMethod.addRequestHeader(AUTHORIZATION, BEARER + " " + apiKeyValuePost);
            try {
                httpClient.executeMethod(httpMethod);
                return new JSONObject(httpMethod.getResponseBodyAsString());
            } finally {
                httpMethod.releaseConnection();
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Retieve all the strava activities.
     * If deleteCache
     *      retrieve the activities on strava (exemple : after the upload of a new activity)
     * Else
     *      retrieve the activities on cache
     */
    private JSONArray getCacheStravaActivities(boolean deleteCache) throws RepositoryException {
        JSONArray activities;
        if (cache.get(CACHE_STRAVA_ACTVITIES) != null && !deleteCache) {
            activities = (JSONArray) cache.get(CACHE_STRAVA_ACTVITIES).getObjectValue();
        } else {
            LOGGER.info("Refresh the activities");
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
                    r.add(StravaUtils.displayNumberTwoDigits(i) + "-" + ACTIVITY + "-" + activity.get(ID));
                }
                // r contains all the system name of the activities
                // with their ID activity and a prefix to order the nodes (activities)
                // example : 08-activity-401034489
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
        String[] idActivity = identifier.split("-");
        if (idActivity.length == 3) {
            try {
                JSONArray activities = getCacheStravaActivities(false);
                // Find the activity by its identifier
                int numActivity = Integer.parseInt(idActivity[0]) - 1;
                JSONObject activity = (JSONObject) activities.get(numActivity);
                // Add some properties
                properties.put(ID,          new String[]{ activity.getString(ID)   });
                properties.put(NAME,        new String[]{ activity.getString(NAME) });
                properties.put(TYPE,        new String[]{ activity.getString(TYPE) });
                properties.put(DISTANCE,    new String[]{ StravaUtils.displayDistance(activity.getString(DISTANCE))      });
                properties.put(MOVING_TIME, new String[]{ StravaUtils.displayMovingTime(activity.getString(MOVING_TIME)) });
                properties.put(START_DATE,  new String[]{ StravaUtils.displayStartDate(activity.getString(START_DATE))   });
                // Return the external data (a node)
                ExternalData data = new ExternalData(identifier, "/" + identifier, JNT_STRAVA_ACTIVITY, properties);
                return data;
            } catch (Exception e) {
                throw new ItemNotFoundException(identifier);
            }
        } else {
            // Node not again created
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

    @Override
    public List<String> search(ExternalQuery query) throws RepositoryException {
        List<String> paths = new ArrayList<>();
        String nodeType = QueryHelper.getNodeType(query.getSource());
        if (NodeTypeRegistry.getInstance().getNodeType(JNT_STRAVA_ACTIVITY).isNodeType(nodeType)) {
            try {
                JSONArray activities = getCacheStravaActivities(false);
                for (int i = 1; i <= activities.length(); i++) {
                    JSONObject activity = (JSONObject) activities.get(i - 1);
                    String path = "/" + StravaUtils.displayNumberTwoDigits(i) + "-" + ACTIVITY + "-" + activity.get(ID);
                    paths.add(path);
                }
                // paths contains all the path of the activities
                // example of a path : /08-activity-401034489
            } catch (JSONException e) {
                throw new RepositoryException(e);
            }
        }
        return paths;
    }

    // Implements : ExternalDataSource.Writable

    @Override
    public void move(String oldPath, String newPath) throws RepositoryException {
        LOGGER.info("Move : oldPath=" + oldPath + " newPath=" + newPath);
    }

    @Override
    public void order(String path, List<String> children) throws RepositoryException {
        LOGGER.info("Order : path=" + path);
    }

    @Override
    public void removeItemByPath(String path) throws RepositoryException {
        LOGGER.info("Remove item by path : path=" + path);
    }

    @Override
    public void saveItem(ExternalData data) throws RepositoryException {
        // Var
        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response;

        // Check file to upload
        if (data.getProperties().get(FILENAME) == null) {
            LOGGER.info("Empty file");
            return;
        }

        // Retrieve the path of the file to upload in Strava and the activity's type
        // Properties of the node stravaActivity
        String filename = data.getProperties().get(FILENAME)[0];

        // Check extension of the file
        String[] tabsFilename = filename.split("\\.");
        String extension = tabsFilename[tabsFilename.length - 1];
        boolean validExtension = (extension.equals(GPX) || extension.equals(TCX) || extension.equals(FIT));
        if (!validExtension) {
            LOGGER.info("Bad file extension");
            return;
        }

        // Upload the activity file on Strava

        try {
            // Prepare the post
            HttpPost httpPost = new HttpPost(HTTPS + URL_STRAVA + API_V3_UPLOADS);
            httpPost.addHeader(AUTHORIZATION, BEARER + " " + apiKeyValuePost);
            httpPost.setHeader(ENCTYPE, MULTIPART_FORM_DATA);
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart(DATA_TYPE, new StringBody(extension));
            FileBody body = new FileBody(new File(filename));
            reqEntity.addPart(FILE, body);
            httpPost.setEntity(reqEntity);
            LOGGER.info("POST : " + HTTPS + URL_STRAVA + API_V3_UPLOADS + " (filename = " + filename + ")");

            // Execute
            response = httpClient.execute(httpPost);

            // Look the response
            HttpEntity respEntity = response.getEntity();
            if (respEntity != null) {
                String content = EntityUtils.toString(respEntity);

                // Activity's ID uploaded
                String activityId = new JSONObject(content).getString(ID);

                // Wait until the activity was loaded
                boolean activityLoaded = false;
                while (!activityLoaded) {
                    Thread.sleep(1000);
                    JSONObject uploadResponse = queryStravaJSONObject(API_V3_UPLOADS + "/" + activityId);
                    activityLoaded = uploadResponse.get("status").equals(ACTIVITY_READY);
                    if (activityLoaded) {
                        LOGGER.info("Activity loaded");
                    } else {
                        LOGGER.info("Activity not loaded, wait one second ...");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error during the post : " + e.getMessage());
        }

        // After the upload, recuperation of the activies on Strava
        getCacheStravaActivities(true);
    }
}
