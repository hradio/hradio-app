package lmu.hradio.hradioshowcase.manager.hradio;

import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

import eu.hradio.httprequestwrapper.dtos.programme.ProgrammeList;
import eu.hradio.httprequestwrapper.dtos.programme.RankedStandaloneProgramme;
import eu.hradio.httprequestwrapper.dtos.programme.StandaloneProgramme;
import eu.hradio.httprequestwrapper.listener.OnErrorListener;
import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import eu.hradio.httprequestwrapper.query.elastic.ESQuery;
import eu.hradio.httprequestwrapper.service.MetaDataSearchClient;
import eu.hradio.httprequestwrapper.service.MetaDataSearchClientImpl;
import lmu.hradio.hradioshowcase.util.DateUtils;

/**
 * Wrapper for hradio programme search rest client
 */
public class HRadioProgrammeManager {

    private MetaDataSearchClient metaDataSearchClient = new MetaDataSearchClientImpl();

    private ProgrammeList currentEPG;

    private Runnable refreshRunnable;

    private Handler refreshHandler = new Handler();

    /**
     * search programme for service with given service hash
     * @param hash - the hash
     * @param listener - result callback
     * @param errorListener - error callback
     */
    public void searchByServiceHash(String hash, OnSearchResultListener<ProgrammeList> listener, OnErrorListener errorListener, boolean autoRefresh) {
        Map<String, String> query = new HashMap<>();
        query.put(ESQuery.Keys.SERVICE_HASH, hash);
        searchProgrammes(query,listener,errorListener, autoRefresh);
    }


    /**
     * search programme for service with given query
     * @param query - the query
     * @param listener - result callback
     * @param errorListener - error callback
     */
    public void searchProgrammes(Map<String,String> query,  OnSearchResultListener<ProgrammeList> listener, OnErrorListener errorListener, boolean autoRefresh) {
        query.put(ESQuery.Keys.START_TIME, DateUtils.format(DateUtils.getDateHoursFromNow(-6)));
        query.put(ESQuery.Keys.END_TIME, DateUtils.format(DateUtils.getDateHoursFromNow(12)));

        if(refreshRunnable != null)
            refreshHandler.removeCallbacks(refreshRunnable);

        metaDataSearchClient.asyncProgrammeSearch(query, list ->{
            if(list.getContent().length == 0){
                metaDataSearchClient.asyncProgrammeSearchForServiceHash(query.get(ESQuery.Keys.SERVICE_HASH), l ->{
                    listener.onResult(l);
                    currentEPG = l;
                    if(autoRefresh)startRefreshTimer(query, listener, errorListener);
                }, errorListener, true);
                return;
            }
            listener.onResult(list);
            currentEPG = list;
            if(autoRefresh)startRefreshTimer(query, listener, errorListener);

        } ,e -> metaDataSearchClient.asyncProgrammeSearchForServiceHash(query.get(ESQuery.Keys.SERVICE_HASH), l ->{
            listener.onResult(l);
            currentEPG = l;
            if(autoRefresh)startRefreshTimer(query, listener, errorListener);
        }, errorListener, true), true);
    }

    private void startRefreshTimer(Map<String,String> query,  OnSearchResultListener<ProgrammeList> listener, OnErrorListener errorListener){
        query.put(ESQuery.Keys.START_TIME, DateUtils.format(DateUtils.getDateHoursFromNow(-6)));
        query.put(ESQuery.Keys.END_TIME, DateUtils.format(DateUtils.getDateHoursFromNow(12)));
        for (RankedStandaloneProgramme rankedProgramme :  currentEPG.getContent()){
            StandaloneProgramme programme = rankedProgramme.getProgramme();
             if(DateUtils.isNowBeetween(programme.getStartTime(), programme.getStopTime())){
                refreshRunnable = () -> searchProgrammes(query, listener, errorListener, true);
                refreshHandler.postDelayed(refreshRunnable, DateUtils.getDistance(programme.getStopTime()) + 1000);
                break;
            }
        }
    }


    /**
     * retrieve last epg from tmp cache
     * @return - the cached epg
     */
    public ProgrammeList getCurrentEPG() {
        return currentEPG;
    }
}
