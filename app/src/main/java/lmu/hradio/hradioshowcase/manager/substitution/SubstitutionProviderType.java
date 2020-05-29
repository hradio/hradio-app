package lmu.hradio.hradioshowcase.manager.substitution;

import lmu.hradio.hradioshowcase.R;

public enum SubstitutionProviderType {

    None(R.string.no_substitution_display),
    Spotify(R.string.spotify_substitution_display);

    private int displayResource;


    SubstitutionProviderType(int displayResource){
        this.displayResource = displayResource;
    }

    public int getDisplayResource() {
        return displayResource;
    }
}
