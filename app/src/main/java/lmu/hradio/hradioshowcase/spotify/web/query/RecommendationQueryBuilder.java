package lmu.hradio.hradioshowcase.spotify.web.query;

public class RecommendationQueryBuilder extends QueryBuilder{

    private static final String ARTIST_SEED = "seed_artists";
    private static final String TRACK_SEED = "seed_tracks";
    private static final String ALBUM_SEED = "seed_albums";

    public static RecommendationQueryBuilder newBuilder() {
        RecommendationQueryBuilder newBuilder = new RecommendationQueryBuilder();
        newBuilder.setLimit("10");
        newBuilder.setMarket("DE");
        return newBuilder;
    }

    public RecommendationQueryBuilder setLimit(String limit) {
        params.put(LIMIT, limit);
        return this;
    }

    public RecommendationQueryBuilder setMarket(String market) {
        params.put(MARKET, market);
        return this;
    }

    public RecommendationQueryBuilder setOffset(String offset) {
        params.put(OFFSET, offset);
        return this;
    }

    public RecommendationQueryBuilder appendTracks(String... tracks) {
        appendEntries(TRACK_SEED, tracks);
        return this;
    }

    public RecommendationQueryBuilder appendArtists(String... artists) {
        appendEntries(ARTIST_SEED, artists);
        return this;
    }

    public RecommendationQueryBuilder appendAlbums(String... albums) {
        appendEntries(ALBUM_SEED, albums);
        return this;
    }


}
