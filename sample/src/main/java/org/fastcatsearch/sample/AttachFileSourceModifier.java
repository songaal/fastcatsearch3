package org.fastcatsearch.sample;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;

import java.io.File;
import java.util.Map;

/**
        * Created by swsong on 2014. 9. 2..
                */
        public class AttachFileSourceModifier extends SourceModifier<Map<String, Object>> {

            @Override
            public void modify(Map<String, Object> data) throws IRException {
        /*
         * data contains all field_id and field_data pair from DBMS or structured file
         */

                //Get attach file path
                String filePath = (String) data.get("attach_file_path");
                //Get file object
                File file = new File(filePath);
                //Extract file contents using Attach file filter
                String fileContents = extractFileContents(file);
                //put new field data fileContents as "attach_file_contents"
                data.put("attach_file_contents", fileContents);
            }

            private String extractFileContents(File f) {
                //
                // TODO Implements file contents extraction using Attach file filter
                //
                return null;
            }

            @Override
            public void init() {
                //TODO Something to initialize
            }

            @Override
            public void close() {
        //TODO Something to close resources
    }
}
