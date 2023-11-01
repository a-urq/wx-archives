package com.ameliaWx.nexradaws4j;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AwsClientSingleton {
	public static final AmazonS3Client s3Client;
	
	static {
		s3Client = (AmazonS3Client) AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	}
}
