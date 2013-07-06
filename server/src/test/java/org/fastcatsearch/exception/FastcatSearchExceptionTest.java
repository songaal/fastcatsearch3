package org.fastcatsearch.exception;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class FastcatSearchExceptionTest {

	@Test
	public void test() {
		FastcatSearchException e = new FastcatSearchException("ERR-00110");
		System.out.println(e);
		System.out.println(e);
		System.out.println(e.getStackTrace()[0].toString());	
	}
	
	@Test
	public void testWrite() throws IOException, ClassNotFoundException {
		IOException ioe = new IOException("file not found");
		System.out.println(ioe.toString());
		System.out.println(ioe.getStackTrace()[0]);
		
		FastcatSearchException fse = new FastcatSearchException("ERR-00200", ioe);
		FastcatSearchException e = new FastcatSearchException("ERR-00110", fse);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(e);
		
		byte[] array = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(array);
		ObjectInputStream ois = new ObjectInputStream(bais);
		FastcatSearchException e2 = (FastcatSearchException) ois.readObject();
		
//		System.out.println(e2.errorCode());
//		System.out.println(e2.getCause());
		System.out.println(e2);
		
		System.out.println("--------");
		e2.printStackTrace();
		
	} 

}
