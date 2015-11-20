package org.fastcatsearch.datasource.reader;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by swsong on 2015. 6. 29..
 */
@SourceReader(name = "CSV_FILE")
public class CSVFileReader extends AbstractFileReader {

    private List<String> fieldList;
    private List<String> fieldNameList;
    private List<Integer> fieldIndexList;

    public CSVFileReader() {
    }

    public CSVFileReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig, SourceModifier<Map<String, Object>> sourceModifier, String lastIndexTime)
            throws IRException {
        super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
    }

    @Override
    public void init() throws IRException {
        super.init();
        String fieldNames = getConfigString("fieldList");
        // * 이면 fieldNameList 가 null이고 모든 필드를 대상으로 한다.
        if (!fieldNames.equals("*")) {
            String[] fields = fieldNames.split(",");
            if (fields.length > 0) {
                fieldList = new ArrayList<String>();
                for (String field : fields) {
                    fieldList.add(field.trim().toUpperCase());
                }
            }
        }
    }

    @Override
    protected Map<String, Object> parse(BufferedReader reader) throws IRException, IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            Map<String, Object> record = new HashMap<String, Object>();
            try {
                String[] els = line.split(",");

                for (int i = 0; i < fieldIndexList.size(); i++) {
                    Integer index = fieldIndexList.get(i);
                    if (index != -1) {
                        record.put(fieldNameList.get(i), els[index]);
                    }
                }
                //정상이면 리턴.
                return record;
            }catch(Exception e) {
                logger.error("parsing error : line= " + line, e);
            }
        }
        throw new IOException("EOF");
    }

    @Override
    protected void initReader(BufferedReader reader) throws IRException, IOException {
        String headerLine = reader.readLine();
        String[] headers = headerLine.split(",");
        List<String> headerList = new ArrayList<String>();
        for (String header : headers) {
            headerList.add(header.trim().toUpperCase());
        }

        fieldNameList = new ArrayList<String>();
        fieldIndexList = new ArrayList<Integer>();

        if (fieldList != null) {
            //가져올 필드를 정의했을 경우.
            for (String field : fieldList) {
                fieldNameList.add(field);
                int index = headerList.indexOf(field);
                fieldIndexList.add(index);
            }
        } else {
            //가져올 필드가 정의 안되있을 경우.
            int i = 0;
            for (String header : headerList) {
                fieldNameList.add(header);
                fieldIndexList.add(i++);
            }

        }


    }

    @Override
    protected void initParameters() {
        super.initParameters();
        registerParameter(new SourceReaderParameter("fieldList", "Field List", "Comma separated fields to use. Use '*' for all fields."
                , SourceReaderParameter.TYPE_STRING_LONG, true, null));
    }
}
