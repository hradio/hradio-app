package lmu.hradio.hradioshowcase.spotify.web.model;

public class PodcastEpisode extends PodcastEpisodeShort {
    private static final long serialVersionUID = 4305202505961651697L;
    private Podcast show;

    public Podcast getShow() {
        return show;
    }

    public void setShow(Podcast show) {
        this.show = show;
    }
}
