package com.ameliaWx.wxArchives;

public class PointF {
	private float x;
	private float y;

	public PointF(double x, double y) {
		this.x = (float) x;
		this.y = (float) y;
	}

	public PointF(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void add(PointF p) {
		x += p.x;
		y += p.y;
	}

	public void subtract(PointF p) {
		x -= p.x;
		y -= p.y;
	}

	public PointF unitVector() {
		return new PointF(x / Math.hypot(x, y), y / Math.hypot(x, y));
	}

	public PointF projectToStereographic(double lonOffset) {
		return new PointF(
				(Math.cos(Math.toRadians(-y)) * Math.cos(Math.toRadians(lonOffset - x)))
						/ (1 - Math.asin(Math.toRadians(-y))),
				(Math.cos(Math.toRadians(-y)) * Math.sin(Math.toRadians(lonOffset - x)))
						/ (1 - Math.asin(Math.toRadians(-y))));
	}

	public void mult(double s) {
		x *= s;
		y *= s;
	}

	public static PointF add(PointF p, PointF q) {
		return new PointF(p.x + q.x, p.y + q.y);
	}

	public static PointF subtract(PointF p, PointF q) {
		return new PointF(p.x - q.x, p.y - q.y);
	}

	public static PointF unitVector(PointF p) {
		return new PointF(p.x / Math.hypot(p.x, p.y), p.y / Math.hypot(p.x, p.y));
	}

	public static PointF mult(PointF p, double s) {
		// System.out.println(p.x);
		// System.out.println(p.y);
		// System.out.println(new PointD(p.x * s, p.y * s));
		return new PointF(p.x * s, p.y * s);
	}

	@Override
	public String toString() {
		return "PointD [x=" + x + ", y=" + y + "]";
	}
}
