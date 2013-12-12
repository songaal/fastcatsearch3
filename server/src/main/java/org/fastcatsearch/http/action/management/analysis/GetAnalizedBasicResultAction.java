package org.fastcatsearch.http.action.management.analysis;

import java.io.CharArrayReader;
import java.io.Writer;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.util.CharsRef;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.analysis.AnalyzerFactory;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.settings.AnalyzerFactoryLoader;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionMapping("/management/analysis/analysis-tools-basic")
public class GetAnalizedBasicResultAction extends AuthAction {

	private static final Logger logger = LoggerFactory.getLogger(GetAnalizedBasicResultAction.class);

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);

		String pluginId = request.getParameter("pluginId");

		String queryWords = request.getParameter("queryWords");

		String errorMessage = null;
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object();
		responseWriter.key("query").value(queryWords);
		try {
			Plugin plugin = pluginService.getPlugin(pluginId);
			if (plugin != null && plugin instanceof AnalysisPlugin) {

				@SuppressWarnings("rawtypes")
				AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;

				//FIXME 실제 운영환경에서는 이 조건이 참이 될수없음. 차후 삭제필요.
				if (!analysisPlugin.isLoaded()) {
					plugin.load(environment.isMasterNode());
				}

				List<AnalysisPluginSetting.Analyzer> analyzerList = analysisPlugin.getPluginSetting().getAnalyzerList();

				Analyzer analyzer = null;

				//TODO 일단 첫번째 analyzer를 사용하는 것으로 구현하며, 여러개일때는 어떻게 할지 고려필요.
				for (int inx = 0; inx < analyzerList.size();) {
					logger.debug("analyzer{} : {} / {}", inx, analyzerList.get(inx).getName(), analyzerList.get(inx).getClassName());
					AnalyzerFactory factory = AnalyzerFactoryLoader.load(analyzerList.get(inx).getClassName());

					factory.init();

					analyzer = factory.create();

					break;
				}
				
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
						CharVector key = null;
						if (termAttribute != null) {
							CharsRef charRef = termAttribute.charsRef();
							char[] buffer = new char[charRef.length()];
							System.arraycopy(charRef.chars, charRef.offset, buffer, 0, charRef.length);
							key = new CharVector(buffer, 0, buffer.length);
						} else {
							key = new CharVector(charTermAttribute.buffer(), 0, charTermAttribute.length());
						}
						responseWriter.value(key);
					}
				}else{
					throw new Exception("No analyzer defined in plugin.xml");
				}
				responseWriter.endArray();
				
			} else {
				throw new Exception("Plugin is not AnalysisPlugin. id="+pluginId);
			}
		} catch (Throwable t) {
			errorMessage = t.toString();
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
