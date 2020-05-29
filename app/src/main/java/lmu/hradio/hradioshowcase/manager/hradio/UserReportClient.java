package lmu.hradio.hradioshowcase.manager.hradio;

import eu.hradio.httprequestwrapper.listener.OnErrorListener;
import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import eu.hradio.httprequestwrapper.service.HRadioHttpClient;
import lmu.hradio.hradioshowcase.model.view.UserReport;
import lmu.hradio.hradioshowcase.model.view.UserReportResult;

public interface UserReportClient extends HRadioHttpClient {

    void asyncUserReportRequest(UserReport userReport, OnSearchResultListener<UserReportResult> listener, OnErrorListener errorListener);

}
