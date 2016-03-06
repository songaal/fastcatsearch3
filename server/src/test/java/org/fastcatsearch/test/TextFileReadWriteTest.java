package org.fastcatsearch.test;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.io.ByteRefArrayOutputStream;
import org.fastcatsearch.ir.io.BytesDataInput;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.junit.Test;

import java.io.*;
import java.util.zip.*;

/**
 * Created by swsong on 2016. 3. 5..
 */
public class TextFileReadWriteTest {

    @Test
    public void testReadingWhileWriting() throws IOException, InterruptedException {

        final File f = File.createTempFile("aaa", "");

        final int LIMIT = 50;
        System.out.println(f.getAbsolutePath());
        Thread reader = new Thread() {

            @Override
            public void run() {
                System.out.println("Reader start!");
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

                    int i = 0;
                    while(true) {
                        String line = reader.readLine();
                        if(line == null) {
                            if(i == LIMIT) {
                                break;
                            }
                            Thread.sleep(300);
                            System.out.println("reading retry..");
                        } else {
                            System.out.println("read " + line);
                            i++;
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Reader end!");
            }
        };

        Thread writer = new Thread() {
            @Override
            public void run() {
                System.out.println("Writer start!");
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));

                    for (int i = 0; i < LIMIT; i++) {
                        String str = "line-" + i;
                        str = "{\"ID\":\"64269\",\"PRODUCTCODE\":\"64269\",\"SHOPCODE\":\"dna\",\"PRODUCTNAME\":\"630 (프레스캇)\",\"PRODUCTMAKER\":\"인텔\",\"MAKERKEYWORD\":\"\",\"PRODUCTBRAND\":\"펜티엄4\",\"BRANDKEYWORD\":\"펜티엄4,  펜티엄\",\"PRODUCTMODEL\":\"\",\"MODELWEIGHT\":\"\",\"SIMPLEDESCRIPTION\":\"인텔(소켓775)|64(32)비트|싱글 코어|쓰레드 2개|3.0GHz|2MB\",\"ADDDESCRIPTION\":\"\",\"CMDESCRIPTION\":\"\",\"LOWESTPRICE\":\"703\",\"MOBILEPRICE\":\"2850\",\"AVERAGEPRICE\":\"1352\",\"SHOPQUANTITY\":\"2\",\"DISCONTINUED\":\"N\",\"CATEGORYCODE1\":\"861\",\"CATEGORYCODE2\":\"873\",\"CATEGORYCODE3\":\"959\",\"CATEGORYCODE4\":\"0\",\"CATEGORYKEYWORD\":\"PC 주요부품,CPU\",\"CATEGORYWEIGHT\":\"CPU\",\"REGISTERDATE\":\"20050811\",\"MANUFACTUREDATE\":\"20050601\",\"MODIFYDATE\":\"20151217\",\"MANAGERKEYWORD\":\"\",\"PROMOTIONPRICE\":\"0\",\"BUNDLENAME\":\"벌크\",\"BUNDLEDISPLAYSEQUENCE\":\"0\",\"SELECTYN\":\"Y\",\"PRICECOMPARESERVICEYN\":\"Y\",\"OPTIONCODEDATAS\":\"소켓 구분^인텔(소켓775), 연산 체계^64(32), 코어 형태^싱글 코어, 동작 속도^3.0 ~ 3.49, L2 캐시 메모리^2MB, 쓰레드 형태^쓰레드 2개\",\"MAKERCODE\":\"3156\",\"BRANDCODE\":\"534\",\"MOVIEYN\":\"N\",\"PRICELOCKYN\":\"N\",\"STVIEWBIT\":\"c2,c3\",\"NATTRIBUTEVALUESEQ\":\"224,245,256,270,283,285,293,300,308,324,346,355,359,89512,90401,90408,91815,146726\"}";
                        writer.write(str);
                        writer.write("\n");
                        writer.flush();
                        Thread.sleep(300);
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Writer end! size = " + f.length());
            }
        };

        reader.start();
        Thread.sleep(2000);
        writer.start();

        reader.join();
        writer.join();
    }

    @Test
    public void testCompressedReadingWhileWriting() throws IOException, InterruptedException {
        final Deflater compressor = new Deflater(Deflater.BEST_SPEED);
        final File f = File.createTempFile("aaa", "");

        final int LIMIT = 50;
        System.out.println(f.getAbsolutePath());


        Thread writer = new Thread() {
            @Override
            public void run() {
                System.out.println("Writer start!");
                byte[] workingBuffer = new byte[1024];
                ByteRefArrayOutputStream baos = new ByteRefArrayOutputStream(1024);
                try {
                    FileOutputStream os = new FileOutputStream(f);

                    for (int i = 0; i < LIMIT; i++) {
                        String str = "line-"+i;
                        str = "{\"ID\":\"64269\",\"PRODUCTCODE\":\"64269\",\"SHOPCODE\":\"dna\",\"PRODUCTNAME\":\"630 (프레스캇)\",\"PRODUCTMAKER\":\"인텔\",\"MAKERKEYWORD\":\"\",\"PRODUCTBRAND\":\"펜티엄4\",\"BRANDKEYWORD\":\"펜티엄4,  펜티엄\",\"PRODUCTMODEL\":\"\",\"MODELWEIGHT\":\"\",\"SIMPLEDESCRIPTION\":\"인텔(소켓775)|64(32)비트|싱글 코어|쓰레드 2개|3.0GHz|2MB\",\"ADDDESCRIPTION\":\"\",\"CMDESCRIPTION\":\"\",\"LOWESTPRICE\":\"703\",\"MOBILEPRICE\":\"2850\",\"AVERAGEPRICE\":\"1352\",\"SHOPQUANTITY\":\"2\",\"DISCONTINUED\":\"N\",\"CATEGORYCODE1\":\"861\",\"CATEGORYCODE2\":\"873\",\"CATEGORYCODE3\":\"959\",\"CATEGORYCODE4\":\"0\",\"CATEGORYKEYWORD\":\"PC 주요부품,CPU\",\"CATEGORYWEIGHT\":\"CPU\",\"REGISTERDATE\":\"20050811\",\"MANUFACTUREDATE\":\"20050601\",\"MODIFYDATE\":\"20151217\",\"MANAGERKEYWORD\":\"\",\"PROMOTIONPRICE\":\"0\",\"BUNDLENAME\":\"벌크\",\"BUNDLEDISPLAYSEQUENCE\":\"0\",\"SELECTYN\":\"Y\",\"PRICECOMPARESERVICEYN\":\"Y\",\"OPTIONCODEDATAS\":\"소켓 구분^인텔(소켓775), 연산 체계^64(32), 코어 형태^싱글 코어, 동작 속도^3.0 ~ 3.49, L2 캐시 메모리^2MB, 쓰레드 형태^쓰레드 2개\",\"MAKERCODE\":\"3156\",\"BRANDCODE\":\"534\",\"MOVIEYN\":\"N\",\"PRICELOCKYN\":\"N\",\"STVIEWBIT\":\"c2,c3\",\"NATTRIBUTEVALUESEQ\":\"224,245,256,270,283,285,293,300,308,324,346,355,359,89512,90401,90408,91815,146726\"}";
                        byte[] data = str.getBytes();
                        compressor.reset();
                        compressor.setInput(data, 0, data.length);
                        compressor.finish();

                        baos.reset();
                        while (!compressor.finished()) {
                            int count = compressor.deflate(workingBuffer);
                            baos.write(workingBuffer, 0, count);
                        }

                        writeInt(os, baos.length());
                        os.write(baos.array(), 0, baos.length());
                        os.flush();
                        Thread.sleep(300);
                    }
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Writer end! size = " + f.length());
            }
        };

        Thread reader = new Thread() {

            byte[] data = new byte[1000];
            @Override
            public void run() {
                System.out.println("Reader start!");
                ByteRefArrayOutputStream inflaterOutput = new ByteRefArrayOutputStream(1024);
                byte[] workingBuffer = new byte[1024];
                try {
                    FileInputStream is = new FileInputStream(f);
                    int i = 0;
                    while(true) {
                        if(is.available() == 0) {
                            if (i == LIMIT) {
                                break;
                            }
                            Thread.sleep(300);
                        } else if(is.available() < 4) {
                            Thread.sleep(300);
                            System.out.println("reading retry..");
                        } else {
                            int size = readInt(is);

                            InflaterInputStream decompressInputStream = null;
                            inflaterOutput.reset();
                            int count = -1;
                            try {
                                BoundedInputStream boundedInputStream = new BoundedInputStream(is, size);
                                boundedInputStream.setPropagateClose(false);//하위 docInput 를 닫지않는다.
                                decompressInputStream = new InflaterInputStream(boundedInputStream, new Inflater(), 512);
                                while ((count = decompressInputStream.read(workingBuffer)) != -1) {
                                    inflaterOutput.write(workingBuffer, 0, count);
                                }
                            } finally {
                                decompressInputStream.close();
                            }

                            BytesRef bytesRef = inflaterOutput.getBytesRef();
                            String str = new String(bytesRef.bytes, 0, bytesRef.length);

                            System.out.println("read " + str);
                            i++;
                        }

                    }
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Reader end!");
            }
        };

        reader.start();
        Thread.sleep(2000);
        writer.start();

        reader.join();
        writer.join();
    }

    private void writeInt(OutputStream os, int v) throws IOException {
        os.write((v >>> 24) & 0xFF);
        os.write((v >>> 16) & 0xFF);
        os.write((v >>> 8) & 0xFF);
        os.write((v >>> 0) & 0xFF);
    }

    private int readInt(InputStream is) throws IOException {
        return ((is.read() & 0xFF) << 24) | ((is.read() & 0xFF) << 16) | ((is.read() & 0xFF) << 8) | (is.read() & 0xFF);
    }
}
