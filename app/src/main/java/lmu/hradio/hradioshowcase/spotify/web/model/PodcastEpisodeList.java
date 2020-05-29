package lmu.hradio.hradioshowcase.spotify.web.model;

public class PodcastEpisodeList extends PlayableItemList {

    private static final long serialVersionUID = -6600198498625042018L;
    private PodcastEpisodeShort[] items;

    public PodcastEpisodeShort[] getItems() {
        return items;
    }

    public void setItems(PodcastEpisodeShort[] items) {
        this.items = items;
    }
}

