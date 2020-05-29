package lmu.hradio.hradioshowcase.model.state;

import java.io.Serializable;

public class AppState implements Serializable {

    private static final long serialVersionUID = 4901196313399218616L;
    private PlayBackState playBackState = new PlayBackState();

    public PlayBackState getPlayBackState() {
        return playBackState;
    }


    public void clear() {
        playBackState = new PlayBackState();
    }
}
