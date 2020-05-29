package lmu.hradio.hradioshowcase.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.omri.radio.Radio;
import org.omri.radioservice.RadioService;
import org.omri.tuner.Tuner;
import org.omri.tuner.TunerType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.hradio.httprequestwrapper.dtos.recommendation.Recommender;
import eu.hradio.httprequestwrapper.dtos.service_search.RankedStandaloneService;
import eu.hradio.httprequestwrapper.dtos.service_use.Context;
import eu.hradio.httprequestwrapper.dtos.service_use.ServiceUse;
import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import eu.hradio.substitutionapi.Substitution;
import eu.hradio.substitutionapi.SubstitutionPlayerListener;
import eu.hradio.timeshiftplayer.SkipItem;
import eu.hradio.timeshiftplayer.TimeshiftPlayer;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.database.Database;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.events.AppEvents;
import lmu.hradio.hradioshowcase.listener.OnEPGUpdateListener;
import lmu.hradio.hradioshowcase.listener.OnManagerErrorListener;
import lmu.hradio.hradioshowcase.listener.OnRadioStatusUpdateListener;
import lmu.hradio.hradioshowcase.listener.OnServiceUpdateTunerScanListener;
import lmu.hradio.hradioshowcase.listener.OnTunerScanListener;
import lmu.hradio.hradioshowcase.listener.PlayBackDelegate;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.listener.PodcastSearchResultListener;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.manager.hradio.HRadioMetaDataManager;
import lmu.hradio.hradioshowcase.manager.hradio.HRadioProgrammeManager;
import lmu.hradio.hradioshowcase.manager.hradio.HRadioRecommendationManager;
import lmu.hradio.hradioshowcase.manager.hradio.HRadioSearchManager;
import lmu.hradio.hradioshowcase.manager.omri.TunerManager;
import lmu.hradio.hradioshowcase.manager.radiomanager.RadioController;
import lmu.hradio.hradioshowcase.manager.radiomanager.TimeshiftManager;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionController;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionPlayerTyp;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionProvider;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionProviderType;
import lmu.hradio.hradioshowcase.model.state.AppState;
import lmu.hradio.hradioshowcase.model.state.PlayBackState;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.ProgrammeInformationViewModel;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.model.view.UserReport;
import lmu.hradio.hradioshowcase.model.weburlconfig.WebUrl;
import lmu.hradio.hradioshowcase.service.MusicService;
import lmu.hradio.hradioshowcase.util.ImageDataHelper;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;

import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.ALLOW_DATA_COLLECTION_KEY;
import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.USER_LATITUDE;
import static lmu.hradio.hradioshowcase.util.SharedPreferencesHelper.USER_LONGITUDE;

public class MainAppController implements OnManagerErrorListener, PlayBackDelegate {

    private static final String TAG = "MainAppController";
    // manager instances
    private TunerManager tunerManager;
    //current state

    //user data collection
    private static final long collectAsListenedThreshold = 5 * 60 * 1000;
    private Runnable userDataTrackCallback;
    private Handler userDataTrackHandler = new Handler();

    private SubstitutionController substitutionManager;
    private HRadioProgrammeManager programmeManager;

    private MusicService.HRadioAudioSinkBinder binder;
    private ServiceConnection connection;

    private RadioController radioManager;

    private static MainAppController instance;

    private AppState appState;

    private boolean isConnected = false;

    private ExecutorService threadPool;

    public static MainAppController getInstance(Activity context, InitilaizationCallback callback) {
        if(BuildConfig.DEBUG)Log.d(TAG, "getting instance");
        if (instance == null && context != null)
            instance = new MainAppController(context, callback);
        else {
            if (context != null && instance.isConnected)
                callback.onInitComplete();
            else if (instance != null)
                instance.bind(context, callback);
        }
        return instance;
    }

    private void checkInitComplete(InitilaizationCallback callback) {
        if(mTunerManagerInitialized && mServiceBound) {
            callback.onInitComplete();
        }
    }

    private boolean mTunerManagerInitialized = false;
    private boolean mServiceBound = false;
    private MainAppController(Activity context, InitilaizationCallback callback) {
        if(BuildConfig.DEBUG)Log.d(TAG, "Creating new instance");
        Database.init(context.getApplicationContext());
        tunerManager = new TunerManager(context, new TunerManager.TunerManagerListener() {
            @Override
            public void tunerManagerInitialized() {
                mTunerManagerInitialized = true;
                checkInitComplete(callback);
            }
        });
        programmeManager = new HRadioProgrammeManager();
        radioManager = new RadioController(context);
        appState = new AppState();
        substitutionManager = new SubstitutionController(context);
        threadPool = Executors.newFixedThreadPool(2);

        Intent intent = new Intent(context.getApplicationContext(), MusicService.class);
        ContextCompat.startForegroundService(context.getApplicationContext(), intent);
        bind(context, callback);
        tryLoadConfig(context);
   }

