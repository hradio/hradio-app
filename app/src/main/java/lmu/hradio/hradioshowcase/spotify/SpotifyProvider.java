package lmu.hradio.hradioshowcase.spotify;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import eu.hradio.substitutionapi.Substitution;
import eu.hradio.substitutionapi.SubstitutionPlayerListener;
import eu.hradio.substitutionapi.SubstitutionPlayerState;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.listener.PodcastSearchResultListener;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionProvider;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionPlayerTyp;
import lmu.hradio.hradioshowcase.model.substitiution.PodcastSpotifySubstitution;
import lmu.hradio.hradioshowcase.model.substitiution.SpotifySubstitution;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.model.substitiution.TrackSpotifySubstitution;
import lmu.hradio.hradioshowcase.model.view.ProgrammeInformationViewModel;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.spotify.player.SpotifySubstitutionPlayer;
import lmu.hradio.hradioshowcase.spotify.web.SpotifyPlayListApiService;
import lmu.hradio.hradioshowcase.spotify.web.SpotifyWebApiTrackService;
import lmu.hradio.hradioshowcase.spotify.web.model.PlayListTrack;
import lmu.hradio.hradioshowcase.spotify.web.model.PodcastEpisodeShort;
import lmu.hradio.hradioshowcase.spotify.web.model.Track;
import lmu.hradio.hradioshowcase.util.PropertieHelper;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class SpotifyProvider implements SubstitutionPlayerListener, SubstitutionProvider {

    private static final String TAG = SpotifyProvider.class.getSimpleName();
    private SpotifySubstitutionPlayer player;
    private SpotifyWebApiTrackService apiService;
    private SpotifyPlayListApiService playListApiService;

    private Queue<TrackSpotifySubstitution> recommendationSubstitutions = new LinkedBlockingDeque<>();
    private Queue<TrackSpotifySubstitution> topPlaylistSubstitutions = new LinkedBlockingDeque<>();
    private Queue<TrackSpotifySubstitution> defaultPlaylistSubstitution = new LinkedBlockingDeque<>();

    private Handler tokenRefreshHandler = new Handler();

    private Runnable tokenRefreshCallback;

    private final String clientID;
    private final String redirectUrl;

    private boolean isAuthenticated = false;
    private BroadcastReceiver receiver;

    public SpotifyProvider(Activity context) {
        player = new SpotifySubstitutionPlayer();
        player.registerSubstitutionPlayerListener(this);
        clientID = PropertieHelper.readClientID(context);
        redirectUrl = PropertieHelper.readRedirectUrl(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("PAUSE_SUBSTITUTION");
        intentFilter.addAction("RESUME_SUBSTITUTION");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("PAUSE_SUBSTITUTION".equals(intent.getAction())) {
                    pauseSubstitution();
                } else if ("RESUME_SUBSTITUTION".equals(intent.getAction())) {
                    resumeSubstitution();
                }
            }
        };
        context.getApplicationContext().registerReceiver(receiver, intentFilter);

    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public boolean isInitialized() {
        return player.isConnected();
    }

    @Override
    public void initialize(Activity context) {
        if(Build.VERSION.SDK_INT > 21) {
            AuthenticationRequest.Builder builder =
                    new AuthenticationRequest.Builder(clientID, AuthenticationResponse.Type.TOKEN, redirectUrl)
                            .setScopes(new String[]{"user-top-read", "playlist-read-private", "user-read-recently-played",
                                    "user-read-currently-playing", "user-modify-playback-state", "user-library-read",
                                    "user-read-private", "user-read-birthdate", "user-read-email",
                                    "playlist-read-collaborative", "playlist-read-private", "app-remote-control", "playlist-modify-public", "playlist-modify-private"});
            AuthenticationRequest request = builder.setShowDialog(true).build();
            AuthenticationClient.openLoginActivity(context, REQUEST_CODE, request);

        }else{
            for(PlayBackListener elistener : listeners) elistener.onError(new GeneralError(GeneralError.API_SPOTIFY_ERROR));

        }

    }

    @Override
    public void refreshState(Activity context, ConnectionCallback connectionCallback) {
        connect(context, connectionCallback::onConnect);
    }

    public void disconnect(Activity context) {
        if (player != null)
            player.disconnect();
        if (apiService != null)
            apiService.saveOffset(context);
        tokenRefreshHandler.removeCallbacks(tokenRefreshCallback);
        context.getApplicationContext().unregisterReceiver(receiver);
    }

    @Override
    public SubstitutionPlayerTyp getType() {
        return SubstitutionPlayerTyp.SPOTIFY_SUBSTITUTION;
    }

    @Override
    public TrackLikeService getLikeService() {
        return playListApiService;
    }

    @Override
    public void removeSubstitutionPlayList() {
        defaultPlaylistSubstitution.clear();
    }

    @Override
    public void setSubstitutionPlayList(String playListID) {
        if (!playListID.isEmpty())
            playListApiService.getAllTracks(playListID, 0, playListTracks -> {

                List<PlayListTrack> tracks = new ArrayList<>(Arrays.asList(playListTracks.getItems()));
                Collections.shuffle(tracks);

                for (PlayListTrack track : tracks) {
                    defaultPlaylistSubstitution.add(new TrackSpotifySubstitution(track.getTrack()));
                }
            }, error -> {if(BuildConfig.DEBUG)Log.e(TAG, "Set substitution playlist");});
    }

    public void authenticated(AuthenticationResponse response, Activity context) {
        switch (response.getType()) {
            // Response was successful and contains auth token
            case TOKEN:
                tokenRefreshCallback = () -> {
                    initialize(context);
                    if(BuildConfig.DEBUG)Log.d(TAG, "token refresh");
                };
                if (apiService == null)
                    apiService = new SpotifyWebApiTrackService(context.getApplicationContext(), response.getAccessToken());
                apiService.updateAccessToken(response.getAccessToken());
                if (playListApiService == null)
                    playListApiService = new SpotifyPlayListApiService(context.getApplicationContext(), response.getAccessToken());
                playListApiService.updateAccessToken(response.getAccessToken());

                if(BuildConfig.DEBUG)Log.d(TAG, "token refreshed " + response.getAccessToken());

                //DIRTY way to refresh access token before expiring
                tokenRefreshHandler.postDelayed(tokenRefreshCallback, response.getExpiresIn() * 1000 * 4 / 5);
                isAuthenticated = true;
                String defaultSubstitutionID = SharedPreferencesHelper.getString(context, SharedPreferencesHelper.SUBSTITUTION_PLAYLIST_ID_KEY);
                setSubstitutionPlayList(defaultSubstitutionID);
                queryForTopTracks();
                break;

            // Auth flow returned an error
            case ERROR:
                Log.e(TAG, response.getError());
                for(PlayBackListener listener : listeners) listener.onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));
                // Handle error response
                break;

            // Most likely auth flow was cancelled
            default:
                Log.e(TAG, response.getType() +"");

                for(PlayBackListener listener : listeners) listener.onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));
                break;
            // Handle other cases
        }
    }

    public void stopSubstitution() {
        if(getLikeService() != null)
            getLikeService().setCurrent(null);
        player.stop();
    }


    private Pair<String, String> lastRecommenderQuery;

    @Override
    public void resetSubstitutionBuffer() {
        lastRecommenderQuery = null;
        recommendationSubstitutions.clear();
        if(playListApiService != null)
         playListApiService.setCurrent(null);
    }

    private Set<PlayBackListener> listeners = new HashSet<>();

    @Override
    public void registerPlayBackListener(PlayBackListener playBackListener) {
        listeners.add(playBackListener);
    }

    public void queryForSubstitution(String artist, String title) {
        if (lastRecommenderQuery != null && lastRecommenderQuery.first.equals(artist) && lastRecommenderQuery.second.equals(title)) {
            return;
        }

        lastRecommenderQuery = new Pair<>(artist, title);

        Map<String, Object> query = new HashMap<>();
        String q = title.trim();
        q += "%20" + artist.trim();
        q = q.replaceAll("ft.", "");
        q = q.replaceAll("feat.", "");
        q = q.replaceAll(" {2}", " ");
        q = q.replaceAll(" ", "%20");
        query.put("q", q);
        query.put("market", "DE");
        query.put("type", "track");
        query.put("limit", 5);
        if(BuildConfig.DEBUG)Log.d(TAG, "Query for: " + artist + " " + title);
        recommendationSubstitutions.clear();
        if (apiService == null) return;
        apiService.getRecommendations(query, result -> getLikeService().getDislikedTracks(blackList -> {
            for (Track track : result.getTracks()) {
                if (!blackList.getTracks().contains(track)) {
                    recommendationSubstitutions.add(new TrackSpotifySubstitution(track));
                    if(BuildConfig.DEBUG)Log.d(TAG, "possible substitution for " + artist + "-" + title + ": " + track.getName() + "-" + track.getArtists()[0].getName());
                } else {
                    if(BuildConfig.DEBUG)Log.d(TAG, "blacklisted substitution substitution: " + track.getName() + "-" + track.getArtists()[0].getName());

                }
            }
        }), track -> playListApiService.updateLikeListeners(track), error -> {
            if (error.networkResponse != null && error.networkResponse.statusCode == GeneralError.SPOTIFY_TOKEN_ERROR)
                for(PlayBackListener listener : listeners) listener.onError(new GeneralError(GeneralError.SPOTIFY_TOKEN_ERROR));
            for(PlayBackListener listener : listeners) listener.onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));

        });
    }

    private void queryForTopTracks() {

        if (apiService == null) return;
        apiService.queryGeneralSubstitutions(result -> getLikeService().getDislikedTracks(blackList -> {
            for (Track track : result) {
                topPlaylistSubstitutions.add(new TrackSpotifySubstitution(track));
                if(BuildConfig.DEBUG)Log.d(TAG, "possible substitution: " + track.getName() + "-" + track.getArtists()[0].getName());

            }
        }), error -> {
            if (error.networkResponse != null && error.networkResponse.statusCode == GeneralError.SPOTIFY_TOKEN_ERROR)
                for(PlayBackListener listener : listeners) listener.onError(new GeneralError(GeneralError.SPOTIFY_TOKEN_ERROR));
            for(PlayBackListener listener : listeners) listener.onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));

        });
    }

    private void connect(Activity context, OnConnectCallback onConnectedFunction) {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(clientID)
                        .setRedirectUri(redirectUrl)
                        .build();
        SpotifyAppRemote.connect(context, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                player.setRemote(spotifyAppRemote, SpotifyProvider.this);
                onConnectedFunction.onConnected();
            }

            @Override
            public void onFailure(Throwable throwable) {
                if(BuildConfig.DEBUG)Log.d(TAG, throwable.toString());
                for(PlayBackListener elistener : listeners) elistener.onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));

            }
        });

    }

    @Override
    public void substitute() {
        if (player.isConnected()) {
            startSubstitution();
        }
    }

    public void searchPodcasts(RadioServiceViewModel runningService, ProgrammeInformationViewModel currentRunningProgramme, PodcastSearchResultListener listener) {
        this.apiService.searchPodcast(runningService, currentRunningProgramme, list -> {
            if (list == null) return;
            List<PodcastSpotifySubstitution> podcastSubstitutions = new ArrayList<>();
            for (PodcastEpisodeShort podcastEpisodeShort : list) {
                if (podcastEpisodeShort != null)
                    podcastSubstitutions.add(new PodcastSpotifySubstitution(podcastEpisodeShort));
            }
            listener.onPodcastsReceived(null, podcastSubstitutions.toArray(new PodcastSpotifySubstitution[0]));
        }, error -> {
            for(PlayBackListener elistener : listeners) elistener.onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));
        });
    }

    @Override
    public void seekTo(long progress) {
        player.seekTo(progress);
    }

    @Override
    public void startSubstitution(Substitution substitution) {
        if(Build.VERSION.SDK_INT >= 21) {
            SpotifySubstitution spotifySubstitution = (SpotifySubstitution) substitution;
            playListApiService.updateLikeListeners(spotifySubstitution);
            player.play(spotifySubstitution);
        }else{
            player.substitutionEnded();
            for(PlayBackListener listener : listeners) listener.onError(new GeneralError(GeneralError.API_SPOTIFY_ERROR));

        }
    }

    private void startSubstitution() {
        if (!defaultPlaylistSubstitution.isEmpty()) {
            startSubstitution(defaultPlaylistSubstitution.remove());
        } else if (!recommendationSubstitutions.isEmpty()) {
            startSubstitution(recommendationSubstitutions.remove());
        } else {
            if (!topPlaylistSubstitutions.isEmpty()) {

                startSubstitution(topPlaylistSubstitutions.remove());
                if (topPlaylistSubstitutions.size() < 2) {
                    queryForTopTracks();
                }
            } else {
                for(PlayBackListener listener : listeners) listener.onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));
            }
        }
    }

    public boolean isPlaying() {
        return player.getState() != SubstitutionPlayerState.STOPPED;
    }

    public void pauseSubstitution() {
        player.pause();
    }

    public void resumeSubstitution() {
        player.resume();
    }

    @Override
    public void started(Substitution substitution) {
        for(PlayBackListener listener : listeners) listener.started((SubstitutionItem) substitution);
    }

    @Override
    public void stopped(Substitution substitution) {
        //player.disconnect();
        for(PlayBackListener listener : listeners) listener.stopped((SubstitutionItem) substitution);

    }

    @Override
    public void paused(Substitution substitution) {
        for(PlayBackListener listener : listeners) listener.paused();
    }

    @Override
    public void playProgress(Substitution substitution, long l, long l1) {
        for(PlayBackListener listener : listeners) listener.playProgress((SubstitutionItem) substitution, l, l1);
    }

    @Override
    public SpotifySubstitution getCurrentSubstitution() {
        return (SpotifySubstitution) player.getSubstitution();
    }

    @FunctionalInterface
    private interface OnConnectCallback {
        void onConnected();
    }
}
