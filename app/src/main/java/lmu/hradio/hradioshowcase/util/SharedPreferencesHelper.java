package lmu.hradio.hradioshowcase.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import eu.hradio.httprequestwrapper.dtos.recommendation.WeightedRecommender;
import eu.hradio.httprequestwrapper.dtos.service_use.AgeGroup;
import eu.hradio.httprequestwrapper.dtos.service_use.Demographics;
import eu.hradio.httprequestwrapper.dtos.service_use.Gender;
import eu.hradio.httprequestwrapper.dtos.service_use.GeoPoint;
import eu.hradio.httprequestwrapper.dtos.service_use.Location;
import eu.hradio.httprequestwrapper.util.TimeUtils;
import lmu.hradio.hradioshowcase.database.ImageStorageManager;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.view.UserData;
import lmu.hradio.hradioshowcase.model.view.UserReport;


public final class SharedPreferencesHelper {

    public static final String ALLOW_DATA_COLLECTION_KEY = "allow-data-collection";
    public static final String ALLOW_LOCATION_TRACING = "allow-location-tracking";
    public static final String ALLOW_COUNTRY_TRACING = "allow-country-tracking";
    public static final String USER_ID = "user-id";
    public static final String USER_LATITUDE = "user-data-latitude";
    public static final String USER_LONGITUDE = "user-data-longitude";
    public static final String USER_COUNTRY = "user-data-country";
    public static final String USER_DISTANCE = "user-data-distance";
    public static final String USER_TIMEZONE = "user-data-timezone";
    public static final String USER_GENDER = "user-data-gender";
    public static final String USER_AGE_GROUP = "user-data-age-group";
    public static final String RECOMMENDER_PREFERENCES_KEY ="recommender-preferences";
    public static final String COMPLETED_SURVEYS ="completed-surveys-preferences";
    public static final String LAST_LISTENED_SERVICE ="last-listened-service";
    public static final String LAST_LISTENED_SERVICEID ="last-listened-service-id";
    public static final String LAST_LISTENED_SERVICE_ECC ="last-listened-service-ecc";
    public static final String WEB_URL_PATH ="web-config-path";

    public static final String SUBSTITUTION_PLAYLIST_ID_KEY = "playlist_id_key";
    public static final String SUBSTITUTION_PLAYLIST_NAME_KEY = "playlist_name_key";
    public static final String SUBSTITUTION_PROVIDER_TYPE = "substitution_provider";

    public static final String CDTS_USERNAME = "cdts_username";

    public static String getWebConfigPath(@NonNull Context context) {
        return getString(context, WEB_URL_PATH);
    }

    public static void putWebConfig(@NonNull Context context, String config) {
        put(context,WEB_URL_PATH, config);
    }

    public static RadioServiceViewModel getLastListenedService(@NonNull Context context){
        String serviceLabel =  getString(context, LAST_LISTENED_SERVICE);
        int serviceID =  getInt(context, LAST_LISTENED_SERVICEID);
        int ensembleECC =  getInt(context, LAST_LISTENED_SERVICE_ECC);
        if (serviceLabel.isEmpty())
            return null;

        RadioServiceViewModel res = new RadioServiceViewModel(serviceLabel, serviceID, ensembleECC, context);
        ImageStorageManager.loadImage(context, serviceLabel.trim().replaceAll(" ", "-"), img ->{
            ImageData data = new ImageData(img, 0, 0);
            res.setImage(data);
        });
        return res;
    }

    public static void saveLastListenedService(@NonNull Context context, String service, ImageData cover, int serviceID, int ensembleECC){
        String lastEntry =  getString(context, LAST_LISTENED_SERVICE);
        if(!lastEntry.isEmpty()){
            ImageStorageManager.deleteImage(context, lastEntry);
        }
        put(context, LAST_LISTENED_SERVICE, service);
        if(cover != null)
            ImageStorageManager.saveImage(context, cover.getImageData(), service);
        put(context, LAST_LISTENED_SERVICEID, serviceID);
        put(context, LAST_LISTENED_SERVICE_ECC, ensembleECC);
    }

    public static List<WeightedRecommender> readRecommenderPreferences(@NonNull Context activity) {
        if(containsKey(activity, RECOMMENDER_PREFERENCES_KEY)){
            String recommenderJson = PreferenceManager.getDefaultSharedPreferences(activity).getString(RECOMMENDER_PREFERENCES_KEY, "");
            return Arrays.asList(new Gson().fromJson(recommenderJson, WeightedRecommender[].class));
        }
        return new ArrayList<>();
    }

    public static void putRecommenderPreferences(@NonNull Context activity, List<WeightedRecommender> prefs) {
        String jsonPrefs = new Gson().toJson(prefs.toArray());
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(RECOMMENDER_PREFERENCES_KEY, jsonPrefs).apply();
    }

