package lmu.hradio.hradioshowcase.manager.radiomanager;

import android.content.Context;
import android.util.Log;
import org.omri.radioservice.RadioService;
import org.omri.radioservice.metadata.Textual;
import org.omri.radioservice.metadata.Visual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import eu.hradio.core.audiotrackservice.AudiotrackService;
import eu.hradio.timeshiftplayer.SkipItem;
import eu.hradio.timeshiftplayer.TimeshiftListener;
import eu.hradio.timeshiftplayer.TimeshiftPlayer;
import eu.hradio.timeshiftplayer.TimeshiftPlayerFactory;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.util.DateUtils;
import lmu.hradio.hradioshowcase.util.ImageDataHelper;

/**
 * timshift player interface
 */
public class TimeshiftManager implements TimeshiftListener {

    protected final static String TAG = TimeshiftManager.class.getSimpleName();

    /**
     * The timesshift player instance
     */
    protected TimeshiftPlayer mTimeshiftPlayer = null;
    /**
     * current instances state
     */
    protected State state = State.STOPPED;
    /**
     * skip item tracker to find position of timepoint
     */
    protected NavigableMap<Long, SkipItem> skipItemTracker = new TreeMap<>();
    /**
     * total duration
     */
    protected long mTotalTimeshiftDuration;
    /**
     * current duration
     */
    protected long currentDuration;
    /**
     * current skip item position
     */
    protected int currentPosition = -1;


    @Override
    public void progress(long curPos, long totalDuration) {

            for (PlayBackListener listener : playBackListeners) {
                listener.playProgress(curPos, totalDuration);
            }
            mTotalTimeshiftDuration = totalDuration * 1000;


        //check if new skip item started
        int actualCurrent = getCurrentSkipItemPosition();
        if (curPos * 1000 != currentDuration) {
            currentDuration = curPos * 1000;
            if (actualCurrent == -1)
                currentPosition = actualCurrent;
            if (currentPosition != actualCurrent) {
                currentPosition = actualCurrent;
                if (getCurrentSkipItem() != null) {
                    for (PlayBackListener playBackListener : playBackListeners) {
                        playBackListener.itemStarted(getCurrentSkipItem());
                    }
                }
            }
        }
    }

