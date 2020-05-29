package lmu.hradio.hradioshowcase.util;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.model.view.ProgrammeInformationViewModel;
import lmu.hradio.hradioshowcase.model.view.ProgrammeViewModel;

public final class BuzzwordExtractor {

    private static final String TAG = BuzzwordExtractor.class.getSimpleName();

    public static List<String> extractBuzzwords(ProgrammeInformationViewModel programmeInfo, int numberOfWords){
        if(programmeInfo == null) return new ArrayList<>();
        StringBuilder builerContent = new StringBuilder();
        StringBuilder builerNames = new StringBuilder();
        for(ProgrammeViewModel programmes: programmeInfo.getProgrammes()){
            if(programmes.getQualifiedDescription() != null)
                builerContent.append(programmes.getQualifiedDescription());
            if(programmes.getQualifiedName() != null)
                builerNames.append(programmes.getQualifiedName());
        }
        List<String> buzzwords = extractBuzzwords(builerContent.toString(), numberOfWords);
        buzzwords.addAll(extractBuzzwords(builerNames.toString(), numberOfWords));
        return buzzwords;
    }


    public static List<String> extractBuzzwords(String description, int numberOfWords){
        if(description == null || numberOfWords == 0 || description.isEmpty()) return new ArrayList<>();
        List<String> buzzwords = new ArrayList<>();
        String[] descriptionWords = getPlainWords(description);
        Map<String, Integer> wordCount = new HashMap<>();
        for(String word: descriptionWords) {
            if (!filter(word)) {
                Integer count = wordCount.get(word);
                count = (count != null) ? count + 1 : 1;
                wordCount.put(word, count);
            }
        }

        List<Map.Entry<String, Integer>> wordCountList = new ArrayList<>(wordCount.entrySet());
        Collections.sort(wordCountList, (entry1, entry2) -> Integer.compare(entry1.getValue(), entry2.getValue()));
        int startIndex = (wordCountList.size() <= numberOfWords) ? 0 : wordCountList.size() - 1 - numberOfWords;

        if(BuildConfig.DEBUG)Log.d(TAG, description);
        for(Map.Entry<String, Integer> e : wordCountList.subList(startIndex, wordCountList.size())){
            buzzwords.add(e.getKey());
            if(BuildConfig.DEBUG)Log.d(TAG, e.getKey());

        }
        return buzzwords;
    }

    private static boolean filter(String word){
        return word.length() <= 3;
    }

    @NonNull
    private static String[] getPlainWords(String description){
        return description.
                replaceAll("[^A-ZÜÄÖa-züäöß]"," ").split(" ");
    }

}
