/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.cli;
/**
 * CLI환경에서 명령을 내리는 프로그램.
 * 
 * [Usage]java Console 192.168.0.100 8080 과 같이 접속하여 사용한다.
 * 
 * @see ConsoleActionServlet
 */

public class Console {
	
	String[] CMD_USE_COLLECTION = new String[]{"use"};
	
	String currentCollection;
	
	/*
	 * 항상 처음에는 use [collection명]으로 컬렉션을 선택하고, 향후 계속 해당 collection명을 사용해 함께 전달하도록 한다.
	 * 
	 * */
	public static void main(String[] args) {
		
		printLicenseHeader();
		
		if(args.length < 2){
			printUsage();
			System.exit(0);
		}
		
		String host = args[0];
		String port = args[1];
		
		//TODO 1. HttpClient를 사용하여 접속한다.
		
		//TODO 2. System.in을 라인별로 받아들여 command를 전송한다.
		
		//결과는 json형식이며,
		//실제 로직은 ConsoleActionServlet에 있으며 모든 요청은 대기없이 즉시 리턴하도록한다.

		
		
	}

	private static void printLicenseHeader() {

		System.out.println("###########################################");
		System.out.println("# Copyright FastSearch. GPL 2.0 License.");
		System.out.println("# FastcatSearch CLI Tool");
		System.out.println("###########################################");
	}

	private static void printUsage() {
		
		System.err.println("[Usage] java Console [host] [port]");
		
	}
}
