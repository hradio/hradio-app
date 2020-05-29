package lmu.hradio.hradioshowcase.spotify.web.query;

public class SearchQueryBuilder extends QueryBuilder{

    private static final String TYPE = "type";

    public static SearchQueryBuilder newBuilder() {
        SearchQueryBuilder newBuilder = new SearchQueryBuilder();
        newBuilder.appendEntries(ENDPOINT, EndPoints.SEARCH);
        newBuilder.setLimit(10);
        newBuilder.setMarket("DE");
        return newBuilder;
    }

    public SearchQueryBuilder appendName(String name){
        String nameWithoutSpaces = name.replaceAll(" ", "%20");
        appendEntries(QUERY, nameWithoutSpaces);
        return this;
    }


    public SearchQueryBuilder appendType(String type){
        appendEntries(TYPE, type);
        return this;
    }

    public SearchQueryBuilder setLimit(int limit) {
        params.put(LIMIT, limit);
        return this;
    }

    public SearchQueryBuilder setMarket(String market) {
        params.put(MARKET, market);
        return this;
    }

    public SearchQueryBuilder setOffset(int offset) {
        params.put(OFFSET, offset);
        return this;
    }

}
