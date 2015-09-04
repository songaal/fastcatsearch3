package org.fastcatsearch.error;

/**
 * Created by swsong on 2015. 8. 30..
 */
public enum CoreErrorCode implements ErrorCode {
    COLLECTION_NOT_INDEXED(1101, "Collection '%s' is not indexed."),
    SEARCH_INDEX_NOT_EXIST(1102, "Search index '%s' doesn't exists."),
    FIELD_INDEX_NOT_EXIST(1103, "Field index '%s' doesn't exists."),
    GROUP_INDEX_NOT_EXIST(1104, "Group index '%s' doesn't exists."),
    INDEX_NOT_EXIST(1105, "Index '%s' doesn't exists.");


    private final int number;
    private final String message;

    private CoreErrorCode(int number, String message) {
        this.number = number;
        this.message = "Error Code: " + number + ". " + message;
    }

    @Override
    public int getNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }
}
