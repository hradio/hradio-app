package lmu.hradio.hradioshowcase.spotify.web.query;

public class AlbumTracksQueryBuilder extends QueryBuilder{

    private String id;

    public static AlbumTracksQueryBuilder newBuilder(String id) {
        AlbumTracksQueryBuilder newBuilder = new AlbumTracksQueryBuilder();
        newBuilder.setLimit("5");
        newBuilder.setID(id);
        return newBuilder;
    }

    public AlbumTracksQueryBuilder setLimit(String limit) {
        params.put(LIMIT, limit);
        return this;
    }


    public AlbumTracksQueryBuilder setMarket(String market) {
        params.put(MARKET, market);
        return this;
    }

    public AlbumTracksQueryBuilder setID(String id) {
        this.id = id;
        return this;
    }

    public AlbumTracksQueryBuilder setOffset(String offset) {
        params.put(OFFSET, offset);
        return this;
    }

    public String buildEndpoint(){
        return EndPoints.ALBUMS +"/" +id +"/" +EndPoints.TRACKS;
    }
}
