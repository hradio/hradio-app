package lmu.hradio.hradioshowcase.spotify.web.model;

public class PodcastList extends PlayableItemList {

    private static final long serialVersionUID = -4029481195969289865L;
    private Podcast[] items;

    public Podcast[] getItems() {
        return items;
    }

    public void setItems(Podcast[] items) {
        this.items = items;
    }
}

