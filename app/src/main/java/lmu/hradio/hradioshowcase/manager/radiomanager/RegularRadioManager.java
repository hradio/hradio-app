package lmu.hradio.hradioshowcase.manager.radiomanager;

import android.content.Context;
import android.util.Log;

import org.omri.radioservice.RadioService;

import java.io.IOException;

import eu.hradio.core.audiotrackservice.AudiotrackService;
import eu.hradio.timeshiftplayer.TimeshiftPlayerFactory;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.service.MusicService;

/**
 * PCM based timeshift manager for IP shoutcast services
 */
public class RegularRadioManager extends TimeshiftManager{

    @Override
    public void started() {
        super.started();

    }

    @Override
    public void paused() {
        super.paused();

    }

    @Override
    public void stopped() {
        super.stopped();

    }

    public void pause() {
        this.pauseTimeshift();

    }

    public void resume() {
        this.resumeTimeshift();

    }

    public void jumpToLive() {
        super.jumpToLive();

    }

    public void stop() {
        this.stopTimeshift();
    }

    public void seekTimeshiftPlayer(long seekTo) {
        super.seekTimeshiftPlayer(seekTo);

    }

    public void startRegularRadio(RadioService service, MusicService.HRadioAudioSinkBinder binder, Context context) {
        startTimeshift(service, context, binder);
    }

    @Override
    public void startTimeshift(RadioService radioService, Context context, AudiotrackService.AudioTrackBinder binder) {
        if (mTimeshiftPlayer != null) {
            this.clearSkipItems();
            try {
                mTimeshiftPlayer.stop(true);
                mTimeshiftPlayer.removeAudioDataListener(binder.getAudioDataListener());
                mTimeshiftPlayer = null;
            } catch (Exception e) {
                for(PlayBackListener listener : playBackListeners)
                    listener.onError(new GeneralError(GeneralError.TIMESHIFT));
                e.printStackTrace();
            }
        }

        try {
            mTimeshiftPlayer = TimeshiftPlayerFactory.createPcmPlayer(context, radioService);
            if (mTimeshiftPlayer != null) {
                mTimeshiftPlayer.addAudioDataListener(binder.getAudioDataListener());
                mTimeshiftPlayer.addListener(this);
                mTimeshiftPlayer.setPlayWhenReady();
            } else {
                throw new IOException("failed create player");
            }
        } catch (IOException ioE) {
            for(PlayBackListener listener : playBackListeners)
                listener.onError(new GeneralError(GeneralError.TIMESHIFT));
            if(BuildConfig.DEBUG)Log.d("startTimeshift", "Please select a DAB+ Service!");
        }
    }
}
