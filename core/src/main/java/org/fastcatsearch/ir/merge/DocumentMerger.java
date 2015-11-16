package org.fastcatsearch.ir.merge;

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.SchemaSetting;

import java.io.File;
import java.util.List;

/**
 * Created by swsong on 2015. 11. 16..
 */
public class DocumentMerger {

    private List<FieldSetting> fields;
    public DocumentMerger(SchemaSetting schemaSetting) {
        fields = schemaSetting.getFieldSettingList();



    }

    public File merge(File... dirs) {



        return null;
    }
}
