package org.fastcatsearch.ir;

import java.util.concurrent.TimeUnit;

/**
 * 이름처럼, 시간제한이 있고, 사이즈도 정해져 있는 롤링가능한 로거다.
 * 롤링은 계속 새로운 이름의 파일이 생성되고 지우는 일은 외부에서 전담한다.
 *
 * Created by swsong on 2016. 1. 13..
 */
public class TimeLimitFixedRollingFileLogger {
    private int sizeLimit;
    private int timeLimit;
    private TimeUnit timeUnit;

    public TimeLimitFixedRollingFileLogger(int sizeLimit, int timeLimit, TimeUnit timeUnit) {
        this.sizeLimit = sizeLimit;
        this.timeLimit = timeLimit;
        this.timeUnit = timeUnit;
    }

    public void writeLine(String line) {

    }


    class RollingWorker extends Thread {

    }
}
