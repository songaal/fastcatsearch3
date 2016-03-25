package org.fastcatsearch.datasource.reader;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.search.SegmentIndexableDocumentReader;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.util.JAXBConfigs;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 *
 * */
@SourceReader(name = "DOCUMENT_STORED_SOURCE")
public class DocumentSourceSingleReader extends SingleSourceReader<Map<String, Object>> {
    private DocumentReader reader;
    private int limit;
    private BitSet deleteSet;
    private int docNo;
    private int lastDocNo;

    private String schemaFilePath;
    private String segmentDirPath;

    public DocumentSourceSingleReader() {
        super();
    }

    public DocumentSourceSingleReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig, SourceModifier<Map<String, Object>> sourceModifier, String lastIndexTime) {
        super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
    }

    @Override
    public void init() throws IRException {
        schemaFilePath = getConfigString("schemaPath");
        segmentDirPath = getConfigString("segmentDir");
        SchemaSetting schemaSetting = null;
        try {
            schemaSetting = JAXBConfigs.readConfig(new File(schemaFilePath), SchemaSetting.class);
            File segHomePath = new File(segmentDirPath);
            reader = new DocumentReader(schemaSetting, segHomePath);
            deleteSet = new BitSet(segHomePath, IndexFileNames.docDeleteSet);
        } catch (JAXBException e) {
            logger.error("", e);
        } catch (IOException e) {
            logger.error("", e);
        }
        limit = reader.getDocumentCount();
    }

    @Override
    public boolean hasNext() throws IRException {
        while (docNo < limit) {
            if (!deleteSet.isSet(docNo)) {
                lastDocNo = docNo;
            } else {
                logger.trace("doc {} is deleted and ignored for merging", docNo);
                lastDocNo = -1;
            }
            docNo++;
            if (lastDocNo != -1) {
                return true;
            }
        }
        lastDocNo = -1;
        return false;
    }

    @Override
    protected Map<String, Object> next() throws IRException {
        try {
            if (lastDocNo != -1) {
                Document doc = reader.readDocument(lastDocNo);
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < doc.size(); i++) {
                    Field f = doc.get(i);
                    map.put(f.getId(), f.rawString());
                }
                return map;
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }
    }

    @Override
    public void close() throws IRException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("", e);
            }
        }
    }

    @Override
    protected void initParameters() {
        registerParameter(new SourceReaderParameter("schemaPath", "Schema File Path", "Schema File Path. (Absolute Path)"
                , SourceReaderParameter.TYPE_STRING_LONG, true, null));
        registerParameter(new SourceReaderParameter("segmentDir", "Segment Dir Path", "Segment Dir Path. (Absolute Path)"
                , SourceReaderParameter.TYPE_STRING_LONG, true, null));
    }
}
