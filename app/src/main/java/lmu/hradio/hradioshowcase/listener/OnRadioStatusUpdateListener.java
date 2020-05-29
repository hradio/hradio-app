package lmu.hradio.hradioshowcase.listener;

import org.omri.radioservice.RadioService;
import org.omri.tuner.Tuner;

public interface OnRadioStatusUpdateListener {

    /**
     * Tuner started radio service
     *
     * @param tuner   - the tuner
     * @param service - the service
     */
    void radioServiceStarted(Tuner tuner, RadioService service);

    /**
     * Tuner stoped radio service
     *
     * @param tuner   - the tuner
     * @param service - the service
     */
    void radioServiceStopped(Tuner tuner, RadioService service);
}
