package org.fastcatsearch.ir.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by swsong on 2015. 9. 1..
 */
public class IndexingSchedulePause {
    private static Logger logger = LoggerFactory.getLogger(IndexingSchedulePause.class);

    private String collectionId;
    private FileLock fileLock;
    private File pauseFile;


    public IndexingSchedulePause(String collectionId, String collectionHome) {
        this.collectionId = collectionId;
        pauseFile = new File(collectionHome, "indexing.pause");

        if(pauseFile.exists()) {
            pause();
        }
    }

    public boolean isAvailable() {
        return fileLock == null || !fileLock.isValid();
    }

    public boolean pause() {
        try {
            if(fileLock != null && fileLock.isValid()) {
                //이미 정지중임.
                logger.info("Indexing schedule is already paused : {}", collectionId);
                return true;
            }
            FileChannel channel = new RandomAccessFile(pauseFile, "rw").getChannel();
            fileLock = channel.tryLock();
            logger.info("Indexing schedule paused : {}", collectionId);
            return true;
        } catch (IOException e) {
            System.err.println("Error! Cannot create indexing pause lock file '" + pauseFile.getAbsolutePath() + "'.");
        }
        return false;
    }

    public boolean resume() {
        try {
            if(fileLock != null && fileLock.isValid()) {
                fileLock.release();
                logger.info("Indexing schedule released : {}", collectionId);
            }
            if(pauseFile.exists()) {
                pauseFile.delete();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error! Cannot release indexing pause lock file '" + pauseFile.getAbsolutePath() + "'.");
        }
        return false;
    }

    public static void main(String[] args) {
        IndexingSchedulePause p = new IndexingSchedulePause("a", "/tmp");
        boolean b = p.pause();
        System.out.println("----"+b);
        b = p.resume();
        System.out.println("----"+b);
        b = p.pause();
        System.out.println("----"+b);
        b = p.resume();
        System.out.println("----"+b);
    }
}
