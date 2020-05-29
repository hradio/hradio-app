package lmu.hradio.hradioshowcase.spotify.web.model;

public class AlbumFull extends Album{

    private static final long serialVersionUID = -7991744900844598619L;
    private TrackList tracks;
    private String[] available_markets;
    private Copyright[] copyrights;
    private ExternalIDs external_ids;
    private String[] genres;
    private String label;
    private int popularity;

    public TrackList getTracks() {
        return tracks;
    }

    public void setTracks(TrackList tracks) {
        this.tracks = tracks;
    }

    public String[] getAvailable_markets() {
        return available_markets;
    }

    public void setAvailable_markets(String[] available_markets) {
        this.available_markets = available_markets;
    }

    public Copyright[] getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(Copyright[] copyrights) {
        this.copyrights = copyrights;
    }

    public ExternalIDs getExternal_ids() {
        return external_ids;
    }

    public void setExternal_ids(ExternalIDs external_ids) {
        this.external_ids = external_ids;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }
}
