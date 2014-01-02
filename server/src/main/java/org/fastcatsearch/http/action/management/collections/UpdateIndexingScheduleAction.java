package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.IndexingScheduleConfig;
import org.fastcatsearch.ir.config.IndexingScheduleConfig.IndexingSchedule;
import org.fastcatsearch.ir.config.IndexingScheduleConfig.ScheduleType;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.JAXBConfigs;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/update-indexing-schedule", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class UpdateIndexingScheduleAction extends AuthAction {

	private final static SimpleDateFormat parsableFormat = new SimpleDateFormat("yyyyMMdd");
	private final static SimpleDateFormat formatableFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		boolean isSuccess = false;

		boolean fullActive = false, addActive = false;
		ScheduleType fullScheduleType = ScheduleType.REGULAR_PERIOD, addScheduleType = ScheduleType.REGULAR_PERIOD;
		String fullStartDateTime = "", addStartDateTime = "";
		int fullPeriodInSecond = 0, addPeriodInSecond = 0;

		String date = "";
		int day = 0;
		int hour = 0;
		int minute = 0;

		Calendar calendar = Calendar.getInstance();

		try {

			fullActive = "true".equals(request.getParameter("fullIndexingScheduled"));
			addActive = "true".equals(request.getParameter("addIndexingScheduled"));

			date = request.getParameter("fullBaseDate", "").replaceAll("[./-]", "");
			hour = request.getIntParameter("fullBaseHour", 0);
			minute = request.getIntParameter("fullBaseMin", 0);

			calendar.setTime(parsableFormat.parse(date));
			calendar.set(Calendar.HOUR, hour);
			calendar.set(Calendar.MINUTE, minute);

			fullStartDateTime = formatableFormat.format(calendar.getTime());

			day = request.getIntParameter("fullPeriodDay", 0);
			hour = request.getIntParameter("fullPeriodHour", 0);
			minute = request.getIntParameter("fullPeriodMin", 0);

			fullPeriodInSecond = minute * 60 + hour * 60 * 60 + day * 60 * 60 * 24;

			date = request.getParameter("addBaseDate", "").replaceAll("[./-]", "");
			hour = request.getIntParameter("addBaseHour", 0);
			minute = request.getIntParameter("addBaseMin", 0);

			calendar.setTime(parsableFormat.parse(date));
			calendar.set(Calendar.HOUR, hour);
			calendar.set(Calendar.MINUTE, minute);

			addStartDateTime = formatableFormat.format(calendar.getTime());

			day = request.getIntParameter("addPeriodDay", 0);
			hour = request.getIntParameter("addPeriodHour", 0);
			minute = request.getIntParameter("addPeriodMin", 0);

			addPeriodInSecond = minute * 60 + hour * 60 * 60 + day * 60 * 60 * 24;

			String collectionId = request.getParameter("collectionId");
			IRService irService = ServiceManager.getInstance().getService(IRService.class);

			CollectionContext collectionContext = irService.collectionContext(collectionId);
			IndexingScheduleConfig indexingScheduleConfig = collectionContext.indexingScheduleConfig();

			IndexingSchedule fullIndexingSchedule = indexingScheduleConfig.getFullIndexingSchedule();
			IndexingSchedule addIndexingSchedule = indexingScheduleConfig.getAddIndexingSchedule();

			fullIndexingSchedule.setScheduleType(fullScheduleType);
			fullIndexingSchedule.setActive(fullActive);
			fullIndexingSchedule.setStart(fullStartDateTime);
			fullIndexingSchedule.setPeriodInSecond(fullPeriodInSecond);

			addIndexingSchedule.setScheduleType(addScheduleType);
			addIndexingSchedule.setActive(addActive);
			addIndexingSchedule.setStart(addStartDateTime);
			addIndexingSchedule.setPeriodInSecond(addPeriodInSecond);

			indexingScheduleConfig.setFullIndexingSchedule(fullIndexingSchedule);
			indexingScheduleConfig.setAddIndexingSchedule(addIndexingSchedule);

			File scheduleConfigFile = collectionContext.collectionFilePaths().file(SettingFileNames.scheduleConfig);

			JAXBConfigs.writeConfig(scheduleConfigFile, indexingScheduleConfig, IndexingScheduleConfig.class);

			//해당 컬렉션의 스케쥴을 다시 로딩.
			irService.reloadSchedule(collectionId);
			
			isSuccess = true;

		} catch (Exception e) {
			logger.error("", e);
			isSuccess = false;
		}

		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		responseWriter.endObject();
		responseWriter.done();
	}
}
