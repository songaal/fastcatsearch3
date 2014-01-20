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

package org.fastcatsearch.yajsw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class ConfWriter {
	private static ArrayList<File> alf = new ArrayList<File>();
	public static String FS = System.getProperty("file.separator");
	
	public static void main(String[] args) { 
		
		if(args.length < 3){
			System.err.println("ConfWriter의 사용법이 잘못되었습니다. 파라미터의 갯수는 최소3개 이상입니다.");
			System.err.println("Usage: ConfWriter [wrapper.conf파일경로] [APP Home경로] [APP java옵션] [OS사용자명] [사용자패스워드]");
			System.exit(1);
		}
		
		String confPath = "";
		try {
			confPath = new File(args[0]).getCanonicalPath();
		} catch (IOException e1) {
			System.err.println("wrapper.conf파일경로가 이상합니다. 입력값 = "+args[0]);
			System.exit(2);
		}
		
		String app_home = args[1];
		String yajsw_java_options = args[2];
		String username = null;
		String password = null;
		
		if(args.length > 3){
			username = args[3];
			password = args[4];
		}
		if(username == null) username = "";
		if(password == null) password = "";
		
		Properties p  = ConfWriter.getConfig(confPath);
		
		///////////////////////
		//검색엔진home 설정
		///////////////////////
		File homeDir = new File(app_home);
		String homeDirPath = "";
		try {
			homeDirPath = homeDir.getCanonicalPath();
			p.setProperty("wrapper.working.dir", homeDirPath);
		} catch (Exception e) {
			System.err.println("APP Home경로가 이상합니다. 입력값 = "+app_home);
			System.exit(2);
		}
		////////////////////////////////////
		//1. ClassPath 설정
		////////////////////////////////////
		int inx = 1;
		//새 설정값을 입력한다.
		File libDir = new File(homeDir, "lib");
		try {
			ConfWriter.fillAllFiles(libDir);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}
		
		for (int i = 0; i < alf.size(); i++) {
			try {
				String path = alf.get(i).getCanonicalPath();
				p.setProperty("wrapper.java.classpath."+inx, path);
				inx++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		p.setProperty("wrapper.java.classpath."+inx, ".");
//		inx++;
		//기존에 추가로 설정되었던 값들은 지워준다. 
		while (p.getProperty("wrapper.java.classpath."+inx) != null) {
			p.remove("wrapper.java.classpath."+inx);
			inx++;
		}
		
		////////////////////////////////////
		//2. Java Option 설정
		////////////////////////////////////
		inx = 1;
		String[] options = yajsw_java_options.split(" ");
		for (int i = 0; i < options.length; i++) {
			if(options[i] != null && !"".equals(options[i])){
				p.setProperty("wrapper.java.additional."+inx, options[i]);
				inx++;
			}
		}
		//-Dserver.home은 homeDir으로 설정해준다.
		p.setProperty("wrapper.app.parameter."+inx, homeDirPath);
		inx++;
		//-Dderby.stream.error.file은 logs/db.log로 설정해준다.
		p.setProperty("wrapper.java.additional."+inx, "-Dderby.stream.error.file="+homeDirPath+FS+"logs"+FS+"db.log");
		inx++;
		//-Dlogback.configurationFile=conf/logback.xml
		p.setProperty("wrapper.java.additional."+inx, "-Dlogback.configurationFile="+homeDirPath+FS+"conf"+FS+"logback.xml");
		inx++;
		//-Dfile.encoding=UTF-8 Java, Jsp파일의 인코딩을 명시해준다. 윈도우의 경우 이를 설정하지 않으면 euc-kr환경에서 문자가 깨지는 현상발생.
		p.setProperty("wrapper.java.additional."+inx, "-Dfile.encoding=UTF-8");
		inx++;
		//기존에 추가로 셋팅되었던 값들은 지워준다. 
		while (p.getProperty("wrapper.java.additional."+inx) != null) {
			p.remove("wrapper.java.additional."+inx);
			inx++;
		}
		
		p.setProperty("wrapper.app.account", username);
		p.setProperty("wrapper.app.password", password);
		//wrapper로그 파일위치 지정.
		p.setProperty("wrapper.logfile", homeDirPath+FS+"logs"+FS+"wrapper.log");
		
		ConfWriter.storeConfig(p, confPath);
	}

	public static Properties getConfig(String path) {
		Properties props = new Properties();
		try {
			FileInputStream fis = new FileInputStream(path);
			props.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	public static void storeConfig(Properties props, String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			props.store(fos, "4 java service wrapper.");
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void fillAllFiles(File dir) throws Exception {
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].isDirectory()) {
				try {
					fillAllFiles(fs[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				if (fs[i].isFile() && !fs[i].isHidden()) {
					alf.add(fs[i]);
				}
			}
		}
	}

}
