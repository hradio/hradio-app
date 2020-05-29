package lmu.hradio.hradioshowcase.manager.hradio;

import java.util.Map;

import eu.hradio.httprequestwrapper.dtos.service_search.ServiceList;
import eu.hradio.httprequestwrapper.listener.OnErrorListener;
import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import eu.hradio.httprequestwrapper.query.elastic.ESQuery;
import eu.hradio.httprequestwrapper.service.ServiceSearchClient;
import eu.hradio.httprequestwrapper.service.ServiceSearchClientImpl;

/**
 * Wrapper for hradio service search rest client
 */
public class HRadioSearchManager {

    private static final String TAG = HRadioSearchManager.class.getSimpleName();
    private ServiceSearchClient service;


    public HRadioSearchManager() {
        this.service = new ServiceSearchClientImpl();
    }

    /**
     * Browse for service matching a given query
     * @param params - the query
     * @param listener - result callback
     * @param errorListener - error callback
     */
    public void serviceSearch(Map<String, String> params, OnSearchResultListener<ServiceList> listener, OnErrorListener errorListener) {
        if(params.containsKey(ESQuery.Keys.NAME)){
            params.put(ESQuery.Keys.NAME, params.get(ESQuery.Keys.NAME).replaceAll(" ", "*"));
        }
        if(params.size() == 1 && params.containsKey(ESQuery.Keys.NAME)){
            serviceSearchName(params.get(ESQuery.Keys.NAME), listener,errorListener);
        }else {
            service.asyncServiceSearch(params, listener, errorListener);
        }
    }

    /**
     * Browse for service matching a given name string
     * @param name - the name
     * @param listener - result callback
     * @param errorListener - error callback
     */
    public void serviceSearchName(String name, OnSearchResultListener<ServiceList> listener, OnErrorListener errorListener) {
        service.asyncServiceSearchByName(name, listener,errorListener);
    }

    /**
     * Browse for service matching a given name string
     * @param name - the name
     * @param listener - result callback
     * @param errorListener - error callback
     */
    public void serviceSearchByExactName(String name, OnSearchResultListener<ServiceList> listener, OnErrorListener errorListener) {
        service.asyncServiceSearchByExactName(name, listener,errorListener);
    }

}
