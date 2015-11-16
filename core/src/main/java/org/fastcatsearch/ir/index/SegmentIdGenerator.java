package org.fastcatsearch.ir.index;

/**
 * Created by swsong on 2015. 11. 16..
 */
public class SegmentIdGenerator {
    private static char[] first = null;
    private static char[] second = null;

    static {
        String firstString = "abcdefghijklmnopqrstuvwxyz";
        String secondString = "0123456789abcdefghijklmnopqrstuvwxyz";
        first = new char[firstString.length()];
        second = new char[secondString.length()];
        for (int i = 0; i < firstString.length(); i++) {
            first[i] = firstString.charAt(i);
        }
        for (int i = 0; i < secondString.length(); i++) {
            second[i] = secondString.charAt(i);
        }
    }

    private int firstPos = 0;
    private int secondPos = 0;

    protected String nextId() {
        String id = new String(new char[]{first[firstPos], second[secondPos++]});

        if (secondPos == second.length) {
            firstPos++;
            secondPos = 0;
        }

        if (firstPos == first.length) {
            firstPos = 0;
        }
        return id;
    }
}
