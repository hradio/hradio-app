package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class UserSavedTrack implements Serializable {
    private static final long serialVersionUID = -1525565555840150684L;
    private String added_at;
    private Track track;

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public String getAdded_at() {
        return added_at;
    }

    public void setAdded_at(String added_at) {
        this.added_at = added_at;
    }
}
