package lmu.hradio.hradioshowcase.listener;

import java.io.Serializable;
import java.util.List;

import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import lmu.hradio.hradioshowcase.spotify.web.model.Image;

/**
 * Track like service interface
 */
public interface TrackLikeService {

    /**
     * toggle track like state
     *
     * @param track    - the track
     * @param listener - is track favorite callback
     */
    void toggleTrack(Track track, OnSearchResultListener<Boolean> listener);

    /**
     * check track like state
     *
     * @param track    - the track
     * @param listener - is track favorite callback
     */
    void containsTrack(Track track, OnSearchResultListener<Boolean> listener);

    /**
     * dislike track
     *
     * @param track    - the track
     * @param listener - boolean callback
     */
    void dislikeTrack(Track track, OnSearchResultListener<Boolean> listener);

    /**
     * remove track from playlist
     *
     * @param playlistId - playlist's id
     * @param trackId    - the track
     */
    void removeTrackFromPlayList(String playlistId, Track trackId);

    /**
     * get all liked tracks
     *
     * @param listener - result callback
     */
    void getLikedTracks(OnSearchResultListener<PlayList> listener);

    /**
     * get disliked tracks
     *
     * @param listener - result callback
     */
    void getDislikedTracks(OnSearchResultListener<PlayList> listener);

    /**
     * get playlist
     *
     * @param id       - playlist's id
     * @param listener - result callback
     */
    void getPlaylist(String id, OnSearchResultListener<PlayList> listener);

    /**
     * get last resolved track
     *
     * @return last resolved track
     */
    Track getCurrent();

    /**
     * register last resolved track change listener
     *
     * @param listener - the listener
     */
    void registerTrackChangeListener(TrackChangeListener listener);

    /**
     * unregister last resolved track change listener
     *
     * @param listener - the listener
     */
    void unregisterTrackChangeListener(TrackChangeListener listener);

    /**
     * set current track
     * @param track - track
     */
    void setCurrent(Track track);

    void getAllPlaylists(OnSearchResultListener<List<PlayList>> resultListener);

    /**
     * Track interface
     */
    interface Track extends Serializable {
        /**
         * get track name
         *
         * @return track name
         */
        String getName();

        /**
         * get track id
         *
         * @return track id
         */
        String getId();

        /**
         * get track uri
         *
         * @return track uro
         */
        String getUri();

        /**
         * get artist name
         *
         * @return artist name
         */
        String getArtist();

        /**
         * get track image data
         *
         * @return track image data
         */
        Image[] getImages();
    }

    /**
     * Track change callback
     */
    @FunctionalInterface
    interface TrackChangeListener {
        /**
         * new track callback method
         *
         * @param newTrack - the new track
         */
        void onTrackChange(Track newTrack);
    }

    /**
     * Playlist model class
     */
    class PlayList implements Serializable {
        private static final long serialVersionUID = 5187590904194409707L;
        /**
         * playlist's id
         */
        private String id;
        /**
         * List of playlist's tracks
         */

        /**
         * playlist's name
         */
        private String name;

        /**
         * playlist's coverUrl
         */
        private String coverUrl;

        private List<Track> tracks;

        public PlayList(String id, String name, String coverUrl, List<Track> tracks) {
            this.id = id;
            this.name = name;
            this.tracks = tracks;
            this.coverUrl = coverUrl;
        }

        public String getName() {
            return name;
        }

        public String getCoverUrl() {
            return coverUrl;
        }

        /**
         * get playlist's id
         *
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * get playlist's id
         *
         * @param id - the id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * get playlist's tracks
         *
         * @return the tracks
         */
        public List<Track> getTracks() {
            return tracks;
        }

        /**
         * get playlist's tracks
         *
         * @param tracks - the tracks
         */
        public void setTracks(List<Track> tracks) {
            this.tracks = tracks;
        }
    }

}