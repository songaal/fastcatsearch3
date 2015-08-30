package org.fastcatsearch.error;

/**
 * Created by swsong on 2015. 8. 30..
 */
public class SearchError extends RuntimeException {

    private final ErrorCode errorCode;
    private final String[] args;

    public SearchError(ErrorCode errorCode, String... args) {
        this.errorCode = errorCode;
        this.args = args;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return String.format(errorCode.getMessage(), args);
    }

    public static void main(String... args) {
        System.out.println(new SearchError(ErrorCode.COLLECTION_NOT_FOUND, "test").getMessage());
        System.out.println(new SearchError(ErrorCode.QUERY_SYNTAX_ERROR, "cn cannot be empty.").getMessage());
    }

}
