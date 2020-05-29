package lmu.hradio.hradioshowcase.spotify.web.model;


import java.io.Serializable;

import lmu.hradio.hradioshowcase.listener.TrackLikeService;

public class Track extends PlayableItem implements TrackLikeService.Track , Serializable {

    private static final long serialVersionUID = 5223123383566286866L;
    private Album album;
    private Artist[] artists;
    private int disc_number;
    private int duration_ms;
    private int popularity;
    private boolean explicit;
    private ExternalIDs external_ids;
    private String preview_url;
    private int track_number;
    private boolean is_local;
    private boolean is_playable;


    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Artist[] getArtists() {
        return artists;
    }

    public void setArtists(Artist[] artists) {
        this.artists = artists;
    }

    public int getDisc_number() {
        return disc_number;
    }

    public void setDisc_number(int disc_number) {
        this.disc_number = disc_number;
    }

    public int getDuration_ms() {
        return duration_ms;
    }

    public void setDuration_ms(int duration_ms) {
        this.duration_ms = duration_ms;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public ExternalIDs getExternal_ids() {
        return external_ids;
    }

    public void setExternal_ids(ExternalIDs external_ids) {
        this.external_ids = external_ids;
    }


    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public int getTrack_number() {
        return track_number;
    }

    public void setTrack_number(int track_number) {
        this.track_number = track_number;
    }

    public boolean isIs_local() {
        return is_local;
    }

    public void setIs_local(boolean is_local) {
        this.is_local = is_local;
    }

    public boolean isIs_playable() {
        return is_playable;
    }

    public void setIs_playable(boolean is_playable) {
        this.is_playable = is_playable;
    }

    @Override
    public String getArtist() {
            StringBuilder artist = new StringBuilder();
            for(int i = 0 ; i < artists.length; i++){
                artist.append(artists[i].getName());
                if(i < artists.length-1)
                    artist.append(" ft. ");
            }
            return artist.toString();

    }

    @Override
    public Image[] getImages() {
        return album.getImages();
    }
}
