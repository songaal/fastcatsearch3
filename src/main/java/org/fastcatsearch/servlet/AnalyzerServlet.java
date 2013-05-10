package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.CharsRef;
import org.fastcatsearch.common.DynamicClassLoader;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.settings.IRSettings;
import org.fastcatsearch.util.ResultStringer;

public class AnalyzerServlet extends WebServiceHttpServlet {
	
	private static final long serialVersionUID = -1112551982298988153L;

	public AnalyzerServlet(int resultType) {
		super(resultType);
	}


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request,response);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String[] uriArray = getURIArray(request);
		
		String resultTypeStr = null;
		String analyzerName =  null;
		
		if(uriArray!=null) {
			if(uriArray.length == 4) {
				resultTypeStr = uriArray[uriArray.length-1];
				analyzerName =  uriArray[uriArray.length-2];
			} else if(uriArray.length == 3) {
				resultTypeStr = "json";
				analyzerName =  uriArray[uriArray.length-1];
			}
		}
		
		if(resultTypeStr!=null && analyzerName!=null) {
			String collectionId = request.getParameter("collection");
			AnalyzerPool analyzerPool = IRSettings.getAnalyzerPoolManager().getPool(collectionId, analyzerName);
			
			Analyzer analyzer = analyzerPool.getFromPool();
			
			String responseCharset = getParameter(request, "responseCharset", "UTF-8");
			
			String jsonCallback = request.getParameter("jsoncallback");
			
			String keyword = request.getParameter("keyword");
			
			ResultStringer rStringer = super.getResultStringer("analyze-result", true, jsonCallback);
			
			try {
				rStringer.object()
					.key("keyword").value(keyword)
					.key("analyzer").value(analyzerName)
					.key("token")
					.array("item");
				
				TokenStream tokenStream = analyzer.tokenStream("", new StringReader(keyword));
				tokenStream.reset();
				CharsRefTermAttribute termAttribute = null;
				PositionIncrementAttribute positionAttribute = null;
				if(tokenStream.hasAttribute(CharsRefTermAttribute.class)){
					termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
				}
				if(tokenStream.hasAttribute(PositionIncrementAttribute.class)){
					positionAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
				}
				CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
				
				while(tokenStream.incrementToken()){
					CharVector key = null;
					if(termAttribute != null){
						CharsRef charRef = termAttribute.charsRef();
						char[] buffer = new char[charRef.length()];
						System.arraycopy(charRef.chars, charRef.offset, buffer, 0, charRef.length);
						key = new CharVector(buffer, 0, buffer.length);
					}else{
						key = new CharVector(charTermAttribute.buffer(), 0, charTermAttribute.length());
					}
					key.toUpperCase();
					rStringer.value(key);
				}
				
				rStringer.endArray()
				.endObject();
				
				super.writeHeader(response, rStringer, responseCharset);
				
				PrintWriter writer = response.getWriter();
				
				writer.write(rStringer.toString());
				
				writer.close();
				
			}catch(Exception e){
				throw new ServletException(e);
			} finally {
				analyzerPool.releaseToPool(analyzer);
			}
		}
	}
}
