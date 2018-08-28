package org.fastcatsearch.util;

import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.query.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchLogger {

    protected static Logger searchLogger = LoggerFactory.getLogger("SEARCH_LOG");

    private static String CACHE = "CACHE";
    private static String NOCACHE = "NOCACHE";
    private static String ERROR = "ERROR";

    public static void writeSearchLog(String collectionId, String searchKeyword, Object obj, long searchTime, boolean isCache, String errorMsg, String tagString) {
        int count = -1;
        int totalCount = -1;
        GroupResults groupResults = null;
        String type = "";
        if (obj instanceof Result) {
            Result result = (Result) obj;
            count = result.getCount();
            totalCount = result.getTotalCount();
            groupResults = result.getGroupResult();
        } else if (obj instanceof InternalSearchResult) {
            // InternalSearchResult 는 앞에 > 를 붙여준다.
            type = ">";
            InternalSearchResult result = (InternalSearchResult) obj;
            count = result.getCount();
            totalCount = result.getTotalCount();
        }

        StringBuilder groupBuilder = null;
        if (groupResults != null) {
            groupBuilder = new StringBuilder();
            int groupSize = groupResults.groupSize();
            for (int i = 0; i < groupSize; i++) {
                GroupResult groupResult = groupResults.getGroupResult(i);
                if (i > 0) {
                    groupBuilder.append(";");
                }
                groupBuilder.append(groupResult.size());
            }
        }

        String header = errorMsg != null ? ERROR : isCache ? CACHE : NOCACHE;

        searchLogger.info("{}[{}]\t{}\t{}\t{} ms\t{}\t{}\t{}\t[{}]\t{}", type, header, collectionId, searchKeyword
                , searchTime, count, totalCount
                , groupBuilder != null ? groupBuilder.toString() : "NOGROUP", errorMsg != null ? errorMsg : "OK", tagString);

    }
}