    @Override
    public void sbtRealTime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration) {
        DateUtils.setCurDate(new Date(streamTimePosix));

        for (PlayBackListener listener : playBackListeners) {
            listener.playProgressRealtime(realTimePosix, streamTimePosix, curPos, totalDuration);
        }

        mTotalTimeshiftDuration = totalDuration * 1000;

        //check if new skip item started
        int actualCurrent = getCurrentSkipItemPosition();
        if (streamTimePosix != currentDuration) {
            currentDuration = streamTimePosix;
            if (actualCurrent == -1) {
                currentPosition = actualCurrent;
            }

            if (currentPosition != actualCurrent) {
                currentPosition = actualCurrent;
                if (getCurrentSkipItem() != null) {
                    for (PlayBackListener playBackListener : playBackListeners) {
                        playBackListener.itemStarted(getCurrentSkipItem());
                    }
                }
            }
        }
    }

    @Override
    public void started() {
        if(BuildConfig.DEBUG)Log.d(TAG, "started");
        if (state != State.STARTED) {
            state = State.STARTED;
            for (PlayBackListener listener : playBackListeners) {
                listener.started();
            }
        }
    }

    @Override
    public void paused() {
        if(BuildConfig.DEBUG)Log.d(TAG, "paused");
        state = State.PAUSED;
        for (PlayBackListener listener : playBackListeners) {
            listener.paused();
        }
    }

    @Override
    public void stopped() {
        if(BuildConfig.DEBUG)Log.d(TAG, "stopped");
        state = State.STOPPED;
        clearSkipItems();
        for (PlayBackListener listener : playBackListeners) {
            listener.stopped();
        }
    }

    private Textual lastTextual;

    @Override
    public void textual(Textual textual) {
        if (textual == null || (lastTextual != null && textual.getText().equals(lastTextual.getText())))
            return;

        lastTextual = textual;
        for (PlayBackListener listener : playBackListeners) {
            listener.textualContent(TextData.fromTextual(textual));
        }
    }

    private Visual lastVisual;

    @Override
    public void visual(Visual visual) {
        if (visual == null || (lastVisual != null && Arrays.equals(visual.getVisualData(), lastVisual.getVisualData())))
            return;
        lastVisual = visual;
        for (PlayBackListener listener : playBackListeners) {
            listener.visualContent(ImageDataHelper.fromVisual(visual));
        }
    }

    @Override
    public void skipItemAdded(SkipItem skipItem) {
        if(BuildConfig.DEBUG)Log.d(TAG, "Adding SkipItem: " + (skipItem.getSkipTextual() != null ? skipItem.getSkipTextual().getText() : "textual is null"));
        if(skipItem.getSbtRealTime() == 0) {
            skipItemTracker.put(skipItem.getRelativeTimepoint(), skipItem);
        } else {
            skipItemTracker.put(skipItem.getSbtRealTime(), skipItem);
        }

        for (PlayBackListener listener : playBackListeners) {
            listener.skipItemAdded(skipItem);
        }

    }

    @Override
    public void skipItemRemoved(SkipItem skipItem) {
        if(BuildConfig.DEBUG)Log.d(TAG, "skipItemRemoved: " + skipItem.getSbtRealTime());

        for (PlayBackListener listener : playBackListeners) {
            listener.skipItemRemoved(skipItem);
        }
    }

    private SkipItem mCurrentItem = null;
    public int getNextIndexForCurrentPosition() {
        if(BuildConfig.DEBUG)Log.d(TAG, "getNextIndexForCurrentPosition");

        Long key = skipItemTracker.higherKey(currentDuration);
        return (key == null) ? -1 : mTimeshiftPlayer.getSkipItems().indexOf(skipItemTracker.get(key));
    }

    public void skipToItem(final SkipItem item) {
        if(BuildConfig.DEBUG)Log.d(TAG, "skipToItem: " + new Date(item.getSbtRealTime()) + " - " + item.getSkipTextual().getText());

        SkipItem selectedItem = null;
        if(item.getSbtRealTime() == 0) {
            selectedItem = skipItemTracker.get(item.getRelativeTimepoint());
        } else {
            selectedItem = skipItemTracker.get(item.getSbtRealTime());
        }

        if(selectedItem != null) {
            mTimeshiftPlayer.skipTo(selectedItem);
            resumeTimeshift();

            for (PlayBackListener listener : playBackListeners) {
                listener.sbtSeeked();
            }

            textual(item.getSkipTextual());
            if (item.getSkipVisual() != null) {
                visual(item.getSkipVisual());
            }
        }
    }

    public void skipToIndex(int index) {
        if(BuildConfig.DEBUG)Log.d(TAG, "skipToIndex: " + index);
        if (mTimeshiftPlayer.getSkipItems() != null) {
            SkipItem item = getSkipItem(index);
            if (item != null) {

                try {
                    mTimeshiftPlayer.skipTo(item);
                    resumeTimeshift();

                    for (PlayBackListener listener : playBackListeners) {
                        listener.sbtSeeked();
                    }
                } catch (Exception e) {
                    for (PlayBackListener listener : playBackListeners)
                        listener.onError(new GeneralError(GeneralError.TIMESHIFT));
                    e.printStackTrace();
                }
                textual(item.getSkipTextual());
                if (item.getSkipVisual() != null)
                    visual(item.getSkipVisual());
            }
        }
    }

//    endregion

