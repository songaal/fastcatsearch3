package org.fastcatsearch.http.action.management.analysis;

import java.io.CharArrayReader;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.AdditionalTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.StopwordAttribute;
import org.apache.lucene.analysis.tokenattributes.SynonymAttribute;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;
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
		String optionValues = request.getParameter("optionValues");
		String[] optionValueArray;
		
		if(optionValues==null) {
			optionValues="";
		}
		
		optionValueArray = optionValues.split(",");
		
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
			@SuppressWarnings("rawtypes")
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
					if(key.equals("pluginId") || key.equals("analyzerId") || key.equals("optionValues")){
						continue;
					}
					responseWriter.key(key).array("e");
					char[] fieldValue = entry.getValue().toCharArray();
					
					AnalyzerOption options = new AnalyzerOption();
					if(optionValueArray.length > 0) {
						options.useStopword("y".equalsIgnoreCase(optionValueArray[0]));
					}
					if(optionValueArray.length > 1) {
						options.useSynonym("y".equalsIgnoreCase(optionValueArray[1]));
					}
					TokenStream tokenStream = analyzer.tokenStream("", new CharArrayReader(fieldValue), options);
					tokenStream.reset();
					CharsRefTermAttribute termAttribute = null;
					if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
						termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
					}
					SynonymAttribute synonymAttribute = null;
					if(tokenStream.hasAttribute(SynonymAttribute.class)) {
						synonymAttribute = tokenStream.getAttribute(SynonymAttribute.class);
					}
					AdditionalTermAttribute additionalTermAttribute = null;
					if(tokenStream.hasAttribute(AdditionalTermAttribute.class)) {
						additionalTermAttribute = tokenStream.getAttribute(AdditionalTermAttribute.class);
					}
					
					StopwordAttribute stopwordAttribute = null;
					if(tokenStream.hasAttribute(StopwordAttribute.class)) {
						stopwordAttribute = tokenStream.getAttribute(StopwordAttribute.class);
					}
					
					CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);

					while (tokenStream.incrementToken()) {
						String word = "";
						if (termAttribute != null) {
							word = termAttribute.toString();
						} else {
							word = charTermAttribute.toString();
						}
						
						//remove stopword
						if(stopwordAttribute.isStopword()) {
							continue;
						}
						responseWriter.value(word);
						appendSynonyms(responseWriter, synonymAttribute);
						
						//if found additiona term. print it.
						if(additionalTermAttribute !=null && additionalTermAttribute.size() > 0) {
							Iterator<String> termIter = additionalTermAttribute.iterateAdditionalTerms();
							while(termIter.hasNext()) {
								String token = termIter.next();
								responseWriter.value(token);
								appendSynonyms(responseWriter, synonymAttribute);
							}
						}
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
	
	private void appendSynonyms(ResponseWriter responseWriter, SynonymAttribute synonymAttribute) throws ResultWriterException {
		//synonym extraction
		if (synonymAttribute!=null) {
			@SuppressWarnings("rawtypes")
			List synonyms = synonymAttribute.getSynonyms();
			if(synonyms != null) {
				for(Object synonymObj : synonyms) {
					if(synonymObj instanceof CharVector) {
						CharVector synonym = (CharVector)synonymObj;
						responseWriter.value(synonym.toString());
					} else if(synonymObj instanceof List) {
						@SuppressWarnings("rawtypes")
						List synonymList = (List)synonymObj;
						for(Object synonym : synonymList) {
							responseWriter.value(synonym.toString());
						}
					}
				}
			}
		}
	}
}
