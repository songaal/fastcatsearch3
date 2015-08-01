package org.fastcatsearch.datasource.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SourceReader(name="JSON_LIST_FILE")
public class JSONListFileReader extends AbstractFileReader {

    public JSONListFileReader() {
    }

    public JSONListFileReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig, SourceModifier sourceModifier, String lastIndexTime) throws IRException {
        super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
    }

    @Override
    protected Map<String, Object> parse(BufferedReader reader) throws IRException, IOException {
        String line = reader.readLine();
        if(line == null) {
            throw new IOException("EOF");
        }

        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>(){};
        return mapper.readValue(line, typeRef);
    }

    @Override
    protected void initReader(BufferedReader reader) throws IRException, IOException {
        //do nothing
    }


}
