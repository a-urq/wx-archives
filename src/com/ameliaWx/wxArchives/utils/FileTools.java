package com.ameliaWx.wxArchives.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileTools {
	private String dataFolder;
	
	public FileTools(String dataFolder) {
		this.dataFolder = dataFolder;
	}

	public File downloadFile(String url, String fileName) throws IOException {
		boolean fileDownloaded = false;
		boolean firstAttempt = true;
		while (!fileDownloaded) {
			if (!firstAttempt) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			firstAttempt = false;

			try {
				// System.out.println("Downloading from: " + url);
				URL dataURL = new URL(url);

				File dataDir = new File(dataFolder);
				// System.out.println("Creating Directory: " + dataFolder);
				dataDir.mkdirs();
				InputStream is = dataURL.openStream();

//				System.out.println("Downloading File: " + dataFolder + fileName);
				OutputStream os = new FileOutputStream(dataFolder + fileName);
				byte[] buffer = new byte[16 * 1024];
				int transferredBytes = is.read(buffer);
				while (transferredBytes > -1) {
					os.write(buffer, 0, transferredBytes);
					// System.out.println("Transferred "+transferredBytes+" for "+fileName);
					transferredBytes = is.read(buffer);
				}
				is.close();
				os.close();

				fileDownloaded = true;
				
				return new File(dataFolder + fileName);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
		
		return new File(dataFolder + fileName);
	}

	public void unzipGz(String name) {
		byte[] buffer = new byte[1024];

		try {
			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(dataFolder + name + ".gz"));
			FileOutputStream out = new FileOutputStream(dataFolder + name);

			int len;
			while ((len = gzip.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

			gzip.close();
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * Example of reading Zip archive using ZipFile class
	 */

	public static ArrayList<File> unzip(String fileName, String outputDir) throws IOException {
		ArrayList<File> files = new ArrayList<File>();
		
		new File(outputDir).mkdirs();
		final ZipFile file = new ZipFile(fileName);
		// System.out.println("Iterating over zip file : " + fileName);

		try {
			final Enumeration<? extends ZipEntry> entries = file.entries();
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				File entryFile = extractEntry(entry, file.getInputStream(entry), outputDir);
				
				files.add(entryFile);
			}
			// System.out.printf("Zip file %s extracted successfully in %s",
			// fileName, outputDir);
		} finally {
			file.close();
		}
		
		return files;
	}

	/*
	 * Utility method to read data from InputStream
	 */
	private static File extractEntry(final ZipEntry entry, InputStream is, String outputDir) throws IOException {
		String extractedFile = outputDir + entry.getName();
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(extractedFile);
			final byte[] buf = new byte[8192];
			int length;

			while ((length = is.read(buf, 0, buf.length)) >= 0) {
				fos.write(buf, 0, length);
			}

		} catch (IOException ioex) {
			fos.close();
		}
		
		return new File(extractedFile);
	}

	public String usingBufferedReader(File filePath) {
		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				contentBuilder.append(sCurrentLine).append(" ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
	}
}
