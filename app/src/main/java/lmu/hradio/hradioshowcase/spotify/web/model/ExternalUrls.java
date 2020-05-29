package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class ExternalUrls implements Serializable {

    private static final long serialVersionUID = -4343776774716756914L;
    private String spotify;

    public String getSpotify() {
        return spotify;
    }

    public void setSpotify(String spotify) {
        this.spotify = spotify;
    }
}
