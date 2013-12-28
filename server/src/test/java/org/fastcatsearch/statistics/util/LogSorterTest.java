package org.fastcatsearch.statistics.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Random;

import org.junit.Test;

/*
 * 한개의 대용량 로그 파일을 COUNT내림차순으로 정렬한다.
 * */
public class LogSorterTest {

	String encoding = "utf-8";
	int MAX_LINE = 100000;
	char[] key = new char[16];
	
	@Test
	public void testSort() throws IOException {
		long st = System.currentTimeMillis();
		int runKeySize = 10 * 10000;
		File workDir = new File("./tmp-" + System.currentTimeMillis());
		InputStream is = getInputStream();
		OutputStream os = System.out;
		Comparator<KeyCountRunEntry> comparator = new Comparator<KeyCountRunEntry>() {

			@Override
			public int compare(KeyCountRunEntry o1, KeyCountRunEntry o2) {
				if (o1 == null && o2 == null) {
					return 0;
				} else if (o1 == null) {
					return -1;
				} else if (o2 == null) {
					return 1;
				}
				
				// 내림차순 정렬.
				return o2.getCount() - o1.getCount();
			}

		};

		LogSorter logSorter = new LogSorter(is, encoding, runKeySize);
		logSorter.sort(os, comparator, workDir);
		
		System.out.println("time : "+(System.currentTimeMillis() - st) / (float)(1000 * 60));
	}

	@Test
	public void testRead() throws IOException {
		InputStream is = getInputStream();
		String line = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}

	}

	private InputStream getInputStream() {
		return new InputStream() {

			
			int n;
			int remain;
			byte[] lineBytes;
			Random r = new Random(System.currentTimeMillis());
			

			@Override
			public int read() throws IOException {
				if (remain == 0) {
					fill();
				}

				return readInternal();
			}

			private int readInternal() {
				try {
					return lineBytes[lineBytes.length - remain--];
				} catch (Exception e) {
					return -1;
				}
			}

			private void fill() throws UnsupportedEncodingException {
				if (n++ >= MAX_LINE) {
					return;
				}

				for (int i = 0; i < key.length; i++) {
					key[i] = (char) (r.nextInt(25) + 65);
				}
				String line = new String(key) + "\t" + r.nextInt(10000) + "\n";
				lineBytes = line.getBytes(encoding);
				remain = lineBytes.length;
			}

		};
	}
}
