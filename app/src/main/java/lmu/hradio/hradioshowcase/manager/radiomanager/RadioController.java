package lmu.hradio.hradioshowcase.manager.radiomanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.omri.radio.Radio;
import org.omri.radio.RadioStatusListener;
import org.omri.radio.impl.RadioImpl;
import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceType;
import org.omri.tuner.ReceptionQuality;
import org.omri.tuner.Tuner;
import org.omri.tuner.TunerListener;
import org.omri.tuner.TunerStatus;
import org.omri.tuner.TunerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.hradio.timeshiftplayer.SkipItem;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.broadcast.SignalStrengthBroadCast;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnManagerErrorListener;
import lmu.hradio.hradioshowcase.listener.OnRadioStatusUpdateListener;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.service.MusicService;

import static org.omri.radioservice.RadioServiceType.RADIOSERVICE_TYPE_DAB;
import static org.omri.radioservice.RadioServiceType.RADIOSERVICE_TYPE_EDI;
import static org.omri.radioservice.RadioServiceType.RADIOSERVICE_TYPE_FM;
import static org.omri.radioservice.RadioServiceType.RADIOSERVICE_TYPE_HDRADIO;
import static org.omri.radioservice.RadioServiceType.RADIOSERVICE_TYPE_IP;

/**
 * Controller to manage different tuner playbacks
 */
public class RadioController implements TunerListener, RadioStatusListener {

    private static final String TAG = RadioController.class.getSimpleName();

    /**
     * Radio state
     */
    private PlayerRadioTypeState state = PlayerRadioTypeState.NotActive;
    /**
     * Time shift manager to handle dab/edi playback
     */
    private TimeshiftManager timeshiftManager;
    /**
     * Radio manager to handle ip/fm playback
     */
    private RegularRadioManager regularRadioManager;
    /**
     * Error callback listeners
     */
    private Set<OnManagerErrorListener> errorListeners = new HashSet<>();
    /**
     * Status callback listeners
     */
    private Set<OnRadioStatusUpdateListener> statusListeners = new HashSet<>();
    /**
     * currently playing service
     */
    private RadioService currentService;
    /**
     * currently playing tuner
     */
    private Tuner currentTuner;

    /**
     * Signal strength broadcast sender
     */
    private SignalStrengthBroadCast.Sender sender;

    /**
     * Signal strength handler map
     */
    private Map<Tuner, ReceptionQuality> tunerQualityMap = new HashMap<>();

