package lmu.hradio.hradioshowcase.model.view;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import eu.hradio.core.radiodns.radioepg.scope.Scope;

public class ScopeViewModel implements Parcelable {
    private Date startTime;
    private Date endTime;

    protected ScopeViewModel(Parcel in) {
        startTime = new Date(in.readLong());
        endTime = new Date(in.readLong());
    }

    protected ScopeViewModel() {
    }

    public static final Creator<ScopeViewModel> CREATOR = new Creator<ScopeViewModel>() {
        @Override
        public ScopeViewModel createFromParcel(Parcel in) {
            return new ScopeViewModel(in);
        }

        @Override
        public ScopeViewModel[] newArray(int size) {
            return new ScopeViewModel[size];
        }
    };

    public ScopeViewModel(Date startDate, Date endDate) {
        this.startTime = startDate;
        this.endTime = endDate;
    }

    public static ScopeViewModel from(Scope scope) {
        ScopeViewModel vm = new ScopeViewModel();
        vm.startTime = scope.getStartTime().getTimePointCalendar().getTime();
        vm.endTime = scope.getStartTime().getTimePointCalendar().getTime();
        return vm;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(startTime.getTime());
        parcel.writeLong(endTime.getTime());
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}
