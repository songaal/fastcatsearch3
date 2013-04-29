package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.ir.analysis.Tokenizer;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerServlet extends WebServiceHttpServlet {
	
	private static final long serialVersionUID = -1112551982298988153L;
	private static final Logger logger = LoggerFactory.getLogger(AnalyzerServlet.class);

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
		
		IRService irService = IRService.getInstance();
		
		String[][] analyzerArray = irService.getTokenizers();
		
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
		
			for(String[] analyzer : analyzerArray) {
				
				if(analyzerName.equalsIgnoreCase(analyzer[0])) {
					
					Exception ex = null;
					
					try {
						
						super.resultType = detectType(resultTypeStr);
						
						String responseCharset = getParameter(request, "responseCharset", "UTF-8");
						
						String jsonCallback = request.getParameter("jsoncallback");
						
						String keyword = request.getParameter("keyword");
						
						Tokenizer tokenizer = (Tokenizer) Class.forName(analyzer[1]).newInstance();
						
						ResultStringer rStringer = super.getResultStringer("analyze-result", true, jsonCallback);
						
						rStringer.object()
							.key("keyword").value(keyword)
							.key("analyzer").value(analyzerName)
							.key("token")
							.array("item");
						
						tokenizer.setInput(keyword.toCharArray());
						
						CharVector token = new CharVector();
						
						for(;tokenizer.nextToken(token);) {
							
							rStringer.value(token);
						}
						
						rStringer.endArray()
						.endObject();
						
						super.writeHeader(response, rStringer, responseCharset);
						
						PrintWriter writer = response.getWriter();
						
						writer.write(rStringer.toString());
						
						writer.close();
						
					} catch (InstantiationException e) { ex = e;
					} catch (IllegalAccessException e) { ex = e;
					} catch (ClassNotFoundException e) { ex = e;
					} catch (StringifyException e) { ex = e;
					} finally {
						if(ex != null) {
							logger.error("",ex);
							throw new ServletException(ex);
						}
					}
					
					return;
				}
			}
		}
	}
}
