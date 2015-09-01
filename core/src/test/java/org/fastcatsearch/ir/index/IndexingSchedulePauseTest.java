package org.fastcatsearch.ir.index;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by swsong on 2015. 9. 1..
 */
public class IndexingSchedulePauseTest {

    @Test
    public void testSequence() {
        IndexingSchedulePause p = new IndexingSchedulePause("col1", "/tmp");
        assertTrue(p.pause());
        assertTrue(p.resume());
        assertTrue(p.pause());
        assertTrue(p.resume());
    }

    @Test
    public void testDuplicateRequest() {
        IndexingSchedulePause p = new IndexingSchedulePause("col1", "/tmp");
        assertTrue(p.pause());
        assertTrue(p.pause());
        assertTrue(p.resume());
        assertTrue(p.resume());
    }

    @Test
    public void testValidation() {
        IndexingSchedulePause p = new IndexingSchedulePause("col1", "/tmp");
        assertEquals(true, p.isAvailable());
        assertTrue(p.pause());
        assertEquals(false, p.isAvailable());
        assertTrue(p.resume());
        assertEquals(true, p.isAvailable());
        assertTrue(p.pause());
        assertEquals(false, p.isAvailable());
        assertTrue(p.resume());
        assertEquals(true, p.isAvailable());

        assertTrue(p.pause());
        assertTrue(p.pause());
        assertTrue(p.pause());
        assertEquals(false, p.isAvailable());
        assertTrue(p.resume());
        assertTrue(p.resume());
        assertTrue(p.resume());
        assertEquals(true, p.isAvailable());
    }
}
