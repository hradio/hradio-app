package lmu.hradio.hradioshowcase.spotify.web.model;

public class TrackList extends PlayableItemList {

    private static final long serialVersionUID = -6879587376245824618L;
    private Track[] items;

    public Track[] getItems() {
        return items;
    }

    public void setItems(Track[] items) {
        this.items = items;
    }

}
