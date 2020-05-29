package lmu.hradio.hradioshowcase.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;

public final class PropertieHelper {

    private static final String TAG = PropertieHelper.class.getSimpleName();

    public static String readClientID(Context context){
        return getConfigValue(context, Keys.CLIENT_ID, R.raw.spotify_config);
    }

    public static String readRedirectUrl(Context context){
        return getConfigValue(context, Keys.REDIRECT_URL, R.raw.spotify_config);
    }

    public static String readQuestionaireUrl(Context context){
        return getConfigValue(context, Keys.URL, R.raw.questionaire);
    }

    private static String getConfigValue(Context context, String name, int ressource) {
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(ressource);
            Properties properties = new Properties();
            properties.load(rawResource);
            return properties.getProperty(name);
        } catch (Resources.NotFoundException e) {
            if(BuildConfig.DEBUG)Log.e(TAG, "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            if(BuildConfig.DEBUG)Log.e(TAG, "Failed to open config file.");
        }

        return null;
    }

    interface Keys{
        String URL = "url";
        String CLIENT_ID = "client_id";
        String REDIRECT_URL = "redirect_url";
    }
}
