package lmu.hradio.hradioshowcase.model.view;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.hradio.core.radiodns.radioepg.programmeinformation.Location;
import eu.hradio.core.radiodns.radioepg.programmeinformation.Time;

public class LocationViewModel implements Parcelable {

    private List<TimeViewModel> times;

    protected LocationViewModel(Parcel in) {
        times = new ArrayList<>();
        in.readTypedList(times, TimeViewModel.CREATOR);
    }

    private LocationViewModel() {

    }

    public static final Creator<LocationViewModel> CREATOR = new Creator<LocationViewModel>() {
        @Override
        public LocationViewModel createFromParcel(Parcel in) {
            return new LocationViewModel(in);
        }

        @Override
        public LocationViewModel[] newArray(int size) {
            return new LocationViewModel[size];
        }
    };

    public LocationViewModel(Date startTime, Date stopTime) {
        times = new ArrayList<>();
        times.add(new TimeViewModel(startTime,stopTime));
    }

    public static LocationViewModel from(Location location) {
        List<TimeViewModel> times = new ArrayList<>();
        for(Time t: location.getTimes()){
            times.add(TimeViewModel.from(t));
        }
        LocationViewModel res = new LocationViewModel();
        res.times = times;
        return res;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(times);
    }

    public List<TimeViewModel> getTimes() {
        return times;
    }
}
