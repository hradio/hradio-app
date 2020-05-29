package lmu.hradio.hradioshowcase.manager.omri;

import android.os.Bundle;
import android.util.Log;

import org.omri.radio.Radio;
import org.omri.radio.RadioStatusListener;
import org.omri.radio.impl.RadioImpl;
import org.omri.radioservice.RadioServiceDabEdi;
import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceDab;
import org.omri.radioservice.RadioServiceIp;
import org.omri.radioservice.RadioServiceType;
import org.omri.tuner.ReceptionQuality;
import org.omri.tuner.Tuner;
import org.omri.tuner.TunerListener;
import org.omri.tuner.TunerStatus;
import org.omri.tuner.TunerType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnManagerErrorListener;
import lmu.hradio.hradioshowcase.listener.OnTunerScanListener;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.weburlconfig.DabIdentifier;
import lmu.hradio.hradioshowcase.model.weburlconfig.WebUrl;
import lmu.hradio.hradioshowcase.util.DabUtlis;

public class ScanWorker implements RadioStatusListener, TunerListener {

    private static final String TAG = ScanWorker.class.getSimpleName();

    private AtomicBoolean isScanning = new AtomicBoolean(false);

    private BlockingDeque<ScanRequest> requestQueue;

    public ScanWorker() {
        initListeners();
        requestQueue = new LinkedBlockingDeque<>();
    }

    private void initListeners() {

    }

