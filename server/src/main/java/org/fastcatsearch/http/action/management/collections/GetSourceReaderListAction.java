package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.List;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.SingleSourceReader;
import org.fastcatsearch.datasource.reader.SourceReaderParameter;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.DynamicClassLoader;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/single-source-reader-list", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class GetSourceReaderListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);

		Settings setting = environment.settingManager().getSystemSettings();
		String packages = setting.getString("source-reader-package", "org.fastcatsearch");
		List<Class<?>> sourceReaderList = DynamicClassLoader.findChildrenClass(packages, SingleSourceReader.class);

		responseWriter.object().key("sourceReaderList").array();
		for (Class<?> sourceReader : sourceReaderList) {
			logger.trace("class:{}", sourceReader);

			SourceReader annotation = sourceReader.getAnnotation(SourceReader.class);
			if (annotation != null && annotation.name() != null) {
				
				try {
					Constructor<?> constructor = sourceReader.getConstructor(String.class, File.class, SingleSourceConfig.class, SourceModifier.class, String.class);
					SingleSourceConfig singleSourceConfig = new SingleSourceConfig();
					SingleSourceReader<?> sreader = (SingleSourceReader<?>) constructor.newInstance("", null, singleSourceConfig, null, null);
					List<SourceReaderParameter> parameterList = sreader.getParameterList();
					responseWriter.object().key("name").value(annotation.name()).key("reader").value(sreader.getClass().getName()).key("parameters").array();
					for (SourceReaderParameter param : parameterList) {
						logger.trace("[{}/{}:{}]", param.getId(), param.getName(), param.getValue());
						responseWriter.object().key("id").value(param.getId()).key("name").value(param.getName()).key("value").value(param.getValue()).key("type")
								.value(param.getType()).key("required").value(param.isRequired()).key("description").value(param.getDescription()).key("defaultValue")
								.value(param.getDefaultValue()).endObject();
					}
					responseWriter.endArray().endObject();
				} catch (NoSuchMethodException e) {
					logger.warn("no constructor : {}", sourceReader);
				} catch (Throwable t) {
                    logger.error("error while create source reader " + sourceReader.getName(), t);
                }
			}
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
	}
}
