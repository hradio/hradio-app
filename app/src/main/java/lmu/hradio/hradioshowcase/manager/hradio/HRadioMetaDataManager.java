package lmu.hradio.hradioshowcase.manager.hradio;

import android.util.Log;

import com.google.gson.Gson;
import eu.hradio.httprequestwrapper.dtos.service_use.ServiceUse;
import eu.hradio.httprequestwrapper.listener.OnErrorListener;
import eu.hradio.httprequestwrapper.service.ServiceUseClient;
import eu.hradio.httprequestwrapper.service.ServiceUseClientImpl;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.model.view.UserReport;

/**
 * Wrapper for hradio metadata rest client
 */
public class HRadioMetaDataManager {

    private static final String TAG = HRadioMetaDataManager.class.getSimpleName();

    private ServiceUseClient client = new ServiceUseClientImpl();
    private UserReportClient urClient = new UserReportClientImpl();
    /**
     * send usage data to rest api
     *
     * @param serviceUse    - the collected data
     * @param errorListener - error callback
     */
    public void postUserData(ServiceUse serviceUse, OnErrorListener errorListener) {
        randomizeAndSend(serviceUse,errorListener);
    }

    public void postUserReport(UserReport report, OnErrorListener errorListener) {
       urClient.asyncUserReportRequest(report, userReportResult ->
       {Log.d(TAG, new Gson().toJson(userReportResult));}, error -> {
            Log.d(TAG, error.getErrorCode().name());
            error.printStackTrace();
            errorListener.onError(error);
        });
      }

    private void randomizeAndSend(ServiceUse serviceUse, OnErrorListener errorListener) {
        if(BuildConfig.DEBUG)Log.d(TAG, new Gson().toJson(serviceUse));
        client.asyncServiceUseRequest(serviceUse, serviceUseResult -> {if(BuildConfig.DEBUG)Log.d(TAG, new Gson().toJson(serviceUseResult));}, error -> {
            if(BuildConfig.DEBUG)Log.d(TAG, error.getErrorCode().name());
            error.printStackTrace();
            errorListener.onError(error);
        });
    }
}
