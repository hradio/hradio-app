package lmu.hradio.hradioshowcase.listener;

import android.app.Activity;

/**
 * Playback controls delegate
 */
public interface PlayBackDelegate {

    /**
     * play clicked
     */
    void onPlayClicked();

    /**
     * pause clicked
     */
    void onPauseClicked();

    /**
     * skip next clicked
     *
     * @param activity - the sender context
     */
    void onSkipNext(Activity activity);

    /**
     * play clicked
     *
     * @param activity - the sender context
     */
    void onSubstitutePodcast(Activity activity);

    /**
     * skip back clicked
     */
    void onSkipBack();

    /**
     * jump live clicked
     */
    void onJumpToLive();

    /**
     * seek to called
     *
     * @param progress - seek position in ms
     */
    void seekTo(long progress);

    /**
     * register playback listener
     *
     * @param listener - the listener
     */
    void registerPlayBackListener(PlayBackListener listener);


    /**
     * unregister playback listener
     *
     * @param listener - the listener
     */
    void unregisterPlayBackListener(PlayBackListener listener);

    /**
     * get the track like service
     *
     * @return track like service instance
     */
    TrackLikeService getTrackLikeService();

    void shareSbtToken();
}
