package lmu.hradio.hradioshowcase.listener;

import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;

/**
 * Play radio service listener
 */
public interface PlayRadioServiceListener {
    /**
     * play selected service callback
     *
     * @param radioService - the selected service
     */
    void onRadioServiceSelected(RadioServiceViewModel radioService);
}
