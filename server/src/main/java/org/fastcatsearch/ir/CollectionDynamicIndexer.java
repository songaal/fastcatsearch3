package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.datasource.reader.DefaultDataSourceReader;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.CollectionSearcher;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.indexing.IndexingStopException;
import org.fastcatsearch.util.FilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by swsong on 2016. 1. 14..
 *
 * API를 통해 단발적으로 들어는 문서들을 색인하는 동적인덱서
 */
public class CollectionDynamicIndexer {
    protected static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
    protected static final Logger logger = LoggerFactory.getLogger(CollectionDynamicIndexer.class);
    protected CollectionContext collectionContext;
    protected AnalyzerPoolManager analyzerPoolManager;

    protected long createTime;

    protected DeleteIdSet deleteIdSet; //삭제문서리스트.

    protected SegmentWriter indexWriter;
    protected DataInfo.SegmentInfo segmentInfo;
    private File segmentDir;
    protected int insertCount;
    protected int updateCount;
    protected int deleteCount;
    private int count;

    private CollectionHandler collectionHandler;
    private DefaultDataSourceReader documentFactory;

    private Schema schema;
    private List<String> pkList;
    private CollectionSearcher collectionSearcher;
    private long lapTime;

    public CollectionDynamicIndexer(String documentId, CollectionHandler collectionHandler) throws IRException {
        this.collectionHandler = collectionHandler;
        this.collectionContext = collectionHandler.collectionContext();
        this.analyzerPoolManager = collectionHandler.analyzerPoolManager();
        this.schema = collectionContext.schema();
        this.segmentInfo = new DataInfo.SegmentInfo(documentId);

        /*
        * 세그먼트 디렉토리가 미리존재한다면 삭제.
        * */
        FilePaths indexFilePaths = collectionContext.indexFilePaths();
        segmentDir = indexFilePaths.file(segmentInfo.getId());
        logger.debug("New Segment Dir = {}", segmentDir.getAbsolutePath());
        try {
            FileUtils.deleteDirectory(segmentDir);
        } catch (IOException e) {
            throw new IRException(e);
        }

        /*
        * PK를 확인한다.
        * */
        pkList =  new ArrayList<String>();
        //PK가 없을경우 에러나는 버그수정.
        if(schema.schemaSetting().getPrimaryKeySetting().getFieldList() != null) {
            for (RefSetting refSetting : schema.schemaSetting().getPrimaryKeySetting().getFieldList()) {
                pkList.add(refSetting.getRef().toUpperCase());
            }
        }

        IndexConfig indexConfig = collectionContext.indexConfig();
        indexWriter = new SegmentWriter(schema, segmentDir, segmentInfo, indexConfig, analyzerPoolManager, null);

        documentFactory = new DefaultDataSourceReader(collectionHandler.schema().schemaSetting());

        deleteIdSet = new DeleteIdSet(pkList.size());
        createTime = System.currentTimeMillis();

        lapTime = createTime;
    }

    public DataInfo.SegmentInfo getSegmentInfo() {
        return segmentInfo;
    }

    public File getSegmentDir() {
        return segmentDir;
    }

    public void insertDocument(Map<String, Object> source) throws IRException, IOException {
        Document document = documentFactory.createDocument(source);
        indexWriter.addDocument(document);
        insertCount++;
        count++;
        if (count % 10000 == 0) {
            printIndexingInfo();
        }
        logger.trace("Insert doc > {}", source);
    }

