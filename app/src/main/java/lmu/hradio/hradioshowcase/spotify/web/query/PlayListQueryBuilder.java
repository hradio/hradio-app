package lmu.hradio.hradioshowcase.spotify.web.query;

/**
 * Spotify does not allow android auth client to request user-read-top scope.
 * -> Top can not be queried
 */
public class PlayListQueryBuilder extends QueryBuilder{

    public static PlayListQueryBuilder newBuilder() {
        PlayListQueryBuilder newBuilder = new PlayListQueryBuilder();
        newBuilder.setLimit("20");
        return newBuilder;
    }

    public PlayListQueryBuilder setLimit(String limit) {
        params.put(LIMIT, limit);
        return this;
    }

    public PlayListQueryBuilder setOffset(String offset) {
        params.put(OFFSET, offset);
        return this;
    }

    public String buildEndpoint(){
        return EndPoints.ME +"/"+ EndPoints.PLAYLISTS;
    }
}
