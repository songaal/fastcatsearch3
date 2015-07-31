package org.fastcatsearch.ir.analysis;

import java.util.List;

/**
 * Created by swsong on 2015. 7. 31..
 */
public class TermsEntry {
    private final List<String> terms;
    private List<String> synonymTerms;

    public TermsEntry(List<String> terms) {
        this.terms = terms;
    }

    public TermsEntry(List<String> terms, List<String> synonymTerms) {
        this.terms = terms;
        this.synonymTerms = synonymTerms;
    }

    public List<String> getTerms() {
        return terms;
    }

    public List<String> getSynonymTerms() {
        return synonymTerms;
    }

    public void setSynonymTerms(List<String> synonymTerms) {
        this.synonymTerms = synonymTerms;
    }
}
