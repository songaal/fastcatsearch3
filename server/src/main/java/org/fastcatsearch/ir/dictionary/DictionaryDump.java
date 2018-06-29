package org.fastcatsearch.ir.dictionary;

import org.fastcatsearch.ir.io.CharVector;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

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
        } else if("map".equalsIgnoreCase(type)) {
            MapDictionary dic = new MapDictionary(f, false);
            Iterator<Map.Entry<CharVector, CharVector[]>> iterator = dic.map().entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<CharVector, CharVector[]> entry = iterator.next();
                System.out.print(entry.getKey().toString());
                System.out.print("\t");
                CharVector[] values = entry.getValue();
                for (int i=0;i<values.length; i++) {
                    if(i > 0) {
                        System.out.print(",");
                    }
                    System.out.print(values[i].toString());
                }
                System.out.println();

            }
        } else if ("space".equalsIgnoreCase(type)) {
            SpaceDictionary dic = new SpaceDictionary(f, false);
            Iterator<Map.Entry<CharVector, CharVector[]>> iterator = dic.map().entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<CharVector, CharVector[]> entry = iterator.next();
                CharVector[] values = entry.getValue();
                for (int i=0;i<values.length; i++) {
                    if(i > 0) {
                        System.out.print(" ");
                    }
                    System.out.print(values[i].toString());
                }
                System.out.println();

            }
        }



    }
}
