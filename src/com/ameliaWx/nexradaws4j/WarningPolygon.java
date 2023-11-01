package com.ameliaWx.nexradaws4j;

import java.util.ArrayList;

import org.joda.time.DateTime;

public class WarningPolygon extends Polygon {
	private DateTime startTime;
	private DateTime endTime;
	private WarningType warningType;
	
	public WarningPolygon(ArrayList<PointF> points, DateTime startTime, DateTime endTime, WarningType warningType) {
		super(points);
		this.startTime = startTime;
		this.endTime = endTime;
		this.warningType = warningType;
	}
	
	public boolean isActive(DateTime queryTime) {
		return (!queryTime.isBefore(startTime) && !queryTime.isAfter(endTime));
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public DateTime getEndTime() {
		return endTime;
	}

	public WarningType getWarningType() {
		return warningType;
	}
}
