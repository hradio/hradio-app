package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class PlayListTrack implements Serializable {
    private static final long serialVersionUID = 103193380537697039L;
    private String added_at;
    private Track track;
    private Owner added_by;
    private boolean is_local;
    private String primary_color;

    public Owner getAdded_by() {
        return added_by;
    }

    public void setAdded_by(Owner added_by) {
        this.added_by = added_by;
    }

    public boolean isIs_local() {
        return is_local;
    }

    public void setIs_local(boolean is_local) {
        this.is_local = is_local;
    }

    public String getPrimary_color() {
        return primary_color;
    }

    public void setPrimary_color(String primary_color) {
        this.primary_color = primary_color;
    }

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
