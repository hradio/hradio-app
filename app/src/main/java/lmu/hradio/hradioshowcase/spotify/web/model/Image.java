package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class Image implements Serializable {
    private static final long serialVersionUID = 5105190036192990868L;
    private int height;
    private int width;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
