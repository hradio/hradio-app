package lmu.hradio.hradioshowcase.spotify.web;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.model.view.ProgrammeInformationViewModel;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.spotify.web.listener.ErrorListener;
import lmu.hradio.hradioshowcase.spotify.web.listener.ResultListener;
import lmu.hradio.hradioshowcase.spotify.web.model.Album;
import lmu.hradio.hradioshowcase.spotify.web.model.AlbumContainer;
import lmu.hradio.hradioshowcase.spotify.web.model.Artist;
import lmu.hradio.hradioshowcase.spotify.web.model.PodcastEpisodeContainer;
import lmu.hradio.hradioshowcase.spotify.web.model.PodcastEpisodeShort;
import lmu.hradio.hradioshowcase.spotify.web.model.RecommendationContainer;
import lmu.hradio.hradioshowcase.spotify.web.model.Track;
import lmu.hradio.hradioshowcase.spotify.web.model.TrackContainer;
import lmu.hradio.hradioshowcase.spotify.web.model.TrackList;
import lmu.hradio.hradioshowcase.spotify.web.model.UserSavedTrackList;
import lmu.hradio.hradioshowcase.spotify.web.query.AlbumTracksQueryBuilder;
import lmu.hradio.hradioshowcase.spotify.web.query.EndPoints;
import lmu.hradio.hradioshowcase.spotify.web.query.NewReleasesQueryBuilder;
import lmu.hradio.hradioshowcase.spotify.web.query.RecommendationQueryBuilder;
import lmu.hradio.hradioshowcase.spotify.web.query.TopQueryBuilder;
import lmu.hradio.hradioshowcase.spotify.web.query.UserSavedQueryBuilder;
import lmu.hradio.hradioshowcase.util.BuzzwordExtractor;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;

public class SpotifyWebApiTrackService extends SpotifyWebSevice {

    private static final int limit = 5;
    private int lastOffset;

    public SpotifyWebApiTrackService(Context context, String accessToken) {
        super(context,accessToken);
        lastOffset = SharedPreferencesHelper.getInt(context, SHARED_LAST_OFFSET_KEY, 0);
        lastOffset = (lastOffset < 0)? 0 : lastOffset;
    }

    private <T> void search(Map<String, Object> queryParams, ResultListener<T> listener, ErrorListener<VolleyError> errorListener, Class<T> tClass){
        request(queryParams, listener,errorListener,tClass, EndPoints.SEARCH);
    }


    private <T> void recommend(Map<String, Object> queryParams, ResultListener<T> listener, ErrorListener<VolleyError> errorListener, Class<T> tClass){
        request(queryParams, listener,errorListener,tClass,EndPoints.RECOMMENDATION);
    }

    public <T> void browse(Map<String, Object> queryParams, ResultListener<T> listener, ErrorListener<VolleyError> errorListener, Class<T> tClass){
        request(queryParams,listener,errorListener,tClass,EndPoints.BROWSE);
    }

    public void getRecommendations(Map<String, Object> queryParams, ResultListener<RecommendationContainer> listener, SearchCallback searchCallback, ErrorListener<VolleyError> errorListener){
        search(queryParams, result -> {
            if (result.getTracks().getItems().length > 0) {
                Track first = result.getTracks().getItems()[0];
                searchCallback.onTrackFound(first);
                RecommendationQueryBuilder builder = RecommendationQueryBuilder.newBuilder();
                builder.appendTracks(first.getId());
                if(queryParams.containsKey("limit")) builder.setLimit(queryParams.get("limit").toString());
                if(queryParams.containsKey("market")) builder.setMarket(queryParams.get("market").toString());

                for (Artist artist :first.getArtists()) {
                    builder.appendArtists(artist.getId());
                }
                recommend(builder.build(), listener, error -> {if(BuildConfig.DEBUG)Log.d(error.getMessage(), error.toString());}, RecommendationContainer.class);

            } else {
                searchCallback.onTrackFound(null);

                StringBuilder entry= new StringBuilder();
                for (Map.Entry<String, Object> e : queryParams.entrySet()){
                    entry.append(e.getKey()).append("=").append(e.getValue());
                }
                if(BuildConfig.DEBUG)Log.d(TAG, "empty Result: " + entry);
            }
        }, e ->{
            searchCallback.onTrackFound(null);
            errorListener.onError(e);
            }, TrackContainer.class);

    }

