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
import java.util.UUID;

/**
 * Created by swsong on 2015. 9. 24..
 */
public class MappedFileBaseHashSetTest {

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
        MappedFileBaseHashSet set = new MappedFileBaseHashSet(f, bucketSize, keySize);

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
    public void testRandom() {
//        int LIMIT = 3000000;
//        int bucketSize = 1000000;
        int LIMIT = 100000;
        int bucketSize = 10000;
        File f = new File("/tmp/random.set");
        int keySize = 36;
        MappedFileBaseHashSet set = new MappedFileBaseHashSet(f, bucketSize, keySize);
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            String key = generateString(keySize);
            set.put(key);
        }
        System.out.println("File Time : " + (System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
        System.out.println("File Size : " + f.length() /1024 / 1024 +" MB");
        set.clean();
    }

    @Test
    public void testRandomMemory() {
//        int LIMIT = 3000000;
        int LIMIT = 100000;
        HashSet<String> set = new HashSet();
        int keySize = 36;
        long st = System.nanoTime();
        for (int i = 0; i < LIMIT; i++) {
            String key = generateString(keySize);
            set.add(key);
        }
        System.out.println("Memory Time : " + (System.nanoTime() - st) / 1000 / 1000 / 1000.0 + "s");
    }

    private String makeString() {
        //size는 36. ex) 2d515f46-c9b5-4c05-b019-8dfb19e62f85
        String key = UUID.randomUUID().toString();
        return key;
    }

    String characters = "qwertyuiopasdfghjklzxcvbnm1234567890";
    public String generateString(int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }

    @Test
    public void test2() {
        File f = new File("/tmp/2.set");
        int bucketSize = 5;
        int keySize = 1;
        MappedFileBaseHashSet set = new MappedFileBaseHashSet(f, bucketSize, keySize);
        System.out.println("size1 : " + f.length());
        set.put("a");
        set.put("b");
        set.put("c");
        set.put("d");
        set.put("d");
        set.put("e");
        set.put("f");
        set.put("g");
        set.put("h");
        set.put("h");
        set.put("i");
        set.put("b");
        set.put("a");
        System.out.println("size2 : " + f.length());
    }
}
