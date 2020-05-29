package lmu.hradio.hradioshowcase.spotify.web.model;

public class Album extends PlayableItem{

    private static final long serialVersionUID = -2628404825616245871L;
    private String album_type;
    private String release_date;
    private String release_date_precision;
    private int total_tracks;
    private Image[] images;
    private Artist[] artists;

    public String getAlbum_type() {
        return album_type;
    }

    public void setAlbum_type(String album_type) {
        this.album_type = album_type;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getRelease_date_precision() {
        return release_date_precision;
    }

    public void setRelease_date_precision(String release_date_precision) {
        this.release_date_precision = release_date_precision;
    }

    public int getTotal_tracks() {
        return total_tracks;
    }

    public void setTotal_tracks(int total_tracks) {
        this.total_tracks = total_tracks;
    }

    public Image[] getImages() {
        return images;
    }

    public void setImages(Image[] images) {
        this.images = images;
    }

    public Artist[] getArtists() {
        return artists;
    }

    public void setArtists(Artist[] artists) {
        this.artists = artists;
    }

}
