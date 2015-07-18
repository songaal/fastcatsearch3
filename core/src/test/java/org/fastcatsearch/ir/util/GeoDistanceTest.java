package org.fastcatsearch.ir.util;

import org.junit.Test;

/**
 * Created by swsong on 2015. 7. 18..
 */
public class GeoDistanceTest {

    @Test
    public void test1() {
        GeoDistance d = new GeoDistance();
        double lat1 = 37.86811;
        double lon1 = 127.70041;
        double lat2 = 37.50448;
        double lon2 = 127.04894;
        double distance = d.calDistance(lat1, lon1, lat2, lon2);
        System.out.println(distance / 1000.0 +" km");
    }
}
