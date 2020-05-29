package lmu.hradio.hradioshowcase.model.view;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.hradio.core.radiodns.RadioEpgProgrammeInformation;
import eu.hradio.core.radiodns.radioepg.programmeinformation.Schedule;
import eu.hradio.httprequestwrapper.dtos.programme.ProgrammeList;
import lmu.hradio.hradioshowcase.util.DateUtils;

public class ProgrammeInformationViewModel implements Parcelable, Serializable {

    private static final long serialVersionUID = 5877362677047638331L;
    private List<ScheduleViewModel> schedules;
    private ProgrammeViewModel currentRunning;

    protected ProgrammeInformationViewModel(Parcel in) {
        schedules = new ArrayList<>();
        in.readTypedList(schedules, ScheduleViewModel.CREATOR);
        currentRunning = in.readParcelable(ProgrammeViewModel.class.getClassLoader());
    }

    public static ProgrammeInformationViewModel from(RadioEpgProgrammeInformation currentEGP) {
        if(currentEGP == null) return null;
        ProgrammeInformationViewModel programmInformation = new ProgrammeInformationViewModel();
        programmInformation.currentRunning = ProgrammeViewModel.from(currentEGP.getSchedules().get(0).getCurrentRunningProgramme());
        programmInformation.schedules = new ArrayList<>();
        for(Schedule schedule: currentEGP.getSchedules()){
            programmInformation.schedules.add(ScheduleViewModel.from(schedule));
        }
        return programmInformation;
    }

    private ProgrammeInformationViewModel(){

    }

    public static ProgrammeInformationViewModel from(ProgrammeList programmeList) {
        if(programmeList == null) return null;
        ScheduleViewModel schedule = new ScheduleViewModel(programmeList.getContent());
        ProgrammeViewModel current = null;
        for(ProgrammeViewModel prog: schedule.getProgrammes()){
            if(prog.getLocations() != null && !prog.getLocations().isEmpty() && prog.getLocations().get(0).getTimes() != null && !prog.getLocations().get(0).getTimes().isEmpty()){
                Date start = prog.getLocations().get(0).getTimes().get(0).getStartTime();
                Date end = prog.getLocations().get(0).getTimes().get(0).getEndTime();
                if(DateUtils.isNowBeetween(start, end)){
                    current = prog;
                    break;
                }
            }
        }
        ProgrammeInformationViewModel info = new ProgrammeInformationViewModel();
        info.currentRunning = current;
        info.schedules = new ArrayList<>();
        info.schedules.add(schedule);
        return info;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(schedules);
        dest.writeParcelable(currentRunning, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProgrammeInformationViewModel> CREATOR = new Creator<ProgrammeInformationViewModel>() {
        @Override
        public ProgrammeInformationViewModel createFromParcel(Parcel in) {
            return new ProgrammeInformationViewModel(in);
        }

        @Override
        public ProgrammeInformationViewModel[] newArray(int size) {
            return new ProgrammeInformationViewModel[size];
        }
    };

    public List<ScheduleViewModel> getSchedules() {
        return schedules;
    }

    public List<ProgrammeViewModel> getProgrammes() {
        return schedules.get(0).getProgrammes();
    }

    public ProgrammeViewModel getCurrentRunningProgramme() {
        return currentRunning;
    }
}
