package lmu.hradio.hradioshowcase.model.view;

import java.io.Serializable;

public class UserAction implements Serializable {
    private static final long serialVersionUID = 372882990003856361L;

    private String actiontime;

    private UserActionEnum action;

    private String actionlabel;

   public String getActionTime() {
        return actiontime;
    }

    public void setActionTime(String s) {
        this.actiontime = s;
    }

    public String getActionLabel() {
        return actionlabel;
    }

    public void setActionLabel(String l) {
        this.actionlabel = l;
    }

    public UserActionEnum getAction() { return action;    }

    public void setAction(UserActionEnum e) {
        this.action = e;
    }


}
