package org.fastcatsearch.ir.util;

import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.CharVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by swsong on 2015. 11. 18..
 */
public class DocumentNumberConverter {
    private static Logger logger = LoggerFactory.getLogger(DocumentNumberConverter.class);

    private int[] deleteIdList;

    private int offset;
    private int size;


    public DocumentNumberConverter(BitSet deleteSet) {
        //TODO 변환한다.
    }
    public DocumentNumberConverter(int[] deleteIdList) {
        this(deleteIdList, 0);
    }
    public DocumentNumberConverter(int[] deleteIdList, int offset) {
        this.deleteIdList = deleteIdList;
        this.size = deleteIdList.length;
        this.offset = offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int convert(int docNo) {

        int newDocNo = binSearch(docNo);

        return newDocNo;
    }

    public int binSearch(int docNo){
        if(size == 0) {
            return docNo + offset;
        }

        int left = 0;
        int right = size - 1;
        int mid = -1;

        while(left <= right){
            mid = (left + right) / 2;

            if(docNo == deleteIdList[mid]){
//                found = true;
                return -1;
//                break;
            }else if(docNo > deleteIdList[mid]){
                left = mid + 1;
            }else{
                right = mid - 1;
            }
        }

        mid = right < mid ? right : mid;

//        logger.debug("docNo[{}] mid[{}] newDocNo[{}] ", docNo, mid, docNo - mid);

        if(mid == -1) {
            //그대로.
            return docNo + offset;
        }
        return docNo - mid - 1 + offset;
    }
}
