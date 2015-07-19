package org.fastcatsearch.ir.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;

/**
 * Created by swsong on 2015. 7. 19..
 */
public class AutocompleteAnalyzer extends Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(AutocompleteAnalyzer.class);


    public AutocompleteAnalyzer() {
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final AutocompleteTokenizer tokenizer = new AutocompleteTokenizer(reader);

        TokenFilter filter = new StandardFilter(tokenizer);

        return new TokenStreamComponents(tokenizer, filter);
    }
}
