package com.ameliaWx.wxArchives.earthWeather.iemWarnings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ameliaWx.wxArchives.PointF;
import com.ameliaWx.wxArchives.utils.FileTools;

/**
 * An API to retrieve archived warnings
 * 
 * @author Amelia Urquhart
 *
 */
public class WarningArchive {
	private FileTools ft;

	public WarningArchive(String dataFolder) {
		this.ft = new FileTools(dataFolder);
	}

	public static void main(String[] args) throws IOException {
		WarningArchive wa = new WarningArchive(System.getProperty("user.home") + "/Documents/NEXRAD Analysis/temp/");

		wa.getWarnings(new DateTime(2023, 2, 14, 18, 0, DateTimeZone.UTC),
				new DateTime(2023, 2, 15, 3, 0, DateTimeZone.UTC));
	}

	public ArrayList<WarningPolygon> getWarnings(DateTime startTime, DateTime endTime) throws IOException {
		File warningFile = downloadWarningFile(startTime, endTime);

		System.out.println("IEM WWA downloaded to: " + warningFile);

		return getWarnings(warningFile);
	}

	public static ArrayList<WarningPolygon> getWarnings(File f) throws IOException {
		ArrayList<WarningPolygon> warningPolygons = new ArrayList<>();

		try {
			warningPolygons = parseArchiveKml(f);
		} catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		} finally {
//			warningFile.delete();
		}

