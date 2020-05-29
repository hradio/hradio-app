package lmu.hradio.hradioshowcase.spotify.web.query;

/**
 * Spotify does not allow android auth client to request user-read-top scope.
 * -> Top can not be queried
 */
public class TopQueryBuilder extends QueryBuilder{

    public static TopQueryBuilder newBuilder() {
        TopQueryBuilder newBuilder = new TopQueryBuilder();
        newBuilder.setLimit("5");
        newBuilder.setTimeRange("medium_term");
        return newBuilder;
    }

    public TopQueryBuilder setLimit(String limit) {
        params.put(LIMIT, limit);
        return this;
    }

    public TopQueryBuilder setTimeRange(String range) {
        params.put("time_range", range);
        return this;
    }

    public TopQueryBuilder setOffset(String offset) {
        params.put(OFFSET, offset);
        return this;
    }

    public String buildEndpoint(){
        return EndPoints.ME +"/" +EndPoints.TOP  +"/" +EndPoints.TRACKS;
    }
}