//    region Timeshiftplayer Controls

    public void startTimeshift(RadioService radioService, Context context, AudiotrackService.AudioTrackBinder binder) {
        if (mTimeshiftPlayer != null) {
            this.clearSkipItems();
            try {
                mTimeshiftPlayer.stop(true);
                mTimeshiftPlayer.removeAudioDataListener(binder.getAudioDataListener());
                mTimeshiftPlayer = null;
            } catch (Exception e) {
                for (PlayBackListener listener : playBackListeners)
                    listener.onError(new GeneralError(GeneralError.TIMESHIFT));
                e.printStackTrace();
            }
        }

        try {
            mTimeshiftPlayer = TimeshiftPlayerFactory.create(context, radioService);
            if (mTimeshiftPlayer != null) {
                mTimeshiftPlayer.addAudioDataListener(binder.getAudioDataListener());
                mTimeshiftPlayer.addListener(this);
                mTimeshiftPlayer.setPlayWhenReady();
            } else {
                throw new IOException("failed create player");
            }
        } catch (IOException ioE) {
            for (PlayBackListener listener : playBackListeners)
                listener.onError(new GeneralError(GeneralError.TIMESHIFT));
            if (BuildConfig.DEBUG) Log.d(TAG, "Please select a DAB+ Service!");
        }
    }

    public void pauseTimeshift() {
        if(BuildConfig.DEBUG)Log.d(TAG, "pauseTimeshift");
        if (mTimeshiftPlayer != null) {
            try {
                mTimeshiftPlayer.pause(true);
            } catch (Exception e) {
                for (PlayBackListener listener : playBackListeners)
                    listener.onError(new GeneralError(GeneralError.TIMESHIFT));
                e.printStackTrace();
            }
        }
    }

    public void resumeTimeshift() {
        if (mTimeshiftPlayer != null) {
            try {
                mTimeshiftPlayer.pause(false);
            } catch (Exception e) {
                for (PlayBackListener listener : playBackListeners)
                    listener.onError(new GeneralError(GeneralError.TIMESHIFT));
                e.printStackTrace();
            }
        }
    }


    public void stopTimeshift() {
        clearSkipItems();

        if (mTimeshiftPlayer != null && isPlaying()) {
            try {
                mTimeshiftPlayer.stop(true);
            } catch (Exception e) {
                for (PlayBackListener listener : playBackListeners)
                    listener.onError(new GeneralError(GeneralError.TIMESHIFT));
                e.printStackTrace();
            }
        }
    }

    private int getPreviousIndexForCurrentPosition() {
        Long currentKey = skipItemTracker.lowerKey(currentDuration);
        if (currentKey == null) return -1;
        Map.Entry<Long, SkipItem> lastEntry = skipItemTracker.lowerEntry(currentKey);
        return (lastEntry == null) ? -1 : mTimeshiftPlayer.getSkipItems().indexOf(lastEntry.getValue());
    }

    public void skipBack() {
        int index = getPreviousIndexForCurrentPosition();
        if (index == -1) return;
        skipToIndex(index);
    }

    public void seekTimeshiftPlayer(long seekTo) {
        if(BuildConfig.DEBUG)Log.d(TAG, "SbtSeeked to: " + seekTo);
        if (mTimeshiftPlayer != null) {
            try {
                mTimeshiftPlayer.seek(seekTo);
                resumeTimeshift();

                for (PlayBackListener listener : playBackListeners) {
                    listener.sbtSeeked();
                }
            } catch (Exception e) {
                for (PlayBackListener listener : playBackListeners)
                    listener.onError(new GeneralError(GeneralError.TIMESHIFT));
                e.printStackTrace();
            }
        }
    }

    public long getDuration() {
        return mTotalTimeshiftDuration;
    }

    public void jumpToLive() {
        //STAY 2 SECONDS BEHIND LIVE TO GIVE PLAYER TIME TO BUFFER
        currentDuration = this.mTotalTimeshiftDuration - 2000;
        seekTimeshiftPlayer(currentDuration);
    }


    public SkipItem getCurrentSkipItem() {
        if(BuildConfig.DEBUG)Log.d(TAG, "getCurrentSkipItem");
        Map.Entry<Long, SkipItem> currentEntry = skipItemTracker.floorEntry(currentDuration);
        return (currentEntry == null) ? null : currentEntry.getValue();
    }

    public int getCurrentSkipItemPosition() {
        Map.Entry<Long, SkipItem> currentEntry = skipItemTracker.floorEntry(currentDuration);

        return (currentEntry == null) ? -1 : mTimeshiftPlayer.getSkipItems().indexOf(currentEntry.getValue());
    }

    private SkipItem getSkipItem(int position) {
        if (mTimeshiftPlayer != null && mTimeshiftPlayer.getSkipItems() != null && mTimeshiftPlayer.getSkipItems().size() > position) {
            return mTimeshiftPlayer.getSkipItems().get(position);
        }

        return null;
    }

    public int countSkipItems() {
        if (mTimeshiftPlayer != null && mTimeshiftPlayer.getSkipItems() != null && mTimeshiftPlayer.getSkipItems().size() > 0) {
            return mTimeshiftPlayer.getSkipItems().size();
        } else return 0;
    }

    public int getIndexForSkipItem(SkipItem item) {
        if (mTimeshiftPlayer != null && mTimeshiftPlayer.getSkipItems() != null && mTimeshiftPlayer.getSkipItems().size() > 0) {
            return mTimeshiftPlayer.getSkipItems().indexOf(item);
        }

        return -1;
    }


    public List<SkipItem> getSkipItems() {
        if(BuildConfig.DEBUG)Log.d(TAG, "getSkipItems timeshiftPlayer is " + (mTimeshiftPlayer == null ? "null" : "okay"));
        if (mTimeshiftPlayer == null) {
            return new ArrayList<>();
        }

        if(BuildConfig.DEBUG)Log.d(TAG, "getSkipItems size: " + mTimeshiftPlayer.getSkipItems().size());
        return mTimeshiftPlayer.getSkipItems();
    }

    public boolean isStopped() {
        return state == State.STOPPED;
    }

    public boolean isPlaying() {
        return state != State.STOPPED;
    }


    public void clearSkipItems() {
        if (mTimeshiftPlayer != null && mTimeshiftPlayer.getSkipItems() != null)
            mTimeshiftPlayer.getSkipItems().clear();
        skipItemTracker.clear();
    }

    public TimeshiftPlayer getCurrentTimeShiftPlayer() {
        return mTimeshiftPlayer;
    }

    protected Set<PlayBackListener> playBackListeners = new HashSet<>();

    public void registerPlayBackListener(PlayBackListener playBackListener) {
        playBackListeners.add(playBackListener);
    }


    public void unregisterPlayBackListener(PlayBackListener playBackListener) {
        playBackListeners.remove(playBackListener);
    }


    private enum State {
        PAUSED, STARTED, STOPPED
    }

}
