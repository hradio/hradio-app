package lmu.hradio.hradioshowcase.spotify.web.model;

public class PodcastEpisodeShort extends PlayableItem{

    private static final long serialVersionUID = -331295347792579858L;
    private String description;
    private boolean explicit;

    private String language;
    private Image[] images;
    private boolean is_externally_hosted;
    private boolean is_playable;
    private String release_date;
    private String release_date_precision;


    private String audio_preview_url;
    private long duration_ms;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }



    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Image[] getImages() {
        return images;
    }

    public void setImages(Image[] images) {
        this.images = images;
    }

    public boolean isIs_externally_hosted() {
        return is_externally_hosted;
    }

    public void setIs_externally_hosted(boolean is_externally_hosted) {
        this.is_externally_hosted = is_externally_hosted;
    }

    public boolean isIs_playable() {
        return is_playable;
    }

    public void setIs_playable(boolean is_playable) {
        this.is_playable = is_playable;
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

    public String getAudio_preview_url() {
        return audio_preview_url;
    }

    public void setAudio_preview_url(String audio_preview_url) {
        this.audio_preview_url = audio_preview_url;
    }

    public long getDuration_ms() {
        return duration_ms;
    }

    public void setDuration_ms(long duration_ms) {
        this.duration_ms = duration_ms;
    }
}
