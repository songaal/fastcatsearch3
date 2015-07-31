package org.fastcatsearch.ir.dictionary;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.util.WordCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by swsong on 2015. 7. 30..
 */
public class DictionaryUtils {

    private static Logger logger = LoggerFactory.getLogger(DictionaryUtils.class);

    private static final AnalyzerOption noOption = new AnalyzerOption();

    /**
     * 제공된 analyzerPool 로 keyword를 분석해 단어 리스트를 만든다.
     */
    public static List<String> makeTermList(AnalyzerPool analyzerPool, String keyword) {
        Analyzer analyzer = null;
        List<String> termList = new ArrayList<String>();
        TokenStream tokenStream = null;
        try {
            analyzer = analyzerPool.getFromPool();
            tokenStream = analyzer.tokenStream("", new StringReader(keyword), noOption);
            tokenStream.reset();

            if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
                CharsRefTermAttribute termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
                while (tokenStream.incrementToken()) {
                    termList.add(termAttribute.charsRef().toString());
                }
            } else {
                CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
                while (tokenStream.incrementToken()) {
                    termList.add(termAttribute.toString());
                }
            }
        } catch (IOException e) {
            logger.error("", e);
            return null;
        } finally {
            if (tokenStream != null) {
                try {
                    tokenStream.close();
                } catch (IOException ignore) {
                }
            }
            analyzerPool.releaseToPool(analyzer);
        }

        return termList;
    }

    /**
     * 스트링 리스트를 조합하여 사전에서 찾아본다.
     *
     * @Param map 사전맵
     * @Param termList 분석기를 통해 만들어진 단어리스트
     */
    public static List<WordCombination.WordEntryPair> findExtendedKey(Map<CharVector, CharVector[]> map, List<WordCombination.WordEntry> wordList) {

        List<WordCombination.WordEntryPair> foundPairList = new ArrayList<WordCombination.WordEntryPair>();

        if (wordList != null && wordList.size() > 0) {

            for (WordCombination.WordEntry wordEntry : wordList) {
                CharVector keyword = new CharVector(wordEntry.getWord());
                CharVector[] value = map.get(keyword);

                if (value != null && value.length > 0) {
                    String foundValue = value[0].toString();
                    foundPairList.add(new WordCombination.WordEntryPair(wordEntry, foundValue));
                }

            }

        }

        return foundPairList;
    }

    public static List<WordCombination.WordEntry> findExtendedKey(Set<CharVector> set, List<WordCombination.WordEntry> wordList) {

        List<WordCombination.WordEntry> foundList = new ArrayList<WordCombination.WordEntry>();

        if (wordList != null && wordList.size() > 0) {
            for (WordCombination.WordEntry wordEntry : wordList) {
                CharVector keyword = new CharVector(wordEntry.getWord());
                if(set.contains(keyword)) {
                    foundList.add(wordEntry);
                }
            }
        }
        return foundList;
    }

    /**
     * 사전에서 발견된 단어를 제외한 나머지 단어를 모은다.
     */
    public static List<String> makeRemnantList(List<String> termList, List<WordCombination.WordEntryPair> pairList) {
        if (pairList == null || pairList.size() == 0) {
            return termList;
        }
        List<String> remnant = new ArrayList<String>();

        for (String word : termList) {
            boolean found = false;
            OUTER:
            for (WordCombination.WordEntryPair pair : pairList) {
                WordCombination.WordEntry entry = pair.getEntry();
                // 하위 엘리먼트를 확인해서 일치하는것 발견시 제거한다.
                for (String e : entry.getElements()) {
                    if (e.equals(word)) {
                        found = true;
                        break OUTER;
                    }
                }
            }
            if (!found) {
                remnant.add(word);
            }
        }

        return remnant;
    }

    public static List<String> makeRemnantList2(List<String> termList, List<WordCombination.WordEntry> list) {
        if (list == null || list.size() == 0) {
            return termList;
        }
        List<String> remnant = new ArrayList<String>();

        for (String word : termList) {
            boolean found = false;
            OUTER:
            for (WordCombination.WordEntry entry : list) {
                // 하위 엘리먼트를 확인해서 일치하는것 발견시 제거한다.
                for (String e : entry.getElements()) {
                    if (e.equals(word)) {
                        found = true;
                        break OUTER;
                    }
                }
            }
            if (!found) {
                remnant.add(word);
            }
        }

        return remnant;
    }

    public static String joinWordList(List<String> wordList, String delimiter) {

        if(wordList.size() == 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        for(String e : wordList) {
            if(sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(e);
        }
        return sb.toString();
    }
}
