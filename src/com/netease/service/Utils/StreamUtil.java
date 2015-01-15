package com.netease.service.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.netease.common.service.BaseService;

public class StreamUtil {

	public static String readString(InputStream in) throws IOException{
		BufferedReader reader = null;
		
		char[] data = BaseService.getCharBuf(2048);
		
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			
			StringBuffer buffer = new StringBuffer();
			int length = 0;
			while (( length = reader.read(data)) != -1) {
				buffer.append(data, 0, length);
			}
			
			return buffer.toString();
		} finally {
			BaseService.returnCharBuf(data);
			
			if (reader != null) {
				reader.close();
			}
		}
	}
	
}
