package lmu.hradio.hradioshowcase.model.view;

import java.io.Serializable;

public class UserData implements Serializable {
    private static final long serialVersionUID = 8890206902256361L;
    private int id;

    private UserAction[] actions;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserAction[] getActions() {
        return actions;
    }

    public void setActions(UserAction[] ua) {
        this.actions=ua;
    }


}
