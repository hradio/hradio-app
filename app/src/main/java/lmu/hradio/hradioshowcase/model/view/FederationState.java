package lmu.hradio.hradioshowcase.model.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.hradio.httprequestwrapper.dtos.service_search.Genre;

public class FederationState implements Serializable, CheckedValue.OnCheckChangeListener {

    private static final long serialVersionUID = -365880766331225108L;

    private int searchDepth;
    private int searchWidth;
    private ArrayList<CheckedValue> genres;
    private ArrayList<CheckedValue> keywords;
    private transient OnChangeListener listener;

    public int getSearchDepth() {
        return searchDepth;
    }

    public void setSearchDepth(int searchDepth) {
        this.searchDepth = searchDepth;
    }

    public int getSearchWidth() {
        return searchWidth;
    }

    public void setSearchWidth(int searchWidth) {
        this.searchWidth = searchWidth;
    }

    public ArrayList<CheckedValue> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        ArrayList<CheckedValue> checkedGenres = new ArrayList<>();
        List<String> alreadyAdded = new ArrayList<>();
        for(Genre genre: genres){
            if (alreadyAdded.contains(genre.getName()))
                continue;
            checkedGenres.add(new CheckedValue<>(genre.getName(), true, this));
            alreadyAdded.add(genre.getName());
        }
        this.genres = checkedGenres;
    }

    public ArrayList<CheckedValue> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        ArrayList<CheckedValue> checkedKewords= new ArrayList<>();
        List<String> alreadyAdded = new ArrayList<>();
        for(String keyword: keywords){
            if (alreadyAdded.contains(keyword))
                continue;
            checkedKewords.add(new CheckedValue<>(keyword, true, this));
            alreadyAdded.add(keyword);
        }
        this.keywords = checkedKewords;
    }

    @Override
    public void onCheckChange() {
        if(listener != null)
            listener.onChange();
    }

    public void setListener(OnChangeListener listener) {
        this.listener = listener;
    }

    @FunctionalInterface
    public interface OnChangeListener{
        void onChange();
    }
}
