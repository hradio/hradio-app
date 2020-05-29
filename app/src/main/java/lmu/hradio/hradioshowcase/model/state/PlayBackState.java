package lmu.hradio.hradioshowcase.model.state;

import org.omri.radioservice.RadioServiceType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.ProgrammeInformationViewModel;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.view.TextData;

public class PlayBackState implements Serializable {
    private static final long serialVersionUID = 6372747945825295300L;
    private TextData textData;
    private ImageData cover;
    private boolean isRunning;
    private SubstituionState substitution = SubstituionState.INACTIVE;
    private ProgrammeInformationViewModel programmeInformationViewModel;
    private List<RadioServiceViewModel> relateRecommendedServices = new ArrayList<>();
    private RadioServiceViewModel runningService;
    private RadioServiceType tunerType;

    public ImageData getCover() {
        return cover;
    }

    private transient Set<OnChangeListener> listeners = new HashSet<>();
    private transient Set<OnChangeServiceListener> onChangeServiceListeners = new HashSet<>();

    public void setCover(ImageData cover) {
        this.cover = cover;
        fireListeners();
    }

    public TextData getTextData() {
        return textData;
    }

    public void setTextData(TextData textData) {
        this.textData = textData;
        fireListeners();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
        fireListeners();

    }

    public ProgrammeInformationViewModel getProgrammeInformationViewModel() {
        return programmeInformationViewModel;
    }

    public void setProgrammeInformationViewModel(ProgrammeInformationViewModel programmeInformationViewModel) {
        this.programmeInformationViewModel = programmeInformationViewModel;

    }

    public List<RadioServiceViewModel> getRelateRecommendedServices() {
        return relateRecommendedServices;
    }

    public void setRelateRecommendedServices(List<RadioServiceViewModel> relateRecommendedServices) {
        this.relateRecommendedServices = relateRecommendedServices;
    }

    public RadioServiceViewModel getRunningService() {
        return runningService;
    }

    public void setRunningService(RadioServiceViewModel runningService) {
        this.runningService = runningService;
        textData = new TextData(runningService.getServiceLabel());
        fireListeners();
        for(OnChangeServiceListener listener: onChangeServiceListeners){
            listener.onChangeService();
        }
    }

    public void registerOnChangeListener(OnChangeListener listener){
        this.listeners.add(listener);
    }

    public void unregisterOnChangeListener(OnChangeListener listener){
        this.listeners.remove(listener);
    }

    public void registerOnChangeServiceListener(OnChangeServiceListener listener){
        this.onChangeServiceListeners.add(listener);
    }

    public void unregisterOnChangeServiceListener(OnChangeServiceListener listener){
        this.onChangeServiceListeners.remove(listener);
    }

    private void fireListeners(){
        for(OnChangeListener listener : listeners){
            listener.onChange(this);
        }
    }

    public void setTunerType(RadioServiceType radioServiceType) {
        this.tunerType = radioServiceType;
    }

    public RadioServiceType getTunerType() {
        return tunerType;
    }

    public SubstituionState getSubstitution() {
        return substitution;
    }

    public void setSubstitution(SubstituionState substitution) {
        this.substitution = substitution;
    }

    @FunctionalInterface
    public interface OnChangeListener{
        void onChange(PlayBackState state);
    }

   @FunctionalInterface
    public interface OnChangeServiceListener{
        void onChangeService();
    }

    public enum SubstituionState{
        INACTIVE, SPOTIFY, NATIVE
    }

}
