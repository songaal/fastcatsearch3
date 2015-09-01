package org.fastcatsearch.query;

import org.fastcatsearch.ir.query.Term;
import org.junit.Test;

/**
 * Created by swsong on 2015. 9. 1..
 */
public class TermProximityParseTest {

    @Test
    public void parse() {
        String str = "ALL(abc def)~2";
        System.out.println("term:"+getType(str));
        str = "ALL(abc def)~-2";
        System.out.println("term:"+getType(str));
        str = "ALL(abc def)";
        System.out.println("term:"+getType(str));
    }

    private String getType(String str) {
        if (str.startsWith("ALL(")) {
            if (str.endsWith(")")) {
                return str.substring(4, str.length() - 1);
            } else {
                int p = str.lastIndexOf(")");
                System.out.println("p="+p);
                System.out.println("len="+str.length());
                if (str.length() > p + 2) {
                    char ch = str.charAt(p + 1);
                    if (ch == '~') {
                        String proximityStr = str.substring(p + 2);
                        int proximity = Integer.parseInt(proximityStr);
                        System.out.println("proximity > " + proximity);
                    }
                    return str.substring(4, p);
                }
            }
        }
        return null;
    }
}
