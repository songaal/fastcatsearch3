package org.fastcatsearch.tools;

import org.fastcatsearch.ir.index.SearchPostingBufferReader;
import org.fastcatsearch.ir.index.SearchPostingReader;
import org.fastcatsearch.ir.io.ByteArrayIndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by swsong on 2015. 11. 17..
 */
public class SearchIndexPrinter {
    private static Logger logger = LoggerFactory.getLogger(SearchIndexPrinter.class);
    private String indexId;
    private File segmentDir;
    public SearchIndexPrinter(String indexId, String dir) {
        this.indexId = indexId;
        this.segmentDir = new File(dir);
    }

    public void print() throws IOException {
        SearchPostingReader reader = new SearchPostingReader(0, indexId, segmentDir);

        int termSize = reader.left();
        logger.debug("Total Term Size = {}", termSize);
        int c = 1;
        while(reader.nextTerm()) {
            SearchPostingBufferReader bufferReader = reader.bufferReader();
            StringBuffer sb = new StringBuffer();
            sb.append(c++);
            sb.append("] ");
            sb.append(reader.term());
            sb.append(" (");
            sb.append(bufferReader.docSize());
            sb.append(" / ");
            sb.append(bufferReader.lastDocNo());
            sb.append(") >>");
            for(int i = 0; i < bufferReader.docSize(); i++) {
                sb.append(" ");
                int docNo = bufferReader.readDocNo();
                int frequency = bufferReader.getFrequency();
                sb.append(bufferReader.isAlive(docNo) ? "" : "(X)");
                sb.append(docNo);
                sb.append(":").append(frequency);
            }
            logger.debug(sb.toString());
        }
        System.out.println("-------------------");
        System.out.println("Total Term Size = " + termSize);
    }

    public static void main(String[] args) throws IOException {
        SearchIndexPrinter printer = new SearchIndexPrinter(args[0], args[1]);
        printer.print();
    }

}
