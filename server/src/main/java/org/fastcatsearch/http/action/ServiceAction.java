package org.fastcatsearch.http.action;

import java.io.Writer;

import org.fastcatsearch.util.JSONPResponseWriter;
import org.fastcatsearch.util.JSONResponseWriter;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.XMLResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class ServiceAction extends HttpAction {
	public static final String DEFAULT_ROOT_ELEMENT = "response";
	public static final String DEFAULT_CHARSET = "utf-8";
	public static enum Type { json, xml, jsonp, html, text };
	
	public ServiceAction(){ 
	}
	
	protected void writeHeader(ActionResponse response) {
		writeHeader(response, DEFAULT_CHARSET);
	}
	protected void writeHeader(ActionResponse response, String responseCharset) {
		response.setStatus(HttpResponseStatus.OK);
		logger.debug("resultType > {}",resultType);
		if (resultType == Type.json) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == Type.jsonp) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == Type.xml) {
			response.setContentType("text/xml; charset=" + responseCharset);
		} else if (resultType == Type.html) {
			response.setContentType("text/html; charset=" + responseCharset);
		} else if (resultType == Type.text) {
			response.setContentType("text/plain; charset=" + responseCharset);
		} else {
			response.setContentType("application/json; charset=" + responseCharset);
		}
	}
	protected ResponseWriter getDefaultResponseWriter(Writer writer){
		return getResponseWriter(writer, DEFAULT_ROOT_ELEMENT, true, null);
	}
	
	protected ResponseWriter getResponseWriter(Writer writer, String rootElement, boolean isBeautify, String jsonCallback) {
		ResponseWriter resultWriter = null;
		if (resultType == Type.json) {
			resultWriter = new JSONResponseWriter(writer, isBeautify);
		} else if (resultType == Type.jsonp) {
			resultWriter = new JSONPResponseWriter(writer, jsonCallback, isBeautify);
		} else if (resultType == Type.xml) {
			resultWriter = new XMLResponseWriter(writer, rootElement, isBeautify);
		}
		return resultWriter;
	}
	
	protected class PageDivider {
		private int totalRecord;
		private int totalPage;
		private int rowSize;
		private int pageSize;
		public PageDivider(int rowSize, int pageSize) {
			this.rowSize = rowSize;
			this.pageSize = pageSize;
		}
		public void setTotal(int totalRecord) {
			this.totalRecord=totalRecord;
			this.totalPage = totalRecord / rowSize + 1;
		}
		public int getTotalRecord() {
			return totalRecord;
		}
		public int totalPage() {
			return totalPage;
		}
		public int currentPage(int rowNumber) {
			return (int)Math.floor((rowNumber+rowSize)/rowSize);
		}
		private int[] rowMargine(int pageNum) {
			int st,ed;
			st=(pageNum-1) * rowSize + 1;
			if(st > totalRecord) st=totalRecord-rowSize+1;
			if(st < 0) { st =0; }
			ed = st + rowSize - 1;
			if(ed > totalRecord) {
				ed = totalRecord;
			}
			return new int[]{st,ed};
		}
		public int rowStarts(int pageNum) { return rowMargine(pageNum)[0]; }
		public int rowFinish(int pageNum) { return rowMargine(pageNum)[1]; }
		public int rowSize () { return this.rowSize; }
		public int pageSize () { return this.pageSize; }
		public int[] pageMargine(int pageNum) {
			int st,ed;
			st = (pageNum-1) - ((pageNum-1) % pageSize) + 1;
			ed = st + (pageSize-1);
			if(ed > totalPage) {
				ed = totalPage;
			}
			return new int[] {st,ed};
		}
		public int pageStarts(int pageNum) { return pageMargine(pageNum)[0]; }
		public int pageFinish(int pageNum) { return pageMargine(pageNum)[1]; }
	}
}
