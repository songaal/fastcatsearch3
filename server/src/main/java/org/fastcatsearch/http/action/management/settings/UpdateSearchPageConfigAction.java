package org.fastcatsearch.http.action.management.settings;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SearchPageSettings;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.settings.SearchPageSettings.SearchCategorySetting;
import org.fastcatsearch.util.JAXBConfigs;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/settings/search-config/update", authority = ActionAuthority.Settings, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class UpdateSearchPageConfigAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);

		Map<String, SearchCategorySetting> categoryMap = new TreeMap<String, SearchCategorySetting>();

		SearchPageSettings searchPageSettings = irService.getSearchPageSettings();
		SearchCategorySetting setting = null;
		int i = 0;
		for (Entry<String, String> e : request.getParameterMap().entrySet()) {
			logger.debug("[{}] key {} > {}", i++, e.getKey(), e.getValue());

			String key = e.getKey();
			String value = e.getValue();
			if(value != null){
				value = value.trim();
			}

			if (key.equals("totalSearchListSize")) {
				int totalSearchListSize = Integer.parseInt(value);
				searchPageSettings.setTotalSearchListSize(totalSearchListSize);
			} else if (key.equals("searchListSize")) {
				int searchListSize = Integer.parseInt(value);
				searchPageSettings.setSearchListSize(searchListSize);
			} else if (key.equals("relateKeywordURL")) {
				searchPageSettings.setRelateKeywordURL(value);
			} else if (key.equals("realtimePopularKeywordURL")) {
				searchPageSettings.setRealtimePopularKeywordURL(value);
			} else if (key.equals("css")) {
				searchPageSettings.setCss(value);
			} else if (key.equals("js")) {
				searchPageSettings.setJavascript(value);
			} else if ((setting = getSetting(categoryMap, key, "order")) != null) {
				setting.setOrder(value);
			} else if ((setting = getSetting(categoryMap, key, "categoryId")) != null) {
				setting.setId(value);
			} else if ((setting = getSetting(categoryMap, key, "categoryName")) != null) {
				setting.setName(value);
			} else if ((setting = getSetting(categoryMap, key, "thumbnailField")) != null) {
				setting.setThumbnailField(value);
			} else if ((setting = getSetting(categoryMap, key, "titleField")) != null) {
				setting.setTitleField(value);
			} else if ((setting = getSetting(categoryMap, key, "bodyField")) != null) {
				setting.setBodyField(value);
			} else if ((setting = getSetting(categoryMap, key, "bundleField")) != null) {
				setting.setBundleField(value);
			} else if ((setting = getSetting(categoryMap, key, "searchQuery")) != null) {
				setting.setSearchQuery(value);
			}

		}
		
		List<SearchCategorySetting> searchCategorySettingList = new ArrayList<SearchCategorySetting>();
		for(Entry<String, SearchCategorySetting> e : categoryMap.entrySet()){
			String seq = e.getKey();
			SearchCategorySetting s = e.getValue();
//			logger.debug(">> {} > {}", seq, s);
			searchCategorySettingList.add(s);
		}
		
		searchPageSettings.setSearchCategorySettingList(searchCategorySettingList);
		
		responseWriter.object();
		responseWriter.key("success").value(true);
		try{
			File file = environment.filePaths().configPath().file(SettingFileNames.searchPageSettings);
			JAXBConfigs.writeConfig(file, searchPageSettings, SearchPageSettings.class);
		}catch(Exception e){
			responseWriter.key("success").value(false);
			responseWriter.key("message").value(e.getMessage());
		}
		
		responseWriter.endObject();
		responseWriter.done();
	}

	private SearchCategorySetting getSetting(Map<String, SearchCategorySetting> categoryMap, String key, String settingFieldName) {
		if (key.startsWith(settingFieldName)) {
			int pos = key.lastIndexOf('_');
			if (pos > 0) {
				String seq = key.substring(pos + 1);
				SearchCategorySetting setting = categoryMap.get(seq);
				if (setting == null) {
					setting = new SearchCategorySetting();
					categoryMap.put(seq, setting);
				}
				return setting;
			}
		}
		return null;
	}
}
