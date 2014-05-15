package org.fastcatsearch.http.action.management.analysis;

import java.io.CharArrayReader;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionMapping(value = "/management/analysis/analyze", authority = ActionAuthority.Analysis, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetAnalizedResultAction extends AuthAction {

	private static final Logger logger = LoggerFactory.getLogger(GetAnalizedResultAction.class);

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);

		String pluginId = request.getParameter("pluginId");
		String analyzerId = request.getParameter("analyzerId");
		Map<String, String> parameterMap = request.getParameterMap();

		String errorMessage = null;
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object();
		
		try {
			Plugin plugin = pluginService.getPlugin(pluginId);
			if (plugin == null) {
				throw new Exception("Cannot find plugin >> " + (pluginId + "." + analyzerId));
			}
			AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
			AnalyzerPool analyzerPool = analysisPlugin.getAnalyzerPool(analyzerId);
			
			if (analyzerPool == null) {
				throw new Exception("Cannot find analyzer >> " + (pluginId + "." + analyzerId));
			}
			
			Analyzer analyzer = null;
			try{
				analyzer = analyzerPool.getFromPool();
			
				responseWriter.key("result").object();
				for(Entry<String, String> entry : parameterMap.entrySet()) {
					
					String key = entry.getKey();
					if(key.equals("pluginId") || key.equals("analyzerId")){
						continue;
					}
					responseWriter.key(key).array("e");
					char[] fieldValue = entry.getValue().toCharArray();
					
					TokenStream tokenStream = analyzer.tokenStream("", new CharArrayReader(fieldValue));
					tokenStream.reset();
					CharsRefTermAttribute termAttribute = null;
					if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
						termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
					}
					CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);

					while (tokenStream.incrementToken()) {
						String word = "";
						if (termAttribute != null) {
							word = termAttribute.toString();
						} else {
							word = charTermAttribute.toString();
						}
						responseWriter.value(word);
					}
					responseWriter.endArray();
				}
				
			}finally{
				responseWriter.endObject();
				analyzerPool.releaseToPool(analyzer);
			}
		} catch (Throwable t) {
			errorMessage = t.toString();
			logger.error("", t);
		} finally {
			
			if(errorMessage != null){
				responseWriter.key("success").value(false);
				responseWriter.key("errorMessage").value(errorMessage);
			}else{
				responseWriter.key("success").value(true);
			}
			responseWriter.endObject().done();
			if (writer != null) {
				writer.close();
			}
		}
	}
	
}
