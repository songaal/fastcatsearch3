package org.fastcatsearch.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class PingTest {
	
	public static void main(String[] args) {
		PingTest pingTest = new PingTest();
		if(args == null || args.length == 0) {
			System.out.println("Usage: java "  + pingTest.getClass().getName() + " [host:port] ...");
			System.exit(1);
		}
		for(String host : args) {
			String[] el = host.split(":");
			if(el.length != 2) {
				continue;
			}
			String address = el[0].trim();
			int port = Integer.parseInt(el[1].trim());
			
			System.out.print("Check " + host + "...");
			String error = pingTest.connectionTest(address, port);
			
			if(error == null) {
				System.out.println("OK");
				
			} else {
				System.out.println("FAIL due to " + error);
			}
		}
	}
	
	/*
	 * @return 성공이면 null, 에러이면 에러메시지.
	 */
	private String connectionTest(String address, int port) {
		Socket socket = null;
		try {
			SocketAddress sockaddr = new InetSocketAddress(address, port);
			socket = new Socket();
            socket.connect(sockaddr, 2000);
            return null;
        } catch (Throwable e){ 
        	return e.getMessage();
        } finally {
        	if(socket != null) {
        		try {
					socket.close();
				} catch (IOException e) {
				}
        	}
        }
	}
}
