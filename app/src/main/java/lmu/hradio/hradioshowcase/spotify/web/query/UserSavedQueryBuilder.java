package lmu.hradio.hradioshowcase.spotify.web.query;

/**
 * Spotify does not allow android auth client to request user-read-top scope.
 * -> Top can not be queried
 */
public class UserSavedQueryBuilder extends QueryBuilder{

    private String type = EndPoints.TRACKS;

    public static UserSavedQueryBuilder newBuilder() {
        UserSavedQueryBuilder newBuilder = new UserSavedQueryBuilder();
        newBuilder.setLimit("5");
        return newBuilder;
    }

    public UserSavedQueryBuilder setLimit(String limit) {
        params.put(LIMIT, limit);
        return this;
    }

    public UserSavedQueryBuilder setMarket(String market) {
        params.put(MARKET, market);
        return this;
    }

    public UserSavedQueryBuilder setOffset(String offset) {
        params.put(OFFSET, offset);
        return this;
    }

    public String buildEndpoint(){
        return EndPoints.ME +"/" +type;
    }
}
