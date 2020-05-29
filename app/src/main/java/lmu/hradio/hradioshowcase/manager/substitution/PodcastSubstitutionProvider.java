package lmu.hradio.hradioshowcase.manager.substitution;

import android.app.Activity;

import eu.hradio.substitutionapi.Substitution;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.model.substitiution.PodcastServiceSubstitution;
import lmu.hradio.hradioshowcase.service.StreamingPlayer;

/**
 * substitution Provider for url podcast streaming
 */
public class PodcastSubstitutionProvider implements SubstitutionProvider {

    private StreamingPlayer player;
    private PodcastServiceSubstitution current;
    private boolean isPlaying = false;

    public PodcastSubstitutionProvider(StreamingPlayer player){
        this.player = player;
    }

    @Override
    public boolean isInitialized() {
        return player != null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void initialize(Activity context) {
        player = new StreamingPlayer(context);

    }

    @Override
    public void refreshState(Activity activity, ConnectionCallback connectionCallback) {
        connectionCallback.onConnect();
    }

    @Override
    public void disconnect(Activity activity) {
        player.close();
    }

    @Override
    public void seekTo(long progress) {
        player.seekTo(progress);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }


    @Override
    public void substitute() {
        isPlaying = true;
        player.play(current.getUri(), current);
    }

    @Override
    public void startSubstitution(Substitution substitution) {
        current = (PodcastServiceSubstitution) substitution;
        substitute();
    }

    @Override
    public void pauseSubstitution() {
        player.pause();
    }

    @Override
    public void queryForSubstitution(String artist, String title) { }

    @Override
    public void resumeSubstitution() {
        player.resume();
    }

    @Override
    public void stopSubstitution() {
        player.stop();
    }

    @Override
    public Substitution getCurrentSubstitution() {
        return current;
    }

    @Override
    public SubstitutionPlayerTyp getType() {
        return SubstitutionPlayerTyp.PODCAST_SUBSTITUTION;
    }

    @Override
    public TrackLikeService getLikeService() {
        return null;
    }

    @Override
    public void removeSubstitutionPlayList() {

    }

    @Override
    public void setSubstitutionPlayList(String playList) {

    }

    @Override
    public void resetSubstitutionBuffer() {

    }

    @Override
    public void registerPlayBackListener(PlayBackListener playBackListener) {
        player.registerPlayBackListener(playBackListener);
    }


}
