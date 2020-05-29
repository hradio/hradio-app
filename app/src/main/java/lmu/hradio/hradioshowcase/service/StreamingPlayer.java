package lmu.hradio.hradioshowcase.service;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;

public class StreamingPlayer implements Player.EventListener, MediaSourceEventListener {

    private SimpleExoPlayer exoPlayer;
    private DataSource.Factory dataSourceFactory;

    private long position;
    private long startTime;
    private Timer progressTimer;
    private SubstitutionItem substitution;

    public StreamingPlayer(Context context) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(context, Context.class.getCanonicalName());
        TrackSelection.Factory mp3Factory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(mp3Factory);
        LoadControl loadControl = new DefaultLoadControl();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(context), trackSelector, loadControl);
        exoPlayer.addListener(this);
    }

    public void play(String mp3Uri, SubstitutionItem substitution) {
        position = 0;
        startTime = System.currentTimeMillis();
        Uri uri = Uri.parse(mp3Uri);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri, mainHandler, this); // Listener defined elsewhere
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        if (substitution != null)
            this.substitution = substitution;
    }

    public void resume() {
        exoPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    public void stop() {
        if (exoPlayer.getPlayWhenReady()) {
            if (progressTimer != null)
                progressTimer.cancel();
            exoPlayer.stop(true);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if(Player.STATE_BUFFERING == playbackState)
            return;

        if (playWhenReady && playbackState == Player.STATE_READY) {
            scheduleTimer();
            if (substitution == null)
                for(PlayBackListener listener : playBackListeners)listener.started();
            else
                for(PlayBackListener listener : playBackListeners)listener.started(substitution);
            return;
        } else if (playbackState == Player.STATE_ENDED) {
            if (substitution == null)
                for(PlayBackListener listener : playBackListeners)listener.stopped();
            else
                for(PlayBackListener listener : playBackListeners)listener.stopped(substitution);
        } else if (!playWhenReady)
            for(PlayBackListener listener : playBackListeners)listener.paused();
        if (progressTimer != null)
            progressTimer.cancel();
    }

    private void scheduleTimer() {
        if (progressTimer != null)
            progressTimer.cancel();
        progressTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                position += 1000;
                notifyProgresslisteners(position);
            }
        };
        progressTimer.scheduleAtFixedRate(task, 1000, 1000);
    }


    private void notifyProgresslisteners(long currentPosition) {
        if (substitution == null)
            for(PlayBackListener listener : playBackListeners)listener.playProgress(currentPosition, System.currentTimeMillis() - startTime);
        else
            for(PlayBackListener listener : playBackListeners)listener.playProgress(substitution, currentPosition, substitution.getDuration());
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        //exoSender.onError(new GeneralError(GeneralError.EXO_ERROR_PLAYBACK));
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        position = exoPlayer.getCurrentPosition();
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {
        position = exoPlayer.getCurrentPosition();
    }


    @Override
    public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {

    }

    @Override
    public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

    }

    @Override
    public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

    }

    @Override
    public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
        //exoSender.onError(new GeneralError(GeneralError.EXO_ERROR_PLAYBACK));
    }

    @Override
    public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {

    }

    public void close() {
        exoPlayer.release();
    }

    public void seekTo(long progress) {
        if(progress > exoPlayer.getBufferedPosition() )
            exoPlayer.seekTo(progress);
        else {
            exoPlayer.seekTo(exoPlayer.getBufferedPosition()-1000);
           // exoSender.onError(new GeneralError(GeneralError.EXO_BUFFER_ERROR));
        }
    }

    private Set<PlayBackListener> playBackListeners = new HashSet<>();
    public void registerPlayBackListener(PlayBackListener playBackListener) {
        playBackListeners.add(playBackListener);
    }
}
