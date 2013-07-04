package org.fastcatsearch.transport;

import static org.junit.Assert.*;

import org.junit.Test;

public class TransportOptionTest {
	@Test
	public void testType(){
		byte type = 0;
		type = TransportOption.setTypeMessage(type);
		assertTrue(TransportOption.isTypeMessage(type));
		System.out.println("Message Type = "+type);
		type = TransportOption.setTypeFile(type);
		assertTrue(TransportOption.isTypeFile(type));
		System.out.println("File Type = "+type);
		
		type = TransportOption.setTypeMessage(type);
		assertTrue(TransportOption.isTypeMessage(type));
		System.out.println("Message Type = "+type);
		type = TransportOption.setTypeFile(type);
		assertTrue(TransportOption.isTypeFile(type));
		System.out.println("File Type = "+type);
		
		
	}
	
	@Test
	public void testStatus(){
		byte status = 0;
		status = TransportOption.setRequest(status);
		assertTrue(TransportOption.isRequest(status));
		System.out.println("Request Status = "+status);
		
		status = TransportOption.setError(status);
		assertTrue(TransportOption.isError(status));
		System.out.println("Error Status = "+status);
		
		status = TransportOption.setResponse(status);
		assertTrue(TransportOption.isResponse(status));
		System.out.println("Response Status = "+status);
	}
	
	@Test
	public void testStatusValue(){
		byte status = 0;
		status = TransportOption.setRequest(status);
		assertTrue(TransportOption.isRequest(status));
		System.out.println("Request Status = "+status);
		
		byte status2 = 0;
		status2 = TransportOption.setError(status2);
		assertTrue(TransportOption.isError(status2));
		System.out.println("Error Status = "+status2);
		
		byte status3 = 0;
		status3 = TransportOption.setResponse(status3);
		assertTrue(TransportOption.isResponse(status3));
		System.out.println("Response Status = "+status3);
		
		byte status4 = 0;
		status4 = TransportOption.setErrorResponse(status4);
		assertTrue(TransportOption.isError(status4));
		assertTrue(TransportOption.isResponse(status4));
		System.out.println("Error & Response Status = "+status4);
		
		byte status5 = 0;
		status5 = TransportOption.setError(status5);
		assertTrue(TransportOption.isError(status5));
		status5 = TransportOption.setRequest(status5);
		assertTrue(TransportOption.isRequest(status5));
		System.out.println("Error & Request Status = "+status5);
		
	}
}
