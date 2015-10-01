package org.fastcatsearch.ir.io;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.UUID;

/**
 * Created by swsong on 2015. 9. 24..
 */
public class FileBaseHashSetTest {

    private Random random = new Random(System.currentTimeMillis());

    @Before
    public void init() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//        root.setLevel(Level.INFO);
    }

    @Test
    public void testNew() throws FileNotFoundException {
        File f = new File("/tmp/a");
        int bucketSize = 10;
        int keySize = 3;
        FileBaseHashSet set = new FileBaseHashSet(f, bucketSize, keySize);

        set.put("AAA");
        set.put("AAA");
        set.put("BBB");
        set.put("AAA");
        set.put("BBB");
        set.put("BBB");
        set.put("CCC");
        set.put("CCC");
    }

    @Test
    public void test() {
        int LIMIT = 7;
        File f = new File("/tmp/random.set");
        int bucketSize = 5;
        int keySize = 10;
        FileBaseHashSet set = new FileBaseHashSet(f, bucketSize, keySize);
        System.out.println("size1 : " + f.length());
        for (int i = 0; i < LIMIT; i++) {
            String key = makeString();
            set.put(key);
        }
        System.out.println("size2 : " + f.length());
    }

    private String makeString() {
        String key = UUID.randomUUID().toString();
        return key;
    }
}
