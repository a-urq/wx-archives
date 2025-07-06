package com.ameliaWx.wxArchives.earthWeather.goes;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ameliaWx.wxArchives.AwsClientSingleton;

public class GoesAws {
	/**
	 * 
	 * @author Amelia Urquhart (github.com/a-urq)
	 */

	/**
	 * Returns a list of links to all GOES-16 files given a date, hour, data type
	 * and sector
	 * 
	 * @param year     Year
	 * @param month    Month of Year
	 * @param day      Day of Month
	 * @param hour     Day of Month
	 * @param dataType The type of GOES data to pull
	 * @param sector   GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes16Files(int year, int month, int day, int hour, String dataType,
			SatelliteSector sector) {
		String sectorIdentifier = "";

		switch (sector) {
		case GOES_PACUS:
			sectorIdentifier = "C";
			break;
		case GOES_CONUS:
			sectorIdentifier = "C";
			break;
		case GOES_FULL_DISK:
			sectorIdentifier = "F";
			break;
		case GOES_MESOSCALE_1:
			sectorIdentifier = "M";
			break;
		case GOES_MESOSCALE_2:
			sectorIdentifier = "M";
			break;
		default:
			sectorIdentifier = "C";
			break;
		}

		DateTime dt = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

		int dayOfYear = dt.getDayOfYear();

		String folderKey = String.format("%4s/%04d/%03d/%02d/", dataType + sectorIdentifier, year, dayOfYear, hour);
		
		ListObjectsRequest request = new ListObjectsRequest()
		        .withBucketName("noaa-goes16")
		        .withPrefix(folderKey)
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

		if(sector == SatelliteSector.GOES_MESOSCALE_1 || 
				sector == SatelliteSector.GOES_MESOSCALE_2) {
			for (S3ObjectSummary object : objectsInFolder) {
				String key = object.getKey();

				if(sector == SatelliteSector.GOES_MESOSCALE_1 && key.contains("M1")) {
					links.add("https://noaa-goes16.s3.amazonaws.com/" + key);
				} else if(sector == SatelliteSector.GOES_MESOSCALE_2 && key.contains("M2")) {
					links.add("https://noaa-goes16.s3.amazonaws.com/" + key);
				}
			}
		} else {
			for (S3ObjectSummary object : objectsInFolder) {
				String key = object.getKey();
		
				links.add("https://noaa-goes16.s3.amazonaws.com/" + key);
			}
		}

		return links;
	}

	/**
	 * Returns a list of links to all GOES-16 Multichannel Cloud and Moisture
	 * Imagery Product files given a date, hour, and sector.
	 * 
	 * @param year   Year
	 * @param month  Month of Year
	 * @param day    Day of Month
	 * @param hour   Day of Month
	 * @param sector GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes16Level2Files(int year, int month, int day, int hour, SatelliteSector sector) {
		return goes16Files(year, month, day, hour, "ABI-L2-MCMIP", sector);
	}

	/**
	 * Returns a list of links to all GOES-16 Band Radiance files given a date,
	 * hour, and sector.
	 * 
	 * @param year   Year
	 * @param month  Month of Year
	 * @param day    Day of Month
	 * @param hour   Day of Month
	 * @param sector GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes16Level1Files(int year, int month, int day, int hour, SatelliteSector sector) {
		return goes16Files(year, month, day, hour, "ABI-L1b-Rad", sector);
	}

	/**
	 * Returns a list of links to all GOES-16 files given a date, hour, data type
	 * and sector
	 * 
	 * @param year     Year
	 * @param month    Month of Year
	 * @param day      Day of Month
	 * @param hour     Day of Month
	 * @param dataType The type of GOES data to pull
	 * @param sector   GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes17Files(int year, int month, int day, int hour, String dataType,
			SatelliteSector sector) {
		String sectorIdentifier = "";

		switch (sector) {
		case GOES_PACUS:
			sectorIdentifier = "C";
			break;
		case GOES_CONUS:
			sectorIdentifier = "C";
			break;
		case GOES_FULL_DISK:
			sectorIdentifier = "F";
			break;
		case GOES_MESOSCALE_1:
			sectorIdentifier = "M";
			break;
		case GOES_MESOSCALE_2:
			sectorIdentifier = "M";
			break;
		default:
			sectorIdentifier = "C";
			break;
		}

		DateTime dt = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

		int dayOfYear = dt.getDayOfYear();

		String folderKey = String.format("%4s/%04d/%03d/%02d/", dataType + sectorIdentifier, year, dayOfYear, hour);

		List<S3ObjectSummary> objectsInFolder = AwsClientSingleton.s3Client.listObjects("noaa-goes17", folderKey)
				.getObjectSummaries();

		List<String> links = new ArrayList<>();

		if(sector == SatelliteSector.GOES_MESOSCALE_1 || 
				sector == SatelliteSector.GOES_MESOSCALE_2) {
			for (S3ObjectSummary object : objectsInFolder) {
				String key = object.getKey();

				if(sector == SatelliteSector.GOES_MESOSCALE_1 && key.contains("M1")) {
					links.add("https://noaa-goes17.s3.amazonaws.com/" + key);
				} else if(sector == SatelliteSector.GOES_MESOSCALE_2 && key.contains("M2")) {
					links.add("https://noaa-goes17.s3.amazonaws.com/" + key);
				}
			}
		} else {
			for (S3ObjectSummary object : objectsInFolder) {
				String key = object.getKey();
		
				links.add("https://noaa-goes17.s3.amazonaws.com/" + key);
			}
		}

		return links;
	}

	/**
	 * Returns a list of links to all GOES-16 Multichannel Cloud and Moisture
	 * Imagery Product files given a date, hour, and sector.
	 * 
	 * @param year   Year
	 * @param month  Month of Year
	 * @param day    Day of Month
	 * @param hour   Day of Month
	 * @param sector GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes17Level2Files(int year, int month, int day, int hour, SatelliteSector sector) {
		return goes17Files(year, month, day, hour, "ABI-L2-MCMIP", sector);
	}

	/**
	 * Returns a list of links to all GOES-16 Band Radiance files given a date,
	 * hour, and sector.
	 * 
	 * @param year   Year
	 * @param month  Month of Year
	 * @param day    Day of Month
	 * @param hour   Day of Month
	 * @param sector GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes17Level1Files(int year, int month, int day, int hour, SatelliteSector sector) {
		return goes17Files(year, month, day, hour, "ABI-L1b-Rad", sector);
	}

	/**
	 * Returns a list of links to all GOES-16 files given a date, hour, data type
	 * and sector
	 * 
	 * @param year     Year
	 * @param month    Month of Year
	 * @param day      Day of Month
	 * @param hour     Day of Month
	 * @param dataType The type of GOES data to pull
	 * @param sector   GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes18Files(int year, int month, int day, int hour, String dataType,
			SatelliteSector sector) {
		String sectorIdentifier = "";

		switch (sector) {
		case GOES_PACUS:
			sectorIdentifier = "C";
			break;
		case GOES_CONUS:
			sectorIdentifier = "C";
			break;
		case GOES_FULL_DISK:
			sectorIdentifier = "F";
			break;
		case GOES_MESOSCALE_1:
			sectorIdentifier = "M";
			break;
		case GOES_MESOSCALE_2:
			sectorIdentifier = "M";
			break;
		default:
			sectorIdentifier = "C";
			break;
		}

		DateTime dt = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

		int dayOfYear = dt.getDayOfYear();

		String folderKey = String.format("%4s/%04d/%03d/%02d/", dataType + sectorIdentifier, year, dayOfYear, hour);

		List<S3ObjectSummary> objectsInFolder = AwsClientSingleton.s3Client.listObjects("noaa-goes18", folderKey)
				.getObjectSummaries();

		List<String> links = new ArrayList<>();

		if(sector == SatelliteSector.GOES_MESOSCALE_1 || 
				sector == SatelliteSector.GOES_MESOSCALE_2) {
			for (S3ObjectSummary object : objectsInFolder) {
				String key = object.getKey();

				if(sector == SatelliteSector.GOES_MESOSCALE_1 && key.contains("M1")) {
					links.add("https://noaa-goes18.s3.amazonaws.com/" + key);
				} else if(sector == SatelliteSector.GOES_MESOSCALE_2 && key.contains("M2")) {
					links.add("https://noaa-goes18.s3.amazonaws.com/" + key);
				}
			}
		} else {
			for (S3ObjectSummary object : objectsInFolder) {
				String key = object.getKey();
		
				links.add("https://noaa-goes18.s3.amazonaws.com/" + key);
			}
		}

		return links;
	}

	/**
	 * Returns a list of links to all GOES-16 Multichannel Cloud and Moisture
	 * Imagery Product files given a date, hour, and sector.
	 * 
	 * @param year   Year
	 * @param month  Month of Year
	 * @param day    Day of Month
	 * @param hour   Day of Month
	 * @param sector GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes18Level2Files(int year, int month, int day, int hour, SatelliteSector sector) {
		return goes18Files(year, month, day, hour, "ABI-L2-MCMIP", sector);
	}

	/**
	 * Returns a list of links to all GOES-16 Band Radiance files given a date,
	 * hour, and sector.
	 * 
	 * @param year   Year
	 * @param month  Month of Year
	 * @param day    Day of Month
	 * @param hour   Day of Month
	 * @param sector GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes18Level1Files(int year, int month, int day, int hour, SatelliteSector sector) {
		return goes18Files(year, month, day, hour, "ABI-L1b-Rad", sector);
	}

	/**
	 * Returns a list of links to all GOES-16 files given a date, hour, data type
	 * and sector
	 * 
	 * @param year     Year
	 * @param month    Month of Year
	 * @param day      Day of Month
	 * @param hour     Day of Month
	 * @param dataType The type of GOES data to pull
	 * @param sector   GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes19Files(int year, int month, int day, int hour, String dataType,
			SatelliteSector sector) {
		String sectorIdentifier = "";

		switch (sector) {
		case GOES_PACUS:
			sectorIdentifier = "C";
			break;
		case GOES_CONUS:
			sectorIdentifier = "C";
			break;
		case GOES_FULL_DISK:
			sectorIdentifier = "F";
			break;
		case GOES_MESOSCALE_1:
			sectorIdentifier = "M";
			break;
		case GOES_MESOSCALE_2:
			sectorIdentifier = "M";
			break;
		default:
			sectorIdentifier = "C";
			break;
		}

		DateTime dt = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

		int dayOfYear = dt.getDayOfYear();

		String folderKey = String.format("%4s/%04d/%03d/%02d/", dataType + sectorIdentifier, year, dayOfYear, hour);

		List<S3ObjectSummary> objectsInFolder = AwsClientSingleton.s3Client.listObjects("noaa-goes19", folderKey)
				.getObjectSummaries();

		List<String> links = new ArrayList<>();

		if(sector == SatelliteSector.GOES_MESOSCALE_1 || 
				sector == SatelliteSector.GOES_MESOSCALE_2) {
			for (S3ObjectSummary object : objectsInFolder) {
				String key = object.getKey();

				if(sector == SatelliteSector.GOES_MESOSCALE_1 && key.contains("M1")) {
					links.add("https://noaa-goes19.s3.amazonaws.com/" + key);
				} else if(sector == SatelliteSector.GOES_MESOSCALE_2 && key.contains("M2")) {
					links.add("https://noaa-goes19.s3.amazonaws.com/" + key);
				}
			}
		} else {
			for (S3ObjectSummary object : objectsInFolder) {
				String key = object.getKey();
		
				links.add("https://noaa-goes19.s3.amazonaws.com/" + key);
			}
		}

		return links;
	}

	/**
	 * Returns a list of links to all GOES-16 Multichannel Cloud and Moisture
	 * Imagery Product files given a date, hour, and sector.
	 * 
	 * @param year   Year
	 * @param month  Month of Year
	 * @param day    Day of Month
	 * @param hour   Day of Month
	 * @param sector GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes19Level2Files(int year, int month, int day, int hour, SatelliteSector sector) {
		return goes19Files(year, month, day, hour, "ABI-L2-MCMIP", sector);
	}

	/**
	 * Returns a list of links to all GOES-16 Band Radiance files given a date,
	 * hour, and sector.
	 * 
	 * @param year   Year
	 * @param month  Month of Year
	 * @param day    Day of Month
	 * @param hour   Day of Month
	 * @param sector GOES Sector (Full Disk, CONUS, Mesoscale)
	 */
	public static List<String> goes19Level1Files(int year, int month, int day, int hour, SatelliteSector sector) {
		return goes19Files(year, month, day, hour, "ABI-L1b-Rad", sector);
	}
}
