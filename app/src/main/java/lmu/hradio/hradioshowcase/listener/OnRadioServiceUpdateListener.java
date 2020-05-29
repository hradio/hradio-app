package lmu.hradio.hradioshowcase.listener;

import java.util.List;

import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;

public interface OnRadioServiceUpdateListener {
    /**
     * radio service list updated
     *
     * @param newServices - the new service list
     */
    void onServiceUpdate(List<RadioServiceViewModel> newServices);
}
