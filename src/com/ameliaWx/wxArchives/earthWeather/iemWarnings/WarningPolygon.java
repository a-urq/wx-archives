package com.ameliaWx.wxArchives.earthWeather.iemWarnings;

import java.util.ArrayList;

import org.joda.time.DateTime;

import com.ameliaWx.wxArchives.PointF;
import com.ameliaWx.wxArchives.Polygon;

public class WarningPolygon extends Polygon {
	private DateTime startTime;
	private DateTime endTime;
	private WarningType warningType;
	private TornadoTag tornadoTag;
	private DamageTag damageTag;
	
	public WarningPolygon(ArrayList<PointF> points, DateTime startTime, DateTime endTime, 
			WarningType warningType, TornadoTag tornadoTag, DamageTag damageTag) {
		super(points);
		this.startTime = startTime;
		this.endTime = endTime;
		this.warningType = warningType;
		this.tornadoTag = tornadoTag;
		this.damageTag = damageTag;
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

	public TornadoTag getTornadoTag() {
		return tornadoTag;
	}

	public DamageTag getDamageTag() {
		return damageTag;
	}
}
