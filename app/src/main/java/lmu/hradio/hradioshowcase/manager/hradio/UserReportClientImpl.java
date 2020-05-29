package lmu.hradio.hradioshowcase.manager.hradio;

import eu.hradio.httprequestwrapper.exception.JsonEncoderTypeMismatch;
import eu.hradio.httprequestwrapper.listener.OnErrorListener;
import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import eu.hradio.httprequestwrapper.parser.JsonParser;
import eu.hradio.httprequestwrapper.query.HRadioQuery;
import eu.hradio.httprequestwrapper.query.HRadioQueryImpl;
import eu.hradio.httprequestwrapper.service.HRadioHttpClientImpl;
import lmu.hradio.hradioshowcase.model.view.UserReport;
import lmu.hradio.hradioshowcase.model.view.UserReportResult;

public class UserReportClientImpl extends HRadioHttpClientImpl implements UserReportClient {

    private final static String TAG = "UserReportClientImpl";
    @Override
    public void asyncUserReportRequest(UserReport userReport, OnSearchResultListener<UserReportResult> listener, OnErrorListener errorListener) {
        HRadioQuery userReportQuery = new HRadioQueryImpl();
        userReportQuery.setPort(HRadioQuery.Ports.SERVICE_USE_PORT);
        userReportQuery.addEndPoint("user_reports");
        userReportQuery.setRequestMethod(HRadioQuery.HttpMethods.POST);
        JsonParser parser = new JsonParser();
        try {
            String userReportBody = parser.toJSON(userReport).toString();
            userReportQuery.setBody(userReportBody);
            asyncRequest(userReportQuery, listener, errorListener, UserReportResult.class);
        } catch (JsonEncoderTypeMismatch jsonEncoderTypeMismatch) {
            errorListener.onError(jsonEncoderTypeMismatch);
        }
    }
}
