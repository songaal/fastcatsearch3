package org.fastcatsearch.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * Created by 전제현 on 2017. 4. 25..
 */
public abstract class TelegramSender {
    private static Logger logger = LoggerFactory.getLogger(SMSSender.class);
    protected Properties properties;

    public TelegramSender(Properties properties) {
        this.properties = properties;
    }

    public abstract void send(List<String> telegramToList, String messageString);

}