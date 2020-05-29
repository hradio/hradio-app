package lmu.hradio.hradioshowcase.spotify.web.model;

import java.io.Serializable;

class Copyright implements Serializable {

    private static final long serialVersionUID = -5222295527626893003L;
    private String text;
    private String type;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
