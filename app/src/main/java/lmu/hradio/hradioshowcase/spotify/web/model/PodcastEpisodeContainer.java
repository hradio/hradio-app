package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class PodcastEpisodeContainer implements Serializable {


    private static final long serialVersionUID = 3344463134152858528L;
    private PodcastEpisodeList episodes;


    public PodcastEpisodeList getEpisodes() {
        return episodes;
    }

    public void setEpisodes(PodcastEpisodeList episodes) {
        this.episodes = episodes;
    }
}
