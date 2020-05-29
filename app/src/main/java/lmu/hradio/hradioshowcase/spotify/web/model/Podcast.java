package lmu.hradio.hradioshowcase.spotify.web.model;

public class Podcast extends PlayableItem{

    private static final long serialVersionUID = -9040029694289539506L;
    private String[] available_markets;
    private String[] copyrights;
    private String description;
    private String media_type;
    private String publisher;
    private boolean explicit;

    private String[] languages;
    private Image[] images;
    private boolean is_externally_hosted;

    public String[] getAvailable_markets() {
        return available_markets;
    }

    public void setAvailable_markets(String[] available_markets) {
        this.available_markets = available_markets;
    }

    public String[] getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(String[] copyrights) {
        this.copyrights = copyrights;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }


    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
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

}
