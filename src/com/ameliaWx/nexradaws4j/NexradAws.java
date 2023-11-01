package com.ameliaWx.nexradaws4j;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * 
 * @author Amelia Urquhart (github.com/a-urq)
 */
public class NexradAws {
	private static final AmazonS3Client s3Client;
	
	public static void main(String[] args) {
		for(String link : nexradLevel2Files(2023, 8, 28, "KTLX", true)) {
			System.out.println(link);
		}
	}
	
	static {
		s3Client = (AmazonS3Client) AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	}

	/**
	 * Returns a list of links to all NEXRAD Level-II files given a date and radar site
	 * 
	 * @param year Year
	 * @param month Month of Year
	 * @param day Day of Month
	 * @param radarCode Four-letter code for the requested radar site
	 * @param filterMdm Whether to include mysterious files labeled MDM
	 */
	public static List<String> nexradLevel2Files(int year, int month, int day, String radarCode) {
		return nexradLevel2Files(year, month, day, radarCode, false);
	}
	
	/**
	 * Returns a list of links to all NEXRAD Level-II files given a date and radar site
	 * 
	 * @param year Year
	 * @param month Month of Year
	 * @param day Day of Month
	 * @param radarCode Four-letter code for the requested radar site
	 * @param filterMdm Whether to include mysterious files labeled MDM
	 */
	public static List<String> nexradLevel2Files(int year, int month, int day, String radarCode, boolean filterMdm) {
		String folderKey = String.format("%04d/%02d/%02d/%4s/", year, month, day, radarCode);
		
		List<S3ObjectSummary> objectsInFolder = s3Client.listObjects("noaa-nexrad-level2", folderKey).getObjectSummaries();
		
		List<String> links = new ArrayList<>();
		
		for(S3ObjectSummary object : objectsInFolder) {
			String key = object.getKey();
			
			if(!filterMdm || !"MDM".equals(key.substring(key.length() - 3))) {
				links.add("https://noaa-nexrad-level2.s3.amazonaws.com/" + key);
			}
		}
		
		return links;
	}
	
	/**
	 * Returns a list of links to all GOES-16 Level-I files given a date and sector
	 * 
	 * @param year Year
	 * @param month Month of Year
	 * @param day Day of Month
	 * @param radarCode Four-letter code for the requested radar site
	 * @param filterMdm Whether to include mysterious files labeled MDM
	 */
	public static List<String> goes16Level1Files(int year, int month, int day, GoesSector sector) {
		String folderKey = String.format("%04d/%02d/%02d/%4s/", year, month, day);
		
		List<S3ObjectSummary> objectsInFolder = s3Client.listObjects("noaa-nexrad-level2", folderKey).getObjectSummaries();
		
		List<String> links = new ArrayList<>();
		
		for(S3ObjectSummary object : objectsInFolder) {
			String key = object.getKey();
			
			links.add("https://noaa-nexrad-level2.s3.amazonaws.com/" + key);
		}
		
		return links;
	}
}
