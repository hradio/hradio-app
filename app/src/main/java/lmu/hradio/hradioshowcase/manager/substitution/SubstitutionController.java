package lmu.hradio.hradioshowcase.manager.substitution;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.hradio.httprequestwrapper.dtos.podcast.PodcastItem;
import eu.hradio.httprequestwrapper.service.PodcastServiceImpl;
import eu.hradio.substitutionapi.Substitution;
import eu.hradio.timeshiftplayer.SkipItem;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnManagerErrorListener;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.listener.PodcastSearchResultListener;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.model.substitiution.PodcastServiceSubstitution;
import lmu.hradio.hradioshowcase.model.substitiution.SpotifySubstitution;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.ProgrammeInformationViewModel;
import lmu.hradio.hradioshowcase.model.view.ProgrammeViewModel;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.service.StreamingPlayer;
import lmu.hradio.hradioshowcase.spotify.SpotifyProvider;

import static lmu.hradio.hradioshowcase.manager.substitution.SubstitutionPlayerTyp.SPOTIFY_SUBSTITUTION;

/**
 * Controller class to handle interaction with different substitution provider classes
 */
public class SubstitutionController {
    private Set<PodcastSearchResultListener> podcastSearchResultListeners = new HashSet<>();
    private Set<OnManagerErrorListener> errorListeners = new HashSet<>();

    private Map<SubstitutionPlayerTyp, SubstitutionProvider> substitutionManagers = new HashMap<>();

    private SubstitutionPlayerTyp primarySubstitutionManager;
    private SubstitutionPlayerTyp currentManager;

    private boolean isActive = false;

    public SubstitutionController(Activity context) {
        SpotifyProvider spotifyManager = new SpotifyProvider(context);
        substitutionManagers.put(SPOTIFY_SUBSTITUTION, spotifyManager);
        spotifyManager.registerPlayBackListener(playBackListener);
    }

    /**
     * Get track like service for song favorisation
     * @return
     */
    public TrackLikeService getTrackLikeService() {
        if(primarySubstitutionManager != null) {
            return substitutionManagers.get(primarySubstitutionManager).getLikeService();
        }else {
            return null;
        }
    }

    public void stopSubstitution() {
        if(currentManager != null && isActive){
            isActive = false;

            getManagerByType(currentManager).stopSubstitution();
        }
    }

