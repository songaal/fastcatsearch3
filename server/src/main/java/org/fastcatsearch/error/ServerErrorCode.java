package org.fastcatsearch.error;

/**
 * Created by swsong on 2015. 8. 30..
 */
public enum ServerErrorCode implements ErrorCode {
    SERVER_SEARCH_ERROR(1000, "Internal Server Search Error : %"),
    QUERY_SYNTAX_ERROR(1001, "You have an error in query syntax: %s"),
    COLLECTION_NOT_FOUND(1002, "Collection '%s' doesn't exist."),
    DATA_NODE_CONNECTION_ERROR(1003, "Cannot connect to data node '%s'."),
    SEARCH_TIMEOUT_ERROR(1004, "Search timeout. limit = '%s'."),
    JOB_TIMEOUT_ERROR(1005, "Job timeout. limit = '%s'.");

    private final int number;
    private final String message;

    private ServerErrorCode(int number, String message) {
        this.number = number;
        this.message = "Error Code: " + number + ". " + message;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
