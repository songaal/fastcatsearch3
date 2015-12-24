package org.fastcatsearch.ir.util;

import org.fastcatsearch.ir.io.CharVector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swsong on 2015. 12. 23..
 */
public class CharVectorUtilsTest {

    @Test
    public void testSplit() {
        CharVector term = new CharVector("    ab c  def asf keie 123   ");
        List<CharVector> list = CharVectorUtils.splitByWhitespace(term);
        for(CharVector cv : list) {
            System.out.println("'" + cv + "'");
        }
    }

}
