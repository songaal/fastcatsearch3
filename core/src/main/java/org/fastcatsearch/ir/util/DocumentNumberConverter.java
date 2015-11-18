package org.fastcatsearch.ir.util;

import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.CharVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 삭제문서를 제외하고 순차번호를 만들어주는 컨버터.
 * input : 문서번호
 * output : 새로운 문서번호
 * 중간에 삭제문서존재시 새로운 문서번호는 삭제문서 갯수만큼 앞으로 shift된다.
 * Created by swsong on 2015. 11. 18..
 */
public class DocumentNumberConverter {
    private static Logger logger = LoggerFactory.getLogger(DocumentNumberConverter.class);

    private int[] deleteIdList;

    private int size;

    public DocumentNumberConverter(int[] deleteIdList) {
        this.deleteIdList = deleteIdList;
        this.size = deleteIdList.length;
    }

    public int convert(int docNo) {

        int newDocNo = binSearch(docNo);

        return newDocNo;
    }

    public int binSearch(int docNo){
        if(size == 0) {
            return docNo;
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
            return docNo;
        }
        return docNo - mid - 1;
    }
}
