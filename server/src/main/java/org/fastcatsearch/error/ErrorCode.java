package org.fastcatsearch.error;

/**
 * Created by swsong on 2015. 8. 30..
 */
public enum ErrorCode {
    QUERY_SYNTAX_ERROR(1000, "You have an error in query syntax: %s"),
    COLLECTION_NOT_FOUND(1100, "Collection '%s' doesn't exist.");

    private final int number;
    private final String message;

    private ErrorCode(int number, String message) {
        this.number = number;
        this.message = "Error Code: " + number + ". " + message;
    }

    public int getNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }
}