		return warningPolygons;
	}

	private File downloadWarningFile(DateTime startTime, DateTime endTime) throws IOException {
		String downloadUrl = "https://mesonet.agron.iastate.edu/kml/sbw_interval.php?simple=yes&accept=shapefile&states%5B%5D=AL&location_group=wfo&wfos%5B%5D=ALL&timeopt=1&"
				+ String.format(
						"year1=%1d&month1=%1d&day1=%1d&hour1=%1d&minute1=%1d&year2=%1d&month2=%1d&day2=%1d&hour2=%1d&minute2=%1d&year3=2023&month3=9&day3=28&hour3=0&minute3=0&phenomena=TO&significance=W",
						startTime.getYear(), startTime.getMonthOfYear(), startTime.getDayOfMonth(),
						startTime.getHourOfDay(), startTime.getMinuteOfHour(), endTime.getYear(),
						endTime.getMonthOfYear(), endTime.getDayOfMonth(), endTime.getHourOfDay(),
						endTime.getMinuteOfHour());

		System.out.println("IEM warning file URL: " + downloadUrl);

		String filename = "wwa.kml";

//		System.out.println(filename);

		File warningFile = ft.downloadFile(downloadUrl, filename);

		return warningFile;
	}

	private static ArrayList<WarningPolygon> parseArchiveKml(File kml) throws ParserConfigurationException, SAXException, IOException {
		ArrayList<WarningPolygon> warnings = new ArrayList<>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//	    factory.setValidating(true);
	    factory.setIgnoringElementContentWhitespace(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(kml);
	    
	    NodeList placemarks = doc.getElementsByTagName("Placemark");

	    for(int i = 0; i < placemarks.getLength(); i++) {
	    	Node placemark = placemarks.item(i);
	    	
	    	NodeList childNodes = placemark.getChildNodes();
	    	
	    	String startTimeStr = "";
	    	String endTimeStr = "";
	    	String tornadoTagStr = "";
	    	String damageTagStr = "";
	    	String name = "";
	    	String multiGeometry = "";
	    	
		    for(int j = 0; j < childNodes.getLength(); j++) {
//		    	if(i == 0 && !"#text".equals(childNodes.item(j).getNodeName())) {
//		    		System.out.println("tag:" + childNodes.item(j).getNodeName());
//		    		System.out.println("content:" + childNodes.item(j).getTextContent());
//		    	}
		    	
		    	if("description".equals(childNodes.item(j).getNodeName())) {
		    		String description = childNodes.item(j).getTextContent();
		    		
		    		Scanner sc = new Scanner(description);
		    		
		    		while(sc.hasNextLine()) {
		    			String line = sc.nextLine();
		    			
		    			int lastAngleBracket = line.lastIndexOf('>');
		    			
		    			if(lastAngleBracket != -1) {
		    				String content = line.substring(lastAngleBracket + 1);
		    				
		    				if(line.contains(">Issued:<")) {
		    					startTimeStr = content.trim();
		    				} else if(line.contains(">Expires:<")) {
			    				endTimeStr = content.trim();
			    			} else if(line.contains(">Tornado Tag:<")) {
		    					tornadoTagStr = content.trim();
		    				} else if(line.contains(">Damage Tag:<")) {
		    					damageTagStr = content.trim();
			    			}
		    			}
		    		}
		    		
		    		sc.close();
		    	}
		    	
		    	if("name".equals(childNodes.item(j).getNodeName())) {
		    		name = childNodes.item(j).getTextContent();
		    	}
		    	
		    	if("MultiGeometry".equals(childNodes.item(j).getNodeName())) {
		    		multiGeometry = childNodes.item(j).getTextContent();
		    	}
		    }
		    
		    WarningType wType = null;
		    TornadoTag tTag = TornadoTag.NONE;
		    DamageTag dTag = DamageTag.STANDARD;
		    
		    if("Severe Thunderstorm Warning".equals(name)) {
		    	wType = WarningType.SEVERE_THUNDERSTORM;
		    } else if("Tornado Warning".equals(name)) {
		    	wType = WarningType.TORNADO;
		    } else if("Marine Warning".equals(name)) {
		    	wType = WarningType.SPECIAL_MARINE;
		    } else if("Flood Advisory".equals(name) || "Flood Warning".equals(name)) {
		    	wType = WarningType.FLOOD;
		    } else if("Flash Flood Warning".equals(name)) {
		    	wType = WarningType.FLASH_FLOOD;
		    } else if("Dust Storm Advisory".equals(name)) {
		    	wType = WarningType.DUST_STORM_ADV;
		    } else if("Dust Storm Warning".equals(name)) {
		    	wType = WarningType.DUST_STORM_WARNING;
		    } else if("Snow Squall Warning".equals(name)) {
		    	wType = WarningType.SNOW_SQUALL_WARNING;
		    } else {
				System.err.printf("WARNING: Unrecognized weather warning type \"%s\".\n", name);
			}
		    
		    if("POSSIBLE".equals(tornadoTagStr)) {
		    	tTag = TornadoTag.POSSIBLE;
		    } else if("RADAR INDICATED".equals(tornadoTagStr)) {
		    	tTag = TornadoTag.RADAR_INDICATED;
		    } else if("OBSERVED".equals(tornadoTagStr)) {
		    	tTag = TornadoTag.OBSERVED;
		    }
		    
		    if("CONSIDERABLE".equals(damageTagStr)) {
		    	dTag = DamageTag.CONSIDERABLE;
		    } else if("DESTRUCTIVE".equals(damageTagStr)) {
		    	dTag = DamageTag.DESTRUCTIVE;
		    } else if("CATASTROPHIC".equals(damageTagStr)) {
		    	dTag = DamageTag.CATASTROPHIC;
		    }
		    
//		    System.out.println(wType);
		    
		    DateTime startTime = kmlStringToDateTime(startTimeStr);
		    DateTime endTime = kmlStringToDateTime(endTimeStr);
		    
//		    System.out.println(startTime);
//		    System.out.println(endTime);
		    
		    String[] coords = multiGeometry.split(" ");
		    
		    ArrayList<PointF> points = new ArrayList<>();
		    
		    for(int j = 0; j < coords.length; j++) {
		    	String[] lonLat = coords[j].split(",");
		    	
		    	double lon = Double.valueOf(lonLat[0]);
		    	double lat = Double.valueOf(lonLat[1]);
		    	
		    	PointF point = new PointF(lat, lon);
		    	
		    	points.add(point);
		    }
		    
//			if(wType == WarningType.SEVERE_THUNDERSTORM) {
//				System.out.println(points.get(0));
//				System.out.println("startTimeStr: " + startTimeStr);
//				System.out.println("endTimeStr: " + endTimeStr);
//				System.out.println("tornadoTagStr: " + tornadoTagStr);
//				System.out.println("damageTagStr: " + damageTagStr);
//			}
		    
		    WarningPolygon poly = new WarningPolygon(points, startTime, endTime, wType, tTag, dTag);
		    
		    warnings.add(poly);
	    }
	    
		return warnings;
	}

	private static String timestamp(DateTime d) {
		return String.format("%04d%02d%02d-%02d%02d%02d", d.getYear(), d.getMonthOfYear(), d.getDayOfMonth(),
				d.getHourOfDay(), d.getMinuteOfHour(), d.getSecondOfMinute());
	}
	
	private static DateTime kmlStringToDateTime(String kmlStr) {
		int day = Integer.valueOf(kmlStr.substring(0, 2));
		String monthStr = kmlStr.substring(3, 6);
		int year = Integer.valueOf(kmlStr.substring(7, 11));
		int hour = Integer.valueOf(kmlStr.substring(12, 14));
		int minute = Integer.valueOf(kmlStr.substring(15, 17));
		
		int month = 0;
		
		if("Jan".equals(monthStr)) {
			month = 1;
		} else if("Feb".equals(monthStr)) {
			month = 2;
		} else if("Mar".equals(monthStr)) {
			month = 3;
		} else if("Apr".equals(monthStr)) {
			month = 4;
		} else if("May".equals(monthStr)) {
			month = 5;
		} else if("Jun".equals(monthStr)) {
			month = 6;
		} else if("Jul".equals(monthStr)) {
			month = 7;
		} else if("Aug".equals(monthStr)) {
			month = 8;
		} else if("Sep".equals(monthStr)) {
			month = 9;
		} else if("Oct".equals(monthStr)) {
			month = 10;
		} else if("Nov".equals(monthStr)) {
			month = 11;
		} else if("Dec".equals(monthStr)) {
			month = 12;
		}
		
		return new DateTime(year, month, day, hour, minute, DateTimeZone.UTC);
	}
}
