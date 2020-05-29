package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class Followers implements Serializable {
    private static final long serialVersionUID = -4955957861408096486L;
    private String href;
    private int total;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
