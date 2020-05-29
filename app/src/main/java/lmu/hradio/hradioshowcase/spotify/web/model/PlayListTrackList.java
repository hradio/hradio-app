package lmu.hradio.hradioshowcase.spotify.web.model;

public class PlayListTrackList extends PlayableItemList {

    private static final long serialVersionUID = -6018701203472464574L;
    private PlayListTrack[] items;

    public PlayListTrack[] getItems() {
        return items;
    }

    public void setItems(PlayListTrack[] items) {
        this.items = items;
    }

}
