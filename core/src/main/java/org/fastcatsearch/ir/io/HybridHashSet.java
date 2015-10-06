package org.fastcatsearch.ir.io;

import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Thread-safe하지 않다. 하나의 쓰레드에서 사용해야 한다.
 * Created by swsong on 2015. 10. 6..
 */
public class HybridHashSet {
    private static final Logger logger = LoggerFactory.getLogger(HybridHashSet.class);
    private static String filePrefix = "hash-";
    private static String fileSuffix = ".set";

    private Set<BytesRef> memorySet;
    private MappedFileBaseByteHashSet fileBaseSet;

    //이 수치를 넘어가면 파일베이스로 변경된다.
    private int itemInMemoryLimit;
//    private File setFileDir;
    private int bucketSize;
    private int keySize;
    private int count;
    private boolean isFileBase;

    public HybridHashSet(int itemInMemoryLimit /*, File setFileDir*/, int bucketSize, int keySize) {
        memorySet = new HashSet<BytesRef>();
        this.itemInMemoryLimit = itemInMemoryLimit;
        if(itemInMemoryLimit < 1) {
            throw new RuntimeException("Limit size must be greater than 0");
        }
//        this.setFileDir = setFileDir;
        this.bucketSize = bucketSize;
        this.keySize = keySize;
    }

    private MappedFileBaseByteHashSet fileBaseSet() {
        if(fileBaseSet == null) {
            try {
//                if(!setFileDir.exists()) {
//                    setFileDir.mkdirs();
//                }
                File tempFile =  File.createTempFile(filePrefix, fileSuffix);
                logger.debug("create file hash tempFile > {}", tempFile);
                fileBaseSet =  new MappedFileBaseByteHashSet(tempFile, bucketSize, keySize);
            } catch (IOException e) {
                logger.error("error while create temp set file.", e);
                return null;
            }
        }
        return fileBaseSet;
    }

    public boolean add(BytesRef key) {

        if(isFileBase) {
            if(fileBaseSet().add(key)) {
                count++;
                return true;
            } else {
                return false;
            }
        }

        if (count >= itemInMemoryLimit) {
            isFileBase = true;
            MappedFileBaseByteHashSet fileSet = fileBaseSet();
            for(BytesRef b : memorySet) {
                fileSet.add(b);
            }
            if(fileBaseSet().add(key)) {
                count++;
                return true;
            } else {
                return false;
            }
        } else {
            if(memorySet.add(key)){
                count++;
                return true;
            } else {
                return false;
            }
        }
    }

    public int size() {
        return count;
    }

    public void clean() {
        if(fileBaseSet != null) {
            fileBaseSet.clean();
        }
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }
}
