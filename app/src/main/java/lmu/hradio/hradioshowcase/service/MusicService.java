package lmu.hradio.hradioshowcase.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.omri.radioservice.RadioService;

import eu.hradio.core.audiosinkinterface.AudioSinkBinder;
import eu.hradio.core.audiotrackservice.AudiotrackService;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.events.AppEvents;
import lmu.hradio.hradioshowcase.model.state.PlayBackState;
import lmu.hradio.hradioshowcase.view.activity.MainActivity;

public class MusicService extends AudiotrackService {

    private final static String TAG = "MusicService";

    public static final int OPEN_APP_REQEUEST = 7701;

    private StreamingPlayer streamingPlayer;

    private HRadioAudioSinkBinder binder;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG)Log.d(TAG, "SrvBind onCreate");
        streamingPlayer = new StreamingPlayer(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startRet = super.onStartCommand(intent, flags, startId);

        if(BuildConfig.DEBUG)Log.d(TAG, "SrvBind onStart: " + startRet);
        //return START_NOT_STICKY;
        return startRet;
    }

    public void updateNotification(PlayBackState state) {

        if(state.getRunningService() != null) {
            binder.getNotification().setNotificationTitle(getString(R.string.currently_running,state.getRunningService().getServiceLabel()));
        }

        if (state.getCover() != null)
            binder.getNotification().setLargeIcon(state.getCover().decode());

        if (state.getTextData() !=null)
            binder.getNotification().setNotificationText(state.getTextData().getText());
    }

    @Override
    public AudioSinkBinder onBind(Intent intent) {
        if(BuildConfig.DEBUG)Log.d(TAG, "SrvBind onBind");
        if(binder == null) {
            binder = new HRadioAudioSinkBinder();
        }

        Intent openHradioIntent = new Intent(getApplicationContext(), MainActivity.class);
        openHradioIntent.putExtra("FROM-NOTIFICATION", true);
        PendingIntent pendingContentIntent = PendingIntent.getActivity(getApplicationContext(),OPEN_APP_REQEUEST, openHradioIntent ,PendingIntent.FLAG_CANCEL_CURRENT);
        binder.getNotification().setContentIntent(pendingContentIntent);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_hradio);
        binder.getNotification().setLargeIcon(icon);
        binder.getNotification().setNotificationTitle("HRadio");
        binder.getNotification().setNotificationText(getString(R.string.hradio_notification));
        return binder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        if(BuildConfig.DEBUG)Log.d(TAG, "SrvBind unbind");

        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if(BuildConfig.DEBUG)Log.d(TAG, "SrvBind onTaskRemoved");

        super.onTaskRemoved(rootIntent);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(new Intent(AppEvents.APP_KILLED));
        this.stopForeground(true);

    }

    public StreamingPlayer getStreamingPlayer() {
        return streamingPlayer;
    }

    public void kill(Context context) {
        if(BuildConfig.DEBUG)Log.d(TAG, "SrvBind kill");
        this.stopForeground(true);
    }

    public class HRadioAudioSinkBinder extends AudioTrackBinder {


        protected HRadioAudioSinkBinder() {
        }

        @Override
        public void setService(RadioService service) {
            super.setService(service);
        }

        @Override
        public void setVolume(int volumePercent) {
            super.setVolume(volumePercent);
        }

        @Override
        public int getVolume() {
            return super.getVolume();
        }

        @Override
        public void mute(boolean mute) {
            super.mute(mute);
        }

        @Override
        public boolean isMuted() {
            return super.isMuted();
        }

        @Override
        public void flush() {
            super.flush();
        }

        public MusicService getService() {
            return MusicService.this;
        }
    }

}
