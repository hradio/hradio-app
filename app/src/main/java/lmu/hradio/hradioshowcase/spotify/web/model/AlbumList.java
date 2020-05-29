package lmu.hradio.hradioshowcase.spotify.web.model;

public class AlbumList extends PlayableItemList {

    private static final long serialVersionUID = 6088944086696060818L;
    private Album[] items;

    public Album[] getItems() {
        return items;
    }

    public void setItems(Album[] items) {
        this.items = items;
    }

}
