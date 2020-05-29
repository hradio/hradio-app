package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class Seed implements Serializable {

    private static final long serialVersionUID = -5340026094890953382L;
    private int initialPoolSize;
    private int afterFilteringSize;
    private String id;
    private String type;
    private String href;

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public int getAfterFilteringSize() {
        return afterFilteringSize;
    }

    public void setAfterFilteringSize(int afterFilteringSize) {
        this.afterFilteringSize = afterFilteringSize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
