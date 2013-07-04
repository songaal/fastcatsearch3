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

package org.fastcatsearch.servlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.servlet.WebServiceHttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 데이터를 받아서 csv파일을 만들어 브라우저로 write한다.
 * 
 * @author swsong
 * 
 */
public class CSVMakeServlet extends WebServiceHttpServlet {

	private static final long serialVersionUID = 963640535656747847L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String filename = request.getParameter("filename");
		if (filename != null) {
			filename = new String(filename.getBytes(),"iso8859_1");
		}
		String data = request.getParameter("data");
//		if (data != null) {
//			data = new String(data.getBytes(),"iso8859_1");
//		}
		ServletOutputStream out = null;
		ByteArrayInputStream byteArrayInputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		try {
			response.setContentType("text/csv");
			String disposition = "attachment; fileName=" + filename;
			response.setHeader("Content-Disposition", disposition);
			out = response.getOutputStream();
			byte[] blobData = data.getBytes("euc-kr");

			byteArrayInputStream = new ByteArrayInputStream(blobData);
			bufferedOutputStream = new BufferedOutputStream(out);
			int length = blobData.length;
			response.setContentLength(length);
			byte[] buff = new byte[(1024 * 1024) * 2];
			int bytesRead;
			while (-1 != (bytesRead = byteArrayInputStream.read(buff, 0,buff.length))) {
				bufferedOutputStream.write(buff, 0, bytesRead);
				bufferedOutputStream.flush();
			}
			out.flush();
			out.close();
		} catch (Exception e) {
			throw new ServletException("CSV파일 생성시 에러 발생",e);
		} finally {
			if (out != null)
				out.close();
			if (byteArrayInputStream != null) {
				byteArrayInputStream.close();
			}
			if (bufferedOutputStream != null) {
				bufferedOutputStream.close();
			}
		}
		
	}//doGet

}
