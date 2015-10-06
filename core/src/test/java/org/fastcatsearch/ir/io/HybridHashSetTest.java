package org.fastcatsearch.ir.io;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.apache.lucene.util.BytesRef;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by swsong on 2015. 9. 24..
 */
public class HybridHashSetTest {

    private Random random = new Random(System.currentTimeMillis());

    String characters = "qwertyuiopasdfghjklzxcvbnm1234567890";
    byte[] bytes;
    int keySize = 32;
    @Before
    public void init() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        bytes = new byte[characters.length()];
        for(int i=0;i<characters.length(); i++) {
            bytes[i] = (byte) characters.charAt(i);
//            System.out.println(bytes[i]);
        }
    }

    @Test
    public void testNew() throws FileNotFoundException {
        HybridHashSet set = createSet(10);

        insertEntry(set, new BytesRef("AAA"));
        insertEntry(set, new BytesRef("AAA"));
        insertEntry(set, new BytesRef("BBB"));
        insertEntry(set, new BytesRef("AAA"));
        insertEntry(set, new BytesRef("BBB"));
        insertEntry(set, new BytesRef("BBB"));
        insertEntry(set, new BytesRef("CCC"));
        insertEntry(set, new BytesRef("CCC"));
    }

    private void insertEntry(HybridHashSet set, BytesRef value) {
        boolean r = set.add(value);
        if(r) {
            System.out.println("OK: " + value);
        } else {
            System.out.println("FAIL: " + value);
        }
    }

    @Test
    public void testRandom() {
        int LIMIT = 1000000;
        int bucketSize = 1000000;
//        int LIMIT = 100000;
        HybridHashSet set = createSet(bucketSize);
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            BytesRef key = generateString(keySize);
            set.add(key);
        }
        System.out.println("File Time : " + (System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
//        set.clean();
    }

    @Test
    public void testRandomMemory() {
        int LIMIT = 10000000;
//        int LIMIT = 100000;
        HashSet<BytesRef> set = new HashSet();
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            BytesRef key = generateString(keySize);
            set.add(key);
        }
        System.out.println("Memory Time : " + (System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("Memory Size : " + mem / 1024 / 1024 + " MB");
    }

    @Test
    public void testGenerateString() {
        int LIMIT = 1000000;
//        int LIMIT = 100000;
        int keySize = 32;
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            BytesRef key = generateString(keySize);
//            System.out.println(key.toString());
        }
        System.out.println("Gen String Time : " + (System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
    }

    public BytesRef generateString(int length) {
        byte[] text = new byte[length];
        for (int i = 0; i < length; i++) {
            text[i] = bytes[random.nextInt(bytes.length)];
        }
        return new BytesRef(text);
    }

    @Test
    public void test2() {

        HybridHashSet set = createSet(20);
        insertEntry(set, new BytesRef("a"));
        insertEntry(set, new BytesRef("b"));
        insertEntry(set, new BytesRef("c"));
        insertEntry(set, new BytesRef("d"));
        insertEntry(set, new BytesRef("d"));
        insertEntry(set, new BytesRef("e"));
        insertEntry(set, new BytesRef("f"));
        insertEntry(set, new BytesRef("g"));
        insertEntry(set, new BytesRef("h"));
        insertEntry(set, new BytesRef("h"));
        insertEntry(set, new BytesRef("i"));
        insertEntry(set, new BytesRef("b"));
        insertEntry(set, new BytesRef("a"));
    }

    public HybridHashSet createSet(int bucketSize) {
        File f = new File("/tmp");
//        int bucketSize = 10000000;
        int limit = 100;
        return new HybridHashSet(limit, bucketSize, keySize);
    }
}
