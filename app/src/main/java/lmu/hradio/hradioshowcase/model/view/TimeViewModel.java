package lmu.hradio.hradioshowcase.model.view;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import eu.hradio.core.radiodns.radioepg.programmeinformation.Time;

public class TimeViewModel implements Parcelable {

    private Date startDate;
    private Date endDate;

    protected TimeViewModel(Parcel in) {
        startDate = new Date(in.readLong());
        endDate = new Date(in.readLong());
    }

    protected TimeViewModel() {
    }

    public static final Creator<TimeViewModel> CREATOR = new Creator<TimeViewModel>() {
        @Override
        public TimeViewModel createFromParcel(Parcel in) {
            return new TimeViewModel(in);
        }

        @Override
        public TimeViewModel[] newArray(int size) {
            return new TimeViewModel[size];
        }
    };

    public TimeViewModel(Date startTime, Date stopTime) {
        this.startDate = startTime;
        this.endDate = stopTime;
    }

    public static TimeViewModel from(Time t) {
        TimeViewModel timeViewModel = new TimeViewModel();
        timeViewModel.startDate = t.getStartTime().getTimePointCalendar().getTime();
        timeViewModel.endDate = t.getEndTime().getTimePointCalendar().getTime();
        return timeViewModel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(startDate.getTime());
        parcel.writeLong(endDate.getTime());
    }

    public Date getStartTime() {
        return startDate;
    }

    public Date getEndTime() {
        return endDate;
    }
}