    private void registerReceivers(Activity context) {
        IntentFilter appEventFilter = new IntentFilter();
        appEventFilter.addAction(AppEvents.APP_KILLED);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                stopAll(context);
            }

        };
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(receiver, appEventFilter);
    }

    public void bind(Activity context, InitilaizationCallback callback) {
        if(BuildConfig.DEBUG)Log.d(TAG, "SrvBind binding service: " + mServiceBound + " : " + isConnected);
        if (!isConnected) {
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    if(BuildConfig.DEBUG)Log.d(TAG, "Service successfully bound");
                    binder = (MusicService.HRadioAudioSinkBinder) iBinder;
                    substitutionManager.setStreamingPlayer(binder.getService().getStreamingPlayer());
                    getAppState().getPlayBackState().registerOnChangeListener((state) -> binder.getService().updateNotification(state));
                    registerListeners(context);
                    mServiceBound = true;
                    checkInitComplete(callback);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    if(BuildConfig.DEBUG)Log.d(TAG, "Service successfully unbound");
                    mServiceBound = false;
                }
            };
            Intent intent = new Intent(context.getApplicationContext(), MusicService.class);
            isConnected = context.getApplicationContext().bindService(intent, connection, android.content.Context.BIND_AUTO_CREATE);
        }
    }

    public void unbind(android.content.Context context) {
        if(BuildConfig.DEBUG)Log.d(TAG, "SrvBind unbinding service: " + mServiceBound);
        if(mServiceBound && isConnected && connection != null) {
            isConnected = false;
            context.getApplicationContext().unbindService(connection);
            binder = null;
        }
    }

    public void killService(android.content.Context context, Activity cont){
        radioManager.stopCurrent();
        substitutionManager.stopSubstitution();
        tunerManager.deinit();
        appState.clear();
        binder.getService().kill(context);

        unregisterListeners();
        unbind(context);
        instance = null;
    }

    public void stopAll(android.content.Context context) {
        instance = null;
        radioManager.stopCurrent();
        substitutionManager.stopSubstitution();
        tunerManager.deinit();
        appState.clear();
        unregisterListeners();
        unbind(context);
    }

    public void tryLoadConfig(Activity context) {
        threadPool.submit(() -> {
            String config = SharedPreferencesHelper.getWebConfigPath(context);
            if (!config.isEmpty()) {
                try {
                    WebUrl[] urls = new Gson().fromJson(config, WebUrl[].class);
                    tunerManager.setWebConfig(urls);
                } catch (JsonSyntaxException je) {
                    onError(new GeneralError(GeneralError.INVALID_WEBVIEW_CONFIG));
                }
            }
        });
    }

    private TimeshiftManager getTimeShiftManager() {
        return radioManager.getTimeshiftManager();
    }

    /**
     * get recommendations for given service label with users recommender preferences
     *
     * @param radioServiceLabel - the service label
     * @param context           - context to read user preferences
     */
    private void getRecommendations(String radioServiceLabel, Activity context, OnTunerScanListener listener) {
        new HRadioRecommendationManager().recommendation(context, radioServiceLabel, list -> {
            List<RadioServiceViewModel> recommendations = new ArrayList<>();
            for (RankedStandaloneService service : list.getContent()) {
                recommendations.add(new RadioServiceViewModel(service.getContent()));
            }
            getAppState().getPlayBackState().setRelateRecommendedServices(recommendations);
            listener.onResult(recommendations);
        }, (e) -> onError(new GeneralError(GeneralError.NETWORK_ERROR)));
    }

    /**
     * fires callback for error handling
     *
     * @param error - which occured
     */
    @Override
    public void onError(@NonNull GeneralError error) {
        for (OnManagerErrorListener listener : errorListeners) {
            listener.onError(error);
        }
    }

    //Playback controls

    /**
     * Skip in present if is time shift else substitute
     */
    private void skipNext(Activity context) {
        boolean substitutionEnabled = substitutionManager.isEnabled(context);
        if (!radioManager.skipNext(substitutionEnabled) && substitutionEnabled ) {
            getAppState().getPlayBackState().setSubstitution(PlayBackState.SubstituionState.SPOTIFY);
            substitutionManager.substitute(context);
        } else
            onError(new GeneralError(GeneralError.SUBSTITUTION_DISABLED));
    }

    public void playPodcast(Substitution podcast, Activity context) {
        radioManager.pauseCurrent();
        getAppState().getPlayBackState().setSubstitution(PlayBackState.SubstituionState.NATIVE);
        substitutionManager.substitute(podcast, context);
    }

    /**
     * skip to a given skip item in time shift player
     *
     * @param item - skip point
     */
    public void skipToItem(SkipItem item) {
        if (isSubstitution()) {
            substitutionManager.stopSubstitution();
        }

        getTimeShiftManager().skipToItem(item);
  }

    /**
     * Playback view control play clicked
     */
    @Override
    public void onPlayClicked() {
        if (isSubstitution())
            substitutionManager.resumeSubstitution();
        else
            radioManager.resumeCurrent();
    }

    /**
     * Playback view control pause clicked
     */
    @Override
    public void onPauseClicked() {

        if (isSubstitution())
            substitutionManager.pauseSubstitution();
        else
            radioManager.pauseCurrent();
    }

    /**
     * Playback view control skip next clicked
     */
    @Override
    public void onSkipNext(Activity context) {
        if(BuildConfig.DEBUG)Log.d(TAG, "onSkipNext");

        if (getTrackLikeService() != null) {
            getTrackLikeService().dislikeTrack(getTrackLikeService().getCurrent(), b -> {
            });
        }


        if (isSubstitution() && getTimeShiftManager().getSkipItems().isEmpty()) {
            onJumpToLive();
            return;
        }
        if (isSubstitution()) {
            substitutionManager.stopSubstitution();
            refreshAppState(getAppState().getPlayBackState().getRunningService(), false);
        }
        int nextSkipItem = getTimeShiftManager().getNextIndexForCurrentPosition();
        if (nextSkipItem == -1) {
            skipNext(context);
            if(BuildConfig.DEBUG)Log.d(TAG, "skip again");
            return;
        }

        getTimeShiftManager().skipToIndex(nextSkipItem);

        binder.flush();

        getTimeShiftManager().resumeTimeshift();
    }

    /**
     * on Substitute podcast clicked
     *
     * @param activity - context
     */
    @Override
    public void onSubstitutePodcast(Activity activity) {
        substitutionManager.queryForPodcasts(getAppState().getPlayBackState().getRunningService(), getAppState().getPlayBackState().getProgrammeInformationViewModel(), activity);
        podcastSearchResultListener.onPodcastsReceived(null , new SubstitutionItem[0]);
    }

    public void loadPodcasts(Activity activity, List<String> urls) {
        substitutionManager.queryForPodcasts(getAppState().getPlayBackState().getRunningService(), urls, activity);
        podcastSearchResultListener.onPodcastsReceived(null , new SubstitutionItem[0]);
    }

    public void getAllRadioServices(OnSearchResultListener<List<RadioServiceViewModel>> listener) {
        threadPool.submit(() -> tunerManager.getRadioServicesSorted(listener));
    }

    /**
     * Playback view control skip back clicked
     */
    @Override
    public void onSkipBack() {
        radioManager.skipBack();
        binder.flush();
    }

    /**
     * check if any audio service is running
     */
    public boolean isServiceRunning() {
        return getAppState().getPlayBackState().isRunning();
    }

    /**
     * check if substitution is active
     */
    public boolean isSubstitution() {
        return getAppState().getPlayBackState().getSubstitution() != PlayBackState.SubstituionState.INACTIVE;
    }

    /**
     * jump to live stream
     */
    public void onJumpToLive() {
        if(BuildConfig.DEBUG)Log.d(TAG, "onJumpToLive");

        if (isSubstitution()) {
            substitutionManager.stopSubstitution();
            getAppState().getPlayBackState().setSubstitution(PlayBackState.SubstituionState.INACTIVE);
            refreshAppState(getAppState().getPlayBackState().getRunningService(), false);
        }

        radioManager.jumpToLive();
    }


    //radio functions

    public boolean startShareService(String shareLink, Activity context) {
        if (!isConnected) {
            bind(context, () -> callStartShareLink(shareLink, context));
        } else {
            return callStartShareLink(shareLink, context);
        }
        return true;
    }

    /**
     * Start dab/edi based live stream
     *
     * @param radioServiceVM - view model of service to be played
     */
    public boolean startService(RadioServiceViewModel radioServiceVM, Activity context) {
        if(BuildConfig.DEBUG)Log.d(TAG, "startService");
        if (!isConnected) {
            bind(context, () -> callStartService(radioServiceVM, context));
        } else {
            return callStartService(radioServiceVM, context);
        }
        return true;
    }

    private boolean callStartShareLink(String shareLink, Activity context) {
        if (isSubstitution()) {
            substitutionManager.stopSubstitution();
        }

        radioManager.stopCurrent();

        getTimeShiftManager().clearSkipItems();


        return radioManager.startDirectEdi(shareLink);
    }

    /**
     * Start selected service if not already running - stops substitution.
     * If service not in omri, scan for name or service hash and start result
     *
     * @param radioServiceVM - the service to be played
     * @param context - view context
     * @return true
     */
    private boolean callStartService(RadioServiceViewModel radioServiceVM, Activity context) {
        if(BuildConfig.DEBUG)Log.d(TAG, "callStartService: " + radioServiceVM.getServiceLabel());
        //stop running services
        if(radioServiceVM == null) {
            return true;
        }

        if(!radioServiceVM.equals(getAppState().getPlayBackState().getRunningService())) {

            if (isSubstitution()) {
                substitutionManager.stopSubstitution();
            }

            radioManager.stopCurrent();

            SharedPreferencesHelper.saveLastListenedService(context, radioServiceVM.getServiceLabel(), radioServiceVM.getLogo(), radioServiceVM.getServiceID(), radioServiceVM.getEnsembleECC());

            refreshAppState(radioServiceVM, true);

            startCollectDataTimer(radioServiceVM.getServiceLabel(), context);
            getTimeShiftManager().clearSkipItems();

            List<RadioService> radioServices = radioServiceVM.getRadioServices();

            if (radioServices != null && !radioServices.isEmpty()) {
                RadioService service = radioManager.findPreferedService(radioServices);
                if (service != null)
                    return radioManager.startService(service);
                else
                    onError(new GeneralError(GeneralError.TUNER_ERROR));
            } else {
                Map<String, String> params = new HashMap<>();
                if (radioServiceVM.getHash() != null)
                    params.put("hash", radioServiceVM.getHash());
                else
                    params.put("name", radioServiceVM.getServiceLabel());
                tunerManager.startScan(params, new OnServiceUpdateTunerScanListener() {
                    @Override
                    public void onResult(List<RadioServiceViewModel> services) {
                        List<RadioService> radioServices = tunerManager.getService(radioServiceVM);
                        if (radioServices != null && !radioServices.isEmpty()) {
                            RadioService service = radioManager.findPreferedService(radioServices);
                            if (service != null) {
                                if(BuildConfig.DEBUG)Log.d(TAG, "starting service");
                                radioManager.startService(service);
                            } else {
                                onError(new GeneralError(GeneralError.FAVORITE_NOT_AVAILABLE_ERROR));
                            }
                        } else
                            onError(new GeneralError(GeneralError.FAVORITE_NOT_AVAILABLE_ERROR));
                    }

                    @Override
                    public void onServiceFound(RadioServiceViewModel service, RadioService type) {
                    }
                }, this, false);
            }
            return true;
        }

        return false;

    }

    /**
     * Read last played service
     * @param context - app context
     * @return last played
     */
    public RadioServiceViewModel getLastService(android.content.Context context){
        return SharedPreferencesHelper.getLastListenedService(context);
    }

    /**
     * Get recommendations for current playing service
     * @param listener - result listener
     * @param context - view context
     */
    public void getRecommendations(OnTunerScanListener listener, Activity context) {
      threadPool.submit(() ->  getRecommendations(getAppState().getPlayBackState().getRunningService().getServiceLabel(), context, listener));
    }

    /**
     * Reset app state and fire callbacks with new
     *
     * @param radioServiceVM - selected radio service
     */
    private void refreshAppState(RadioServiceViewModel radioServiceVM, boolean hardReset) {
	    if(BuildConfig.DEBUG)Log.d(TAG, "refreshAppState EPG update");
        if (hardReset) {
            getAppState().getPlayBackState().setRelateRecommendedServices(new ArrayList<>());
            getAppState().getPlayBackState().setProgrammeInformationViewModel(null);
            if(getTrackLikeService() != null)
                getTrackLikeService().setCurrent(null);
        }
        getAppState().getPlayBackState().setRunningService(radioServiceVM);
        getAppState().getPlayBackState().setTextData(new TextData(radioServiceVM.getServiceLabel()));

        getAppState().getPlayBackState().setSubstitution(PlayBackState.SubstituionState.INACTIVE);
        if (radioServiceVM.getLogo() != null)
            getAppState().getPlayBackState().setCover(radioServiceVM.getLogo());
        else
            getAppState().getPlayBackState().setCover(null);


        for (PlayBackListener listener : playBackListeners) {
            listener.visualContent(getAppState().getPlayBackState().getCover());
            listener.textualContent(getAppState().getPlayBackState().getTextData());
        }
        for (OnEPGUpdateListener listener : metaDataUpdateListeners) {
            listener.onEPGUpdate(getAppState().getPlayBackState().getProgrammeInformationViewModel());
        }
    }

    /**
     * Starts service use timer which will send service use after 5 min of listening
     * Only if user enabled feedback feature
     *
     * @param serviceName - the listened service
     */
    private void startCollectDataTimer(String serviceName, Activity context) {
        if (SharedPreferencesHelper.getBoolean(context, ALLOW_DATA_COLLECTION_KEY)) {
            //Remove pending requests
            if (userDataTrackCallback != null) {
                userDataTrackHandler.removeCallbacks(userDataTrackCallback);
                userDataTrackCallback = null;
            }
            //resolve service hash
            new HRadioSearchManager().serviceSearchByExactName(serviceName, result -> {
                if (result.getNumberOfElements() > 0) {
                    String serviceHash = result.getContent()[0].getContent().getHash();
                    //start collect timer and post service as listened after 5 min
                    userDataTrackCallback = () -> {
                        if (SharedPreferencesHelper.getBoolean(context, SharedPreferencesHelper.ALLOW_COUNTRY_TRACING)) {
                            LocationReader.getInstance().readUserLocation(context, (location, error) -> {
                                if (location != null) {
                                    SharedPreferencesHelper.put(context, USER_LATITUDE, (float) location.getLatitude());
                                    SharedPreferencesHelper.put(context, USER_LONGITUDE, (float) location.getLongitude());
                                }
                                ServiceUse collectedData = readCollectedData(context, serviceHash);
                                postCollectedUserData(collectedData);
                            });
                        } else {
                            ServiceUse collectedData = readCollectedData(context, serviceHash);
                            postCollectedUserData(collectedData);
                        }
                    };
                    userDataTrackHandler.postDelayed(userDataTrackCallback, collectAsListenedThreshold);

                }
            }, e -> onError(new GeneralError(GeneralError.NETWORK_ERROR)));
        }
    }

    /**
     * Read collected user data from shared preferences and
     *
     * @param serviceHash - the listened services
     * @return the dto to be transfered
     */
    private ServiceUse readCollectedData(Activity context, String... serviceHash) {
        Context userData = SharedPreferencesHelper.readUserData(context);
        ServiceUse serviceUse = new ServiceUse();
        serviceUse.setContext(userData);
        serviceUse.setServices(serviceHash);
        return serviceUse;
    }

    //TimeShift functions

    /**
     * Get collected skip items
     *
     * @return list of skip items for current timeshift session
     */
    public List<SkipItem> getSkipItems() {
        return getTimeShiftManager().getSkipItems();
    }

    /**
     * seek to current progress
     *
     * @param progress - current progress
     */
    public void seekTo(long progress) {
        if (isSubstitution())
            substitutionManager.seekTo(progress);
        else
            radioManager.seekTo(progress);
    }

    @Override
    public void shareSbtToken() {

    }

    /**
     * get currently listening skip item
     *
     * @return the current skip item
     */
    public SkipItem getCurrentSkipItem() {
        return radioManager.getCurrentSkipItem();
    }

    //tuner interaction

    /**
     * Search for services which matches a given query
     *
     * @param params - query params
     */
    public void searchForServices(android.content.Context context, Map<String, String> params, List<TunerType> selectedTuners) {
        for(Map.Entry param : params.entrySet()) {
            if (BuildConfig.DEBUG) Log.d(TAG, "searchFor Services with param Key: " + param.getKey() + ", Value: " + param.getValue());
        }

        if(!Radio.getInstance().getRadioServices().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
            builder.setTitle(R.string.service_search_alert_delete_services);
            builder.setPositiveButton(R.string.service_search_alert_delete_services_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startSearchForServices(params, selectedTuners, true);
                    dialog.cancel();
                }
            });
            builder.setNegativeButton(R.string.service_search_alert_delete_services_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startSearchForServices(params, selectedTuners, false);
                    dialog.cancel();
                }
            });
            builder.setNeutralButton(R.string.service_search_alert_delete_services_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setCancelable(false);
            builder.show();
        } else {
            startSearchForServices(params, selectedTuners, false);
        }
    }

    private void startSearchForServices(Map<String, String> params, List<TunerType> selectedTuners, boolean deleteServices) {
        threadPool.submit(() -> {
            List<TunerType> tuners = selectedTuners.isEmpty() ? Arrays.asList(TunerType.values()) : selectedTuners;
            tunerManager.startScan(tuners, params, scanListener, this, deleteServices);
        });
    }

    /**
     * Read available recommenders
     * @param recommenderAction - result callback
     */
    public void retrieveAvailableRecommenders(OnSearchResultListener<Recommender[]> recommenderAction) {
        new HRadioRecommendationManager().getAvailableRecommender(recommenderAction, e -> onError(new GeneralError(GeneralError.RECOMMENDER)));
    }

    /**
     * Randomize and post collected user data
     * @param collectedData - collected user data
     */
    private void postCollectedUserData(ServiceUse collectedData) {
        new HRadioMetaDataManager().postUserData(collectedData, (e -> {
            for (OnManagerErrorListener listener : errorListeners) {
                listener.onError(new GeneralError(GeneralError.USER_DATA_COLLECTION));
            }
        }));
    }

    /**
     * post collected user reports
     * @param report - user report
     */
    private void postUserReport(UserReport report) {
        new HRadioMetaDataManager().postUserReport(report, (e -> {
            for (OnManagerErrorListener listener : errorListeners) {
                listener.onError(new GeneralError(GeneralError.USER_REPORT_ERROR));
            }
        }));
    }

    //getter
    public String getRunningServiceLabel() {
        return radioManager.getRunningService();
    }

    public SubstitutionProvider getSubstitutionManager(SubstitutionPlayerTyp type) {
        if (substitutionManager == null)
            return null;
        return substitutionManager.getManagerByType(type);
    }

    public long getTotalTimeShiftDuration() {
        return getTimeShiftManager().getDuration();
    }

    // listener
    private Set<PlayBackListener> playBackListeners = new HashSet<>();
    private Set<OnManagerErrorListener> errorListeners = new HashSet<>();
    private Set<OnEPGUpdateListener> metaDataUpdateListeners = new HashSet<>();
    private Set<PodcastSearchResultListener> podcastSearchResultListeners = new HashSet<>();
    private Set<OnTunerScanListener> tunerScanListeners = new HashSet<>();


    public void unregisterTunerScanListeener(OnTunerScanListener listener) {
        tunerScanListeners.remove(listener);
    }

    public void registerTunerScanListeener(OnTunerScanListener listener) {
        tunerScanListeners.add(listener);
    }

    public void registerPodcastSearchResultListener(PodcastSearchResultListener listener) {
        this.podcastSearchResultListeners.add(listener);
    }

    public void unregisterPodcastSearchResultListener(PodcastSearchResultListener listener) {
        this.podcastSearchResultListeners.remove(listener);
    }

    public void registerOnErrorListener(OnManagerErrorListener errorListener) {
        this.errorListeners.add(errorListener);
    }

    public void unregisterOnErrorListener(OnManagerErrorListener errorListener) {
        this.errorListeners.remove(errorListener);
    }

    public void registerPlayBackListener(PlayBackListener listener) {
        playBackListeners.add(listener);
    }

    public void unregisterPlayBackListener(PlayBackListener listener) {
        playBackListeners.remove(listener);
    }

    @Override
    public TrackLikeService getTrackLikeService() {
        return substitutionManager.getTrackLikeService();
    }

    public void registerMetaDataUpdateListener(OnEPGUpdateListener listener) {
        metaDataUpdateListeners.add(listener);
    }

    public void unregisterMetaDataUpdateListener(OnEPGUpdateListener listener) {
        metaDataUpdateListeners.remove(listener);
    }

    private OnRadioStatusUpdateListener radioStatusUpdateListener;

    private ProgrammeInformationViewModel mCurrentProgramme = null;

    public ProgrammeInformationViewModel getCurrentEpg() {
        return mCurrentProgramme;
    }

    private void registerListeners(Activity context) {
        //register
        registerReceivers(context);
        radioManager.registerErrorListener(this);
        tunerManager.registerErrorListener(this);
        radioManager.registerPlayBackListener(playBackListener);
        substitutionManager.registerPlayBackListener(playBackListener);
        substitutionManager.registerPodcastSearchListener(podcastSearchResultListener);
        substitutionManager.registerErrorListener(this);
        radioStatusUpdateListener = new OnRadioStatusUpdateListener() {

            /**
             * Omri started radio service - start timeshift and retrieve epg and first update view with service label and image
             * @param tuner   - the tuner
             * @param service - the service
             */
            @Override
            public void radioServiceStarted(Tuner tuner, RadioService service) {
                getAppState().getPlayBackState().setTunerType(service.getRadioServiceType());
                threadPool.submit(() -> radioManager.serviceStarted(service, binder, tuner, context));

                new HRadioSearchManager().serviceSearchByExactName(service.getServiceLabel(), result -> {
                    if (result.getNumberOfElements() > 0) {
                        String serviceHash = result.getContent()[0].getContent().getHash();
                        programmeManager.searchByServiceHash(serviceHash, programmeList -> {
                            ProgrammeInformationViewModel programmInformationViewModel = ProgrammeInformationViewModel.from(programmeList);
                            if(getAppState().getPlayBackState().getRunningService() != null && getAppState().getPlayBackState().getRunningService().equalsRadioService(service)) {
                                mCurrentProgramme = programmInformationViewModel;
                                for (OnEPGUpdateListener listener : metaDataUpdateListeners) {
                                    listener.onEPGUpdate(programmInformationViewModel);
                                }
                                getAppState().getPlayBackState().setProgrammeInformationViewModel(programmInformationViewModel);
                            }
                        }, e -> onError(new GeneralError(GeneralError.EPG_ERROR)), true);
                    }
                }, e -> onError(new GeneralError(GeneralError.EPG_ERROR)));

                TextData textData = new TextData(service.getServiceLabel());
                ImageData data = ImageDataHelper.fromVisuals(service.getLogos());
                for (PlayBackListener listener : playBackListeners) {
                    listener.textualContent(textData);
                    listener.visualContent(data);
                }
            }

            @Override
            public void radioServiceStopped(Tuner tuner, RadioService service) {
                getAppState().getPlayBackState().setTunerType(null);
            }
        };
        radioManager.registerOnRadioStatusUpdateListener(radioStatusUpdateListener);
    }

    private void unregisterListeners() {
        //register
        if (radioStatusUpdateListener != null)
            radioManager.unregisterOnRadioStatusUpdateListener(radioStatusUpdateListener);

        radioManager.unregisterPlayBackListener(playBackListener);
        substitutionManager.unregisterPlayBackListener(playBackListener);
        radioManager.unregisterErrorListener(this);
        tunerManager.unregisterErrorListener(this);
        substitutionManager.unregisterPodcastSearchListener(podcastSearchResultListener);
        substitutionManager.unregisterErrorListener(this);

    }

    private PodcastSearchResultListener podcastSearchResultListener = (source, podcastContainer) -> {
        for (PodcastSearchResultListener listener : MainAppController.this.podcastSearchResultListeners) {
            listener.onPodcastsReceived(source, podcastContainer);
        }
    };


    private OnTunerScanListener scanListener = new OnTunerScanListener() {
        @Override
        public void tunerScanStarted() {
            for (OnTunerScanListener listener : tunerScanListeners)
                listener.tunerScanStarted();
        }

        @Override
        public void tunerScanProgress(int percentScanned) {
            for (OnTunerScanListener listener : tunerScanListeners)
                listener.tunerScanProgress(percentScanned);
        }

        @Override
        public void tunerScanFinished() {
            for (OnTunerScanListener listener : tunerScanListeners)
                listener.tunerScanFinished();
        }

        @Override
        public void onResult(List<RadioServiceViewModel> services) {
            for (OnTunerScanListener listener : tunerScanListeners)
                listener.onResult(services);
        }

        @Override
        public void onServiceFound(RadioServiceViewModel service, RadioService type) {
            for (OnTunerScanListener listener : tunerScanListeners)
                listener.onServiceFound(service, type);
        }
    };

    //substitution functions
    private SubstitutionPlayerListener substitutionPlayerListener = new SubstitutionPlayerListener() {
        @Override
        public void started(Substitution substitution) {
            getAppState().getPlayBackState().setRunning(true);
            getAppState().getPlayBackState().setTextData(TextData.fromSubstitution(substitution));
            for (PlayBackListener listener : playBackListeners) {
                listener.started((SubstitutionItem) substitution);
                listener.textualContent(getAppState().getPlayBackState().getTextData());
            }

            if (substitution instanceof SubstitutionItem) {

                //Download image and send to listeners
                if (((SubstitutionItem) substitution).getCover() != null) {
                    getAppState().getPlayBackState().setCover(((SubstitutionItem) substitution).getCover());
                    for (PlayBackListener listener : playBackListeners) {
                        listener.visualContent(((SubstitutionItem) substitution).getCover());
                    }
                } else {
                    ((SubstitutionItem) substitution).setListener(() -> {
                        getAppState().getPlayBackState().setCover(((SubstitutionItem) substitution).getCover());
                        for (PlayBackListener listener : playBackListeners) {
                            listener.visualContent(((SubstitutionItem) substitution).getCover());
                        }
                    });
                }
            }
        }

        @Override
        public void stopped(Substitution substitution) {
            if(BuildConfig.DEBUG)Log.d(TAG, "stopped " + substitution.getTitle());
            if (getAppState().getPlayBackState().getRunningService().getLogo() != null)
                getAppState().getPlayBackState().setCover(getAppState().getPlayBackState().getRunningService().getLogo());
            else
                getAppState().getPlayBackState().setCover(null);

            getAppState().getPlayBackState().setTextData(new TextData(getAppState().getPlayBackState().getRunningService().getServiceLabel()));

            if (getTimeShiftManager().countSkipItems() > 0) {
                int skipIndex = getTimeShiftManager().getCurrentSkipItemPosition();
                if (skipIndex == getTimeShiftManager().countSkipItems() - 1) {
                    substitutionManager.substitute();
                } else {
                    substitutionManager.stopSubstitution();
                    getAppState().getPlayBackState().setSubstitution(PlayBackState.SubstituionState.INACTIVE);
                    getTimeShiftManager().skipToIndex(skipIndex + 1);
                }

            } else {
                substitutionManager.stopSubstitution();
                getAppState().getPlayBackState().setSubstitution(PlayBackState.SubstituionState.INACTIVE);
                onJumpToLive();
            }

        }

        @Override
        public void paused(Substitution substitution) {
            getAppState().getPlayBackState().setRunning(false);
            for (PlayBackListener listener : playBackListeners) {
                listener.paused();
            }
        }

        @Override
        public void playProgress(Substitution substitution, long current, long total) {
            for (PlayBackListener listener : playBackListeners) {
                listener.playProgress((SubstitutionItem) substitution, current, total);
            }
        }
    };

    private PlayBackListener playBackListener = new PlayBackListener() {
        @Override
        public void onError(@NonNull GeneralError error) {
            MainAppController.this.onError(error);
        }

        @Override
        public void started() {
            getAppState().getPlayBackState().setRunning(true);
            for (PlayBackListener listener : playBackListeners) {
                listener.started();
            }

        }

        @Override
        public void stopped() {
            getAppState().getPlayBackState().setRunning(false);
            for (PlayBackListener listener : playBackListeners) {
                listener.stopped();
            }
        }

        @Override
        public void paused() {

            getAppState().getPlayBackState().setRunning(false);
            for (PlayBackListener listener : playBackListeners) {
                listener.paused();
            }
        }

        @Override
        public void playProgress(long current, long total) {
            for (PlayBackListener listener : new HashSet<>(playBackListeners)) {
                listener.playProgress(current, total);
            }
        }

        @Override
        public void playProgressRealtime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration) {
            //if(BuildConfig.DEBUG)Log.d(TAG, "playProgressRealtime: " + curPos);

            for (PlayBackListener listener : new HashSet<>(playBackListeners)) {
                listener.playProgressRealtime(realTimePosix, streamTimePosix, curPos, totalDuration);
            }
        }

        @Override
        public void sbtSeeked() {
            if(BuildConfig.DEBUG)Log.d(TAG, "SbtSeeked");

            binder.flush();

	        for (PlayBackListener listener : new HashSet<>(playBackListeners)) {
		        listener.sbtSeeked();
	        }

	        if(BuildConfig.DEBUG)Log.d(TAG, "SbtSeeked loading epg");
	        RadioService service = getCurrentTimeShiftPlayer().getRadioService();
            new HRadioSearchManager().serviceSearchByExactName(service.getServiceLabel(), result -> {
                if (result.getNumberOfElements() > 0) {
                    String serviceHash = result.getContent()[0].getContent().getHash();
                    programmeManager.searchByServiceHash(serviceHash, programmeList -> {
                        ProgrammeInformationViewModel programmInformationViewModel = ProgrammeInformationViewModel.from(programmeList);
                        if(getAppState().getPlayBackState().getRunningService() != null && getAppState().getPlayBackState().getRunningService().equalsRadioService(service)) {
                            mCurrentProgramme = programmInformationViewModel;
                            for (OnEPGUpdateListener listener : metaDataUpdateListeners) {
                                listener.onEPGUpdate(programmInformationViewModel);
                            }
                            getAppState().getPlayBackState().setProgrammeInformationViewModel(programmInformationViewModel);
                        }
                    }, e -> onError(new GeneralError(GeneralError.EPG_ERROR)), true);
                }
            }, e -> onError(new GeneralError(GeneralError.EPG_ERROR)));
        }

        @Override
        public void skipItemRemoved(SkipItem skipItem) {
            for (PlayBackListener listener : playBackListeners) {
                listener.skipItemRemoved(skipItem);
            }
        }

        @Override
        public void textualContent(TextData data) {
            getAppState().getPlayBackState().setTextData(data);
            if (data.getTextType() == TextData.TextType.Track)
                substitutionManager.queryForSubstitution(data.getContent(), data.getTitle());
            else if(data.getTextType() == TextData.TextType.News)
                substitutionManager.resetSubstitutionBuffer();
            for (PlayBackListener listener : playBackListeners) {
                listener.textualContent(data);
            }
        }

        @Override
        public void visualContent(ImageData visual) {
            if (visual != null) {
                getAppState().getPlayBackState().setCover(visual);
                for (PlayBackListener listener : playBackListeners) {
                    listener.visualContent(visual);
                }
            }
        }

        @Override
        public void started(SubstitutionItem substitution) {
            substitutionPlayerListener.started(substitution);
        }

        @Override
        public void stopped(SubstitutionItem substitution) {
            substitutionPlayerListener.stopped(substitution);
        }

        @Override
        public void playProgress(SubstitutionItem substitution, long current, long total) {
            substitutionPlayerListener.playProgress(substitution, current, total);

        }

        @Override
        public void skipItemAdded(SkipItem skipItem) {
            for (PlayBackListener listener : playBackListeners) {
                listener.skipItemAdded(skipItem);
            }
        }

        @Override
        public void itemStarted(SkipItem skipItem) {
            for (PlayBackListener listener : new HashSet<>(playBackListeners)) {
                listener.itemStarted(skipItem);
            }
        }
    };

    public AppState getAppState() {
        return appState;
    }

    public TimeshiftPlayer getCurrentTimeShiftPlayer() {
        return getTimeShiftManager().getCurrentTimeShiftPlayer();
    }

    public void refreshSubstitutionState(Activity context, SubstitutionProvider.ConnectionCallback o) {
        if(substitutionManager != null)
            substitutionManager.refreshConnection(context, o);
    }

    public void getSearchResultState(OnSearchResultListener<List<RadioServiceViewModel>> resultCallback) {
        tunerManager.getRadioServicesSorted(resultCallback);
    }

    public void removeSubstitutionPlayList() {
        substitutionManager.removeSubstitutionPlayList();
    }

    public void setSubstitutionPlayList(TrackLikeService.PlayList playList) {
        substitutionManager.setSubstitutionPlayList(playList);
    }

    public void setPrimarySubstitution(SubstitutionProviderType provider, android.content.Context context) {
        if(substitutionManager != null)
            substitutionManager.setPrimarySubstitutionProvider(provider, context);
    }

    public void stopScanningAndPlay(RadioService radioService) {
        radioManager.playWhenReady(radioService);
        tunerManager.stopScan();
    }

    public void stopService() {
        radioManager.stopCurrent();
    }

    public SubstitutionItem getCurrentSubstitution() {
        if(substitutionManager == null)
            return null;
        return (SubstitutionItem) substitutionManager.getCurrentSubstitution();
    }

    @FunctionalInterface
    public interface InitilaizationCallback {
        void onInitComplete();
    }


}
