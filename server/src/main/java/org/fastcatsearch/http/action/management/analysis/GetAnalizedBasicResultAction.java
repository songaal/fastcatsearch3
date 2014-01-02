package org.fastcatsearch.http.action.management.analysis;

import java.io.CharArrayReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.analysis.AnalyzerFactory;
import org.fastcatsearch.ir.settings.AnalyzerFactoryLoader;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalyzerInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionMapping(value = "/management/analysis/analysis-tools", authority = ActionAuthority.Analysis, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetAnalizedBasicResultAction extends AuthAction {

	private static final Logger logger = LoggerFactory.getLogger(GetAnalizedBasicResultAction.class);

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);

		String pluginId = request.getParameter("pluginId");
		String analyzerId = request.getParameter("analyzerId");
		String queryWords = request.getParameter("queryWords");

		String errorMessage = null;
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object();
		responseWriter.key("query").value(queryWords);
		
		try {
			
			AnalyzerFactory factory = pluginService.getAnalyzerFactoryManager().getAnalyzerFactory(pluginId+"."+analyzerId);
			if(factory == null){
				throw new Exception("Cannot find analyzer >> "+ (pluginId+"."+analyzerId));
			}
			AnalyzerOption option = new AnalyzerOption();
			option.useStopword(true);
			option.useSynonym(true);
			Analyzer analyzer = factory.create(option);
			
			responseWriter.key("result").array("terms");
			
			if (analyzer != null) {
				
				char[] fieldValue = queryWords.toCharArray();
				TokenStream tokenStream = analyzer.tokenStream("", new CharArrayReader(fieldValue));
				tokenStream.reset();
				CharsRefTermAttribute termAttribute = null;
				if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
					termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
				}
				CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);

				while (tokenStream.incrementToken()) {
					String key = "";
					if (termAttribute != null) {
						key = termAttribute.toString();
					} else {
						key = charTermAttribute.toString();
					}
					responseWriter.value(key);
				}
			}else{
				throw new Exception("Analyzer create error >> "+factory.getAnalyzerClass().getName());
			}
			responseWriter.endArray();
				
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
		}
	}
	
}
