package org.ionkin.search;

import java.util.*;

public class PagePreview {

    private Map<LightString, Integer> frequence;

    public PagePreview(Map<LightString, Integer> frequence) {
        this.frequence = frequence;
    }

    public Long getWeight(LightString[] articleWords, List<LightString> queryWords) {
        Map<LightString, Integer> localFreq = new HashMap<>();
        queryWords.forEach(w -> {
            if (frequence.containsKey(w)) {
                localFreq.put(w, frequence.get(w));
            }
        });
        Map.Entry<LightString, Integer>[] localFreqAsArray = localFreq.entrySet().toArray(new Map.Entry[0]);
        Arrays.sort(localFreqAsArray, Comparator.comparingInt(Map.Entry::getValue));

        return null;
    }
}
