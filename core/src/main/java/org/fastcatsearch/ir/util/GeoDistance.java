package org.fastcatsearch.ir.util;

/**
 * Created by swsong on 2015. 7. 14..
 */
public class GeoDistance {

    public double calDistance2(double lat1, double lon1, double lat2, double lon2) {

        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(lon1 - lon2));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }



    public double calDistance(double lat1, double lon1, double lat2, double lon2) {

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private double deg2rad(double deg) {
        return (double) (deg * Math.PI / (double) 180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private double rad2deg(double rad) {
        return (double) (rad * (double) 180d / Math.PI);
    }


    private double getDistance(double sLat, double sLong, double dLat, double dLong) {
        final int radius = 6371009;
        double uLat = Math.toRadians(sLat - dLat);
        double uLong = Math.toRadians(sLong - dLong);

        double a = Math.sin(uLat / 2) * Math.sin(uLat / 2) +
                Math.cos(Math.toRadians(sLong)) * Math.cos(Math.toRadians(dLong)) *
                        Math.sin(uLong / 2) * Math.sin(uLong / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = radius * c;

        return distance / 1000;
    }

    public static void main(String[] args) {
        GeoDistance d = new GeoDistance();

        double lat1 = 128, lon1 = 37, lat2 = 140, lon2 = 20;
        System.out.println("calDistance=" + d.calDistance(lat1, lon1, lat2, lon2));
        System.out.println("calDistance2=" + d.calDistance2(lat1, lon1, lat2, lon2));
        System.out.println("getDistance=" + d.getDistance(lat1, lon1, lat2, lon2));
    }
}
