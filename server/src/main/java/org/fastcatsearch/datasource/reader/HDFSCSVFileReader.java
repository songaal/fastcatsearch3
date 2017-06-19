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
 * Created by 전제현 on 2017. 6. 15.
 */
@SourceReader(name="HDFS_CSV_FILE")
public class HDFSCSVFileReader extends AbstractHDFSFileReader {

    private List<String> fieldList;
    private List<String> fieldNameList;
    private List<Integer> fieldIndexList;

    public HDFSCSVFileReader() {
    }

    public HDFSCSVFileReader(String collectionId, File file, SingleSourceConfig singleSourceConfig, SourceModifier<Map<String, Object>> sourceModifier, String lastIndexTime)
            throws IRException, IOException {
        super(collectionId, file, singleSourceConfig, sourceModifier, lastIndexTime);
        reader_file_type = "CSV";
    }

    @Override
    public void init() throws IRException, IOException {
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
                String[] els = line.split(",", fieldList.size());
                for (int i = 0; i < fieldIndexList.size(); i++) {
                    Integer index = fieldIndexList.get(i);
                    if (index != -1) {
                        String val;
                        if(index >= els.length) {
                            val = "";
                        } else {
                            val = els[index];
                        }
                        record.put(fieldNameList.get(i), val);
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
