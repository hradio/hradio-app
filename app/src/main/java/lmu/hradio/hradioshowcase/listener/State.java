package lmu.hradio.hradioshowcase.listener;


import java.util.List;

import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import lmu.hradio.hradioshowcase.model.state.PlayBackState;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;

/**
 * State holder interfaces
 */
public interface State {

    /**
     * combined holder
     */
    interface Holder extends PlayBackStateHolder, SearchStateHolder {
    }

    /**
     * Playback state holder interface
     */
    interface PlayBackStateHolder {
        PlayBackState getState();

        void getRecommendations(OnTunerScanListener listener);
    }

    /**
     * Search state holder interface
     */
    interface SearchStateHolder {
        void getLastSearchResultState(OnSearchResultListener<List<RadioServiceViewModel>> resultCallback);
    }

}
