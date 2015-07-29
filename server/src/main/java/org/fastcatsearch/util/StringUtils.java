package org.fastcatsearch.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swsong on 2015. 7. 29..
 */
public class StringUtils {

    /**
     * 긴 조합부터 후보를 뽑아낸다. 단어 순서는 지켜준다. 단어순서가 없는 permutation은 연산비용이 너무 많이 들게 되므로 지원하지 않음.
     * */
    public static List<String> getDescCombination(List<String> candidates) {
        return getDescCombination(candidates, null);
    }
    public static List<String> getDescCombination(List<String> candidates, String delimiter) {
        List<String> result = new ArrayList<String>();
        int SIZE = candidates.size();
        for(int m = SIZE; m > 0; m--) {
            //기준위치를 앞에서 부터 한칸씩 이동시키며 후보를 찾는다.
            for(int p = 0; p < SIZE; p++) {
                getCombination(null, candidates, p, m, result, delimiter);
            }
        }
        return result;
    }

    private static void getCombination(String prevWord, List<String> candidates, int pos, int m, List<String> result, String delimiter) {
        if( pos + m > candidates.size() ) {
            //후보가 있을 수 없다.
            return;
        }
        String word = null;
        if(prevWord != null) {
            if(delimiter != null) {
                word = prevWord + " " + candidates.get(pos);
            } else {
                word = prevWord + candidates.get(pos);
            }
        } else {
            word = candidates.get(pos);
        }
        pos++;
        m--;
        if(m > 0) {
            for(int p = pos; p + m - 1 < candidates.size(); p++ ){
                //다음 pos부터 n-1개를 재귀적으로 뽑는다.
                getCombination(word, candidates, p, m, result, delimiter);
            }
            return;
        }
        result.add(word);
    }
}
