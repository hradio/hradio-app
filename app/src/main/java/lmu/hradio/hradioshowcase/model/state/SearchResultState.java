package lmu.hradio.hradioshowcase.model.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;

public class SearchResultState implements Serializable {

    private static final long serialVersionUID = -464937944956495775L;
    private List<RadioServiceViewModel> results = new ArrayList<>();

    public SearchResultState(){}

    public List<RadioServiceViewModel> getResults() {
        return results;
    }

    public void setResults(List<RadioServiceViewModel> results) {
        this.results = results;
    }
}