    public static eu.hradio.httprequestwrapper.dtos.service_use.Context readUserData(@NonNull Context activity) {
        Gender gender;
        AgeGroup userGroup = null;
        TimeZone timeZone = null;
        GeoPoint geoPoint = null;
        String countryCode = null;
        int userDistance = 100;

        gender = containsKey(activity, USER_GENDER)? Gender.values()[getInt(activity, USER_GENDER)] : Gender.NotSpecified;
        userGroup = containsKey(activity, USER_AGE_GROUP)? AgeGroup.values()[getInt(activity, USER_AGE_GROUP)] : AgeGroup.NotSpecified;
        if(containsKey(activity, USER_TIMEZONE)) timeZone = TimeZone.getTimeZone(getString(activity, USER_TIMEZONE));
        if(containsKey(activity, USER_LATITUDE)){
            geoPoint = new GeoPoint();
            geoPoint.setLat((double)getFloat(activity,USER_LATITUDE));
            geoPoint.setLon((double)getFloat(activity,USER_LONGITUDE));
        }
        if(containsKey(activity, USER_COUNTRY)) countryCode = getString(activity, USER_COUNTRY);
        if(containsKey(activity, USER_DISTANCE)) userDistance = getInt(activity, USER_DISTANCE);
        Location location = new Location();
        location.setCountryCode(countryCode);
        location.setGeoPoint(geoPoint);
        location.setDistance(userDistance);

        if(timeZone != null)
            location.setTimezone(timeZone.getDisplayName());

        Demographics demographics = new Demographics();
        demographics.setLocation(location);
        demographics.setGender(gender);
        demographics.setAgeGroup(userGroup);

        eu.hradio.httprequestwrapper.dtos.service_use.Context context = new eu.hradio.httprequestwrapper.dtos.service_use.Context();
        context.setTime(TimeUtils.dateToString(Calendar.getInstance().getTime()));
        context.setDemographics(demographics);

        return context;
    }

    public static UserReport readUserReport(@NonNull Context activity) {

        UserReport report = new UserReport();

        String reportId= "report";
        String description= "userreport";

        String[] values = new String[1];
        String[] labels = new String[2];

        values[0]="yes";
        labels[0]="yes";
        labels[1]="no";

        report.setContext(readUserData(activity));
        report.setReportId(reportId);
        report.setDescription(description);
        report.setValues(values);
        report.setLabels(labels);

        UserData ud= new UserData();
        ud.setId(SharedPreferencesHelper.getInt(activity, USER_ID));

        report.setUserData(ud);

        return report;
    }

    public static void putDistance(Context activity, int progress) {
        put(activity, USER_DISTANCE, progress);
    }

    public static int readUserDistance(Context activity) {
        return getInt(activity, USER_DISTANCE);
    }

    public static boolean containsKey(@NonNull Context activity, String key){
       return PreferenceManager.getDefaultSharedPreferences(activity).contains(key);
    }

    public static boolean getBoolean(@NonNull Context activity, String key, boolean defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(key, defaultValue);
    }


    public static boolean getBoolean(@NonNull Context activity, String key){
        return PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(key, false);
    }

    public static int getInt(@NonNull Context activity, String key){
        return getInt(activity, key, -1);
    }

    public static int getInt(@NonNull Context activity, String key, int defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(activity).getInt(key, defaultValue);
    }

    public static String getString(@NonNull Context activity, String key){
        return PreferenceManager.getDefaultSharedPreferences(activity).getString(key, "");
    }

    public static float getFloat(@NonNull Context activity, String key){
        return PreferenceManager.getDefaultSharedPreferences(activity).getFloat(key, 0);
    }

    public static long getLong(@NonNull Context activity, String key) {
        return PreferenceManager.getDefaultSharedPreferences(activity).getLong(key, 0);
    }

    public static void put(@NonNull Context activity, String key, String value){
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(key, value).apply();
    }

    public static void put(@NonNull Context activity, String key, boolean value){
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean(key, value).apply();
    }

    public static void put(@NonNull Context activity, String key, int value){
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt(key, value).apply();
    }

    public static void put(@NonNull Context activity, String key, long value){
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putLong(key, value).apply();
    }

    public static void put(@NonNull Context activity, String key, float value){
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putFloat(key, value).apply();
    }

    public static void saveCompletedSurvey(@NonNull Context context, String surveyid){
        Set<String> completed = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(COMPLETED_SURVEYS, new HashSet<>());
        completed.add(surveyid);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(COMPLETED_SURVEYS, completed).apply();
    }


    public static boolean isSurveyCompleted(@NonNull Context context, String surveyid) {
         return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(COMPLETED_SURVEYS, new HashSet<>()).contains(surveyid);
    }

}
