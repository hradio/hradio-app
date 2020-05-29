package lmu.hradio.hradioshowcase.spotify.web.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QueryBuilder {

    protected static final String ENDPOINT = "endpoint";
    protected static final String QUERY = "q";
    protected static final String LIMIT = "limit";
    protected static final String MARKET = "market";
    protected static final String OFFSET = "offset";
    protected static final String COUNTRY = "country";


    protected Map<String, Object> params = new HashMap<>();

    protected void appendEntries(String key, String... entries) {
        if (!params.containsKey(key)) {
            params.put(key, "");
        }
        String existing = params.get(key).toString();
        existing = existing.isEmpty()? existing : existing +"%2C";
        StringBuilder builder = new StringBuilder(existing);
        Iterator<String> entryIterator = Arrays.asList(entries).iterator();
        while (entryIterator.hasNext()) {
            String entry = entryIterator.next().replace(" " , "20%");
            builder.append(entry);
            if (entryIterator.hasNext()) {
                builder.append("%2C");
            }
        }
        params.put(key, builder.toString());
    }


    public Map<String ,Object> build(){
        return params;
    }

}
