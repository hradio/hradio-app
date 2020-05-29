package lmu.hradio.hradioshowcase.model.view;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import eu.hradio.core.radiodns.radioepg.description.Description;
import eu.hradio.core.radiodns.radioepg.description.DescriptionType;
import eu.hradio.core.radiodns.radioepg.link.Link;
import eu.hradio.core.radiodns.radioepg.mediadescription.MediaDescription;
import eu.hradio.core.radiodns.radioepg.name.Name;
import eu.hradio.core.radiodns.radioepg.name.NameType;
import eu.hradio.core.radiodns.radioepg.programmeinformation.Location;
import eu.hradio.core.radiodns.radioepg.programmeinformation.Programme;
import eu.hradio.httprequestwrapper.dtos.programme.StandaloneProgramme;
import eu.hradio.httprequestwrapper.dtos.programme.WebContent;

public class ProgrammeViewModel implements Parcelable {

    private String name;
    private String description;
    private List<LocationViewModel> locations;
    private List<String> podcastUrls = new ArrayList<>();

    protected ProgrammeViewModel(Parcel in) {
        name = in.readString();
        description = in.readString();
        locations = new ArrayList<>();
        in.readTypedList(locations, LocationViewModel.CREATOR);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof ProgrammeViewModel && name != null){
            if(name.equals(((ProgrammeViewModel) o).name)){
                return (description == null && ((ProgrammeViewModel) o).description == null) || (description != null && description.equals(((ProgrammeViewModel) o).description));
            }
        }
        return false;
    }

    private ProgrammeViewModel() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProgrammeViewModel> CREATOR = new Creator<ProgrammeViewModel>() {
        @Override
        public ProgrammeViewModel createFromParcel(Parcel in) {
            return new ProgrammeViewModel(in);
        }

        @Override
        public ProgrammeViewModel[] newArray(int size) {
            return new ProgrammeViewModel[size];
        }
    };

    public List<LocationViewModel> getLocations() {
        return locations;
    }

    public String getQualifiedName() {
        return name;
    }

    public String getQualifiedDescription() {
        return description;
    }

    public static ProgrammeViewModel from(Programme programme) {
        ProgrammeViewModel vm = new ProgrammeViewModel();
        for(Name name : programme.getNames()){
            if(name.getType() == NameType.NAME_LONG){
                vm.name = name.getName();
                break;
            }
            vm.name = name.getName();
        }
        for(Link link : programme.getLinks()){
            if("application/rss+xml".equals(link.getMime())){
                vm.podcastUrls.add(link.getUri());
            }
        }

        for(MediaDescription mediaDescription : programme.getMediaDescriptions()){
            for(Description description : mediaDescription.getDescriptions()) {
                if (description.getType() == DescriptionType.DESCRIPTION_LONG) {
                    vm.description = description.getDescription();
                    break;
                }
                vm.description = description.getDescription();
            }
        }
        vm.locations = new ArrayList<>();
        for (Location location: programme.getLocations()){
            vm.locations.add(LocationViewModel.from(location));
        }
        return vm;
    }

    public static ProgrammeViewModel from(StandaloneProgramme programme) {
        ProgrammeViewModel res = new ProgrammeViewModel();
        if(programme.getLongName() != null && !programme.getLongName().isEmpty()){
            res.name = programme.getLongName();
        }else {
            res.name = programme.getName();
        }
        for(WebContent link : programme.getWebContents()){
            if("application/rss+xml".equals(link.getMimeType())){
                res.podcastUrls.add(link.getUrl());
            }
        }
        res.description = programme.getDescription();
        List<LocationViewModel> locationViewModels = new ArrayList<>();
        locationViewModels.add(new LocationViewModel(programme.getStartTime(), programme.getStopTime()));
        res.locations = locationViewModels;
        return res;
    }

    public List<String> getPodcastUrls() {
        return podcastUrls;
    }

    public void setPodcastUrls(List<String> podcastUrls) {
        this.podcastUrls = podcastUrls;
    }
}
