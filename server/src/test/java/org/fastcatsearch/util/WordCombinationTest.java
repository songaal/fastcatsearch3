package org.fastcatsearch.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swsong on 2015. 7. 29..
 */
public class WordCombinationTest {

    @Test
    public void testCombination() {

        List<String> candidates = new ArrayList<String>();

        candidates.add("A");
        candidates.add("b");
        candidates.add("C");
        candidates.add("d");

        List<WordCombination.WordEntry> result = WordCombination.getDescCombination(candidates);

        for(WordCombination.WordEntry r : result) {
            System.out.println(r);
        }
    }
}
