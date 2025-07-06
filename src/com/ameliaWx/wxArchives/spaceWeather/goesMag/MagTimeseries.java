package com.ameliaWx.wxArchives.spaceWeather.goesMag;

import java.util.ArrayList;

import org.joda.time.DateTime;

public class MagTimeseries {
	private ArrayList<DateTime> time1sec;
	private ArrayList<Float> hp1sec;
	
	private ArrayList<DateTime> time1min;
	private ArrayList<Float> hp1min;
	
	// assumes sorted from start time to end time
	public MagTimeseries(ArrayList<DateTime> time1sec, ArrayList<Float> hp1sec) {
		this.time1sec = time1sec;
		this.hp1sec = hp1sec;
		
		assert time1sec.size() == hp1sec.size();

		time1min = new ArrayList<>();
		hp1min = new ArrayList<>();
		
		for(int i = 0; i < time1sec.size(); i+=60) {
			float sum = 0;
			
			for(int j = 0; j < 60; j++) {
				sum += hp1sec.get(i + j);
			}
			
			float hpm = sum/60.0f;
			
			time1min.add(time1sec.get(i));
			hp1min.add(hpm);
		}
	}
	
	public float hp1secAvg(DateTime queryTime) {
		if(!queryTime.isAfter(time1sec.get(0))) {
			return hp1sec.get(0);
		} else if (!queryTime.isBefore(time1sec.get(time1sec.size() - 1))) {
			return hp1sec.get(hp1sec.size() - 1);
		} else {
			for(int i = 0; i < time1sec.size() - 1; i++) {
				DateTime timeEarly = time1sec.get(i);
				DateTime timeLate = time1sec.get(i + 1);
				
				if(queryTime.isEqual(timeEarly)) {
					return hp1sec.get(i);
				} else if(queryTime.isAfter(timeEarly) && queryTime.isBefore(timeLate)) {
					long duration1 = timeLate.getMillis() - queryTime.getMillis();
					long duration2 = queryTime.getMillis() - timeEarly.getMillis();
					long duration3 = timeLate.getMillis() - timeEarly.getMillis();
					
					float weight1 = (float) duration1 / duration3;
					float weight2 = (float) duration2 / duration3;
					float hp1 = hp1sec.get(i);
					float hp2 = hp1sec.get(i + 1);
					
					return weight1 * hp1 + weight2 * hp2;
				} else {
					continue;
				}
			}
			
			return hp1sec.get(hp1sec.size() - 1);
		}
	}
	
	public float hp1minAvg(DateTime queryTime) {
		if(!queryTime.isAfter(time1min.get(0))) {
			return hp1min.get(0);
		} else if (!queryTime.isBefore(time1min.get(time1min.size() - 1))) {
			return hp1min.get(hp1min.size() - 1);
		} else {
			for(int i = 0; i < time1min.size() - 1; i++) {
				DateTime timeEarly = time1min.get(i);
				DateTime timeLate = time1min.get(i + 1);
				
				if(queryTime.isEqual(timeEarly)) {
					return hp1min.get(i);
				} else if(queryTime.isAfter(timeEarly) && queryTime.isBefore(timeLate)) {
					long duration1 = timeLate.getMillis() - queryTime.getMillis();
					long duration2 = queryTime.getMillis() - timeEarly.getMillis();
					long duration3 = timeLate.getMillis() - timeEarly.getMillis();
					
					float weight1 = (float) duration1 / duration3;
					float weight2 = (float) duration2 / duration3;
					float hp1 = hp1min.get(i);
					float hp2 = hp1min.get(i + 1);
					
					return weight1 * hp1 + weight2 * hp2;
				} else {
					continue;
				}
			}
			
			return hp1min.get(hp1min.size() - 1);
		}
	}
}