    public RadioController(Context context){
        timeshiftManager = new TimeshiftManager();
        regularRadioManager = new RegularRadioManager();

        timeshiftManager.registerPlayBackListener(playBackListener);
        regularRadioManager.registerPlayBackListener(playBackListener);

        for(Tuner tuner: Radio.getInstance().getAvailableTuners()) {
            tuner.subscribe(this);
            tunerQualityMap.put(tuner, ReceptionQuality.OKAY);
        }
        this.sender = new SignalStrengthBroadCast.Sender(context);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            registerAPI24NetworkListener(context);
        } else{
            registerNetworkListener(context);
        }

    }

    /**
     * receiver for android os signal strength broadcasts
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean notConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if(notConnected)
                updateIpTunerQuality(ReceptionQuality.NO_SIGNAL);
            else
                updateIpTunerQuality(ReceptionQuality.GOOD);

        }
    };

    /**
     * update calls for ip based tuners
     * @param newQuality - new quality
     */
    private void updateIpTunerQuality(ReceptionQuality newQuality){
        List<Tuner> ipTuners = new ArrayList<>(Radio.getInstance().getAvailableTuners(TunerType.TUNER_TYPE_IP_EDI));
        ipTuners.addAll(Radio.getInstance().getAvailableTuners(TunerType.TUNER_TYPE_IP_SHOUTCAST));
        for(Tuner tuner : ipTuners){
            tunerReceptionStatistics(tuner, currentTuner != null && currentTuner.equals(tuner), newQuality);
        }
    }

    /**
     * register network listener for all api levels
     * @param context - context
     */
    private void registerNetworkListener(Context context) {
        context.getApplicationContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * register network listener for api 21 and above
     * @param context- context
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void registerAPI24NetworkListener(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            manager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback(){

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                }

                @Override
                public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                    if(BuildConfig.DEBUG)Log.d("NetworkCallback","onLinkPropertiesChanged");

                }

                @Override
                public void onBlockedStatusChanged(Network network, boolean blocked) {
                    super.onBlockedStatusChanged(network, blocked);
                    if(BuildConfig.DEBUG)Log.d("NetworkCallback","onBlockedStatusChanged");

                }

                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    updateIpTunerQuality(ReceptionQuality.GOOD);

                    if(BuildConfig.DEBUG)Log.d("NetworkCallback","onAvailable");
                }

                @Override
                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    updateIpTunerQuality(ReceptionQuality.POOR);
                    if(BuildConfig.DEBUG)Log.d("NetworkCallback","onLosing");

                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    updateIpTunerQuality(ReceptionQuality.NO_SIGNAL);
                    if(BuildConfig.DEBUG)Log.d("NetworkCallback","onLost");

                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    updateIpTunerQuality(ReceptionQuality.NO_SIGNAL);
                    if(BuildConfig.DEBUG)Log.d("NetworkCallback","onUnavailable");

                }
            });
        }
    }

    /**
     * Handle tuner quality changes - simple service following if dab or wifi lost
     * @param tuner - tuner
     * @param quality - new tuner quality
     */
    private void handleQualityChange(Tuner tuner, ReceptionQuality quality) {
        if(BuildConfig.DEBUG)Log.d(TAG, "handleQualityChange: " + tuner.getTunerType().toString() + " : " + quality.toString());
        tunerQualityMap.put(tuner, quality);

        if(currentTuner == null || currentService == null) {
            return;
        }

        if(tuner.getTunerType().equals(currentTuner.getTunerType())) {
            sender.receptionQualityChanged(quality);
            if(stoppedOnConnectionLost){
                //startService(currentService);
                return;
            }
        }

        if (tuner.getTunerType() == currentTuner.getTunerType() && !isQualityOkay(quality)){
            tryFollowService(currentService);
        }
    }

    private boolean stoppedOnConnectionLost;

    /** simple service following if dab or wifi lost
     * @param currentService - service to follow
     */
    private void tryFollowService(RadioService currentService) {
        if(BuildConfig.DEBUG)Log.d(TAG, "tryFollowService");
        Tuner preferableTuner = currentTuner;
        RadioService preferableService = currentService;
        for(Tuner tuner : tunerQualityMap.keySet()){
            if(tuner == currentTuner) {
                continue;
            }
            if(isQualityOkay(tunerQualityMap.get(tuner))){
                RadioService serviceForTuner = findService(tuner, currentService);
                if(!isQualityOkay(tunerQualityMap.get(preferableTuner)) && serviceForTuner != null){
                    preferableTuner = tuner;
                    preferableService = serviceForTuner;
                } else if(isPreferableTuner(tuner, preferableTuner) && serviceForTuner != null){
                    preferableTuner = tuner;
                    preferableService = serviceForTuner;
                }
            }
        }
        if(preferableTuner.getTunerType() != currentTuner.getTunerType()) {
            if(BuildConfig.DEBUG)Log.d(TAG, "tryFollowService");
            stopCurrent();
            startService(preferableService);
        } else{
            stoppedOnConnectionLost = currentTuner != null;
            for(OnManagerErrorListener errorListener :errorListeners){
                errorListener.onError(new GeneralError(GeneralError.TUNER_ERROR));
            }
        }
    }

    /**
     * Skip next content
     * @param isSubstitutionEnabled - substituted content ended with skip
     * @return true if items to skip false else
     */
    public boolean skipNext(boolean isSubstitutionEnabled) {
        if (state == RadioController.PlayerRadioTypeState.TimeShift) {
            int nextSkipItem = timeshiftManager.getNextIndexForCurrentPosition();
            if (nextSkipItem != -1) {
                timeshiftManager.skipToIndex(nextSkipItem);
                return true;
            }
            if(isSubstitutionEnabled) {
                if(BuildConfig.DEBUG)Log.d(TAG, "Pausing timeshiftplayer for substitution");
                timeshiftManager.pauseTimeshift();
            }
        } else {
            if(isSubstitutionEnabled)
                regularRadioManager.pause();
        }
        return false;
    }

    /**
     * Pause current playback
     */
    public void pauseCurrent() {
        if (state == RadioController.PlayerRadioTypeState.TimeShift) {
            getTimeshiftManager().pauseTimeshift();
        } else {
            regularRadioManager.pause();
        }
    }

    /**
     * resume current content
     */
    public void resumeCurrent() {
        if (state == PlayerRadioTypeState.TimeShift)
            getTimeshiftManager().resumeTimeshift();
        else
            regularRadioManager.resume();
    }

    /**
     * Skip back in timeshift buffer
     */
    public void skipBack() {
        if (state == PlayerRadioTypeState.TimeShift)
            getTimeshiftManager().skipBack();
        else
            for(OnManagerErrorListener errorListener: errorListeners)
                errorListener.onError(new GeneralError(GeneralError.CONTROLLS_ERROR));
    }

    /**
     * jump to live in timeshift buffer
     */
    public void jumpToLive() {
        if (state == PlayerRadioTypeState.TimeShift)
            getTimeshiftManager().jumpToLive();
        else
            regularRadioManager.jumpToLive();
    }

    /**
     * stop service if running
     */
    public void stopCurrent() {

        if(regularRadioManager.isPlaying()) {
            regularRadioManager.stop();
        }else if (getTimeshiftManager().isPlaying()) {
            getTimeshiftManager().stopTimeshift();
        }
        state = PlayerRadioTypeState.NotActive;

    }

    /**
     * Handle started service and start timeshifting
     * @param service - the started service
     * @param binder - AudioSinkBinder
     * @param tuner  - started tuner
     * @param context - android context
     */
    public void serviceStarted(RadioService service, MusicService.HRadioAudioSinkBinder binder, Tuner tuner, Context context) {
        this.currentService = service;
        this.currentTuner = tuner;

        //start time shift
        if(service.getRadioServiceType().equals(RadioServiceType.RADIOSERVICE_TYPE_DAB) || service.getRadioServiceType().equals(RadioServiceType.RADIOSERVICE_TYPE_EDI)) {
            RadioController.this.state = RadioController.PlayerRadioTypeState.TimeShift;

            getTimeshiftManager().startTimeshift(service, context, binder);
        }else{
            RadioController.this.state = PlayerRadioTypeState.Ip;
            regularRadioManager.startRegularRadio(service, binder, context);
        }

    }

    /**
     * Seek to selected buffer position
     * @param progress - position
     */
    public void seekTo(long progress) {
        if (state == PlayerRadioTypeState.TimeShift)
            getTimeshiftManager().seekTimeshiftPlayer(progress);
        else
            regularRadioManager.seekTimeshiftPlayer(progress);
    }

    /**
     * Calculate tuner prio
     * @param t1 - first tuner
     * @param t2 - second tuner
     * @return is t1 preferred to t2
     */
    private boolean isPreferableTuner(Tuner t1, Tuner t2){
        if(t1.getTunerType() == TunerType.TUNER_TYPE_DAB && isQualityOkay(tunerQualityMap.get(t1)))
            return true;
        if(t2.getTunerType() == TunerType.TUNER_TYPE_DAB && isQualityOkay(tunerQualityMap.get(t2)))
            return false;
        if(t1.getTunerType() == TunerType.TUNER_TYPE_IP_EDI && isQualityOkay(tunerQualityMap.get(t1)) )
            return true;
        if(t2.getTunerType() == TunerType.TUNER_TYPE_IP_EDI && isQualityOkay(tunerQualityMap.get(t2)))
            return false;
        if(t1.getTunerType() == TunerType.TUNER_TYPE_FM && isQualityOkay(tunerQualityMap.get(t1)))
            return true;
        return false;
    }

    /**
     * Check if quality is ok
     * @param quality - the quality
     * @return if not no signal
     */
    private static boolean isQualityOkay(ReceptionQuality quality){
        if(quality == null) return false;
        return quality == ReceptionQuality.BAD ||quality == ReceptionQuality.POOR || quality == ReceptionQuality.BEST || quality == ReceptionQuality.GOOD || quality == ReceptionQuality.OKAY;
    }

    /**
     * Find best Service from list of different services for equal stations
     * @param radioServices - service list
     * @return preferred service
     */
    public RadioService findPreferedService(List<RadioService> radioServices){
        RadioService prefered= null;

        for(RadioService service : radioServices){

            Tuner tuner = getTuner(service.getRadioServiceType());
            if(tuner == null )
                continue;

            if(prefered == null)
                prefered = service;

            if(RADIOSERVICE_TYPE_DAB.equals(service.getRadioServiceType())){
                return service;
            }

            if(!isQualityOkay(tunerQualityMap.get(tuner)))
                continue;

            if(RADIOSERVICE_TYPE_EDI.equals(service.getRadioServiceType()))
                prefered = service;
            else if(RADIOSERVICE_TYPE_FM.equals(service.getRadioServiceType()) &&
                    ( prefered.getRadioServiceType().equals(RADIOSERVICE_TYPE_IP)|| prefered.getRadioServiceType().equals(RADIOSERVICE_TYPE_HDRADIO)))
                prefered = service;

        }
        return prefered;
    }

    /**
     * Find Tuner for given radio service type
     * @param radioServiceType  - the service type
     * @return matching tuner
     */
    private Tuner getTuner(RadioServiceType radioServiceType){
        List<Tuner> tuners;
        switch (radioServiceType) {
            case RADIOSERVICE_TYPE_DAB:
                tuners = Radio.getInstance().getAvailableTuners(TunerType.TUNER_TYPE_DAB);
                break;
            case RADIOSERVICE_TYPE_EDI:
                tuners = Radio.getInstance().getAvailableTuners(TunerType.TUNER_TYPE_IP_EDI);
                break;
            case RADIOSERVICE_TYPE_FM:
                tuners = Radio.getInstance().getAvailableTuners(TunerType.TUNER_TYPE_FM);
                break;
            case RADIOSERVICE_TYPE_IP:
                tuners = Radio.getInstance().getAvailableTuners(TunerType.TUNER_TYPE_IP_SHOUTCAST);
                break;
            default: tuners = new ArrayList<>();
                break;
        }
        return (tuners == null || tuners.isEmpty())? null : tuners.get(0);
    }

    /**
     * Find service for given tuner
     * @param tuner - the tuner
     * @param currentService - the service on different tuner
     * @return service representation for given tuner
     */
    private RadioService findService(Tuner tuner, RadioService currentService) {
        for(RadioService service: tuner.getRadioServices()){
            if(service.equalsRadioService(currentService))
                return service;
        }
        return null;
    }


    //listener section

    @Override
    public void radioServiceStarted(Tuner tuner, RadioService radioService) {
        if(BuildConfig.DEBUG)Log.d(TAG, "radioServiceStarted: " + radioService.getRadioServiceType().toString() + " : " + radioService.getServiceLabel());

        if (currentService != null && !currentService.equals(radioService)) {
            if(BuildConfig.DEBUG)Log.d(TAG, "Stopping previous service: " + this.currentService.getServiceLabel());
            Radio.getInstance().stopRadioService(currentService);
        }
        currentService = radioService;
        for(OnRadioStatusUpdateListener statusListener : new HashSet<>(statusListeners))
            statusListener.radioServiceStarted(tuner, radioService);


    }

    @Override
    public void radioServiceStopped(Tuner tuner, RadioService radioService) {
        if(BuildConfig.DEBUG)Log.d(TAG, "radioServiceStopped: " + radioService.getRadioServiceType().toString() + " : " + radioService.getServiceLabel());

        for(OnRadioStatusUpdateListener statusListener :  new HashSet<>(statusListeners))
            statusListener.radioServiceStopped(tuner, radioService);
    }

    @Override
    public void tunerReceptionStatistics(Tuner tuner, boolean b, ReceptionQuality receptionQuality) {
        //if(BuildConfig.DEBUG)Log.d(TAG, tuner.getTunerType() + " " + receptionQuality.name());
        //handleQualityChange(tuner, receptionQuality);
    }

    private RadioService playWhenReadyService;

    public void playWhenReady(RadioService radioService){
        playWhenReadyService = radioService;
    }

    @Override
    public void tunerRawData(Tuner tuner, byte[] bytes) { }

    @Override
    public void tunerStatusChanged(Tuner tuner, TunerStatus tunerStatus) {
        if(playWhenReadyService != null){
            Tuner tunerService = getTuner(playWhenReadyService.getRadioServiceType());
            if(tunerService == tuner && tunerStatus == TunerStatus.TUNER_STATUS_INITIALIZED) {
                if(BuildConfig.DEBUG)Log.d(TAG, "tunerStatusChanged: " + tunerStatus.toString());
                startService(playWhenReadyService);
                playWhenReadyService = null;
            }
        }
    }

    @Override
    public void tunerScanStarted(Tuner tuner) { }

    @Override
    public void tunerScanProgress(Tuner tuner, int i) {}

    @Override
    public void tunerScanFinished(Tuner tuner) {}

    @Override
    public void tunerScanServiceFound(Tuner tuner, RadioService radioService) { }

    public boolean startService(RadioService service) {
        if(BuildConfig.DEBUG)Log.d(TAG, "startService: " + service.getServiceLabel() + " : " + service.getRadioServiceType().toString());
        if(currentService != null) {
            if(BuildConfig.DEBUG)Log.d(TAG, "startService previously running service" + currentService.getServiceLabel() + " : " + currentService.getRadioServiceType().toString());
            Radio.getInstance().stopRadioService(currentService);
        }

        currentService = service;

        Tuner tuner = getTuner(service.getRadioServiceType());
        if(tuner != null && tuner.getTunerStatus() == TunerStatus.TUNER_STATUS_SCANNING){
            GeneralError error = new GeneralError(GeneralError.PLAY_WHEN_SCANNING);
            for(OnManagerErrorListener errorListener : errorListeners)
                errorListener.onError(error);
            return false;
        }
        if(tuner != null && tuner.getTunerStatus() == TunerStatus.TUNER_STATUS_INITIALIZED)
            tuner.startRadioService(service);
        return true;
    }

    public boolean startDirectEdi(String ediLink) {
        ((RadioImpl)Radio.getInstance()).startDirectSbtStream(ediLink);
        return true;
    }


    @Override
    public void tunerAttached(Tuner tuner) {
        tuner.subscribe(this);
        tunerQualityMap.put(tuner, ReceptionQuality.NO_SIGNAL);

    }

    @Override
    public void tunerDetached(Tuner tuner) {
        tuner.unsubscribe(this);
        tunerQualityMap.remove(tuner);
    }

    private Set<PlayBackListener> playBackListeners = new HashSet<>();

    public void unregisterPlayBackListener(PlayBackListener playBackListener) {
        this.playBackListeners.remove(playBackListener);
    }

    public void registerPlayBackListener(PlayBackListener playBackListener) {
        this.playBackListeners.add(playBackListener);
    }

    private PlayBackListener playBackListener = new PlayBackListener() {
        @Override
        public void onError(@NonNull GeneralError error) {
            for(PlayBackListener listener : playBackListeners){
                listener.onError(error);
            }
        }

        @Override
        public void started() {
            for(PlayBackListener listener : playBackListeners){
                listener.started();
            }
        }

        @Override
        public void stopped() {
            for(PlayBackListener listener : playBackListeners){
                listener.stopped();
            }
        }

        @Override
        public void paused() {
            for(PlayBackListener listener : playBackListeners){
                listener.paused();
            }
        }

        @Override
        public void playProgress(long current, long total) {
            for(PlayBackListener listener : playBackListeners){
                listener.playProgress(current, total);
            }
        }

        @Override
        public void playProgressRealtime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration) {
            for(PlayBackListener listener : playBackListeners){
                listener.playProgressRealtime(realTimePosix, streamTimePosix, curPos, totalDuration);
            }
        }

	    @Override
	    public void sbtSeeked() {
            for(PlayBackListener listener : playBackListeners){
                listener.sbtSeeked();
            }
	    }

	    @Override
        public void skipItemRemoved(SkipItem skipItem) {
            for(PlayBackListener listener : playBackListeners){
                listener.skipItemRemoved(skipItem);
            }
        }

        @Override
        public void textualContent(TextData content) {
            for(PlayBackListener listener : playBackListeners){
                listener.textualContent(content);
            }
        }

        @Override
        public void visualContent(ImageData visual) {
            for(PlayBackListener listener : playBackListeners){
                listener.visualContent(visual);
            }
        }

        @Override
        public void started(SubstitutionItem substitution) {
            for(PlayBackListener listener : playBackListeners){
                listener.started(substitution);
            }
        }

        @Override
        public void stopped(SubstitutionItem substitution) {
            for(PlayBackListener listener : playBackListeners){
                listener.stopped(substitution);
            }
        }

        @Override
        public void playProgress(SubstitutionItem substitution, long current, long total) {
            for(PlayBackListener listener : playBackListeners){
                listener.playProgress(substitution, current, total);
            }
        }

        @Override
        public void skipItemAdded(SkipItem skipItem) {
            for(PlayBackListener listener : playBackListeners){
                listener.skipItemAdded(skipItem);
            }
        }

        @Override
        public void itemStarted(SkipItem skipItem) {
            for(PlayBackListener listener : playBackListeners){
                listener.itemStarted(skipItem);
            }
        }
    };

    public void registerErrorListener(OnManagerErrorListener errorListener) {
        this.errorListeners.add(errorListener);
    }


    public void unregisterErrorListener(OnManagerErrorListener errorListener) {
        this.errorListeners.remove(errorListener);
    }

    public void registerOnRadioStatusUpdateListener(OnRadioStatusUpdateListener listener) {
        this.statusListeners.add(listener);
    }


    public void unregisterOnRadioStatusUpdateListener(OnRadioStatusUpdateListener listener) {
        this.statusListeners.remove(listener);
    }


    public TimeshiftManager getTimeshiftManager() {
        return timeshiftManager;
    }

    public SkipItem getCurrentSkipItem() {
        if(state == PlayerRadioTypeState.TimeShift) {
            return getTimeshiftManager().getCurrentSkipItem();
        }
        return null;
    }

    public String getRunningService() {
        if (currentService != null)
            return currentService.getServiceLabel();
        else return "Radio";
    }

    private enum PlayerRadioTypeState {
        TimeShift, Ip, NotActive
    }
}
