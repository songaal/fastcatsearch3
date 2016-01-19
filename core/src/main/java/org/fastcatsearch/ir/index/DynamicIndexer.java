package org.fastcatsearch.ir.index;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.*;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.util.CoreFileUtils;
import org.fastcatsearch.util.FilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by swsong on 2016. 1. 10..
 */
public class DynamicIndexer {
    private static Logger logger = LoggerFactory.getLogger(DynamicIndexer.class);

    private CollectionHandler collectionHandler;
    private CollectionContext collectionContext;
    private DataInfo.SegmentInfo workingSegmentInfo;
    private IndexWritable indexWriter;

    private SchemaSetting schemaSetting;
    private DeleteIdSet deleteIdList;

    public DynamicIndexer(CollectionHandler collectionHandler) throws IRException {
        this.collectionHandler = collectionHandler;
        collectionContext = collectionHandler.collectionContext();
        String newSegmentId = collectionHandler.nextSegmentId();
        workingSegmentInfo = new DataInfo.SegmentInfo(newSegmentId);
        Schema schema = collectionHandler.schema();
        schemaSetting = schema.schemaSetting();
        FilePaths dataFilePaths = collectionContext.collectionFilePaths().dataPaths();
        int dataSequence = collectionContext.getIndexSequence();

        IndexConfig indexConfig = collectionContext.indexConfig();

        logger.debug("WorkingSegmentInfo = {}", workingSegmentInfo);
        String segmentId = workingSegmentInfo.getId();

        File segmentDir = dataFilePaths.segmentFile(dataSequence, segmentId);
        indexWriter = new SegmentWriter(schema, segmentDir, workingSegmentInfo, indexConfig, collectionHandler.analyzerPoolManager(), null);

        PrimaryKeySetting primaryKeySetting = schemaSetting.getPrimaryKeySetting();
        if (primaryKeySetting != null && primaryKeySetting.getFieldList() != null && primaryKeySetting.getFieldList().size() > 0) {
            int pkFieldSize = primaryKeySetting.getFieldList().size();
            deleteIdList = new DeleteIdSet(pkFieldSize);
        }
    }

    protected void prepare() throws IRException {
        FilePaths indexFilePaths = collectionContext.indexFilePaths();
        // 증분색인이면 기존스키마그대로 사용.
        String newSegmentId = collectionHandler.nextSegmentId();
        workingSegmentInfo = new DataInfo.SegmentInfo(newSegmentId);
        File segmentDir = indexFilePaths.file(workingSegmentInfo.getId());
        logger.debug("#색인시 세그먼트를 생성합니다. {}", workingSegmentInfo);
        try {
            CoreFileUtils.removeDirectoryCascade(segmentDir);
        } catch (IOException e) {
            throw new IRException(e);
        }
    }

    public void addDocument(Map<String, Object> documentMap) throws IRException, IOException {
        Document document = createDocument(documentMap);
        indexWriter.addDocument(document);
    }

    public void updateDocument(Map<String, Object> documentMap) throws IRException, IOException {

        //TODO 기존 문서를 읽어와서 업데이트 한다.

        Document document = createDocument(documentMap);
        indexWriter.addDocument(document);
    }

    public void deleteDocument(Map<String, Object> documentMap) throws IRException {
        String pk = null;
        deleteIdList.add(pk);
    }

    public void finish() {
    }

    private Document createDocument(Map<String, Object> map) throws IRException {
        List<FieldSetting> fieldSettingList = schemaSetting.getFieldSettingList();
        FieldSetting fs = null;
        Object data = null;
        try {

            /*
            * @see DefaultDataSourceReader
            * 루프를 돌기전에 먼저 target속성이 존재하면 그 필드를 먼저수행해준다. target은 transform이 있어야만 한다.
            * 왜냐하면, 다른 필드에 값을 넣어주기 때문에..
            * 또한 해당 값이 Multivalue라면 먼저 파싱해서 transform을 해야한다.
            *
            * */
//			logger.debug("doc >> {}", map);
            // Schema를 기반으로 Document로 만든다.
            Document document = new Document(fieldSettingList.size());
            for (int i = 0; i < fieldSettingList.size(); i++) {
                fs = fieldSettingList.get(i);

                String key = fs.getId();
                String source = fs.getSource();
                if(source != null && source.length() > 0){
                    //source가 있다면 source에서 데이터를 가져온다.
                    key = source;
                }
                data = map.get(key);
                //데이터가 없으면 소문자로 시도. 퍼블릭 REST API를 통해 받은 json등은 키를 변경하기가 어려우므로 추가된 기능.
                if(data == null) {
                    data = map.get(key.toLowerCase());
                }
                //null이면 공백문자로 치환.
                if(data == null) {
                    data = "";
                } else if (data instanceof String) {
                    data = ((String) data).trim();
                }


//				logger.debug("Get {} : {}", key, data);
                String multiValueDelimiter = fs.getMultiValueDelimiter();
                Field f = fs.createIndexableField(data, multiValueDelimiter);
                document.add(f);
//				logger.debug("doc [{}]{}:{}", i, fs.getId(), f);
            }
            return document;
        } catch (Throwable e) {
            if(fs!=null) {
                logger.error("", e);
                throw new IRException("Exception At Field ["+fs.getName()+"] in \""+data+"\"", e);
            } else {
                logger.error("", e);
                throw new IRException(e.getMessage());
            }
        }
    }
}
