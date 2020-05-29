package lmu.hradio.hradioshowcase.model.view;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import eu.hradio.core.radiodns.radioepg.programmeinformation.Programme;
import eu.hradio.core.radiodns.radioepg.programmeinformation.Schedule;
import eu.hradio.httprequestwrapper.dtos.programme.RankedStandaloneProgramme;

public class ScheduleViewModel implements Parcelable {

    private List<ProgrammeViewModel> programmeList;
    private ScopeViewModel scope;

    public ScheduleViewModel(RankedStandaloneProgramme[] content) {
        programmeList = new ArrayList<>();
        for(RankedStandaloneProgramme programme: content){
            programmeList.add(ProgrammeViewModel.from(programme.getProgramme()));
        }

        Collections.sort(programmeList, (p1, p2) -> p1.getLocations().get(0).getTimes().get(0).getStartTime().compareTo(p2.getLocations().get(0).getTimes().get(0).getStartTime()));

        int i = content.length;
        if(i>0){
            Date startDate = content[0].getProgramme().getStartTime();
            Date endDate = content[i-1].getProgramme().getStopTime();
            scope = new ScopeViewModel(startDate, endDate);
        }
    }

    public static ScheduleViewModel from(Schedule schedule) {
        ScheduleViewModel scheduleVM = new ScheduleViewModel();
        scheduleVM.programmeList = new ArrayList<>();
        for(Programme p: schedule.getProgrammes()){
            scheduleVM.programmeList.add(ProgrammeViewModel.from(p));
        }
        scheduleVM.scope = ScopeViewModel.from(schedule.getScope());
        return scheduleVM;
    }

    public  ScheduleViewModel(Parcel in){
        programmeList = new ArrayList<>();
        in.readTypedList(programmeList, ProgrammeViewModel.CREATOR);
        scope = in.readParcelable(ProgrammeViewModel.class.getClassLoader());
    }

    public  ScheduleViewModel(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(programmeList);
        parcel.writeParcelable(scope, i);
    }

    public static final Creator<ScheduleViewModel> CREATOR = new Creator<ScheduleViewModel>() {
        @Override
        public ScheduleViewModel createFromParcel(Parcel in) {
            return new ScheduleViewModel(in);
        }

        @Override
        public ScheduleViewModel[] newArray(int size) {
            return new ScheduleViewModel[size];
        }
    };

    public List<ProgrammeViewModel> getProgrammes() {
        return programmeList;
    }

    public ScopeViewModel getScope() {
        return scope;
    }
}
