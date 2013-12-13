package org.fastcatsearch.http.action.management.analysis;

import java.io.CharArrayReader;
import java.io.Writer;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.analysis.AnalyzerFactory;
import org.fastcatsearch.ir.settings.AnalyzerFactoryLoader;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionMapping("/management/analysis/analysis-tools")
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

				Analyzer analyzer = getAnalyzer(getAnalysisPlugin(pluginId));
				
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
					throw new Exception("No analyzer defined in plugin.xml");
				}
				responseWriter.endArray();
				
			} else {
				throw new Exception("Plugin is not AnalysisPlugin. id=" + pluginId);
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
	
	@SuppressWarnings("rawtypes")
	protected Analyzer getAnalyzer(AnalysisPlugin analysisPlugin) {
		
		Analyzer analyzer = null;
		
		if(analysisPlugin!=null) {
			
			List<org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.Analyzer> 
			analyzerList = analysisPlugin.getPluginSetting().getAnalyzerList();
	
			for(int inx=0;inx<analyzerList.size();) {
				logger.debug("analyzer{} : {} / {}", new Object[] { inx, 
						analyzerList.get(inx).getName(),
						analyzerList.get(inx).getClassName()
	
				});
				AnalyzerFactory factory = AnalyzerFactoryLoader.load(analyzerList.get(inx).getClassName());
	
				factory.init();
	
				analyzer = factory.create();
	
				break;
			}
		}
		
		return analyzer;
		
	}
	
	@SuppressWarnings("rawtypes")
	protected AnalysisPlugin getAnalysisPlugin(String pluginId) {
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		
		Plugin plugin = pluginService.getPlugin(pluginId);
		
		if(plugin instanceof AnalysisPlugin) {
			
			AnalysisPlugin analysisPlugin = (AnalysisPlugin)plugin;
			
//			if(analysisPlugin.getDictionary().size()==0) {
//				plugin.load(false);
//			}
			//FIXME 실제 운영환경에서는 이 조건이 참이 될수없음. 차후 삭제필요.
			if (!analysisPlugin.isLoaded()) {
				plugin.load(environment.isMasterNode());
			}
			
			return analysisPlugin;
		}
		return null;
	}
}
