package com.ameliaWx.wxArchives.spaceWeather.aceRtsw;

import org.joda.time.DateTime;

public class SolarWindImfDatum {
	public DateTime time;
	public double density;
	public double speed;
	public double temperature;
	public double bx;
	public double by;
	public double bz;
	public double bt;
	public double lat;
	public double lon;
	
	public SolarWindImfDatum() {
		time = null;
		
		density = Double.NaN;
		speed = Double.NaN;
		temperature = Double.NaN;
		
		bx = Double.NaN;
		by = Double.NaN;
		bz = Double.NaN;
		bt = Double.NaN;
		lat = Double.NaN;
		lon = Double.NaN;
	}
}
