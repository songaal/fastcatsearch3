package org.fastcatsearch.job.indexing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.datasource.reader.AbstractDataSourceReader;
import org.fastcatsearch.datasource.reader.DefaultDataSourceReaderFactory;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;

public class CollectionDatasourceDumpJob extends Job implements Streamable {
	private static final long serialVersionUID = 7991088210024664812L;

	@Override
	public void readFrom(DataInput input) throws IOException {
		args = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString((String) args);
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		String collectionId = getStringArgs();

		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		if(collectionContext == null) {
			throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
		}
		String indexNodeId = collectionContext.collectionConfig().getIndexNode();
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node indexNode = nodeService.getNodeById(indexNodeId);

		if (!nodeService.isMyNode(indexNode)) {
			throw new RuntimeException("Invalid index node collection[" + collectionId + "] node[" + indexNodeId + "]");
		}

		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		File filePath = collectionContext.collectionFilePaths().file();

		SchemaSetting schemaSetting = collectionContext.schema().schemaSetting();

		SchemaSetting workSchemaSetting = collectionContext.workSchemaSetting();
		if (workSchemaSetting != null) {
			List<FieldSetting> list = workSchemaSetting.getFieldSettingList();
			if (list != null && list.size() > 0) {
				schemaSetting = workSchemaSetting;
			}
		}

		Writer writer = null;
		AbstractDataSourceReader dataSourceReader = null;
		try {
			long startTime = System.currentTimeMillis();
			long lapTime = startTime;
			
			writer = new OutputStreamWriter(new FileOutputStream(new File(filePath,  "datasource."+System.currentTimeMillis()+".txt")));
			dataSourceReader = DefaultDataSourceReaderFactory.createFullIndexingSourceReader(collectionContext.collectionId(), filePath, schemaSetting, dataSourceConfig);
			int count = 0;
			while (dataSourceReader.hasNext()) {
				Document document = dataSourceReader.nextDocument();
				count++;
				if (count % 1000 == 0) {
					logger.debug(
							"{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
							new Object[] { count, System.currentTimeMillis() - lapTime,
									Formatter.getFormatTime(System.currentTimeMillis() - startTime),
									Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) });
					lapTime = System.currentTimeMillis();
				}
				writer.write(document.toString());
				writer.write("\n");
			}

		} catch (Exception e) {
			logger.error("", e);
			return new JobResult(false);
		} finally {
			if(writer != null){
				try {
					writer.close();
				} catch (IOException ignore) {
				}
			}
		}
		return new JobResult(true);
	}
}
