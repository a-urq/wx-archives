package com.ameliaWx.wxArchives.test;

import com.ameliaWx.wxArchives.earthWeather.goes.GoesAws;
import com.ameliaWx.wxArchives.earthWeather.goes.SatelliteSector;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

public class S3Test {
    public static void main(String[] args) {
        DateTime time = new DateTime(2025, 5, 25, 21, 50, 0, DateTimeZone.UTC);

        List<String> satFilesCurr = new ArrayList<>();

        satFilesCurr = GoesAws.goes19Level1Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
                time.getHourOfDay(), SatelliteSector.GOES_CONUS);

        System.out.println("satFiles.size(): " + satFilesCurr.size());

        for (String str : satFilesCurr) {
            System.out.println("satFile: " + str);
        }

    }
}
