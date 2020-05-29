package lmu.hradio.hradioshowcase.manager.omri;

import android.content.Context;
import android.util.Log;

import org.omri.radio.Radio;
import org.omri.radio.RadioErrorCode;
import org.omri.radio.RadioStatus;
import org.omri.radio.RadioStatusListener;
import org.omri.radio.impl.RadioImpl;
import org.omri.radioservice.RadioService;
import org.omri.tuner.ReceptionQuality;
import org.omri.tuner.Tuner;
import org.omri.tuner.TunerListener;
import org.omri.tuner.TunerStatus;
import org.omri.tuner.TunerType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnManagerErrorListener;
import lmu.hradio.hradioshowcase.listener.OnTunerScanListener;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.weburlconfig.WebUrl;

/**
 * Omri tuner manager interface
 */
public class TunerManager implements TunerListener, RadioStatusListener {

    private static final String TAG = TunerManager.class.getSimpleName();

    private Set<OnManagerErrorListener> errorListeners = new HashSet<>();

    private boolean initialized = false;

    private ScanWorker worker;

    private List<RadioService> tmpServices = new ArrayList<>();

    private final TunerManagerListener mListener;

    public TunerManager(Context context, TunerManagerListener listener) {
        mListener = listener;
        init(context);
        worker = new ScanWorker();
    }

    public void deinit() {
        Radio.getInstance().deInitialize();
    }

    private void init(Context context) {
        Radio.getInstance().registerRadioStatusListener(this);
        RadioStatus stat = Radio.getInstance().getRadioStatus();
        if (stat == RadioStatus.STATUS_RADIO_SUSPENDED) {
            RadioErrorCode initCode = Radio.getInstance().initialize(context);
            if (initCode == RadioErrorCode.ERROR_INIT_OK) {
                if(BuildConfig.DEBUG)Log.d(TAG, "Radio successfully initialized!");
            }
        } else {
            for (OnManagerErrorListener listener : errorListeners) {
                listener.onError(new GeneralError(GeneralError.TUNER_ERROR));
            }
        }
        for (Tuner tuner : Radio.getInstance().getAvailableTuners()) {
            tuner.subscribe(this);
            if (tuner.getTunerStatus() != TunerStatus.TUNER_STATUS_INITIALIZED) {
                tuner.initializeTuner();
            }
        }
    }

    public void stopScan() {
        Radio.getInstance().stopRadioServiceScan();
        tmpServices.clear();
    }

    public void startScan(Map<String, String> params, OnTunerScanListener serviceSearchListener, OnManagerErrorListener errorListener, boolean deleteExisting) {
        startScan(Arrays.asList(TunerType.values()), params, serviceSearchListener, errorListener, deleteExisting);
    }

    public void startScan(List<TunerType> selectedTuners, Map<String, String> params, OnTunerScanListener serviceSearchListener, OnManagerErrorListener errorListener, boolean deleteExisting) {
        if(BuildConfig.DEBUG)Log.d(TAG, "searchFor startScan");
        tmpServices.clear();
        worker.startScan(selectedTuners, params, serviceSearchListener, errorListener, deleteExisting);
    }

    private void getLastScanState() {
        List<RadioServiceViewModel> services = getRadioServicesSorted();
        for (OnSearchResultListener<List<RadioServiceViewModel>> listener : pendingRequests) {
            listener.onResult(services);
        }
        if(BuildConfig.DEBUG)Log.d("getLastScanState", "Found Services: " + services.size());

        pendingRequests.clear();
    }

    private List<OnSearchResultListener<List<RadioServiceViewModel>>> pendingRequests = new ArrayList<>();

    public void getRadioServicesSorted(OnSearchResultListener<List<RadioServiceViewModel>> listener) {
        if (initialized)
            listener.onResult(getRadioServicesSorted());
        else
            pendingRequests.add(listener);
    }

    private List<RadioServiceViewModel> getRadioServicesSorted() {

        return this.worker.getRadioServicesSorted();

    }

    public void setWebConfig(WebUrl[] urls) {
        worker.setWebConfig(urls);
    }

    public List<RadioService> getService(RadioServiceViewModel radioService) {
        List<RadioService> services = Radio.getInstance().getRadioServices().isEmpty() ? tmpServices : Radio.getInstance().getRadioServices();
        for (RadioService service : services) {
            if (service.getServiceLabel().equals(radioService.getServiceLabel())) {
                List<RadioService> result = new ArrayList<>();
                result.add(service);
                result.addAll(((RadioImpl) Radio.getInstance()).getFollowingServices(service));
                return result;
            }
        }
        return new ArrayList<>();
    }


    @Override
    public void tunerAttached(Tuner tuner) {
        tuner.subscribe(this);

        if (tuner.getTunerStatus() == TunerStatus.TUNER_STATUS_NOT_INITIALIZED) {
            tuner.initializeTuner();
        }
    }

    @Override
    public void tunerDetached(Tuner tuner) {
        tuner.unsubscribe(this);
    }

    @Override
    public void tunerReceptionStatistics(final Tuner tuner, final boolean locked, final ReceptionQuality quality) {

    }

    @Override
    public void tunerRawData(Tuner tuner, byte[] data) {
        //Do something useful with this later
    }

    private int initTuners = 0;

    @Override
    public void tunerStatusChanged(Tuner tuner, TunerStatus tunerStatus) {
        if (tunerStatus == TunerStatus.TUNER_STATUS_INITIALIZED && !initialized) {
            initTuners++;
            if(BuildConfig.DEBUG)Log.d(TAG, "Tuner initialized");
            if (initTuners >= Radio.getInstance().getAvailableTuners().size()) {
                initialized = true;
                if(mListener != null) {
                    mListener.tunerManagerInitialized();
                }
                getLastScanState();
            }
        }

    }


    @Override
    public void tunerScanStarted(Tuner tuner) {
    }

    @Override
    public void tunerScanProgress(Tuner tuner, int i) {

    }

    @Override
    public void tunerScanFinished(Tuner tuner) {
    }

    @Override
    public void tunerScanServiceFound(Tuner tuner, RadioService radioService) {
        //Do something useful with this later e.g. update scan status with found services
        if(BuildConfig.DEBUG)Log.d(tuner.getTunerType().name(), radioService.getServiceLabel());

        tmpServices.add(radioService);
    }

    @Override
    public void radioServiceStarted(Tuner tuner, RadioService radioService) {

    }

    @Override
    public void radioServiceStopped(Tuner tuner, RadioService radioService) {

    }

    public void registerErrorListener(OnManagerErrorListener listener) {
        errorListeners.add(listener);
    }


    public void unregisterErrorListener(OnManagerErrorListener listener) {
        errorListeners.remove(listener);
    }

    public interface TunerManagerListener {
        void tunerManagerInitialized();
    }
}