    public void startScan(List<TunerType> selectedTuners, Map<String, String> params, OnTunerScanListener serviceSearchListener, OnManagerErrorListener errorListener, boolean deleteExisting) {
        for(Map.Entry srchParam : params.entrySet()) {
            if (BuildConfig.DEBUG) Log.d(TAG, "searchFor startScan() Param: " + srchParam.getKey() + " : " + srchParam.getValue());
        }
        for(TunerType tType : selectedTuners) {
            if (BuildConfig.DEBUG) Log.d(TAG, "searchFor TunerTypes: " + tType.toString());
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("use_hradio", true);
        bundle.putBoolean("delete_services", deleteExisting);

        for (String s : params.keySet()) {
            bundle.putString(s, params.get(s));
        }

        Collections.sort(selectedTuners, (t1, t2) -> Integer.compare(t1.name().length(), t2.name().length()));

        List<Tuner> toBeScanned = new ArrayList<>();
        for (TunerType type : selectedTuners) {
            toBeScanned.addAll(Radio.getInstance().getAvailableTuners(type));
        }
        boolean dabOnly = true;
        for(Tuner tuner: toBeScanned){
            if(tuner.getTunerType() != TunerType.TUNER_TYPE_DAB){
                dabOnly = false;
                break;
            }
        }
        if(dabOnly){
            bundle.putBoolean(RadioImpl.SERVICE_SEARCH_OPT_HYBRID_SCAN, true);
        }


        ScanRequest request = new ScanRequest();
        request.listener = serviceSearchListener;
        request.scanTask = ScanTask.createTask(bundle, toBeScanned.iterator(), toBeScanned.size(), deleteExisting);
        request.errorListener = errorListener;

        requestQueue.addLast(request);
        if (!isScanning.get()) {
            startNextScan();
        }
    }

    private void startNextScan() {
        if(BuildConfig.DEBUG)Log.d(TAG, "searchFor startNextScan()");

        if (requestQueue.isEmpty()) {
            isScanning.set(false);
        } else {
            isScanning.set(true);
            ScanRequest next = getCurrentRequest();

            if (next.scanTask.isDeleteExisting()) {
                for (RadioService service : new ArrayList<>(Radio.getInstance().getRadioServices()))
                    ((RadioImpl) Radio.getInstance()).deleteRadioService(service);
            }

            if (!next.scanTask.scanNextTuner(this)) {
                next.errorListener.onError(new GeneralError(GeneralError.TUNER_NOT_AVAILABLE));
                requestQueue.removeFirst().listener.onResult(new ArrayList<>());
                startNextScan();
            }
        }
    }

    private ScanRequest getCurrentRequest() {
        return requestQueue.getFirst();
    }

    @Override
    public void tunerStatusChanged(final Tuner tuner, TunerStatus tunerStatus) {

        if(BuildConfig.DEBUG)Log.d("tunerStatusChanged", tuner.getTunerType() + ": " + tunerStatus);

        if (tunerStatus == TunerStatus.TUNER_STATUS_INITIALIZED) {
            ScanRequest request = getCurrentRequest();
            int realProgress = (int) request.scanTask.calculateProgress(100);
            request.listener.tunerScanProgress(realProgress);
            tuner.unsubscribe(this);
            //Dirty workaround: ip scan listeners fire before stop scan ->
            //immediate start scan of second ip tuner would be canceled
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (request.scanTask == null || !request.scanTask.scanNextTuner(ScanWorker.this)) {
                        requestQueue.removeFirst().listener.onResult(getRadioServicesSorted());
                        startNextScan();
                    }

                }
            }, 500);
        }

    }


    @Override
    public void tunerScanStarted(Tuner tuner) {
    }

    @Override
    public void tunerScanProgress(Tuner tuner, final int percentScanned) {
        ScanRequest request = getCurrentRequest();
        int realProgress = (int) request.scanTask.calculateProgress(percentScanned);
        request.listener.tunerScanProgress(realProgress);
    }

    @Override
    public void tunerScanFinished(Tuner tuner) {
        if(BuildConfig.DEBUG)Log.d(TAG, "searchFor tunerScanFinished: " + tuner.getRadioServices().size());
        tuner.stopRadioServiceScan();
    }

    private WebUrl[] configUrls;

    public void setWebConfig(WebUrl[] urls) {
        this.configUrls = urls;
    }

    protected List<RadioServiceViewModel> getRadioServicesSorted() {
        List<RadioServiceViewModel> result = new ArrayList<>();
        List<RadioService> foundServices = new ArrayList<>();
        for (RadioService service : Radio.getInstance().getRadioServices()) {
            if (service.getRadioServiceType().equals(RadioServiceType.RADIOSERVICE_TYPE_DAB) && !((RadioServiceDab) service).isProgrammeService())
                continue;
            if (service.getRadioServiceType().equals(RadioServiceType.RADIOSERVICE_TYPE_EDI) && !((RadioServiceDabEdi) service).isProgrammeService())
                continue;

            boolean containsService = false;
            for (RadioService foundService : foundServices) {
                if (foundService.equalsRadioService(service)) {
                    containsService = true;
                    break;
                }
            }
            if (!containsService) {
                foundServices.add(service);
                RadioServiceViewModel vm = new RadioServiceViewModel(service);
                vm.setWebAppUrl(getWebUrl(service));
                RadioImpl radio = (RadioImpl) Radio.getInstance();
                vm.addRadioServices(radio.getFollowingServices(service));
                result.add(vm);
            }
            // add all available tuner types
        }

        Collections.sort(result, (serv1, serv2) -> serv1.getServiceLabel().compareToIgnoreCase(serv2.getServiceLabel()));
        return result;
    }


    private String getWebUrl(RadioService service) {
        if(configUrls == null) return null;
        for (WebUrl url : configUrls) {
            if (service instanceof RadioServiceIp && url.getServiceLabel().equals(service.getServiceLabel())) {
                return url.getRadioWebUrl();
            }
            if (service instanceof RadioServiceDab) {
                RadioServiceDab serviceDab = (RadioServiceDab) service;
                for (DabIdentifier id : url.getIds()) {
                    if (serviceDab.getEnsembleEcc() == id.getEnsembleEcc() && serviceDab.getServiceId() == id.getmServiceId()) {
                        return url.getRadioWebUrl();
                    }
                }
            }

        }
        return null;
    }

    @Override
    public void tunerScanServiceFound(Tuner tuner, RadioService radioService) {
        if (radioService.getRadioServiceType().equals(RadioServiceType.RADIOSERVICE_TYPE_DAB) && !((RadioServiceDab) radioService).isProgrammeService())
            return;
        if (radioService.getRadioServiceType().equals(RadioServiceType.RADIOSERVICE_TYPE_EDI) && !((RadioServiceDabEdi) radioService).isProgrammeService())
            return;
        //TODO DAB FILTER

        if(tuner.getTunerType() == TunerType.TUNER_TYPE_DAB){
           Bundle bundle = getCurrentRequest().scanTask.getBundle();
           if(DabUtlis.filter(radioService, bundle)){
               ((RadioImpl) Radio.getInstance()).deleteRadioService(radioService);
           }

        }
        if(BuildConfig.DEBUG)Log.d(radioService.getRadioServiceType().name(), radioService.getServiceLabel());
        RadioServiceViewModel viewModel = new RadioServiceViewModel(radioService);
        getCurrentRequest().listener.onServiceFound(viewModel, radioService);
    }


    @Override
    public void radioServiceStarted(Tuner tuner, RadioService radioService) {
    }

    @Override
    public void radioServiceStopped(Tuner tuner, RadioService radioService) {
    }

    @Override
    public void tunerReceptionStatistics(Tuner tuner, boolean b, ReceptionQuality receptionQuality) {
    }

    @Override
    public void tunerAttached(Tuner tuner) {
    }

    @Override
    public void tunerDetached(Tuner tuner) {
    }

    @Override
    public void tunerRawData(Tuner tuner, byte[] bytes) {
    }


    private static class ScanRequest {
        private ScanTask scanTask;
        private OnTunerScanListener listener;
        private OnManagerErrorListener errorListener;
    }


}
