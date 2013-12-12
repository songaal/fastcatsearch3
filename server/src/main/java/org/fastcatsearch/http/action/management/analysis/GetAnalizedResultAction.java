package org.fastcatsearch.http.action.management.analysis;

import java.io.CharArrayReader;
import java.io.Writer;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
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
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		
		String pluginId = request.getParameter("pluginId");
		
		String testString = request.getParameter("testString");
		
		Plugin plugin = pluginService.getPlugin(pluginId);
		
		if(plugin instanceof AnalysisPlugin) {
			
			
			@SuppressWarnings("rawtypes")
			AnalysisPlugin analysisPlugin = (AnalysisPlugin)plugin;
			
			if(analysisPlugin.getDictionary().size()==0) {
				plugin.load(false);
			}
			
			List<org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.Analyzer> 
				analyzerList = analysisPlugin.getPluginSetting().getAnalyzerList();
			
			Analyzer analyzer = null;
			
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
			
			Writer writer = response.getWriter();
			ResponseWriter responseWriter = getDefaultResponseWriter(writer);
			responseWriter.object().key("result").array("terms");
			if(analyzer!=null) {
				char[] fieldValue = testString.toCharArray();
				TokenStream tokenStream = analyzer.tokenStream("", new CharArrayReader(fieldValue));
				tokenStream.reset();
				CharsRefTermAttribute termAttribute = null;
				PositionIncrementAttribute positionAttribute = null;
				if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
					termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
				}
				if (tokenStream.hasAttribute(PositionIncrementAttribute.class)) {
					positionAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
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
					key.toUpperCase();
					int position = -1;
					if (positionAttribute != null) {
						position = positionAttribute.getPositionIncrement();
					}
					responseWriter.value(key);
				}
			}
			responseWriter.endArray().endObject();
			responseWriter.done();
		}
	}
}
