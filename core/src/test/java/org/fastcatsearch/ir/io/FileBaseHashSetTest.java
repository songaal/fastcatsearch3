package org.fastcatsearch.ir.io;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by swsong on 2015. 9. 24..
 */
public class FileBaseHashSetTest {

    @Test
    public void testNew() throws FileNotFoundException {
        File f = new File("/tmp/a");
        int bucketSize= 10;
        int keySize = 2;
        FileBaseHashSet set = new FileBaseHashSet(f, bucketSize, keySize);

    }
}
