package org.fastcatsearch.error;

/**
 * Created by swsong on 2016. 3. 26..
 */
public class SearchAbortError extends RuntimeException {
    public SearchAbortError() {
    }

    public SearchAbortError(String message) {
        super(message);
    }
}
