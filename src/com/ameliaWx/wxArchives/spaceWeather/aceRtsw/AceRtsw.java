package com.ameliaWx.wxArchives.spaceWeather.aceRtsw;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.wxArchives.utils.FileTools;

public class AceRtsw {
	private FileTools ft;
	
	public static void main(String[] args) throws IOException {
		DateTime startTime = new DateTime(2025, 1, 1, 8, 46, 0, DateTimeZone.UTC);
		DateTime endTime = new DateTime(2025, 1, 1, 10, 5, 0, DateTimeZone.UTC);
		
		AceRtsw aceRtsw = new AceRtsw("");
		
		SolarWindImf data = aceRtsw.getSolarWindImf(startTime, endTime);
		
		SolarWindImfDatum datum = data.getSolarWindAtTime(endTime.minusHours(1));

//		System.out.println(datum.bt);
//		System.out.println(datum.bz);
		
		datum = data.getSolarWindAtTime(endTime.minusHours(1), true);

		System.out.println(datum.speed);
		System.out.println(datum.density);
		System.out.println(datum.bt);
		System.out.println(datum.bz);
	}

	public AceRtsw(String dataFolder) {
		this.ft = new FileTools(dataFolder);
	}
	
	public SolarWindImf getSolarWindImf() throws IOException {
		DateTime[] bounds = mostRecent24Hours();
		
		return getSolarWindImf(bounds[0], bounds[1]);
	}
	
	public SolarWindImf getSolarWindImf(DateTime startTime, DateTime endTime) throws IOException {
		SolarWindImf ret = new SolarWindImf();
		
		ArrayList<DateTime> dates = datesInRange(startTime, endTime);
		
		for(DateTime d : dates) {
			File plasmaData = ft.downloadFile(String.format("https://sohoftp.nascom.nasa.gov/sdb/goes/ace/daily/%04d%02d%02d_ace_swepam_1m.txt", d.getYear(), d.getMonthOfYear(), d.getDayOfMonth()), String.format("ace_solar_wind_%04d%02d%02d.txt", d.getYear(), d.getMonthOfYear(), d.getDayOfMonth()));
			File imfData = ft.downloadFile(String.format("https://sohoftp.nascom.nasa.gov/sdb/goes/ace/daily/%04d%02d%02d_ace_mag_1m.txt", d.getYear(), d.getMonthOfYear(), d.getDayOfMonth()), String.format("ace_imf_%04d%02d%02d.txt", d.getYear(), d.getMonthOfYear(), d.getDayOfMonth()));
			
			ret.addData(plasmaData, imfData, startTime, endTime);
			
			plasmaData.delete();
			imfData.delete();
		}
		
		return ret;
	}
	
	public ProtonElectronFlux getProtonElectronFlux() {
		DateTime[] bounds = mostRecent24Hours();
		
		return getProtonElectronFlux(bounds[0], bounds[1]);
	}
	
	public ProtonElectronFlux getProtonElectronFlux(DateTime startTime, DateTime endTime) {
		ArrayList<DateTime> dates = datesInRange(startTime, endTime);
		
		return null;
	}
	
	public DateTime[] mostRecent24Hours() {
		DateTime endTime = DateTime.now(DateTimeZone.UTC);
		
		DateTime startTime = endTime.minusDays(1);
		
		return new DateTime[] {startTime, endTime};
	}
	
	private ArrayList<DateTime> datesInRange(DateTime startTime, DateTime endTime) {
		ArrayList<DateTime> datesInRange = new ArrayList<>();
		
		DateTime runningTime = startTime.minusSeconds(startTime.getSecondOfDay());
		
		while (!runningTime.isAfter(endTime)) {
			datesInRange.add(runningTime);
			
			runningTime = runningTime.plusDays(1);
		}
		
		return datesInRange;
	}
	
	private ArrayList<DateTime> datesInRange(DateTime[] bounds) {
		return datesInRange(bounds[0], bounds[1]);
	}
}
