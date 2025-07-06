package com.ameliaWx.wxArchives.spaceWeather.goesMag;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ameliaWx.wxArchives.AwsClientSingleton;
import com.ameliaWx.wxArchives.utils.FileTools;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class GoesMag {
	private static FileTools ft = new FileTools("goes-mag");
	
	public static void main(String[] args) throws IOException {
		PrintWriter pw = new PrintWriter("/home/a-urq/Coding Projects/Python/random-mpl/2024-06-27-mag-east-1sec.csv");
		
//		DateTime startTime = new DateTime(2025, 1, 1, 9, 0, DateTimeZone.UTC);
//		DateTime endTime = new DateTime(2025, 1, 1, 11, 0, DateTimeZone.UTC);

		DateTime startTime = new DateTime(2024, 6, 28, 0, 0, DateTimeZone.UTC);
		DateTime endTime = new DateTime(2024, 6, 28, 12, 0, DateTimeZone.UTC);
		
		TreeMap<DateTime, Float> files = goesEastMagData(startTime, endTime);
		
		for(DateTime dt : files.keySet()) {
			pw.println((dt.getMillis() - startTime.getMillis())/1000.0 + ", " + files.get(dt));
		}
		pw.close();
	}

	private static final DateTime GOES_17_18_OPERATIONAL_CUTOFF = new DateTime(2023, 1, 3, 0, 0, DateTimeZone.UTC);
	private static final DateTime GOES_16_19_OPERATIONAL_CUTOFF = new DateTime(2025, 4, 7, 15, 0, DateTimeZone.UTC);

	public static HashMap<String, MagTimeseries> getGoesMagData(DateTime startTime, DateTime endTime) throws IOException {
		startTime = startTime.toDateTime(DateTimeZone.UTC);
		endTime = endTime.toDateTime(DateTimeZone.UTC);
		
		MagTimeseries goesEast = goesEastMagTimeseries(startTime, endTime);
		MagTimeseries goesWest = goesWestMagTimeseries(startTime, endTime);
		
		HashMap<String, MagTimeseries> magData = new HashMap<>();
		magData.put("East", goesEast);
		magData.put("West", goesWest);
		
		return magData;
	}
	
	private static MagTimeseries goesEastMagTimeseries(DateTime startTime, DateTime endTime) throws IOException {
		TreeMap<DateTime, Float> magData = goesEastMagData(startTime, endTime);
		
		ArrayList<DateTime> time = new ArrayList<>();
		ArrayList<Float> hp = new ArrayList<>();
		for(DateTime dt : magData.keySet()) {
			time.add(dt);
			hp.add(magData.get(dt));
		}
		
		MagTimeseries timeseries = new MagTimeseries(time, hp);
		return timeseries;
	}
	
	private static MagTimeseries goesWestMagTimeseries(DateTime startTime, DateTime endTime) throws IOException {
		TreeMap<DateTime, Float> magData = goesWestMagData(startTime, endTime);
		
		ArrayList<DateTime> time = new ArrayList<>();
		ArrayList<Float> hp = new ArrayList<>();
		for(DateTime dt : magData.keySet()) {
			time.add(dt);
			hp.add(magData.get(dt));
		}
		
		MagTimeseries timeseries = new MagTimeseries(time, hp);
		return timeseries;
	}

	@SuppressWarnings("deprecation")
	private static TreeMap<DateTime, Float> goesEastMagData(DateTime startTime, DateTime endTime) throws IOException {
		TreeMap<DateTime, String> files = goesEastFiles(startTime, endTime);
		
		TreeMap<DateTime, Float> magData = new TreeMap<>();
		for(DateTime dt : files.keySet()) {
			String fileUrl = files.get(dt);

			System.out.println(dateTimeString(dt));
			File dataFile = ft.downloadFile(fileUrl, "mag-" + dateTimeString(dt) + ".nc");
			NetcdfFile ncfile = NetcdfFile.open(dataFile.getAbsolutePath());
			
			float[][][] ambMagEPN = readVariable3Dim(ncfile, "amb_mag_EPN");
			
			float[][] ambMagHp = new float[ambMagEPN.length][ambMagEPN[0].length];
			for(int i = 0; i < ambMagEPN.length; i++) {
				for(int j = 0; j < ambMagEPN[i].length; j++) {
					ambMagHp[i][j] = ambMagEPN[i][j][1];
				}
			}
			
			float magHp = mean(ambMagHp);

			for(int i = 0; i < ambMagHp.length; i++) {
				magData.put(dt.plusSeconds(i), mean(ambMagHp[i]));
			}
			
//			magData.put(dt, magHp);
		}
		
		return magData;
	}

	@SuppressWarnings("deprecation")
	private static TreeMap<DateTime, Float> goesWestMagData(DateTime startTime, DateTime endTime) throws IOException {
		TreeMap<DateTime, String> files = goesWestFiles(startTime, endTime);
		
		TreeMap<DateTime, Float> magData = new TreeMap<>();
		for(DateTime dt : files.keySet()) {
			String fileUrl = files.get(dt);
			
			System.out.println(dateTimeString(dt));
			File dataFile = ft.downloadFile(fileUrl, "mag-" + dateTimeString(dt) + ".nc");
			NetcdfFile ncfile = NetcdfFile.open(dataFile.getAbsolutePath());
			
			float[][][] ambMagEPN = readVariable3Dim(ncfile, "amb_mag_EPN");
			
			float[][] ambMagHp = new float[ambMagEPN.length][ambMagEPN[0].length];
			for(int i = 0; i < ambMagEPN.length; i++) {
				for(int j = 0; j < ambMagEPN[i].length; j++) {
					ambMagHp[i][j] = ambMagEPN[i][j][1];
				}
			}
			
			float magHp = mean(ambMagHp);

			for(int i = 0; i < ambMagHp.length; i++) {
				magData.put(dt.plusSeconds(i), mean(ambMagHp[i]));
			}
			
//			magData.put(dt, magHp);
		}
		
		return magData;
	}
	
	private static TreeMap<DateTime, String> goesEastFiles(DateTime startTime, DateTime endTime) {
		TreeMap<DateTime, String> files = new TreeMap<>();

		DateTime time = startTime;

		while (!time.isAfter(endTime)) {
			List<String> filesInHour;
			if (time.isBefore(GOES_16_19_OPERATIONAL_CUTOFF)) {
				filesInHour = goes16Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay());
			} else {
				filesInHour = goes19Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay());
			}
			
			System.out.println(filesInHour);

			for (String file : filesInHour) {
				String[] fileTokens = file.split("/");
				String filename = fileTokens[fileTokens.length - 1];

				int year = Integer.valueOf(filename.substring(21, 25));
				int dayOfYear = Integer.valueOf(filename.substring(25, 28));
				int hour = Integer.valueOf(filename.substring(28, 30));
				int minute = Integer.valueOf(filename.substring(30, 32));
				int second = Integer.valueOf(filename.substring(32, 34));

				DateTime fileTime = new DateTime(year, 1, 1, hour, minute, second, DateTimeZone.UTC);
				fileTime = fileTime.dayOfYear().setCopy(dayOfYear);

				if (!fileTime.isBefore(startTime) && !fileTime.isAfter(endTime)) {
					files.put(fileTime, file);
				}
			}

			time = time.plusHours(1);
		}
		
		return files;
	}
	
	private static TreeMap<DateTime, String> goesWestFiles(DateTime startTime, DateTime endTime) {
		TreeMap<DateTime, String> files = new TreeMap<>();

		DateTime time = startTime;

		while (!time.isAfter(endTime)) {
			List<String> filesInHour;
			if (time.isBefore(GOES_17_18_OPERATIONAL_CUTOFF)) {
				filesInHour = goes17Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay());
			} else {
				filesInHour = goes18Files(time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
						time.getHourOfDay());
			}

			for (String file : filesInHour) {
				String[] fileTokens = file.split("/");
				String filename = fileTokens[fileTokens.length - 1];

				int year = Integer.valueOf(filename.substring(21, 25));
				int dayOfYear = Integer.valueOf(filename.substring(25, 28));
				int hour = Integer.valueOf(filename.substring(28, 30));
				int minute = Integer.valueOf(filename.substring(30, 32));
				int second = Integer.valueOf(filename.substring(32, 34));

				DateTime fileTime = new DateTime(year, 1, 1, hour, minute, second, DateTimeZone.UTC);
				fileTime = fileTime.dayOfYear().setCopy(dayOfYear);

				if (!fileTime.isBefore(startTime) && !fileTime.isAfter(endTime)) {
					files.put(fileTime, file);
				}
			}

			time = time.plusHours(1);
		}

		return files;
	}

	/**
	 * Returns a list of links to all GOES-16 MAG files given a date and hour
	 * 
	 * @param year  Year
	 * @param month Month of Year
	 * @param day   Day of Month
	 * @param hour  Day of Month
	 */
	private static List<String> goes16Files(int year, int month, int day, int hour) {
		final String dataType = "MAG-L1b-GEOF"; // doublecheck

		DateTime dt = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

		int dayOfYear = dt.getDayOfYear();

		String folderKey = String.format("%4s/%04d/%03d/%02d/", dataType, year, dayOfYear, hour);

		ListObjectsRequest request = new ListObjectsRequest().withBucketName("noaa-goes16").withPrefix(folderKey)
				.withDelimiter("/");

		ObjectListing objectListing;

		List<S3ObjectSummary> objectsInFolder = AwsClientSingleton.s3Client.listObjects("noaa-goes16", folderKey)
				.getObjectSummaries();

		do {
			objectListing = AwsClientSingleton.s3Client.listObjects(request);
			objectsInFolder.addAll(objectListing.getObjectSummaries());

			// Prepare for the next request if more objects are available
			request.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());

		List<String> links = new ArrayList<>();
		for (S3ObjectSummary object : objectsInFolder) {
			String key = object.getKey();

			links.add("https://noaa-goes16.s3.amazonaws.com/" + key);
		}

		return links;
	}

	/**
	 * Returns a list of links to all GOES-17 MAG files given a date and hour
	 * 
	 * @param year  Year
	 * @param month Month of Year
	 * @param day   Day of Month
	 * @param hour  Day of Month
	 */
	private static List<String> goes17Files(int year, int month, int day, int hour) {
		final String dataType = "MAG-L1b-GEOF"; // doublecheck

		DateTime dt = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

		int dayOfYear = dt.getDayOfYear();

		String folderKey = String.format("%4s/%04d/%03d/%02d/", dataType, year, dayOfYear, hour);

		ListObjectsRequest request = new ListObjectsRequest().withBucketName("noaa-goes17").withPrefix(folderKey)
				.withDelimiter("/");

		ObjectListing objectListing;

		List<S3ObjectSummary> objectsInFolder = AwsClientSingleton.s3Client.listObjects("noaa-goes17", folderKey)
				.getObjectSummaries();

		do {
			objectListing = AwsClientSingleton.s3Client.listObjects(request);
			objectsInFolder.addAll(objectListing.getObjectSummaries());

			// Prepare for the next request if more objects are available
			request.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());

		List<String> links = new ArrayList<>();
		for (S3ObjectSummary object : objectsInFolder) {
			String key = object.getKey();

			links.add("https://noaa-goes17.s3.amazonaws.com/" + key);
		}

		return links;
	}

	/**
	 * Returns a list of links to all GOES-18 MAG files given a date and hour
	 * 
	 * @param year  Year
	 * @param month Month of Year
	 * @param day   Day of Month
	 * @param hour  Day of Month
	 */
	private static List<String> goes18Files(int year, int month, int day, int hour) {
		final String dataType = "MAG-L1b-GEOF"; // doublecheck

		DateTime dt = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

		int dayOfYear = dt.getDayOfYear();

		String folderKey = String.format("%4s/%04d/%03d/%02d/", dataType, year, dayOfYear, hour);

		ListObjectsRequest request = new ListObjectsRequest().withBucketName("noaa-goes18").withPrefix(folderKey)
				.withDelimiter("/");

		ObjectListing objectListing;

		List<S3ObjectSummary> objectsInFolder = AwsClientSingleton.s3Client.listObjects("noaa-goes18", folderKey)
				.getObjectSummaries();

		do {
			objectListing = AwsClientSingleton.s3Client.listObjects(request);
			objectsInFolder.addAll(objectListing.getObjectSummaries());

			// Prepare for the next request if more objects are available
			request.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());

		List<String> links = new ArrayList<>();
		for (S3ObjectSummary object : objectsInFolder) {
			String key = object.getKey();

			links.add("https://noaa-goes18.s3.amazonaws.com/" + key);
		}

		return links;
	}

	/**
	 * Returns a list of links to all GOES-19 MAG files given a date and hour
	 * 
	 * @param year  Year
	 * @param month Month of Year
	 * @param day   Day of Month
	 * @param hour  Day of Month
	 */
	private static List<String> goes19Files(int year, int month, int day, int hour) {
		final String dataType = "MAG-L1b-GEOF"; // doublecheck

		DateTime dt = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

		int dayOfYear = dt.getDayOfYear();

		String folderKey = String.format("%4s/%04d/%03d/%02d/", dataType, year, dayOfYear, hour);

		ListObjectsRequest request = new ListObjectsRequest().withBucketName("noaa-goes19").withPrefix(folderKey)
				.withDelimiter("/");

		ObjectListing objectListing;

		List<S3ObjectSummary> objectsInFolder = AwsClientSingleton.s3Client.listObjects("noaa-goes19", folderKey)
				.getObjectSummaries();

		do {
			objectListing = AwsClientSingleton.s3Client.listObjects(request);
			objectsInFolder.addAll(objectListing.getObjectSummaries());

			// Prepare for the next request if more objects are available
			request.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());

		List<String> links = new ArrayList<>();
		for (S3ObjectSummary object : objectsInFolder) {
			String key = object.getKey();

			links.add("https://noaa-goes19.s3.amazonaws.com/" + key);
		}

		return links;
	}

	private static float[][] readVariable2Dim(NetcdfFile ncfile, String varName) {
		Variable var = ncfile.findVariable(varName);
		return readVariable2Dim(var);
	}

	private static float[][] readVariable2Dim(Variable rawData) {
		int[] shape = rawData.getShape();
		Array _data = null;

		try {
			_data = rawData.read();
		} catch (IOException e) {
			e.printStackTrace();
			return new float[shape[0]][shape[1]];
		}

		float[][] data = new float[shape[0]][shape[1]];
		// see if an alternate data-reading algorithm that avoids division and modulos
		// could be faster
		for (int i = 0; i < _data.getSize(); i++) {
			int x = i % shape[0];
			int y = (i / shape[0]);

			float record = _data.getFloat(i);

			data[x][y] = record;
		}

		return data;
	}

	private static float[][][] readVariable3Dim(NetcdfFile ncfile, String varName) {
		Variable var = ncfile.findVariable(varName);
		return readVariable3Dim(var);
	}
	
	private static float[][][] readVariable3Dim(Variable rawData) {
		int[] shape = rawData.getShape();
		Array _data = null;

		try {
			_data = rawData.read();
		} catch (IOException e) {
			e.printStackTrace();
			return new float[shape[0]][shape[1]][shape[2]];
		}

		float[][][] data = new float[shape[0]][shape[1]][shape[2]];
		for (int i = 0; i < _data.getSize(); i++) {
			int x = i % shape[2];
			int y = (i / shape[2]) % shape[1];
			int t = (i / (shape[2] * shape[1])) % shape[0];

			float record = _data.getFloat(i);

			data[t][shape[1] - 1 - y][x] = record;
		}

		return data;
	}
	
	private static float mean(float[] arr) {
		float sum = 0;
		
		for(int i = 0; i < arr.length; i++) {
			sum += arr[i];
		}
		
		return sum/arr.length;
	}
	
	private static float mean(float[][] arr) {
		float sum = 0;
		
		for(int i = 0; i < arr.length; i++) {
			sum += mean(arr[i]);
		}
		
		return sum/arr.length;
	}

	private static String dateTimeString(DateTime dt) {
		String timeStr = String.format("%04d%02d%02d-%02d%02d%02d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(), dt.getHourOfDay(), dt.getMinuteOfHour(), dt.getSecondOfMinute());
		
		return timeStr;
	}
}