    public void substitute() {
        if(primarySubstitutionManager != null) {
            substitute(primarySubstitutionManager);
        }else{
            for (OnManagerErrorListener errorListener : errorListeners){
                errorListener.onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));
            }
        }
    }

    public void substitute(Activity context) {
        if(primarySubstitutionManager != null) {
            SubstitutionProvider manager = getManagerByType(primarySubstitutionManager);
            if (manager.isInitialized())
                substitute();
            else
                manager.refreshState(context, this::substitute);
        }else{
            for (OnManagerErrorListener errorListener : errorListeners){
                errorListener.onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));
            }
        }
    }

    private void substitute(SubstitutionPlayerTyp type){
        currentManager = type;
        isActive = true;

        getManagerByType(type).substitute();
    }

    public void substitute(Substitution substitution, Activity context) {
        isActive = true;

        if(substitution instanceof SpotifySubstitution){
            currentManager = SPOTIFY_SUBSTITUTION;
        }else {
            currentManager = SubstitutionPlayerTyp.PODCAST_SUBSTITUTION;
        }
       if(getManagerByType(currentManager).isInitialized()){
           getManagerByType(currentManager).startSubstitution(substitution);
       } else{
           getManagerByType(currentManager).refreshState(context, () -> getManagerByType(currentManager).startSubstitution(substitution));
       }
    }

    public void removeSubstitutionPlayList() {
        if(primarySubstitutionManager != null) {
            getManagerByType(primarySubstitutionManager).removeSubstitutionPlayList();
        }
    }

    public void setSubstitutionPlayList(TrackLikeService.PlayList playList) {
        if(primarySubstitutionManager != null) {
            getManagerByType(primarySubstitutionManager).setSubstitutionPlayList(playList.getId());
        }
    }

    public void resumeSubstitution() {
        if(getManagerByType(currentManager) != null)
            getManagerByType(currentManager).resumeSubstitution();
    }

    public void pauseSubstitution() {
        if(getManagerByType(currentManager) != null)
            getManagerByType(currentManager).pauseSubstitution();
    }

    public void queryForPodcasts(RadioServiceViewModel runningService, List<String> urls, Activity activity){
        for (String url : urls) {
            new PodcastServiceImpl().parsePodcasts(url, podcast -> {
                if (podcast != null) {
                    SubstitutionItem[] res = new SubstitutionItem[podcast.getItems().size()];
                    int i = 0;
                    for (PodcastItem item : podcast.getItems()) {
                        res[i++] = new PodcastServiceSubstitution(item, podcast);
                    }

                    for (PodcastSearchResultListener listener : podcastSearchResultListeners)
                        if (runningService.getLogo() != null)
                            listener.onPodcastsReceived(runningService.getLogo().getImageData(), res);
                        else {
                            Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.outline_radio_white_48dp);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] bitMapData = stream.toByteArray();
                            listener.onPodcastsReceived(bitMapData, res);
                        }
                }
            }, e -> {if(BuildConfig.DEBUG)Log.d(e.getLocalizedMessage(), "no podcasts found");});
        }
    }

    public void queryForPodcasts(RadioServiceViewModel runningService, ProgrammeInformationViewModel programmeInformationViewModel, Activity activity) {
        if (programmeInformationViewModel != null && programmeInformationViewModel.getCurrentRunningProgramme() != null) {
            ProgrammeViewModel podcastOwner = null;

            if (programmeInformationViewModel.getCurrentRunningProgramme().getPodcastUrls() != null && !programmeInformationViewModel.getCurrentRunningProgramme().getPodcastUrls().isEmpty()) {
                podcastOwner = programmeInformationViewModel.getCurrentRunningProgramme();
            } else {
                for (ProgrammeViewModel p : programmeInformationViewModel.getProgrammes()) {
                    if (p != null && p.getPodcastUrls() != null && !p.getPodcastUrls().isEmpty() ) {
                        podcastOwner = p;
                        break;
                    }
                }
            }
            if (podcastOwner != null) {
                    queryForPodcasts(runningService, podcastOwner.getPodcastUrls(), activity);
                return;
            }
        }
        podcastFallbackToSpotifyManager(runningService,programmeInformationViewModel, activity);

    }

    private void podcastFallbackToSpotifyManager(RadioServiceViewModel runningService, ProgrammeInformationViewModel programmeInformation, Activity context){
        if(primarySubstitutionManager != null && isEnabled(context) && getManagerByType(primarySubstitutionManager) != null)
            ((SpotifyProvider)getManagerByType(primarySubstitutionManager)).
                searchPodcasts(runningService, programmeInformation, ((sourceCover, podcastContainer) -> {
                    for(PodcastSearchResultListener listener : podcastSearchResultListeners)
                        listener.onPodcastsReceived(sourceCover,podcastContainer);
                }));
        else
            for (OnManagerErrorListener errorListener : errorListeners){
                errorListener.onError(new GeneralError(GeneralError.SUBSTITUTION_DISABLED));
            }
    }

    public void queryForSubstitution(String artist, String title) {
        if(primarySubstitutionManager != null) {
            getManagerByType(primarySubstitutionManager).queryForSubstitution(artist, title);
        }
    }

    public void resetSubstitutionBuffer() {
        if(primarySubstitutionManager != null) {
            getManagerByType(primarySubstitutionManager).resetSubstitutionBuffer();
        }
    }

    public Substitution getCurrentSubstitution() {
        if(getManagerByType(currentManager) == null)
            return null;

       return getManagerByType(currentManager).getCurrentSubstitution();
    }

    public void seekTo(long progress) {
        getManagerByType(currentManager).seekTo(progress);
    }

    public SubstitutionProvider getManagerByType(SubstitutionPlayerTyp type) {
       return substitutionManagers.get(type);
    }

    public void unregisterPodcastSearchListener(PodcastSearchResultListener podcastSearchResultListener) {
        podcastSearchResultListeners.remove(podcastSearchResultListener);
    }

    public void registerPodcastSearchListener(PodcastSearchResultListener podcastSearchResultListener) {
        podcastSearchResultListeners.add(podcastSearchResultListener);
    }


    public void unregisterErrorListener(OnManagerErrorListener listener) {
        errorListeners.remove(listener);
    }

    public void registerErrorListener(OnManagerErrorListener listener) {
        errorListeners.add(listener);
    }
    public void refreshConnection(Activity context, SubstitutionProvider.ConnectionCallback o) {
        if(primarySubstitutionManager != null) {
            getManagerByType(primarySubstitutionManager).refreshState(context, o);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isEnabled(Activity context) {
        return primarySubstitutionManager != null;
    }


    public void setPrimarySubstitutionProvider(SubstitutionProviderType provider, Context context) {
        switch (provider){
            case Spotify: primarySubstitutionManager = SPOTIFY_SUBSTITUTION; break;
            case None: primarySubstitutionManager = null; break;
            default: break;
        }
    }


    public void setStreamingPlayer(StreamingPlayer streamingPlayer) {
        PodcastSubstitutionProvider podcastSubstitutionManager = new PodcastSubstitutionProvider(streamingPlayer);
        substitutionManagers.put(SubstitutionPlayerTyp.PODCAST_SUBSTITUTION, podcastSubstitutionManager);
        podcastSubstitutionManager.registerPlayBackListener(playBackListener);
    }

    private Set<PlayBackListener> playBackListeners = new HashSet<>();

    public void unregisterPlayBackListener(PlayBackListener playBackListener) {
        this.playBackListeners.remove(playBackListener);
    }

    public void registerPlayBackListener(PlayBackListener playBackListener) {
        this.playBackListeners.add(playBackListener);
    }


    private PlayBackListener playBackListener = new PlayBackListener() {
        @Override
        public void onError(@NonNull GeneralError error) {
            for(PlayBackListener listener : playBackListeners)
                listener.onError(error);
        }

        @Override
        public void started() {
            for(PlayBackListener listener : playBackListeners){
                listener.started();
            }
        }

        @Override
        public void stopped() {
            for(PlayBackListener listener : playBackListeners){
                listener.stopped();
            }
        }

        @Override
        public void paused() {
            for(PlayBackListener listener : playBackListeners){
                listener.paused();
            }
        }

        @Override
        public void playProgress(long current, long total) {
            for(PlayBackListener listener : playBackListeners){
                listener.playProgress(current, total);
            }
        }

        @Override
        public void playProgressRealtime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration) {
            for(PlayBackListener listener : playBackListeners){
                listener.playProgressRealtime(realTimePosix, streamTimePosix, curPos, totalDuration);
            }
        }

	    @Override
	    public void sbtSeeked() {
		    
	    }

	    @Override
        public void skipItemRemoved(SkipItem skipItem) {
            for(PlayBackListener listener : playBackListeners){
                listener.skipItemRemoved(skipItem);
            }
        }

        @Override
        public void textualContent(TextData content) {
            for(PlayBackListener listener : playBackListeners){
                listener.textualContent(content);
            }
        }

        @Override
        public void visualContent(ImageData visual) {
            for(PlayBackListener listener : playBackListeners){
                listener.visualContent(visual);
            }
        }

        @Override
        public void started(SubstitutionItem substitution) {
            for(PlayBackListener listener : playBackListeners){
                listener.started(substitution);
            }
        }

        @Override
        public void stopped(SubstitutionItem substitution) {
            for(PlayBackListener listener : playBackListeners){
                listener.stopped(substitution);
            }
        }

        @Override
        public void playProgress(SubstitutionItem substitution, long current, long total) {
            for(PlayBackListener listener : playBackListeners){
                listener.playProgress(substitution, current, total);
            }
        }

        @Override
        public void skipItemAdded(SkipItem skipItem) {
            for(PlayBackListener listener : playBackListeners){
                listener.skipItemAdded(skipItem);
            }
        }

        @Override
        public void itemStarted(SkipItem skipItem) {
            for(PlayBackListener listener : playBackListeners){
                listener.itemStarted(skipItem);
            }
        }
    };

}
