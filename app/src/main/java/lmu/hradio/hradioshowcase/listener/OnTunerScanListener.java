package lmu.hradio.hradioshowcase.listener;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceType;

import java.util.List;

import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;

/**
 * Tuner scan listener interface
 */
public interface OnTunerScanListener {

    /**
     * Tuner started scanning
     */
    void tunerScanStarted();

    /**
     * Tuner progress scanning
     *
     * @param percentScanned - scanned percentage
     */
    void tunerScanProgress(final int percentScanned);

    /**
     * Tuner scan finished
     */
    void tunerScanFinished();

    /**
     * result of last tuner scan
     *
     * @param services - found services
     */
    void onResult(List<RadioServiceViewModel> services);

    /**
     * service found while scanning
     *
     * @param service - the found service
     * @param type    - the found services type
     */
    void onServiceFound(RadioServiceViewModel service, RadioService type);

}
