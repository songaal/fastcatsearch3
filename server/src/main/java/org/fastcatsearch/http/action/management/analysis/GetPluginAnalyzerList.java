package org.fastcatsearch.http.action.management.analysis;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.plugin.GetPluginAnalyzerJob;
import org.fastcatsearch.job.plugin.GetPluginAnalyzerJob.PluginAnalyzerResult;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.Analyzer;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.DictionarySetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

@ActionMapping(value = "/management/analysis/plugin-analyzer-list", authority = ActionAuthority.Analysis, authorityLevel = ActionAuthorityLevel.NONE)
public class GetPluginAnalyzerList extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
	
		
		String nodeId = request.getParameter("nodeId");
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object().key("pluginList").array("plugin");
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node node = nodeService.getNodeById(nodeId);
		GetPluginAnalyzerJob job = new GetPluginAnalyzerJob();
		ResultFuture resultFuture = nodeService.sendRequest(node, job);
		
		Object obj = resultFuture.take();
		
		if(obj instanceof PluginAnalyzerResult) {
			
			PluginAnalyzerResult result = (PluginAnalyzerResult) obj;
			
			String resultStr = result.getResult();
			
			JSONObject root = new JSONObject(resultStr);
			JSONArray array = root.optJSONArray("pluginList");
			
			for (int inx = 0; inx < array.length(); inx++) {
				JSONObject plugins = array.optJSONObject(inx);
				JSONArray analyzerArray = plugins.optJSONArray("analyzer");
				
				responseWriter.object()
					.key("id").value(plugins.optString("id"))
					.key("name").value(plugins.optString("name"))
					.key("version").value(plugins.optString("version"))
					.key("description").value(plugins.optString("description"))
					.key("className").value(plugins.optString("className"))
					.key("analyzer").array();
				
				
				for(int inx2=0;inx2<analyzerArray.length();inx2++) {
					JSONObject analyzers = analyzerArray.optJSONObject(inx2);
					responseWriter.object()
						.key("id").value(analyzers.optString("id"))
						.key("name").value(analyzers.optString("name"))
						.endObject();
				}
				
				responseWriter.endArray().endObject();
			}
			
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
	}
	
//	public class GetPluginAnalyzerJob extends Job implements Streamable {
//		
//		private static final long serialVersionUID = -4342583698931447970L;
//		
//		@Override
//		public JobResult doRun() throws FastcatSearchException {
//			
//			PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
//			Collection<Plugin> pluginList = pluginService.getPlugins();
//			
//			JSONStringer stringer = new JSONStringer();
//			try {
//			
//				stringer.object().key("pluginList").array();
//				for(Plugin plugin : pluginList){
//					PluginSetting pluginSetting = plugin.getPluginSetting();
//					if(pluginSetting instanceof AnalysisPluginSetting){
//						
//						AnalysisPluginSetting setting = (AnalysisPluginSetting) pluginSetting;
//						
//						List<Analyzer> analyzerList = setting.getAnalyzerList();
//						
//						stringer.object()
//						.key("id").value(pluginSetting.getId())
//						.key("name").value(pluginSetting.getName())
//						.key("version").value(pluginSetting.getVersion())
//						.key("description").value(pluginSetting.getDescription())
//						.key("className").value(pluginSetting.getClassName())
//						.key("analyzer").array();
//						
//						for(Analyzer analyzer : analyzerList) {
//							stringer.object()
//								.key("id").value(analyzer.getId())
//								.key("name").value(analyzer.getName())
//								.endObject();
//						}
//						stringer.endArray().endObject();
//					}
//				}
//				stringer.endArray().endObject();
//				PluginAnalyzerResult result = new PluginAnalyzerResult(stringer.toString());
//				
//				return new JobResult(result);
//				
//				
//			} catch (JSONException e) {
//				logger.debug("error orrurs : {}",e.getMessage());
//				
//			} finally {
//			}
//			return new JobResult(false);
//		}
//
//		@Override
//		public void readFrom(DataInput input) throws IOException { }
//
//		@Override
//		public void writeTo(DataOutput output) throws IOException { }
//	}
	
//	public class PluginAnalyzerResult implements Streamable {
//		
//		private String result;
//		public PluginAnalyzerResult(String result) {
//			this.result = result;
//		}
//		
//		public String getResult() {
//			return result;
//		}
//
//		@Override
//		public void readFrom(DataInput input) throws IOException {
//			result = input.readString();
//		}
//
//		@Override
//		public void writeTo(DataOutput output) throws IOException {
//			output.writeString(result);
//			
//		}
//	}
}
