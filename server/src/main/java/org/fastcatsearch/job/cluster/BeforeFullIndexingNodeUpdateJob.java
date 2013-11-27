//package org.fastcatsearch.job.cluster;
//
//import java.io.IOException;
//
//import org.fastcatsearch.exception.FastcatSearchException;
//import org.fastcatsearch.ir.IRService;
//import org.fastcatsearch.ir.config.CollectionContext;
//import org.fastcatsearch.ir.io.DataInput;
//import org.fastcatsearch.ir.io.DataOutput;
//import org.fastcatsearch.job.StreamableJob;
//import org.fastcatsearch.service.ServiceManager;
//import org.fastcatsearch.transport.vo.StreamableCollectionContext;
//import org.fastcatsearch.util.CollectionContextUtil;
///**
// * 전체색인을 시작하기전에 master => index로 전송할 셋팅들.
// * config.xml, datasource.xml, index-config.xml, schema.work.xml 보내고 즉시 context에 적용 & write
// * 
// * */
//public class BeforeFullIndexingNodeUpdateJob extends StreamableJob {
//	private static final long serialVersionUID = 7222232821891387399L;
//
//	private CollectionContext collectionContext;
//
//	public BeforeFullIndexingNodeUpdateJob() {
//	}
//
//	public BeforeFullIndexingNodeUpdateJob(CollectionContext collectionContext) {
//		
//		if(collectionContext.workSchema() != null){
//			//work schema를 schema자리에 넣어서 보낸다.
//			CollectionContext newCollectionContext = collectionContext.copy();
//			newCollectionContext.setSchema(newCollectionContext.workSchema());
//			newCollectionContext.setWorkSchema(null);
//			this.collectionContext = newCollectionContext;
//		}else{
//			this.collectionContext = collectionContext;
//			collectionContext.setSchema(null); //work schema가 없으므로 null.
//		}
//	}
//
//	@Override
//	public JobResult doRun() throws FastcatSearchException {
//
//		try {
//			String collectionId = collectionContext.collectionId();
//			IRService irService = ServiceManager.getInstance().getService(IRService.class);
//			CollectionContext nodeCollectionContext = irService.collectionContext(collectionId);
//			logger.debug("work schema >> {}", collectionContext.schema());
//			nodeCollectionContext.init(nodeCollectionContext.schema()
//					, collectionContext.schema() //work 스키마를 받아서 넣는다.
//					, collectionContext.collectionConfig()
//					, collectionContext.indexConfig()
//					, collectionContext.dataSourceConfig()
//					, nodeCollectionContext.indexStatus()
//					, nodeCollectionContext.dataInfo()
//					, nodeCollectionContext.indexingScheduleConfig()); //실제로 schedule은 master이외에서는 사용안됨.
//			
//			
//			
//			CollectionContextUtil.write(nodeCollectionContext);
//			
//			logger.info("CollectionContext is updated by master!");
//			
//			return new JobResult();
//		} catch (Exception e) {
//			logger.error("", e);
//			throw new FastcatSearchException("ERR-00525", e);
//		}
//
//	}
//
//	@Override
//	public void readFrom(DataInput input) throws IOException {
//		StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(environment);
//		streamableCollectionContext.readFrom(input);
//		this.collectionContext = streamableCollectionContext.collectionContext();
//	}
//
//	@Override
//	public void writeTo(DataOutput output) throws IOException {
//		StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(collectionContext);
//		streamableCollectionContext.writeTo(output);
//	}
//
//}
