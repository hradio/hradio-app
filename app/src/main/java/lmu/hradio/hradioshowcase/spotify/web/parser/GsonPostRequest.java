package lmu.hradio.hradioshowcase.spotify.web.parser;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class GsonPostRequest<T> extends StringRequest {

    private static final Gson gson = new Gson();
    private final Map<String, String> headers;

    private String body;

    public GsonPostRequest(int method, String body, String url, Class<T> clazz, Map<String, String> headers, Response.Listener<T> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, json -> listener.onResponse(gson.fromJson(json, clazz)), errorListener);
        this.headers = headers;
        this.body = body;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    public byte[] getBody()  {
        try {
            return body == null ? null : body.getBytes("utf-8");
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", body, "utf-8");
            return null;
        }
    }
}