    public void updateDocument(Map<String, Object> source) throws IRException, IOException {

        //1. pk를 뽑아내어 내부검색으로 이전 문서를 가져온다.
        BytesDataOutput pkbaos = new BytesDataOutput();
        StringBuffer pkSb = new StringBuffer();
        for(String pkId : pkList) {
            Object o = source.get(pkId);
            if(o == null) {
                o = source.get(pkId.toLowerCase());
            }
            Field f = makeField(pkId, o);
            if(f == null || f.isNull()){
                throw new IOException("PK field value cannot be null. fieldId="+pkId+", field="+f);
            } else {
                f.writeFixedDataTo(pkbaos);
            }
            if(o != null) {
                if(pkSb.length() > 0) {
                    pkSb.append(";");
                }
                pkSb.append(o.toString());
            } else {
                throw new IRException("Cannot find primary key : " + pkId);
            }
        }
        String pkValue = pkSb.toString();

        /*
         * 2016.4 swsong
         * 먼저 현재색인중인 세그먼트내에서 문서를 찾는다.
         */
        Document document = indexWriter.getDocumentByPk(pkbaos);
        if(document == null) {
            if(collectionSearcher == null) {
                collectionSearcher = new CollectionSearcher(collectionHandler);
            }
            document = collectionSearcher.getIndexableDocumentByPk(pkValue);
        }

        if(document == null) {
            //업데이트할 문서를 찾지 못함.
            logger.debug("Collection [{}] cannot find document : {}", collectionContext.collectionId(), pkValue);
        } else {
            //2. 들어온 문서에서 각 필드를 업데이트 한다.
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                String fieldId = entry.getKey().toUpperCase();
                Object data = entry.getValue();

                Integer idx = schema.fieldSequenceMap().get(fieldId);
                if (idx == null) {
                    //존재하지 않음.
                } else {
                    Field field = makeField(fieldId, data);
                    //교체.
                    document.set(idx, field);
                }
            }

            indexWriter.addDocument(document);
            updateCount++;
            count++;
            if (count % 10000 == 0) {
                printIndexingInfo();
            }
            logger.trace("Update {} doc > {}", pkValue, source);
        }

    }

    private Field makeField(String fieldId, Object data) throws FieldDataParseException {
        Integer idx = schema.fieldSequenceMap().get(fieldId);
        FieldSetting fs = schema.fieldSettingMap().get(fieldId);
        if (idx == null) {
            //존재하지 않음.
            return null;
        } else {
            //null이면 공백문자로 치환.
            if (data == null) {
                data = "";
            } else if (data instanceof String) {
                data = ((String) data).trim();
            }

//				logger.debug("Get {} : {}", key, data);
            String multiValueDelimiter = fs.getMultiValueDelimiter();
            Field field = fs.createIndexableField(data, multiValueDelimiter);

            return field;
        }
    }

    public void deleteDocument(Map<String, Object> source) throws IRException, IOException {

        //1. PK만 뽑아내어 현재 들어온 문서중에서 삭제후보가 있는지 찾아 현재 delete.set에 넣어준다.
        //1. pk를 뽑아내어 내부검색으로 이전 문서를 가져온다.
        BytesDataOutput pkbaos = new BytesDataOutput();
        String[] pkArray = new String[pkList.size()];
        int i = 0;
        for(String pkId : pkList) {
            Object data = source.get(pkId);
            if(data == null) {
                data = source.get(pkId.toLowerCase());
            }
            Field f = makeField(pkId, data);
            if(f == null || f.isNull()){
                throw new IOException("PK field value cannot be null. fieldId="+pkId+", field="+f);
            } else {
                f.writeFixedDataTo(pkbaos);
            }
            pkArray[i++] = String.valueOf(data);
        }
        indexWriter.deleteDocument(pkbaos);
        deleteIdSet.add(pkArray);
        deleteCount++;
        count++;
        if (count % 10000 == 0) {
            printIndexingInfo();
        }
        logger.debug("Delete doc > {}", source);
    }

    private void printIndexingInfo() {
        indexingLogger.info(
                "[{}] Dynamic Indexing {} ... lap = {} ms, elapsed = {}, mem = {}",
                collectionContext.collectionId(), count, System.currentTimeMillis() - lapTime,
                Formatter.getFormatTime(System.currentTimeMillis() - createTime),
                Formatter.getFormatSize(Runtime.getRuntime().totalMemory()));
        lapTime = System.currentTimeMillis();
    }

    public DeleteIdSet getDeleteIdSet() {
        return deleteIdSet;
    }

    public DataInfo.SegmentInfo close() throws IRException, SettingException, IndexingStopException {

        if (indexWriter != null) {
            try {
                segmentInfo = indexWriter.close();
                logger.debug("##Indexer close {}", segmentInfo);
            } catch (IOException e) {
                throw new IRException(e);
            }
        }

        return segmentInfo;
    }
}
