package org.fastcatsearch.ir.io;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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
public class MappedFileBaseCharHashSetTest {

    private Random random = new Random(System.currentTimeMillis());

    @Before
    public void init() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }

    @Test
    public void testNew() throws FileNotFoundException {
        File f = new File("/tmp/a");
        int bucketSize = 10;
        int keySize = 3;
        MappedFileBaseCharHashSet set = new MappedFileBaseCharHashSet(f, bucketSize, keySize);

        insertEntry(set, "AAA");
        insertEntry(set, "AAA");
        insertEntry(set, "BBB");
        insertEntry(set, "AAA");
        insertEntry(set, "BBB");
        insertEntry(set, "BBB");
        insertEntry(set, "CCC");
        insertEntry(set, "CCC");
    }

    private void insertEntry(MappedFileBaseCharHashSet set, String value) {
        boolean r = set.add(value);
        if(r) {
            System.out.println("OK: " + value);
        } else {
            System.out.println("FAIL: " + value);
        }
    }

    @Test
    public void testRandom() {
        int LIMIT = 3000000;
        int bucketSize = 1000000;
//        int LIMIT = 100000;
//        int bucketSize = 10000;
        File f = new File("/tmp/random.set");
        int keySize = 36;
        MappedFileBaseCharHashSet set = new MappedFileBaseCharHashSet(f, bucketSize, keySize);
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            String key = generateString(keySize);
            set.add(key);
        }
        System.out.println("File Time : " + (System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
        System.out.println("File Size : " + f.length() / 1024 / 1024 + " MB");
        set.clean();
    }

    @Test
    public void testRandomMemory() {
        int LIMIT = 3000000;
//        int LIMIT = 100000;
        HashSet<String> set = new HashSet();
        int keySize = 36;
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            String key = generateString(keySize);
            set.add(key);
        }
        System.out.println("Memory Time : " + (System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("Memory Size : " + mem / 1024 / 1024 + " MB");
    }

    @Test
    public void testGenerateString() {
        int LIMIT = 3000000;
//        int LIMIT = 100000;
        int keySize = 36;
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            String key = generateString(keySize);
        }
        System.out.println("Gen String Time : " + (System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
    }

    String characters = "qwertyuiopasdfghjklzxcvbnm1234567890";

    public String generateString(int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }

    @Test
    public void test2() {
        File f = new File("/tmp/2.set");
        int bucketSize = 5;
        int keySize = 1;
        MappedFileBaseCharHashSet set = new MappedFileBaseCharHashSet(f, bucketSize, keySize);
        insertEntry(set, "a");
        insertEntry(set, "b");
        insertEntry(set, "c");
        insertEntry(set, "d");
        insertEntry(set, "d");
        insertEntry(set, "e");
        insertEntry(set, "f");
        insertEntry(set, "g");
        insertEntry(set, "h");
        insertEntry(set, "h");
        insertEntry(set, "i");
        insertEntry(set, "b");
        insertEntry(set, "a");
        System.out.println("size2 : " + f.length());
    }
}
