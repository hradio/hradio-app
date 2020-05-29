package lmu.hradio.hradioshowcase.util;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import eu.hradio.httprequestwrapper.dtos.recommendation.Recommender;
import lmu.hradio.hradioshowcase.R;

public final class Parser {

    public static String parseUri(Uri uri, Context context) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
            return stringBuilder.toString();

    }

    public static String parseTBA(String href){
        if(href.startsWith("urn:radioplayer:metadata:cs:Category:2012:1")) return "Pop/Chart";
        if(href.startsWith("urn:radioplayer:metadata:cs:Category:2012:2")) return "Rock/Indie";
        if(href.startsWith("urn:radioplayer:metadata:cs:Category:2012:3")) return "Dance/RnB";
        if(href.startsWith("urn:radioplayer:metadata:cs:Category:2012:4")) return "Easy/Oldies/Jazz";
        if(href.startsWith("urn:radioplayer:metadata:cs:Category:2012:5")) return "Classical/World";
        if(href.startsWith("urn:radioplayer:metadata:cs:Category:2012:6")) return "News/Sport/Talk";
        if(href.startsWith("urn:radioplayer:metadata:cs:Category:2012:7")) return "Comedy/Drama/Kids";
        return "";
    }

    public static String parseRecommender(Recommender recommender, Context context){
        switch (recommender.getRecommenderName().toLowerCase()){
            case "mlt" : return context.getString(R.string.mlt_description);
            case "histogram" : return context.getString(R.string.histogram_description);
            case "expert" : return context.getString(R.string.expert_description);
            case "trend" : return context.getString(R.string.trend_description);
            case "location" : return context.getString(R.string.location_description);
            case "category" : return context.getString(R.string.category_description);
            default: return  "";
        }
    }

}
