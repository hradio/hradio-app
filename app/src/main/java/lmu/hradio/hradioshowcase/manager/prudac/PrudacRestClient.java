package lmu.hradio.hradioshowcase.manager.prudac;

import android.content.Context;
import android.util.Log;

import com.hradio.prudac.RestApi;
import com.hradio.prudac.model.Report;
import com.hradio.prudac.model.Survey;

import java.util.ArrayList;
import java.util.List;

import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnManagerErrorListener;
import lmu.hradio.hradioshowcase.util.PropertieHelper;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;

public final class PrudacRestClient {

    private static final String TAG = PrudacRestClient.class.getSimpleName();

    public static void getAllNewSurveys(OnSearchResultListener<List<Survey>> listener, OnManagerErrorListener errorListener, Context context){
        RestApi restApi = new RestApi(PropertieHelper.readQuestionaireUrl(context), context);
        restApi.getSurveys(surveys -> {
            List<Survey> filteredSurveys = new ArrayList<>();
            for(Survey survey : surveys){
                if(!SharedPreferencesHelper.isSurveyCompleted(context, survey.getSurveyid()))
                    filteredSurveys.add(survey);
            }
            listener.onResult(filteredSurveys);
        },error -> {
            if(BuildConfig.DEBUG)Log.e(TAG, error.toString());
            errorListener.onError(new GeneralError(GeneralError.PRUDAC_ERROR));
        });
    }

    public static void sendReport(Report report, Survey survey, Context context){
        RestApi restApi = new RestApi(PropertieHelper.readQuestionaireUrl(context), context);
        restApi.sendReport(report, s -> SharedPreferencesHelper.saveCompletedSurvey(context, survey.getSurveyid()));
        if(BuildConfig.DEBUG)Log.e(TAG, report.toString());
    }

}
