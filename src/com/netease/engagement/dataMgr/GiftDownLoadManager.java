package com.netease.engagement.dataMgr;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;


import android.text.TextUtils;

import com.netease.common.http.filedownload.FileDownloadListener;
import com.netease.common.http.filedownload.FileDownloadManager;
import com.netease.engagement.app.EngagementApp;
import com.netease.service.Utils.ZipUtil;
import com.netease.service.Utils.ZipUtil.IZip;
import com.netease.service.preferMgr.EgmPrefHelper;

/**
 * 礼物包下载
 */
public class GiftDownLoadManager extends BaseDownLoadManager {
	
	private static GiftDownLoadManager manager ;
	
	private GiftDownLoadManager(){
		File dirFile = new File(GIFT_ZIP_DIR);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
	}
	public static GiftDownLoadManager getInstance(){
		if(manager == null){
			manager = new GiftDownLoadManager();
		}
		return manager ;
	}
	
	//礼物图片目录
	public static final String GIFT_ZIP_DIR = EngagementApp.getAppInstance().getFilesDir().getPath()+"/gift_dir";
	
	public void downLoadGiftZip(final String version , String url){
		if(TextUtils.isEmpty(version) || TextUtils.isEmpty(url)){
			return ;
		}
		final File desFile = new File(GIFT_ZIP_DIR + "/" + generateFileName(version));
		
		if (desFile.exists()){
			return ;
		}
		else{
			try {
				desFile.getParentFile().mkdirs();
				
				desFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(FileDownloadManager.getInstance().checkIsDownloading(url)){
			return ;
		}
		
		FileDownloadManager.getInstance().downloadFile(url, null, null,new FileDownloadListener() {
			@Override
			public void onSuccess(String path) {
				if(copyToData(path,desFile.getPath())){
					//解压文件
					new Thread(
						new Runnable(){
							@Override
							public void run() {
								try {
									ZipUtil.upZipFile(desFile, GIFT_ZIP_DIR, new IZip() {
										@Override
										public void onProgress(long current, long total, int percent) {
											if(percent == 100){
												//存储版本信息
												EgmPrefHelper.putGiftDataVersion(EngagementApp.getAppInstance().getApplicationContext(), version);
												desFile.delete();
											}
										}
									});
								} catch (ZipException e) {
									e.printStackTrace();
									desFile.delete();
								} catch (IOException e) {
									desFile.delete();
									e.printStackTrace();
								}
							}
					}).start();
				}
			}
			@Override
			public void onProgress(long current, long total, int percent, int speed) {
				
			}
			@Override
			public void onFailed(String err, int errCode) {
				if(desFile.exists()){
					desFile.delete();
				}
			}
		});
	}
	
	private String generateFileName(String version){
		if(TextUtils.isEmpty(version)){
			return null ;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("gift_").append(version).append(".zip");
		return sb.toString();
	}
	
	
}
