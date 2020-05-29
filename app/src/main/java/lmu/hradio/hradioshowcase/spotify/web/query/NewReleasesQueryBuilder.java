package lmu.hradio.hradioshowcase.spotify.web.query;

/**
 * Spotify does not allow android auth client to request user-read-top scope.
 * -> Top can not be queried
 */
public class NewReleasesQueryBuilder extends QueryBuilder{

    private String type = "new-releases";

    public static NewReleasesQueryBuilder newBuilder() {
        NewReleasesQueryBuilder newBuilder = new NewReleasesQueryBuilder();
        newBuilder.setLimit("5");
        return newBuilder;
    }

    public NewReleasesQueryBuilder setLimit(String limit) {
        params.put(LIMIT, limit);
        return this;
    }

    public NewReleasesQueryBuilder setCountry(String country) {
        params.put(COUNTRY, country);
        return this;
    }

    public NewReleasesQueryBuilder setOffset(String offset) {
        params.put(OFFSET, offset);
        return this;
    }

    public String buildEndpoint(){
        return EndPoints.BROWSE +"/" +type;
    }
}