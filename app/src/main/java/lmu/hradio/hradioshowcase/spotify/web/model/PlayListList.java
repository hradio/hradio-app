package lmu.hradio.hradioshowcase.spotify.web.model;

public class PlayListList extends PlayableItemList {

    private static final long serialVersionUID = 8848102315679802525L;
    private PlayList[] items;

    public PlayList[] getItems() {
        return items;
    }

    public void setItems(PlayList[] items) {
        this.items = items;
    }

}
