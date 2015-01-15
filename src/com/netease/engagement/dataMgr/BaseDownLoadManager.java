package com.netease.engagement.dataMgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.text.TextUtils;

public class BaseDownLoadManager {
	
	public static boolean copyToData(String srcPath ,String desPath){
		if(TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(desPath)){
			return false ;
		}
		File srcFile = new File(srcPath);
		if(srcFile.exists()){
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				fis = new FileInputStream(srcFile);
				File desFile = new File(desPath);
				if(!desFile.exists()){
					desFile.createNewFile();
				}
				fos = new FileOutputStream(desPath);
				byte[] buffer = new byte[1024];   
				int length = 0;
				while((length = fis.read(buffer))!= -1){
					fos.write(buffer,0, length);
				}
				return true ;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(fis != null) {
					try {
						fis.close();
						fis = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(fos != null) {
					try {
						fos.close();
						fos = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false ;
	}

}
