package com.ameliaWx.nexradaws4j;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class GoesAws {
	/**
	 * 
	 * @author Amelia Urquhart (github.com/a-urq)
	 */
	
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
		
		List<S3ObjectSummary> objectsInFolder = AwsClientSingleton.s3Client.listObjects("noaa-nexrad-level2", folderKey).getObjectSummaries();
		
		List<String> links = new ArrayList<>();
		
		for(S3ObjectSummary object : objectsInFolder) {
			String key = object.getKey();
			
			links.add("https://noaa-nexrad-level2.s3.amazonaws.com/" + key);
		}
		
		return links;
	}
}
