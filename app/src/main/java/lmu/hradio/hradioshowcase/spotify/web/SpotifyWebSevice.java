package lmu.hradio.hradioshowcase.spotify.web;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lmu.hradio.hradioshowcase.spotify.web.listener.ErrorListener;
import lmu.hradio.hradioshowcase.spotify.web.listener.ResultListener;
import lmu.hradio.hradioshowcase.spotify.web.parser.GsonPostRequest;
import lmu.hradio.hradioshowcase.spotify.web.parser.GsonRequest;

public class SpotifyWebSevice {
    protected static final String SHARED_LAST_OFFSET_KEY = "last-offset";

    protected static final String API_ADDRESS = "https://api.spotify.com/v1/";
    public static final String TAG = SpotifyWebApiTrackService.class.getSimpleName();
    protected RequestQueue queue;
    protected Map<String,String> headers;


    public SpotifyWebSevice(Context context, String accessToken) {
        queue = Volley.newRequestQueue(context);
        headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        updateAccessToken(accessToken);


    }

    protected  <T> void request(Map<String, Object> queryParams, ResultListener<T> listener, ErrorListener<VolleyError> errorListener, Class<T> tClass, String ...endpoint ){
        String url = buildUri(queryParams, endpoint);
        GsonRequest<T> request = new GsonRequest<>(url, tClass, headers, listener::onResult, errorListener::onError);
        queueRequest(request);
    }

    protected  <T> void request(String jsonBody, int method,Map<String, Object> queryParams, ResultListener<T> listener, ErrorListener<VolleyError> errorListener, Class<T> tClass, String ...endpoint ){
        String url;
        if(jsonBody == null)
            url = buildUri(queryParams, endpoint);
        else {
            url = buildUri(endpoint);
        }
        GsonPostRequest<T> request = new GsonPostRequest<>(method, jsonBody, url, tClass, headers, listener::onResult, errorListener::onError);
        queueRequest(request);
    }

    protected <T> void request(String jsonBody, ResultListener<T> listener, ErrorListener<VolleyError> errorListener, Class<T> tClass, String ...endpoint ){
        String url = buildUri(endpoint);
        GsonPostRequest<T> request = new GsonPostRequest<>(Request.Method.POST, jsonBody, url, tClass, headers, listener::onResult, errorListener::onError);
        queueRequest(request);
    }

    protected String buildUri(String... endpoint) {
        StringBuilder builder = new StringBuilder(API_ADDRESS);
        Iterator<String> endpointIterator = Arrays.asList(endpoint).iterator();
        while(endpointIterator.hasNext()){
            builder.append(endpointIterator.next());
            if(endpointIterator.hasNext()){
                builder.append("/");
            }
        }
        return builder.toString();
    }

    public void updateAccessToken( String accessToken){
        headers.put("Authorization", "Bearer " + accessToken);
    }

    protected  <T>void queueRequest(Request<T> request){
        queue.add(request);
    }

    public void stopRequests(){
        queue.cancelAll(TAG);
    }

    protected String buildUri(Map<String, Object> queryParams, String ...endPoint){
        StringBuilder builder = new StringBuilder(buildUri(endPoint));

        Iterator<Map.Entry<String, Object>> iterator = queryParams.entrySet().iterator();
        if(iterator.hasNext()){
            builder.append("?");
        }
        while (iterator.hasNext()){
            Map.Entry<String, Object> next = iterator.next();
            builder.append(next.getKey()).append("=").append(next.getValue());
            if (iterator.hasNext()){
                builder.append("&");
            }
        }
        return builder.toString();
    }
}
