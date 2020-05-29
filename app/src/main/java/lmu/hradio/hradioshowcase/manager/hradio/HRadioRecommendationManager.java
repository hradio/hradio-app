package lmu.hradio.hradioshowcase.manager.hradio;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import eu.hradio.httprequestwrapper.dtos.recommendation.Recommender;
import eu.hradio.httprequestwrapper.dtos.recommendation.WeightedRecommender;
import eu.hradio.httprequestwrapper.dtos.service_search.ServiceList;
import eu.hradio.httprequestwrapper.listener.OnErrorListener;
import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import eu.hradio.httprequestwrapper.service.RecommendationClient;
import eu.hradio.httprequestwrapper.service.RecommendationClientImpl;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;

/**
 * Wrapper for hradio recommender rest client
 */
public class HRadioRecommendationManager {

    private static final String TAG = HRadioRecommendationManager.class.getSimpleName();

    private RecommendationClient recommendationClient = new RecommendationClientImpl();
    private Recommender[] availableRecommenders;

    /**
     * Find recommendations for given service name with stored recommender preferences
     * @param context - context for preferences look up
     * @param name - service name
     * @param listener - result callback
     * @param errorListener - error callback
     */
    public void recommendation(Context context, String name, OnSearchResultListener<ServiceList> listener, OnErrorListener errorListener) {
        eu.hradio.httprequestwrapper.dtos.service_use.Context userContext = SharedPreferencesHelper.readUserData(context);

        if(userContext == null)
        {
            recommendationClient.asyncRecommendationRequestByName(name, listener, errorListener);
        }
        else
        {
            List<WeightedRecommender> recommenders = SharedPreferencesHelper.readRecommenderPreferences(context);

            if(recommenders == null || recommenders.isEmpty())
            {
                if(availableRecommenders == null)
                {
                    getAvailableRecommender(recs -> {
                        recommendationClient.asyncRecommendationRequestName(getWeightedRecommenders(availableRecommenders), name, userContext, listener, errorListener);
                    }, errorListener);
                }
                else
                {
                    recommenders = getWeightedRecommenders(availableRecommenders);
                    recommendationClient.asyncRecommendationRequestName(recommenders, name, userContext, listener, errorListener);
                }
            }
            else
            {
                recommendationClient.asyncRecommendationRequestName(recommenders, name, userContext, listener, errorListener);
            }
        }
    }

    public List<WeightedRecommender> getWeightedRecommenders (Recommender[] availableRecommenders)
    {
        List<WeightedRecommender> recommenders = new ArrayList<>();

        if(availableRecommenders != null){
            for(Recommender recommender:  availableRecommenders){
                if(recommender.getRecommenderName().equals("MoreLikeThis")){
                    recommenders.add(new WeightedRecommender(recommender, 0.05));
                } else if(recommender.getRecommenderName().equals("Expert")){
                    recommenders.add(new WeightedRecommender(recommender, 0.4));
                }else if(recommender.getRecommenderName().equals("Trend")){
                    recommenders.add(new WeightedRecommender(recommender, 0.1));
                }else if(recommender.getRecommenderName().equals("Location")){
                    recommenders.add(new WeightedRecommender(recommender, 0.25));
                }else if(recommender.getRecommenderName().equals("Category")){
                    recommenders.add(new WeightedRecommender(recommender, 0.1));
                }else if(recommender.getRecommenderName().equals("Histogram")){
                    recommenders.add(new WeightedRecommender(recommender, 0.1));
                }
            }
        }

        return recommenders;
    }

    /**
     * Look up existing recommender strategies
     * @param listener - result callback
     * @param errorListener - error callback
     */
    public void getAvailableRecommender(OnSearchResultListener<Recommender[]> listener, OnErrorListener errorListener) {
        if(availableRecommenders != null){
            listener.onResult(availableRecommenders);
        }else{
        recommendationClient.asyncGetAvailableRecommenders(recommenderStats -> {
            availableRecommenders = recommenderStats.getRecommenderStats();
            listener.onResult(recommenderStats.getRecommenderStats());
        },errorListener);
        }
    }
}
