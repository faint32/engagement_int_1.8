package com.netease.engagement.dataMgr;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.filedownload.FileDownloadListener;
import com.netease.common.http.filedownload.FileDownloadManager;
import com.netease.engagement.app.EngagementApp;
import com.netease.service.Utils.ZipUtil;
import com.netease.service.Utils.ZipUtil.IZip;
import com.netease.service.protocol.meta.FaceGroupInfo;
import com.netease.share.sticker.model.CategoryData;
import com.netease.share.sticker.model.StickerHelper;
import com.netease.share.sticker.util.DependentUtils;


public class FaceDownLoadManager extends BaseDownLoadManager {
	
	private static FaceDownLoadManager manager ;
	
	private String stickerPath;
	
	private FaceDownLoadManager(){
		File dirFile = new File(FACE_ZIP_DIR);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
	}
	public static FaceDownLoadManager getInstance(){
		if(manager == null){
			manager = new FaceDownLoadManager();
		}
		return manager ;
	}
	
	public void downLoadAllFaceZip() {
		String stickersFile = StickerHelper.configFilePath(DependentUtils.getUid(), CategoryData.NV_CONFIG_FILE);
        String stickers = DependentUtils.loadAsString(stickersFile);
        if (TextUtils.isEmpty(stickers)) {
        	return;
        }
        
        Gson gson = new Gson();
        List<FaceGroupInfo> faceGroupList = null; 
        try {
        	faceGroupList = gson.fromJson(stickers, new TypeToken<List<FaceGroupInfo>>(){}.getType()); 
        } catch (JsonSyntaxException e) {
        	e.printStackTrace();
        }
        if (faceGroupList == null) {
        	return;
        }
        
        if (TextUtils.isEmpty(stickerPath)) {
        	stickerPath = DependentUtils.getNvStickerPath();
        }
        
        FaceGroupInfo faceGroup = null;
        String path = null;
        
        for (int i=0; i<faceGroupList.size(); i++) {
        	FaceGroupInfo group = faceGroupList.get(i);
        	
        	// /storage/sdcard1/com/netease/share/sticker/dajmd/
        	path = stickerPath + group.id + "/"; 
        	File f = new File(path);
        	if (f.exists()) {
        		if (f.isDirectory()) {
        			File[] files = f.listFiles();
        			if (files.length == group.faceList.size()+2) {
        				continue;
        			}
        		}
        	}
        	
        	faceGroup = group;
    		break;
        }
        
        if (faceGroup != null) { // 下载faceGroup对应的zip图片包
        	downLoadFaceZip(faceGroup, path);
        } else { // 全部下载完毕
        	
        }
        
	}

	
	
	// 表情图片目录
	public static final String FACE_ZIP_DIR = EngagementApp.getAppInstance().getFilesDir().getPath()+"/face_dir";
	
	private void downLoadFaceZip(FaceGroupInfo faceGroup, final String p) {
		
		final File desFile = new File(FACE_ZIP_DIR + "/" + generateFileName(faceGroup.id) );
		
		if(desFile.exists()){
			return ;
		} else {
			try {
				desFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(FileDownloadManager.getInstance().checkIsDownloading(faceGroup.url)){
			return ;
		}
		
		FileDownloadManager.getInstance().downloadFile(faceGroup.url, null, null,new FileDownloadListener() {
			@Override
			public void onSuccess(final String path) {
				if(copyToData(path, desFile.getPath())){
					//解压文件
					new Thread(
						new Runnable(){
							@Override
							public void run() {
								try {
									File f = new File(p);
									if (!f.exists()) {
										f.mkdirs();
									}
									ZipUtil.upZipFile(desFile, p, new IZip() {
										@Override
										public void onProgress(long current, long total, int percent) {
											if(percent == 100){
												desFile.delete();
												FaceDownLoadManager.this.downLoadAllFaceZip();
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
	
	
	private String generateFileName(String id){
		if(TextUtils.isEmpty(id)){
			return null ;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("face_").append(id).append(".zip");
		return sb.toString();
	}

}
