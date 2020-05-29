package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

public class PlayableItem implements Serializable {

    private static final long serialVersionUID = 6046446580717966296L;
    private String href;
    private String name;
    private String type;
    private String uri;
    private String id;

    private ExternalUrls external_urls;

    public ExternalUrls getExternal_urls() {
        return external_urls;
    }

    public void setExternal_urls(ExternalUrls external_urls) {
        this.external_urls = external_urls;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
