package lmu.hradio.hradioshowcase.spotify.player;

import android.util.Log;

import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import eu.hradio.substitutionapi.Substitution;
import eu.hradio.substitutionapi.SubstitutionPlayer;
import eu.hradio.substitutionapi.SubstitutionPlayerListener;
import eu.hradio.substitutionapi.SubstitutionPlayerState;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.model.substitiution.SpotifySubstitution;

public class SpotifySubstitutionPlayer implements SubstitutionPlayer, Subscription.EventCallback<PlayerState> {

    private static String TAG = SpotifySubstitutionPlayer.class.getSimpleName();
    private SpotifyAppRemote spotifyAppRemote;
    private SubstitutionPlayerState state = SubstitutionPlayerState.STOPPED;
    private Set<SubstitutionPlayerListener> listeners = new HashSet<>();
    private SpotifySubstitution current;
    private Subscription<PlayerState> subscription;
    private Timer progressTimer;
    private long progress;
    private long duration;

    public void setRemote(SpotifyAppRemote remote, SubstitutionPlayerListener substitutionPlayerListener) {
        this.spotifyAppRemote = remote;
        this.registerSubstitutionPlayerListener(substitutionPlayerListener);
    }

    public void substitutionEnded(){
        stop();
        for (SubstitutionPlayerListener listener : listeners) {
            listener.stopped(current);
        }
    }

    private void notifyProgresslisteners(long currentPosition, long duration){
        for (SubstitutionPlayerListener listener : listeners) {
            listener.playProgress(current, currentPosition, duration);
        }
        //DIRTY Workaround to spotify do not fire track end event and sometimes enables track repeat
        if(currentPosition >= duration - 2000 && duration != 0){
            substitutionEnded();
        }
    }

    @Override
    public SubstitutionPlayerState getState() {
        return state;
    }

    @Override
    public void play(Substitution substitution) {

            if (substitution instanceof SpotifySubstitution) {
                if (subscription != null)
                    subscription.cancel();
                duration = substitution.getDuration();
                subscription = spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(this);
                spotifyAppRemote.getPlayerApi().setRepeat(0);

                progress = 0;
                current = (SpotifySubstitution) substitution;
                state = SubstitutionPlayerState.TRANSITIONING;
                spotifyAppRemote.getPlayerApi().play(((SpotifySubstitution) substitution).getUri());
            } else {
                throw new IllegalArgumentException("SpotifySubstitutionPlayer needs SpotifySubstitutions");
            }
    }

    private void scheduleTimer(){
        if(progressTimer != null)
            progressTimer.cancel();
        progressTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                progress += 1000;
                notifyProgresslisteners(progress, duration);
            }
        };
        progressTimer.scheduleAtFixedRate(task,  1000, 1000);
    }

    @Override
    public void pause() {
        state = SubstitutionPlayerState.PAUSED;

        if(spotifyAppRemote != null) {
            if(progressTimer != null)
                progressTimer.cancel();
            spotifyAppRemote.getPlayerApi().pause();
        }
    }


    public void resume() {
        state = SubstitutionPlayerState.STARTED;

        if(spotifyAppRemote != null) {
            scheduleTimer();
            spotifyAppRemote.getPlayerApi().resume();
        }
    }

    public void seekTo(long progress) {

        if(spotifyAppRemote != null) {
            if( progress >= this.duration - 3000){
                substitutionEnded();
                return;
            }
            spotifyAppRemote.getPlayerApi().seekTo(progress);
        }
    }

    @Override
    public void stop() {
        state = SubstitutionPlayerState.STOPPED;
        if(progressTimer != null)
            progressTimer.cancel();
        if(spotifyAppRemote != null)
            spotifyAppRemote.getPlayerApi().pause();
        if(subscription != null && !subscription.isCanceled())
            subscription.cancel();
    }

    @Override
    public void registerSubstitutionPlayerListener(SubstitutionPlayerListener substitutionPlayerListener) {
        listeners.add(substitutionPlayerListener);
    }

    @Override
    public void unregisterSubstitutionPlayerListener(SubstitutionPlayerListener substitutionPlayerListener) {
        listeners.remove(substitutionPlayerListener);
    }

    public void disconnect() {
        listeners.clear();
        SpotifyAppRemote.disconnect(spotifyAppRemote);
    }

    public boolean isConnected() {
        return spotifyAppRemote != null && spotifyAppRemote.isConnected();
    }

    public Substitution getSubstitution() {
        return current;
    }

    public void setSubstitution(Substitution substitution) {
        if(substitution instanceof SpotifySubstitution)
            current = (SpotifySubstitution) substitution;

    }

    @Override
    public void onEvent(PlayerState playerState) {
        if(BuildConfig.DEBUG)Log.d(TAG, playerState.track.name +" " + playerState.playbackPosition +" " + playerState.track.duration);
        progress = playerState.playbackPosition;
        //DIRTY Do not work sometimes if spotify enables trakc repeat
        if(playerState.track.uri.equals(current.getUri()) ){
            //since track duration varies sometimes
            if(playerState.track.duration != 0)
                duration = playerState.track.duration;
            if (playerState.isPaused) {
                progressTimer.cancel();
                state = SubstitutionPlayerState.PAUSED;
                for (SubstitutionPlayerListener listener : listeners) {
                    listener.paused(current);
                }

            } else {
                state = SubstitutionPlayerState.STARTED;
                scheduleTimer();
                for (SubstitutionPlayerListener listener : listeners) {
                    listener.started(current);
                }

            }

        } else {
            if(state != SubstitutionPlayerState.TRANSITIONING && state != SubstitutionPlayerState.STOPPED){
                substitutionEnded();

            }

        }
    }
}
