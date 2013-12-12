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
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionMapping("/management/analysis/analysis-test")
public class GetAnalizedResultAction extends AuthAction {
	
	private static final Logger logger = LoggerFactory.getLogger(GetAnalizedResultAction.class);

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		String pluginId = request.getParameter("pluginId");
		
		String testString = request.getParameter("testString");
		
		Analyzer analyzer = getAnalyzer(getAnalysisPlugin(pluginId));
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object().key("result").array("terms");
		
		if(analyzer!=null) {

			char[] fieldValue = testString.toCharArray();
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
				responseWriter.value(key.toUpperCase());
			}
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
	}
	
	@SuppressWarnings("rawtypes")
	protected static Analyzer getAnalyzer(AnalysisPlugin analysisPlugin) {
		
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
	protected static AnalysisPlugin getAnalysisPlugin(String pluginId) {
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		
		Plugin plugin = pluginService.getPlugin(pluginId);
		
		if(plugin instanceof AnalysisPlugin) {
			
			AnalysisPlugin analysisPlugin = (AnalysisPlugin)plugin;
			
			if(analysisPlugin.getDictionary().size()==0) {
				plugin.load(false);
			}
			return analysisPlugin;
		}
		return null;
	}
}
