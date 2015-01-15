package com.netease.util;

import java.io.File;

import android.text.TextUtils;

public class FileUtil {

	public static long getLength(String path) {
		if (TextUtils.isEmpty(path)) {
			return 0;
		}
		
		File file = new File(path);
		
		return file.length();
	}
	
}
