package com.ameliaWx.wxArchives.spaceWeather.aceRtsw;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class SolarWindImf {
	private ArrayList<DateTime> times = new ArrayList<>();
	private ArrayList<Double> density = new ArrayList<>();
	private ArrayList<Double> speed = new ArrayList<>();
	private ArrayList<Double> temperature = new ArrayList<>();
	private ArrayList<Double> bx = new ArrayList<>();
	private ArrayList<Double> by = new ArrayList<>();
	private ArrayList<Double> bz = new ArrayList<>();
	private ArrayList<Double> bt = new ArrayList<>();
	private ArrayList<Double> lat = new ArrayList<>();
	private ArrayList<Double> lon = new ArrayList<>();

	public SolarWindImf() {

	}

	public SolarWindImf(File plasmaData, File imfData, DateTime startTime, DateTime endTime) {
		this();

		addData(plasmaData, imfData, startTime, endTime);
	}

	public void addData(File plasmaData, File imfData, DateTime startTime, DateTime endTime) {
		startTime = startTime.toDateTime(DateTimeZone.UTC);
		endTime = endTime.toDateTime(DateTimeZone.UTC);
		
		try {
			Scanner scPlasma = new Scanner(plasmaData);
			Scanner scImf = new Scanner(imfData);

			while (scPlasma.hasNextLine()) {
				String line = scPlasma.nextLine();

				if (':' == line.charAt(0))
					continue;
				if ('#' == line.charAt(0))
					continue;

				String[] tokens = line.split("\\s+");

				int year = Integer.valueOf(tokens[0]);
				int month = Integer.valueOf(tokens[1]);
				int day = Integer.valueOf(tokens[2]);
				int hour = Integer.valueOf(tokens[3].substring(0, 2));
				int minute = Integer.valueOf(tokens[3].substring(2, 4));

				DateTime time = new DateTime(year, month, day, hour, minute, DateTimeZone.UTC);
				
//				System.out.print("\n" + time);
//				System.out.println(startTime);
//				System.out.println(time);
//				System.out.println(endTime);

				if (!time.isBefore(startTime) && !time.isAfter(endTime)) {
					times.add(time);
//					System.out.print(" added.");

					double den = Double.valueOf(tokens[7]);
					double spd = Double.valueOf(tokens[8]);
					double tmp = Double.valueOf(tokens[9]);

//					System.out.println(den + "\t" + spd + "\t" + tmp);
					
					if (den == -9999.9)
						den = Double.NaN;
					if (spd == -9999.9)
						spd = Double.NaN;
					if (tmp == -100000)
						tmp = Double.NaN;

					density.add(den);
					speed.add(spd);
					temperature.add(tmp);
				}
			}

			while (scImf.hasNextLine()) {
				String line = scImf.nextLine();

				if (':' == line.charAt(0))
					continue;
				if ('#' == line.charAt(0))
					continue;

				String[] tokens = line.split("\\s+");

				int year = Integer.valueOf(tokens[0]);
				int month = Integer.valueOf(tokens[1]);
				int day = Integer.valueOf(tokens[2]);
				int hour = Integer.valueOf(tokens[3].substring(0, 2));
				int minute = Integer.valueOf(tokens[3].substring(2, 4));

				DateTime time = new DateTime(year, month, day, hour, minute, DateTimeZone.UTC);

				if (!time.isBefore(startTime) && !time.isAfter(endTime)) {
					double bx_ = Double.valueOf(tokens[7]);
					double by_ = Double.valueOf(tokens[8]);
					double bz_ = Double.valueOf(tokens[9]);
					double bt_ = Double.valueOf(tokens[10]);
					double lat_ = Double.valueOf(tokens[11]);
					double lon_ = Double.valueOf(tokens[12]);

					if (bx_ == -999.9)
						bx_ = Double.NaN;
					if (by_ == -999.9)
						by_ = Double.NaN;
					if (bz_ == -999.9)
						bz_ = Double.NaN;
					if (bt_ == -999.9)
						bt_ = Double.NaN;
					if (lat_ == -999.9)
						lat_ = Double.NaN;
					if (lon_ == -999.9)
						lon_ = Double.NaN;

					bx.add(bx_);
					by.add(by_);
					bz.add(bz_);
					bt.add(bt_);
					lat.add(lat_);
					lon.add(lon_);
				}
			}

			scPlasma.close();
			scImf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public SolarWindImfDatum getSolarWindAtTime(DateTime dateTime) {
		return getSolarWindAtTime(dateTime, false);
	}

	private static final double L1_DISTANCE = 1501557.0; // km
	
	public SolarWindImfDatum getSolarWindAtTime(DateTime dateTime, boolean atEarth) {
		dateTime = dateTime.toDateTime(DateTimeZone.UTC);
		
		if(atEarth) {
			SolarWindImfDatum datumAtAce = getSolarWindAtTime(dateTime, false);
			
			double speed = datumAtAce.speed; // km/sec
			
			int minuteOffset = (int) Math.round((L1_DISTANCE / speed) / 60);
			
			return getSolarWindAtTime(dateTime.minusMinutes(minuteOffset), false);
		} else {
			SolarWindImfDatum datum = new SolarWindImfDatum();
			
			boolean searching = true;
	
			double indexMove = -99;
			
			if(dateTime.isBefore(times.get(0))) {
				return datum;
			}
			
			if(dateTime.isAfter(times.get(times.size() - 1))) {
				return datum;
			}
	
			int index = times.size() / 2;
			
			while (searching) {
				if (indexMove == -99) {
					indexMove = index / 2.0;
				} else {
					indexMove /= 2.0;
				}
	
				DateTime timeAtIndex = times.get(index);
	
				if (timeAtIndex.isEqual(dateTime)) {
					return createDatum(index);
				} else if (timeAtIndex.isBefore(dateTime)) {
					index += Math.ceil(indexMove);
				} else if (timeAtIndex.isAfter(dateTime)) {
					index -= Math.ceil(indexMove);
				}
			}
	
			return datum; // line should never be reached, just here to make it compile
		}
	}

	private SolarWindImfDatum createDatum(int index) {
		SolarWindImfDatum datum = new SolarWindImfDatum();
		
		datum.time = times.get(index);
		
		datum.density = density.get(index);
		datum.speed = speed.get(index);
		datum.temperature = temperature.get(index);
		
		datum.bx = bx.get(index);
		datum.by = by.get(index);
		datum.bz = bz.get(index);
		datum.bt = bt.get(index);
		datum.lat = lat.get(index);
		datum.lon = lon.get(index);
		
		return datum;
	}
}
