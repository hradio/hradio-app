package lmu.hradio.hradioshowcase.manager.substitution;

import android.app.Activity;

import eu.hradio.substitutionapi.Substitution;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;

public interface SubstitutionProvider {

    boolean isInitialized();
    boolean isAuthenticated();

    void initialize(Activity activity);

    void refreshState(Activity activity, ConnectionCallback connectionCallback);

    void disconnect(Activity activity);


    void seekTo(long progress);

    boolean isPlaying();

    void substitute();

    void startSubstitution(Substitution substitution);


    void pauseSubstitution();

    void queryForSubstitution(String artist, String title);

    void resumeSubstitution();

    void stopSubstitution();

    Substitution getCurrentSubstitution();

    SubstitutionPlayerTyp getType();

    TrackLikeService getLikeService();

    void removeSubstitutionPlayList();

    void setSubstitutionPlayList(String playList);

    void resetSubstitutionBuffer();

    void registerPlayBackListener(PlayBackListener playBackListener);


    @FunctionalInterface
    interface ConnectionCallback{
        void onConnect();
    }

}
