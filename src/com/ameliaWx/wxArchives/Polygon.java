package com.ameliaWx.wxArchives;

import java.util.ArrayList;

public class Polygon {
	private ArrayList<PointF> points;

	public Polygon(ArrayList<PointF> points) {
		init(points);
	}
	
	public Polygon(PointF... points) {
		ArrayList<PointF> pointsList = new ArrayList<>();
		
		for(int i = 0; i < points.length; i++) {
			pointsList.add(points[i]);
		}

		init(pointsList);
	}
	
	private void init(ArrayList<PointF> points) {
		this.points = points;
	}

	public ArrayList<PointF> getPoints() {
		return points;
	}
}
