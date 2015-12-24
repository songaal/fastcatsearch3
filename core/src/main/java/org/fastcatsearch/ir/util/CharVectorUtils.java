package org.fastcatsearch.ir.util;

import org.fastcatsearch.ir.io.CharVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swsong on 2015. 12. 24..
 */
public class CharVectorUtils {
    public static List<CharVector> splitByWhitespace(CharVector term) {
        int start = 0;
        boolean isPrevWhitespace = true;
        List<CharVector> list = new ArrayList<CharVector>();
        for(int i = 0; i < term.length(); i++) {
            char ch = term.charAt(i);
            if(ch == ' ') {
                if(!isPrevWhitespace) {
                    list.add(new CharVector(term.array(), start + term.start(), i - start, term.isIgnoreCase()));

                }
                start = i + 1;
                isPrevWhitespace = true;
            } else {
                isPrevWhitespace = false;
            }
        }
        if(!isPrevWhitespace) {
            list.add(new CharVector(term.array(), start + term.start(), term.length() - start, term.isIgnoreCase()));
        }
        return list;
    }
}
