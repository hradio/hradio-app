package lmu.hradio.hradioshowcase.model.weburlconfig;

import java.io.Serializable;

public class WebUrl implements Serializable {

    private static final long serialVersionUID = -2264157214597977924L;

    private DabIdentifier[] ids;
    private String serviceLabel;

    private String radioWebUrl;

    public WebUrl() {
    }

    public DabIdentifier[] getIds() {
        return ids;
    }
    public String getServiceLabel() {
        return serviceLabel;
    }

    public String getRadioWebUrl() {
        return radioWebUrl;
    }
}
