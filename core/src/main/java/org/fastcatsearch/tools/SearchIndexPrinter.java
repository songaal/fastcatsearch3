package org.fastcatsearch.tools;

import org.fastcatsearch.ir.index.SearchPostingBufferReader;
import org.fastcatsearch.ir.index.SearchPostingReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by swsong on 2015. 11. 17..
 */
public class SearchIndexPrinter {
    private String indexId;
    private File segmentDir;
    public SearchIndexPrinter(String indexId, String dir) {
        this.indexId = indexId;
        this.segmentDir = new File(dir);
    }

    public void print() throws IOException {
        SearchPostingReader reader = new SearchPostingReader(0, indexId, segmentDir);

        int termSize = reader.left();
        System.out.println("Total Term Size = " + termSize);
        int c = 1;
        while(reader.nextTerm()) {
            SearchPostingBufferReader bufferReader = reader.bufferReader();
            System.out.print(c++);
            System.out.print("] ");
            System.out.print(reader.term());
            System.out.print(" (");
            System.out.print(bufferReader.docSize());
            System.out.print(" / ");
            System.out.print(bufferReader.lastDocNo());
            System.out.print(") >>");
            for(int i = 0; i < bufferReader.docSize(); i++) {
                System.out.print(" ");
                int docNo = bufferReader.readDocNo();
                System.out.print(bufferReader.isAlive(docNo) ? "" : "(X)");
                System.out.print(docNo);
            }
            System.out.println();
        }
        System.out.println("-------------------");
        System.out.println("Total Term Size = " + termSize);
    }

    public static void main(String[] args) throws IOException {
        SearchIndexPrinter printer = new SearchIndexPrinter(args[0], args[1]);
        printer.print();
    }

}
