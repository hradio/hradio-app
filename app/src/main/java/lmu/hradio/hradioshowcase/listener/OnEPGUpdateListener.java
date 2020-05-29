package lmu.hradio.hradioshowcase.listener;

import lmu.hradio.hradioshowcase.model.view.ProgrammeInformationViewModel;

/**
 * Epg programme information updated callback
 */
public interface OnEPGUpdateListener {

    /**
     * Epg programme information updated callback
     *
     * @param programmeInformation - the new epg information
     */
    void onEPGUpdate(ProgrammeInformationViewModel programmeInformation);

}
