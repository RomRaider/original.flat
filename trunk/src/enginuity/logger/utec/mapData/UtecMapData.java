package enginuity.logger.utec.mapData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UtecMapData {
	private StringBuffer rawMapData = new StringBuffer();

	private double[][] fuelMap = new double[11][40];
	private double[][] timingMap = new double[11][40];
	private double[][] boostMap = new double[11][40];

	private String mapName = "";
	private String mapComment = "";
	
	private double[] tempStorage = new double[440];
	
	private String stringChecksumHexValue = "";
	
	public void addRawData(int byteData) {
		rawMapData.append(byteData);
	}
	
	/**
	 * Generic constructor
	 *
	 */
	public UtecMapData() {
	}
	
	/**
	 * Constructor takes a file location to import
	 * @param fileLocation
	 */
	public UtecMapData(String fileLocation) {
		importFileData(fileLocation);
	}


	/**
	 * Import UTEC maps from a file
	 * @param fileLocation
	 */
	public void importFileData(String fileLocation) {
		rawMapData = new StringBuffer();
		BufferedReader br = null;
		try {
			FileReader fr = new FileReader(fileLocation);
			br = new BufferedReader(fr);

			String record = new String();
			try {
				while ((record = br.readLine()) != null) {
					this.rawMapData.append(record+"\r");
					// System.out.println(record);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		populateMapDataStructures();
	}

	public void populateMapDataStructures() {
		
		// Functionality as the method names suggest
		cleanUpMapData();
		populateMapName();
		populateMapComment();
		populateFuelMapData();
		populateTimingMapData();
		populateBoostMapData();
		calculateChecksum();
	}
	 
	/**
	 * Return map based on current data.
	 * @return
	 */
	public StringBuffer getUpdatedMap(){
		calculateChecksum();
		
		StringBuffer totalFile = new StringBuffer("");
		
		totalFile.append("[START][MAPGROUP1][MAPGROUP]\r");
		totalFile.append("Map Name:-["+this.mapName+"]\r");
		totalFile.append("Map Comments:-["+this.mapComment+"]\r");
		
		totalFile.append("Fuel Map\r");
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				String temp = this.fuelMap[j][i]+"";
				if(temp.endsWith(".0")){
					temp = temp.substring(0, temp.length()-2);
				}
				String number = "["+temp+"]";
				int count = 9-number.length();
				String spaces = "";
				for(int k=0 ; k<count ; k++){
					spaces += " ";
				}
				
				number = spaces+number;
				totalFile.append(number);
			}
			totalFile.append("\r");
		}
		totalFile.append("\r");
		
		totalFile.append("Timing Map\r");
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				String temp = this.timingMap[j][i]+"";
				if(temp.endsWith(".0")){
					temp = temp.substring(0, temp.length()-2);
				}
				String number = "["+temp+"]";
				int count = 9-number.length();
				String spaces = "";
				for(int k=0 ; k<count ; k++){
					spaces += " ";
				}
				
				number = spaces+number;
				totalFile.append(number);
			}
			totalFile.append("\r");
		}
		totalFile.append("\r");
		

		totalFile.append("Boost Map\r");
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				String temp = this.boostMap[j][i]+"";
				if(temp.endsWith(".0")){
					temp = temp.substring(0, temp.length()-2);
				}
				String number = "["+temp+"]";
				int count = 9-number.length();
				String spaces = "";
				for(int k=0 ; k<count ; k++){
					spaces += " ";
				}
				
				number = spaces+number;
				totalFile.append(number);
			}
			totalFile.append("\r");
		}
		
		totalFile.append("[END][MAPGROUP]["+this.stringChecksumHexValue.toUpperCase()+"][EOF]");
		return totalFile;
	}
	
	public void calculateChecksum(){
		
		int mapChecksumValue = 0;
		char[] charArray = this.mapName.toCharArray();
		for(int i=0 ; i<charArray.length ; i++){
			int charValue = charArray[i];
			mapChecksumValue += charValue;
		}
		//System.out.println("Map name Checksum:"+mapChecksumValue);
		
		int mapCommentChecksumValue = 0;
		charArray = this.mapComment.toCharArray();
		for(int i=0 ; i<charArray.length ; i++){
			int charValue = charArray[i];
			mapCommentChecksumValue += charValue;
		}
		//System.out.println("Map comment Checksum:"+mapCommentChecksumValue);
		
		int fuelChecksum = 0; 
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				fuelChecksum += (this.fuelMap[j][i]*10 + 1000 + 1);
			}
		}
		//System.out.println("Fuel Checksum:"+fuelChecksum);
		
		int timingChecksum = 0; 
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				timingChecksum += (this.timingMap[j][i]*10 + 1000 + 1);
			}
		}
		//System.out.println("Timing Checksum:"+timingChecksum);
		
		int boostChecksum = 0; 
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				boostChecksum += (this.boostMap[j][i]*100 + 1);
			}
		}
		//System.out.println("Boost Checksum:"+boostChecksum);
		
		int totalChecksum = mapChecksumValue+mapCommentChecksumValue+fuelChecksum+timingChecksum+boostChecksum;
		//System.out.println("Total decimal checksum:"+totalChecksum);
		String hexString = Integer.toHexString(totalChecksum);
		//System.out.println("Total hex checksum:"+hexString);
		
		hexString = hexString.substring(hexString.length()-4, hexString.length());
		hexString = 0 + hexString;
		totalChecksum = Integer.parseInt(hexString, 16);
		
		this.stringChecksumHexValue = hexString;
		System.out.println("Total hex checksum:"+Integer.toHexString(totalChecksum));
	}
	
	public void populateBoostMapData(){
		int fuelStart = rawMapData.indexOf("Boost Map\r")+9;
		int fuelEnd = rawMapData.indexOf("\r[END]");
		String singleRow = rawMapData.substring(fuelStart, fuelEnd);
		
		String numericalValue = "";
		String[] split = singleRow.split(" *");
		int counter = 0;
		boolean makeDouble = false;
		for(int i=0; i<split.length; i++){
			String tempString = split[i];
			
			if(tempString.equals("[")){
				makeDouble = true;
				numericalValue = "";
			}
			else if(tempString.equals("]")){
				makeDouble = false;
				double parsedDouble = Double.parseDouble(numericalValue);
				this.tempStorage[counter] = parsedDouble;
				counter++;
			}
			else{
				if(makeDouble == true){
					numericalValue+=tempString;
				}
			}
		}
		
		// Move temp stored data into appropriate double array
		counter = 0;
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				this.boostMap[j][i] = this.tempStorage[counter];
				counter++;
			}
		}
		
		// Test print of data
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				//System.out.println("Boost Output: "+ this.boostMap[j][i]);
			}
		}
		
	}
	
	public void populateTimingMapData(){
		int fuelStart = rawMapData.indexOf("Timing Map\r")+9;
		int fuelEnd = rawMapData.indexOf("\r\rBoost Map");
		String singleRow = rawMapData.substring(fuelStart, fuelEnd);
		
		String numericalValue = "";
		String[] split = singleRow.split(" *");
		int counter = 0;
		boolean makeDouble = false;
		for(int i=0; i<split.length; i++){
			String tempString = split[i];
			
			if(tempString.equals("[")){
				makeDouble = true;
				numericalValue = "";
			}
			else if(tempString.equals("]")){
				makeDouble = false;
				double parsedDouble = Double.parseDouble(numericalValue);
				this.tempStorage[counter] = parsedDouble;
				counter++;
			}
			else{
				if(makeDouble == true){
					numericalValue+=tempString;
				}
			}
		}
		
		// Move temp stored data into appropriate double array
		counter = 0;
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				this.timingMap[j][i] = this.tempStorage[counter];
				counter++;
			}
		}
		
		// Test print of data
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				//System.out.println("Timing Output: "+ this.timingMap[j][i]);
			}
		}
		
	}
	
	public void populateFuelMapData(){
		int fuelStart = rawMapData.indexOf("Fuel Map\r")+9;
		int fuelEnd = rawMapData.indexOf("\r\rTiming Map");
		String singleRow = rawMapData.substring(fuelStart, fuelEnd);
		
		String numericalValue = "";
		String[] split = singleRow.split(" *");
		int counter = 0;
		boolean makeDouble = false;
		for(int i=0; i<split.length; i++){
			String tempString = split[i];
			
			if(tempString.equals("[")){
				makeDouble = true;
				numericalValue = "";
			}
			else if(tempString.equals("]")){
				makeDouble = false;
				double parsedDouble = Double.parseDouble(numericalValue);
				this.tempStorage[counter] = parsedDouble;
				counter++;
			}
			else{
				if(makeDouble == true){
					numericalValue+=tempString;
				}
			}
		}
		
		// Move temp stored data into appropriate double array
		counter = 0;
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				this.fuelMap[j][i] = this.tempStorage[counter];
				counter++;
			}
		}
		
		// Test print of data
		for(int i=0 ; i<40 ; i++){
			for(int j=0 ; j<11 ; j++){
				//System.out.println("Fuel Output: "+ this.fuelMap[j][i]);
			}
		}
		
	}
	
	public void populateMapComment(){
		int start = rawMapData.indexOf("Map Comments:-[")+15;
		int stop = rawMapData.indexOf("]\rFuel Map");
		this.mapComment = rawMapData.substring(start, stop);
		//System.out.println("Map comment:"+mapComment+":");
	}
	
	public void populateMapName(){
		int start = rawMapData.indexOf("Map Name:-[")+11;
		int stop = rawMapData.indexOf("]\rMap Comments");
		
		//System.out.println("Start:"+start);
		//System.out.println("Stop:"+stop);
		
		this.mapName = rawMapData.substring(start, stop);
		//System.out.println("Map name:"+mapName+":");
	}

	/**
	 * Remove possible cruft from map file
	 *
	 */
	public void cleanUpMapData() {
		int start = rawMapData.indexOf("[START]");
		int stop = rawMapData.indexOf("[EOF]") + 5;
		int size = rawMapData.length();

		if (stop != size) {
			rawMapData.delete(0, start);
			stop = rawMapData.indexOf("[EOF]") + 5;
			rawMapData.delete(stop, size);
		} else {
			//System.out.println("Nothing to update.");
			//System.out.println(rawMapData);
		}
	}
	
	/**
	 * Write method overwrites file of same name.
	 * @param entirePath
	 */
	public void writeMapToFile(String entirePath){
		
		// Overwrite file if it exists already
		File tempFile = new File(entirePath);
		boolean exists = (tempFile).exists();
	    if (exists) {
	    	tempFile.delete();
	    } 
		tempFile = null;
		
		// Write data to new file
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(entirePath));
	        out.write(this.getUpdatedMap().toString());
	        out.close();
	    } catch (IOException e) {
	    }
	}
}