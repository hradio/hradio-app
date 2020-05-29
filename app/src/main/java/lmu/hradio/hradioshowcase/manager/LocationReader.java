package lmu.hradio.hradioshowcase.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.error.GeneralError;

import static android.content.Context.LOCATION_SERVICE;
import static lmu.hradio.hradioshowcase.PermissionRequestCodes.REQUEST_LOCATION_PERMISSION;

public class LocationReader {

    private static String TAG = LocationReader.class.getSimpleName();

    private static LocationReader instance;

    private List<LocationCallback> pendingCallbacks = Collections.synchronizedList(new ArrayList<>());

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationReader() {

    }

    public static LocationReader getInstance() {
        if (instance == null)
            instance = new LocationReader();
        return instance;
    }

    /**
     * Read user location once, use last known location as fallback
     * @param context
     * @param callback
     */
    public void readUserLocation(Activity context, @Nullable LocationCallback callback) {
        if (callback != null)
            pendingCallbacks.add(callback);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            if(BuildConfig.DEBUG)Log.d(TAG, "Request Location Update");

            if(isGPSenabled(context) || isNetworkEnabled(context)) {
                LocationRequest request = LocationRequest.create();
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                request.setInterval(100);
                com.google.android.gms.location.LocationCallback locationCallback = new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if(BuildConfig.DEBUG)Log.d(TAG, "Location received");
                        if (mFusedLocationClient != null) {
                            mFusedLocationClient.removeLocationUpdates(this);
                            mFusedLocationClient = null;
                        }
                        if (locationResult == null) {
                            firePendingCallbacks(null, new GeneralError(GeneralError.LOCATION_ERROR_DISABLED));
                            return;
                        }
                        for (Location location : locationResult.getLocations()) {
                            if (location != null) {
                                firePendingCallbacks(location, null);

                                return;
                            }
                        }
                    }
                };
                mFusedLocationClient.requestLocationUpdates(request, locationCallback, null);
            }else{
                mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        firePendingCallbacks(task.getResult(), null);
                    }else{
                        firePendingCallbacks(null, new GeneralError(GeneralError.LOCATION_ERROR_DISABLED));
                    }
                });
            }

        } else {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private boolean isNetworkEnabled(Context context){
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return  service!= null && service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isGPSenabled(Context context){
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return  service!= null && service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void firePendingCallbacks(Location location, GeneralError error) {
        List<LocationCallback> copy = new ArrayList<>(pendingCallbacks);
        pendingCallbacks.clear();
        for (LocationCallback callback : copy) {
            callback.onLocationProvided(location, error);
        }
    }

    @FunctionalInterface
    public interface LocationCallback {
        void onLocationProvided(Location location, GeneralError error);
    }

}
