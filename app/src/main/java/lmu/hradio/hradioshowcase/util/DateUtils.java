package lmu.hradio.hradioshowcase.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import eu.hradio.httprequestwrapper.util.TimeUtils;

public final class DateUtils {

    private static Date mDate = null;
    public static void setCurDate(Date curDate) {
        mDate = curDate;
    }

    public static boolean isNowBeetween(Date start, Date end){
        Date current;
        if(mDate == null) {
            current = Calendar.getInstance().getTime();
        } else {
            current = mDate;
        }

        return current.after(start) && current.before(end);
    }

    public static Date getDateHoursFromNow(int hours) {
        Calendar calendar = Calendar.getInstance();
        if(mDate != null) {
            calendar.setTime(mDate);
        }
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    public static long getDistance(Date date) {
        Calendar calendar = Calendar.getInstance();
        if(mDate != null) {
            calendar.setTime(mDate);
        }

        return Math.abs(calendar.getTime().getTime() - date.getTime());
    }

    public static String format(Date date){
       return TimeUtils.dateToString(date);
    }

    public static String getAbsolut(long curPos, long totalDuration) {
        Calendar calendar = Calendar.getInstance();
        //if(mDate != null) {
        //    calendar.setTime(mDate);
        //}
        long distance =  curPos -totalDuration + calendar.getTimeInMillis();
        calendar.setTimeInMillis(distance);
        DateFormat format = new SimpleDateFormat("kk:mm:ss", Locale.getDefault());
        return format.format(calendar.getTime());
    }

    public static String formatMinAndSeconds(long totalInMS) {
        DateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
        return format.format(totalInMS);
    }
}
