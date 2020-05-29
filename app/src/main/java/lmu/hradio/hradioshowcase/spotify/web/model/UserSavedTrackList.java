package lmu.hradio.hradioshowcase.spotify.web.model;

public class UserSavedTrackList extends PlayableItemList {

    private static final long serialVersionUID = 6167237278343243330L;
    private UserSavedTrack[] items;

    public UserSavedTrack[] getItems() {
        return items;
    }

    public void setItems(UserSavedTrack[] items) {
        this.items = items;
    }

}