    public void queryGeneralSubstitutions(ResultListener<Track[]> listener, ErrorListener<VolleyError> errorListener){
        Random rand = new Random();
        int randomValue = rand.nextInt(100);
        if(randomValue < 60)
            getUserTop(listener, errorListener);
        else if (randomValue < 90)
            getNewReleases(listener, errorListener);
        else
            getUserSavedTracks(listener, errorListener);
    }

    private void getUserTop(ResultListener<Track[]> listener, ErrorListener<VolleyError> errorListener){
        TopQueryBuilder builder = TopQueryBuilder.newBuilder().setLimit(limit +"").setOffset(lastOffset+"");
        request(builder.build() , result -> {
            if(result.getItems().length == 0){
                if(lastOffset == 0) {
                    getUserSavedTracks(listener, errorListener);
                }else {
                    lastOffset = 0;
                    getUserTop(listener, errorListener);
                }
            }else {
               listener.onResult(result.getItems());
            }
        }, errorListener, TrackList.class, builder.buildEndpoint());
        lastOffset += limit;
    }

    private void getUserSavedTracks(ResultListener<Track[]> listener, ErrorListener<VolleyError> errorListener){
        UserSavedQueryBuilder builder = UserSavedQueryBuilder.newBuilder().setLimit(limit +"").setOffset(lastOffset+"");
        request(builder.build(), result -> {
            if(result.getItems().length == 0){
                if(lastOffset == 0) {
                    getNewReleases(listener, errorListener);
                }else {
                    lastOffset = 0;
                    getUserSavedTracks( listener, errorListener);
                }
            }else {
                Track[] tracks = new Track[result.getItems().length];
                for (int i = 0 ; i < tracks.length; i++){
                    tracks[i] = result.getItems()[i].getTrack();
                }
                listener.onResult(tracks);
            }
        }, errorListener, UserSavedTrackList.class, builder.buildEndpoint());
        lastOffset += limit;
    }

    private void getNewReleases(ResultListener<Track[]> listener, ErrorListener<VolleyError> errorListener){
        NewReleasesQueryBuilder builder = NewReleasesQueryBuilder.newBuilder().setLimit(limit +"").setOffset(lastOffset+"");
        request(builder.build() , result ->{
            for (Album a : result.getAlbums().getItems()){
                AlbumTracksQueryBuilder albumQueryBuilder = AlbumTracksQueryBuilder.newBuilder(a.getId());
                request(albumQueryBuilder.build(),  trackResult ->
                        listener.onResult(trackResult.getItems()), errorListener, TrackList.class, albumQueryBuilder.buildEndpoint());
            }
        },errorListener, AlbumContainer.class, builder.buildEndpoint());
        lastOffset += limit;
        if(lastOffset > 1000)
            lastOffset = 0;
    }
    public void saveOffset(Activity context) {
        SharedPreferencesHelper.put(context, SHARED_LAST_OFFSET_KEY, lastOffset);
    }

    public void searchPodcast(RadioServiceViewModel runningService, ProgrammeInformationViewModel currentRunningProgramme, ResultListener<PodcastEpisodeShort[]> listener, ErrorListener<VolleyError> errorListener) {

        List<String> buzzwordsProgramme = BuzzwordExtractor.extractBuzzwords(currentRunningProgramme, 3);
        if(runningService.getKeywords() != null)
            buzzwordsProgramme.addAll(runningService.getKeywords());
        else
            buzzwordsProgramme.add(runningService.getServiceLabel());

        for(String s : buzzwordsProgramme) {
            Map<String, Object> params = new HashMap<>();

            s = s.replaceAll(" ", "%20");
            params.put("type", EndPoints.EPISODES);
            params.put("market", "DE");
            params.put("limit", 3);
            final String queryString = s;
            params.put("query", queryString);

            search(params, list -> {
                listener.onResult(list.getEpisodes().getItems());
                for (PodcastEpisodeShort episode : list.getEpisodes().getItems()) {
                    if(episode != null)
                        if(BuildConfig.DEBUG)Log.d(queryString, episode.getName());
                }
            }, errorListener, PodcastEpisodeContainer.class);

        }
    }

    @FunctionalInterface
    public interface SearchCallback{
       void onTrackFound(Track track);
    }
}
