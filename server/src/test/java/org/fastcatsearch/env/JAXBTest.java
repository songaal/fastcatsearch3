package org.fastcatsearch.env;

import org.fastcatsearch.settings.NodeListSettings;
import org.fastcatsearch.util.JAXBConfigs;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.io.StringWriter;

public class JAXBTest {

    private static Logger logger = LoggerFactory.getLogger(JAXBTest.class);

    @Test
    public void testReadWrite() throws JAXBException {
        String configStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<node-list>\n" +
                "    <node address=\"192.168.1.1\" enabled=\"true\" id=\"node1\" name=\"node1\" port=\"9090\"/>\n" +
                "    <node address=\"192.168.1.2\" dataAddress=\"10.10.2.2\" enabled=\"true\" id=\"node2\" name=\"node2\" port=\"9090\"/>\n" +
                "    <node address=\"192.168.1.3\" dataAddress=\"\" enabled=\"true\" id=\"node3\" name=\"node3\" port=\"9090\"/>\n" +
                "</node-list>";
        logger.info("========== configStr1 > \n{}", configStr);
        StringReader reader = new StringReader(configStr);
        NodeListSettings nodeListSettings = JAXBConfigs.readConfig(reader, NodeListSettings.class);
        logger.info("========== configStr-read > \n{}", nodeListSettings);

        StringWriter writer = new StringWriter();
        JAXBConfigs.writeRawConfig(writer, nodeListSettings, NodeListSettings.class);
        String configWrite = writer.toString();
        logger.info("========== configStr-write > \n{}", configWrite);

    }
}
