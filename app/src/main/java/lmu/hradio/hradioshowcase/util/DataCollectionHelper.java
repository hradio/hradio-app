package lmu.hradio.hradioshowcase.util;

import android.app.Activity;
import android.content.Context;

import java.util.TimeZone;

import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnManagerErrorListener;
import lmu.hradio.hradioshowcase.manager.LocationReader;

import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.USER_ID;
import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.USER_AGE_GROUP;
import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.USER_COUNTRY;
import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.USER_GENDER;
import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.USER_LATITUDE;
import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.USER_LONGITUDE;
import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.USER_TIMEZONE;

public final class DataCollectionHelper {

    public static void allowDataCollection(int genderIndex, int ageIndex, boolean trackLocation, boolean trackCountry, Activity context, OnManagerErrorListener errorListener){

        SharedPreferencesHelper.put(context, SharedPreferencesHelper.ALLOW_LOCATION_TRACING, trackLocation);
        SharedPreferencesHelper.put(context, SharedPreferencesHelper.ALLOW_DATA_COLLECTION_KEY, true);
        if (trackLocation) {
            trackCountry = true;

            LocationReader.getInstance().readUserLocation(context, ((location, error) -> {
                if (location != null) {
                    SharedPreferencesHelper.put(context, USER_LATITUDE, (float) location.getLatitude());
                    SharedPreferencesHelper.put(context, USER_LONGITUDE, (float) location.getLongitude());
                } else if(error != null){
                    errorListener.onError(error);
                }else
                    errorListener.onError(new GeneralError(GeneralError.LOCATION_ERROR_DISABLED));
            }));
        }

        SharedPreferencesHelper.put(context, SharedPreferencesHelper.ALLOW_COUNTRY_TRACING, trackCountry);
        if (trackCountry) {
            String localeCountry = context.getResources().getConfiguration().locale.getCountry();
            SharedPreferencesHelper.put(context, USER_COUNTRY, localeCountry);
        }

        TimeZone timeZone = TimeZone.getDefault();
        SharedPreferencesHelper.put(context, USER_TIMEZONE, timeZone.toString());

        if (genderIndex >= 0)
            SharedPreferencesHelper.put(context, USER_GENDER, genderIndex);
        if (ageIndex >= 0)
            SharedPreferencesHelper.put(context, USER_AGE_GROUP, ageIndex);
    }

    public static void disableDataCollection(Context context) {
        SharedPreferencesHelper.put(context, SharedPreferencesHelper.ALLOW_DATA_COLLECTION_KEY, false);
    }

    public static boolean hasUserDataCollectionPreference(Context context) {
        return SharedPreferencesHelper.containsKey(context, SharedPreferencesHelper.ALLOW_DATA_COLLECTION_KEY);
    }
}
