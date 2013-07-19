package com.wireless.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class PinReader {
	
	private final static String FILE_PATH = android.os.Environment.getExternalStorageDirectory().getPath() + "/digi-e/android/pin";
	
	/**
	 * Read the pin value 
	 * @return the pin value
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static String read() throws IOException, FileNotFoundException{
		StringBuilder pinValue = new StringBuilder();
		InputStreamReader in = new InputStreamReader(new FileInputStream(new File(FILE_PATH)));
		int val;
		while((val = in.read()) != -1){
			pinValue.append((char)val);
		}
		in.close();
		return pinValue.toString();
	}
}
