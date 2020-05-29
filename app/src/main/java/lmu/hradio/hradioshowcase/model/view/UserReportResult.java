package lmu.hradio.hradioshowcase.model.view;

import java.io.Serializable;

import eu.hradio.httprequestwrapper.dtos.service_use.Context;

public class UserReportResult implements Serializable {
    private static final long serialVersionUID = 17888990227896361L;

    private long id;

    private Context context;

    private String reportId;

    private String description;

    private String[] values;

    private String[] labels;

    private UserData data;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String id) {
        this.reportId = id;
    }

    public String getDescription() { return description;    }

    public void setDescription(String d) {
        this.description = d;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public UserData getUserData() { return data;  }

    public void setUserData(UserData ud) { this.data=ud;   }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
