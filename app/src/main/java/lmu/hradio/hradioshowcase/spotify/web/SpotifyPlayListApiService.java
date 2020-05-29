package lmu.hradio.hradioshowcase.spotify.web;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.model.substitiution.SpotifySubstitution;
import lmu.hradio.hradioshowcase.spotify.web.listener.ErrorListener;
import lmu.hradio.hradioshowcase.spotify.web.listener.ResultListener;
import lmu.hradio.hradioshowcase.spotify.web.model.Image;
import lmu.hradio.hradioshowcase.spotify.web.model.PlayListList;
import lmu.hradio.hradioshowcase.spotify.web.model.PlayListTrack;
import lmu.hradio.hradioshowcase.spotify.web.model.PlayListTrackList;
import lmu.hradio.hradioshowcase.spotify.web.query.EndPoints;
import lmu.hradio.hradioshowcase.spotify.web.query.PlayListQueryBuilder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpotifyPlayListApiService extends SpotifyWebSevice implements ErrorListener<VolleyError>, TrackLikeService {

    private final int limit = 20;

    private lmu.hradio.hradioshowcase.spotify.web.model.PlayList likeList;
    private lmu.hradio.hradioshowcase.spotify.web.model.PlayList dislikeList;

    private List<Track> likedTracks = new ArrayList<>();
    private List<Track> skippedTracks = new ArrayList<>();

    private List<ResultListener<List<Track>>> pendingLikedCallbacks = new ArrayList<>();
    private List<ResultListener<List<Track>>> pendingDislikedCallbacks = new ArrayList<>();

    private List<TrackChangeListener> trackChangeListeners = new ArrayList<>();

    private boolean initLike = false;
    private boolean initDislike = false;

    private Track current;

    public SpotifyPlayListApiService(Context context, String accessToken) {
        super(context, accessToken);
        String nameLikeList = context.getString(R.string.like_list_name);
        String nameDislikeList = context.getString(R.string.dislike_list_name);
        String descriptionLikeList = context.getString(R.string.like_list_description);
        String descriptionDislikeList = context.getString(R.string.dislike_list_description);
        getPlayList(nameLikeList, descriptionLikeList, 0, list -> {
            likeList = list;
            getAllTracks(list.getId(), 0, result -> {

                for (PlayListTrack track : result.getItems()) {
                    likedTracks.add(track.getTrack());
                }
                initLike = true;
                for (ResultListener<List<Track>> callback : pendingLikedCallbacks) {
                    callback.onResult(likedTracks);
                }
                pendingLikedCallbacks.clear();
            }, this);
        }, this);
        getPlayList(nameDislikeList, descriptionDislikeList, 0, list -> {
            dislikeList = list;
            getAllTracks(list.getId(), 0, result -> {
                for (PlayListTrack track : result.getItems()) {
                    skippedTracks.add(track.getTrack());
                }
                initDislike = true;
                for (ResultListener<List<Track>> callback : pendingDislikedCallbacks) {
                    callback.onResult(skippedTracks);
                }
                pendingDislikedCallbacks.clear();
            }, this);
        }, this);
    }

    public void getLikedList(ResultListener<List<Track>> listener) {
        if (!initLike) {
            pendingLikedCallbacks.add(listener);
        } else {
            listener.onResult(likedTracks);
        }
    }

    public void getDisikedList(ResultListener<List<Track>> listener) {
        if (!initDislike) {
            pendingDislikedCallbacks.add(listener);
        } else {
            listener.onResult(skippedTracks);
        }
    }

    public void isInFavorites(String uri, ResultListener<Boolean> listener) {
        getLikedList(likedTracks -> {
            for (Track track : likedTracks) {
                if (track.getUri().equals(uri)) {
                    listener.onResult(true);
                    return;
                }
            }
            listener.onResult(false);
        });
    }

    public void addToFavorites(Track track) {
        if (!likedTracks.contains(track)) {
            likedTracks.add(track);
            this.addTrackToPlayList(likeList.getId(), track.getUri(), this);
        }

        if(skippedTracks.contains(track)){
            this.removeFromDisliked(track);
        }
    }

    public void addToDisliked(Track track) {
        if (!skippedTracks.contains(track)) {
            skippedTracks.add(track);
            this.addTrackToPlayList(dislikeList.getId(), track.getUri(), this);
        }
    }

    public void removeFromFavorites(Track track) {
        likedTracks.remove(track);
        this.removeTrackFromPlayList(likeList.getId(), track);
    }

    public void removeFromDisliked(Track track) {
        skippedTracks.remove(track);
        this.removeTrackFromPlayList(dislikeList.getId(), track);
    }


    private void getPlayList(String name, String description, final int lastOffset, ResultListener<lmu.hradio.hradioshowcase.spotify.web.model.PlayList> listener, ErrorListener<VolleyError> errorListener) {
        PlayListQueryBuilder builder = PlayListQueryBuilder.newBuilder().setLimit(limit + "").setOffset(lastOffset + "");
        request(builder.build(), result -> {

            for (lmu.hradio.hradioshowcase.spotify.web.model.PlayList list : result.getItems()) {
                if (list.getName().equals(name)) {
                    listener.onResult(list);
                    return;
                }
            }

            if (result.getItems().length == limit) {
                getPlayList(name, description, lastOffset + limit, listener, errorListener);
            } else {
                createPlayList(name, description, listener, errorListener);
            }
        }, errorListener, PlayListList.class, builder.buildEndpoint());
    }

    private void createPlayList(String name, String description, ResultListener<lmu.hradio.hradioshowcase.spotify.web.model.PlayList> listener, ErrorListener<VolleyError> errorListener) {
        NewPlaylist newPlaylist = new NewPlaylist();
        newPlaylist.description = description;
        newPlaylist.isPublic = false;
        newPlaylist.name = name;
        String json = new Gson().toJson(newPlaylist);
        request(json, listener, errorListener, lmu.hradio.hradioshowcase.spotify.web.model.PlayList.class, EndPoints.ME, EndPoints.PLAYLISTS);
    }


    public void getAllTracks(String playlistId, int lastOffset, ResultListener<PlayListTrackList> listener, ErrorListener<VolleyError> errorListener) {
        PlayListQueryBuilder builder = PlayListQueryBuilder.newBuilder().setLimit(limit + "").setOffset(lastOffset + "");
        request(builder.build(), result -> {
            listener.onResult(result);
            if (result.getTotal() == result.getLimit()) {
                getAllTracks(playlistId, lastOffset + limit, listener, errorListener);
            }
        }, errorListener, PlayListTrackList.class, EndPoints.PLAYLISTS, playlistId, EndPoints.TRACKS);
    }

    private void addTrackToPlayList(String playlistId, String trackId, ErrorListener<VolleyError> errorListener) {
        Map<String, Object> params = new HashMap<>();
        params.put("uris", trackId);
        request(null, com.android.volley.Request.Method.POST, params, res -> {
        }, errorListener, SnapShot.class, EndPoints.PLAYLISTS, playlistId, EndPoints.TRACKS);
    }

    public void removeTrackFromPlayList(String playlistId, Track trackId) {
        Uri uri = new Uri();
        uri.setUri(trackId.getUri());
        RemoveTracks tracks = new RemoveTracks();
        tracks.setTracks(new Uri[]{uri});

        OkHttpClient client = new OkHttpClient();
        String url = buildUri(EndPoints.PLAYLISTS, playlistId, EndPoints.TRACKS);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .delete(RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(tracks)));

        for (String s : headers.keySet()) {
            builder.addHeader(s, headers.get(s));
        }

        Request request = builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.body() != null) {
                    if(BuildConfig.DEBUG)Log.d(SpotifyPlayListApiService.class.getSimpleName(), Objects.requireNonNull(response.body()).string());
                }

            }
        });

    }

    @Override
    public void getLikedTracks(OnSearchResultListener<PlayList> listener) {
        if (initLike) {
            Image[] covers = likeList.getImages();
            String coverUrl = "";
            if(covers != null && covers.length != 0){
                coverUrl = covers[0].getUrl();
            }

            listener.onResult(new PlayList(likeList.getId(), likeList.getName(), coverUrl , likedTracks));
        }else {
            pendingLikedCallbacks.add(services -> getLikedTracks(listener));
        }
    }

    @Override
    public void getDislikedTracks(OnSearchResultListener<PlayList> listener) {
        if (initDislike) {
            Image[] covers = dislikeList.getImages();
            String coverUrl = "";
            if(covers != null && covers.length != 0){
                coverUrl = covers[0].getUrl();
            }
            listener.onResult(new PlayList(dislikeList.getId(),dislikeList.getName(), coverUrl ,  skippedTracks));
        }else {
            pendingDislikedCallbacks.add(services -> getDislikedTracks(listener));
        }
    }

    @Override
    public void getPlaylist(String id, OnSearchResultListener<PlayList> listener) {
        if (id.equals(likeList.getId()))
            getLikedTracks(listener);
        else
            getDislikedTracks(listener);
    }

    @Override
    public void onError(VolleyError error) {
        error.printStackTrace();
    }

    @Override
    public void toggleTrack(Track track, OnSearchResultListener<Boolean> listener) {
        isInFavorites(track.getUri(), isFavorite -> {

            if (isFavorite)
            {
                removeFromFavorites(track);
            }
            else
            {
                addToFavorites(track);
            }
            listener.onResult(!isFavorite);

        });
    }

    @Override
    public void containsTrack(Track track, OnSearchResultListener<Boolean> listener) {
        isInFavorites(track.getUri(), listener::onResult);
    }

    @Override
    public void dislikeTrack(Track track, OnSearchResultListener<Boolean> listener) {
        if (track == null) return;
        isInFavorites(track.getUri(), isFavorite -> {
            if (isFavorite) {
                listener.onResult(false);
                return;
            }

            getDisikedList(disliked -> {
                for (Track t : disliked) {
                    if (track.getUri().equals(t.getUri())) {
                        listener.onResult(true);
                        return;
                    }
                }
                addToDisliked(track);
                listener.onResult(false);
            });
        });
    }

    @Override
    public Track getCurrent() {
        return current;
    }

    @Override
    public void registerTrackChangeListener(TrackChangeListener listener) {
        trackChangeListeners.add(listener);
    }

    @Override
    public void unregisterTrackChangeListener(TrackChangeListener listener) {
        trackChangeListeners.remove(listener);

    }

    @Override
    public void setCurrent(Track track) {
        this.current = track;
        for (TrackChangeListener listener : trackChangeListeners) {
            listener.onTrackChange(track);
        }
    }

    @Override
    public void getAllPlaylists(OnSearchResultListener<List<PlayList>> resultListener) {
       crawlAllPlaylists(0, new ArrayList<>(), resultListener);
    }

    private void crawlAllPlaylists(int lastOffset, List<PlayList> playLists, OnSearchResultListener<List<PlayList>> resultListener ){
        PlayListQueryBuilder builder = PlayListQueryBuilder.newBuilder().setLimit(limit + "").setOffset(lastOffset + "");
        request(builder.build(), result -> {

            for (lmu.hradio.hradioshowcase.spotify.web.model.PlayList list : result.getItems()) {
                Image[] covers = list.getImages();
                String coverUrl = "";
                if(covers != null && covers.length != 0){
                    int randomIndex = new Random().nextInt(covers.length);
                    coverUrl = covers[randomIndex].getUrl();
                }
                playLists.add(new PlayList(list.getId(), list.getName(), coverUrl, new ArrayList<>()));
            }

            if (result.getItems().length == limit) {
                crawlAllPlaylists(lastOffset + limit, playLists, resultListener);
            } else {
                resultListener.onResult(playLists);
            }
        }, error -> {
            if(BuildConfig.DEBUG)Log.d(TAG, "error while loading playlists");
            if(BuildConfig.DEBUG)error.printStackTrace();
        }, PlayListList.class, builder.buildEndpoint());
    }

    public void updateLikeListeners(lmu.hradio.hradioshowcase.spotify.web.model.Track track) {
        this.current = track;
        for (TrackChangeListener listener : trackChangeListeners) {
            listener.onTrackChange(track);
        }
    }

    public void updateLikeListeners(SpotifySubstitution spotifySubstitution) {
        this.current = spotifySubstitution;
        for (TrackChangeListener listener : trackChangeListeners) {
            listener.onTrackChange(spotifySubstitution);
        }
    }

    static class RemoveTracks {
        private Uri[] tracks;

        public Uri[] getTracks() {
            return tracks;
        }

        public void setTracks(Uri[] tracks) {
            this.tracks = tracks;
        }
    }

    static class Uri {
        private String uri;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    static class SnapShot {
        private String snapshot_id;

        public String getSnapshot_id() {
            return snapshot_id;
        }

        public void setSnapshot_id(String snapshot_id) {
            this.snapshot_id = snapshot_id;
        }
    }

    static class NewPlaylist {
        String name;
        String description;
        @SerializedName("public")
        boolean isPublic;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isPublic() {
            return isPublic;
        }

        public void setPublic(boolean aPublic) {
            isPublic = aPublic;
        }
    }
}
