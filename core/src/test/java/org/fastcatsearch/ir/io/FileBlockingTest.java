package org.fastcatsearch.ir.io;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FileBlockingTest extends TestCase {

    public void test1() throws IOException, InterruptedException {

        File f = new File("/Users/swsong/Downloads/Microsoft_Office_2016_15.41.17120500_Installer.pkg");
        long fileSize = f.length();
        RandomAccessFile file = new RandomAccessFile(f, "r");
        final FileChannel channel = file.getChannel();

        int THREADS = 100;
        final int LOOP = 1000;
        final int bufferSize = 8 * 1024 * 1024;
        final int fileLimit = (int) (fileSize - bufferSize);

        List<Thread> list = new ArrayList();
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        ByteBuffer byteBuf = ByteBuffer.allocate(bufferSize);
                        byteBuf.flip();
                        Random r = new Random();
                        for (int k = 0; k < LOOP; k++) {
                            int nRead = 0;
                            int toRead = bufferSize;
                            byteBuf.clear();
                            byteBuf.limit(toRead);
                            int pos = r.nextInt(fileLimit);
                            long st = System.nanoTime();
                            while(nRead < toRead) {
                                int i = channel.read(byteBuf, pos);
                                pos += i;
                                nRead += i;
//                                System.out.println(nRead);
                            }
                            long elapsed = System.nanoTime() - st;
                            if (elapsed > 1000000000) {
                                System.out.println("Too Long!!! ");
                                System.out.println(">>>>>>>>>>>> " + Thread.currentThread().getId() + "-Thread read.." + nRead + "byte. k=" + k + ", elapsed="+(elapsed/1000000.0) + "ms");
                            }
//                            if (k % 10000 == 0) {
//                                System.out.println(Thread.currentThread().getId() + "-Thread read.." + nRead + "byte. k=" + k + ", elapsed="+(elapsed));
//                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println("------" + Thread.currentThread().getId() + "-Thread Finished.");

                }
            };
            list.add(t);
            t.start();
        }
        for (Thread t : list) {
            t.join();
        }


    }
}
