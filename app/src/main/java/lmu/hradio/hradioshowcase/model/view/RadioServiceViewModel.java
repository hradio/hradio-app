package lmu.hradio.hradioshowcase.model.view;

import android.content.Context;

import androidx.annotation.UiThread;

import org.omri.radio.Radio;
import org.omri.radio.impl.RadioImpl;
import org.omri.radioservice.RadioServiceDabEdi;
import org.omri.radio.impl.RadioServiceImpl;
import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceDab;
import org.omri.radioservice.RadioServiceListener;
import org.omri.radioservice.RadioServiceType;
import org.omri.radioservice.metadata.Group;
import org.omri.radioservice.metadata.Location;
import org.omri.radioservice.metadata.TermId;
import org.omri.radioservice.metadata.Visual;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.hradio.httprequestwrapper.dtos.service_search.StandaloneService;
import lmu.hradio.hradioshowcase.database.Database;
import lmu.hradio.hradioshowcase.util.ImageDataHelper;

public class RadioServiceViewModel implements Serializable, RadioService {

    private static final long serialVersionUID = 6457005710300369578L;
    private String serviceLabel;
    private String source;
    private String hash;
    private ImageData image;
    private String webAppUrl;
    private List<RadioService> services = new ArrayList<>();
    private int serviceID;
    private int ensembleECC;

    private transient OnImageDownloadedListener listener;

    public RadioServiceViewModel(RadioService service) {
        this.serviceLabel = service.getServiceLabel();
        this.image = ImageDataHelper.fromVisuals(service.getLogos());
        if(service instanceof RadioServiceImpl){
            source = ((RadioServiceImpl)service).getHradioSearchSource();
        }
        this.addService(service);
    }

    public RadioServiceViewModel(StandaloneService service) {
        serviceLabel = service.getName();
        ImageDataHelper.fromMediaDescription(service.getMediaDescriptions(), images -> {
            image = ImageDataHelper.findBiggest(images);
            fireListener();
        });
        this.hash = service.getHash();
    }

    public RadioServiceViewModel(String serviceLabel,  ImageData data) {
       this.serviceLabel = serviceLabel;
       this.image = data;
    }

    public RadioServiceViewModel(String serviceLabel, int serviceId, int ensembleECC, ImageData data,Context context ) {
        this.serviceLabel = serviceLabel;
        this.serviceID = serviceId;
        this.ensembleECC = ensembleECC;
        this.image = data;
        loadFollowingServices(context);
    }

    public RadioServiceViewModel(String serviceLabel, int serviceId, int ensembleECC, Context context ) {
        this.serviceLabel = serviceLabel;
        this.serviceID = serviceId;
        this.ensembleECC = ensembleECC;
        loadFollowingServices(context);
    }

    @UiThread
    private void fireListener(){
        if (listener != null) {
            listener.onDownloaded();
            listener = null;
        }
    }

    public void setImage(ImageData data){
        this.image = data;
        fireListener();
    }


    @Override
    public String getServiceLabel() {
        return serviceLabel;
    }

    @Override
    public RadioServiceType getRadioServiceType() {
        RadioServiceType preferedType = RadioServiceType.RADIOSERVICE_TYPE_UNKNOWN;
        for(RadioService service: services){
            if(service.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_DAB)
                return service.getRadioServiceType();
            if(service.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI)
                preferedType = service.getRadioServiceType();
            else if(preferedType == null)
                preferedType = service.getRadioServiceType();
        }
        return preferedType;
    }

    @Override
    public String getShortDescription() {
        if(!services.isEmpty())
            return services.get(0).getShortDescription();
        return null;
    }

    @Override
    public String getLongDescription() {
        if(!services.isEmpty())
            return services.get(0).getLongDescription();
        return null;
    }

    @Override
    public List<Visual> getLogos() {
        if(!services.isEmpty())
            return services.get(0).getLogos();
        return null;
    }

    @Override
    public List<TermId> getGenres() {
        if(!services.isEmpty())
            return services.get(0).getGenres();
        return null;
    }

    @Override
    public List<String> getLinks() {
        if(!services.isEmpty())
            return services.get(0).getLinks();
        return null;
    }

    @Override
    public List<Location> getLocations() {
        if(!services.isEmpty())
            return services.get(0).getLocations();
        return null;
    }

    @Override
    public List<String> getKeywords() {
        if(!services.isEmpty())
            return services.get(0).getKeywords();
        return null;
    }

    public ImageData getLogo(){
        return image;
    }

    public OnImageDownloadedListener getListener() {
        return listener;
    }

    public void setListener(OnImageDownloadedListener listener) {
        this.listener = listener;
    }

    @Override
    public List<Group> getMemberships() {
        if(!services.isEmpty())
            return services.get(0).getMemberships();
        return null;
    }

    @Override
    public void subscribe(RadioServiceListener radioServiceListener) {

    }

    @Override
    public void unsubscribe(RadioServiceListener radioServiceListener) {

    }

    @Override
    public boolean equalsRadioService(RadioService radioService) {
        for(RadioService service : services){
            if(service.equalsRadioService(radioService))
                return true;
        }

        if(radioService.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_IP){
            return radioService.getServiceLabel().equals(serviceLabel);
        } else if(radioService.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_DAB || radioService.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI){
            RadioServiceDab other = (RadioServiceDab) radioService;
            return other.getServiceId() == serviceID && other.getEnsembleEcc() == ensembleECC || other.getServiceLabel().equals(serviceLabel);
        }
        return false;
    }


    public void addService(RadioService service) {
        if(service != null){
            services.add(service);
            if(service.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_DAB){
                ensembleECC = ((RadioServiceDab) service).getEnsembleEcc();
                serviceID = ((RadioServiceDab) service).getServiceId();
            } else if(service.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI){
                ensembleECC = ((RadioServiceDabEdi) service).getEnsembleEcc();
                serviceID = ((RadioServiceDabEdi) service).getServiceId();
            }

            if(this.image == null && service.getLogos() != null && !service.getLogos().isEmpty()){
                this.image = ImageDataHelper.fromVisuals(service.getLogos());
            }

        }
    }

    public String getWebAppUrl() {
        return webAppUrl;
    }

    public void setWebAppUrl(String webAppUrl) {
        this.webAppUrl = webAppUrl;
    }

    public List<RadioService> getRadioServices() {
        return services;
    }

    public void addRadioServices(List<RadioService> services) {
        for(RadioService service : services){
            addService(service);
        }
    }

    public String getSource() {
        return source;
    }

    public String getHash() {
        return hash;
    }

    @FunctionalInterface
    public interface OnImageDownloadedListener{
        void onDownloaded();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RadioServiceViewModel that = (RadioServiceViewModel) o;
        return serviceLabel.equals(that.serviceLabel);
    }

    @Override
    public int hashCode() {
        if(serviceLabel != null)
            return serviceLabel.hashCode();
        return 0;
    }


    private void loadFollowingServices(Context context){
        for(RadioService service : Radio.getInstance().getRadioServices()){
            if(this.equalsRadioService(service)) {
                boolean updateImage = image == null || image.getImageData() == null;
                addService(service);
                addRadioServices(((RadioImpl)Radio.getInstance()).getFollowingServices(service));
                if(updateImage)
                    Database.getInstance().contains(this, b -> {
                        if(b)
                            Database.getInstance().updateImage(this, context);
                    });

                return;
            }
        }
    }

    public int getServiceID() {
        return serviceID;
    }

    public int getEnsembleECC() {
        return ensembleECC;
    }
}
