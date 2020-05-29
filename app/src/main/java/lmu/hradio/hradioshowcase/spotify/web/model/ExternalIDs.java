package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class ExternalIDs implements Serializable {

    private static final long serialVersionUID = -8680080899187316704L;
    private String isrc;

    public String getIsrc() {
        return isrc;
    }

    public void setIsrc(String isrc) {
        this.isrc = isrc;
    }
}
