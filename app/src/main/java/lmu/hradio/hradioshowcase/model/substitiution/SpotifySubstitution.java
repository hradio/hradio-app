package lmu.hradio.hradioshowcase.model.substitiution;

import lmu.hradio.hradioshowcase.listener.TrackLikeService;

public abstract class SpotifySubstitution  extends SubstitutionItem implements TrackLikeService.Track {
    private static final long serialVersionUID = -8029786650570195320L;

    public abstract String getUri();
    public abstract String getCoverUrl();

    @Override
    public String getSubstitutionType() {
        return SPOTIFY_TYPE;
    }

}
