package org.fastcatsearch.ir.dictionary;

import org.fastcatsearch.ir.io.CharVector;

import java.io.File;

/**
 * Created by swsong on 17. 11. 1..
 */
public class DictionaryDump {


    public static void main(String[] args) {


        if(args.length < 2) {
            System.out.println("Usage: java " + DictionaryDump.class.getSimpleName() + " <TYPE> <DICT FILE>");
            System.exit(1);
        }

        String type = args[0];
        String filepath = args[1];

        File f = new File(filepath);
        if("set".equalsIgnoreCase(type)) {
            SetDictionary dic = new SetDictionary(f, false);
            for(CharVector cv : dic.set()) {
                System.out.println(cv.toString());
            }
        }

    }
}
