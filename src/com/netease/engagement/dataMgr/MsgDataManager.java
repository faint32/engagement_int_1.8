package com.netease.engagement.dataMgr;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.netease.common.cache.CacheManager;
import com.netease.common.service.BaseService;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.EgmProtocolConstants;

/**
 * 消息管理
 */
public class MsgDataManager {
	
	private static MsgDataManager manager ;
	
	private static String LOCAL_IMAGE_DIR = "image" ;
	private static String AUDIO_DIR = "audio" ;
	private static String VIDEO_DIR = "video" ;
	
	private static String[] DIRS = new String[]{
		LOCAL_IMAGE_DIR,
		AUDIO_DIR,
		VIDEO_DIR
	};
	 
	private MsgDataManager(){
		File file = null ;
		
		String root = EgmUtil.getCacheDir();
		
		boolean changed = false;
		
		for(String dir : DIRS){
			file = new File(root + dir);
			if(! file.exists()){
				file.mkdirs();
			}
			
			changed = createNoMedia(file.getPath()) | changed; // 不能调换位置
		}
		
		Context context = BaseService.getServiceContext();
		if (context != null && changed) {
			try {
				if (Build.VERSION.SDK_INT >= 19) {
					String[] dirs = new String[1];
					dirs[0] = root;
					
					MediaScannerConnection.scanFile(context, dirs, null, null);
				}
				else {
					context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
							Uri.parse("file://" + root)));
				}
			} catch (Exception e) {
			}
		}
	}
	
	private static boolean createNoMedia(String dir) {
		boolean changed = false;
		
		File file = new File(dir, ".nomedia");
		
		if (! file.exists()) {
			try {
				file.createNewFile();
				changed = true;
			} catch (Exception e) {
			}
		}
		
		return changed;
	}
	
	public static MsgDataManager getInstance(){
		if(manager == null){
			manager = new MsgDataManager();
		}
		return manager ;
	}
	
	/**
	 * 转化路径并且创建文件
	 * @param msgType
	 * @param time
	 * @return
	 */
	public String convertPath(int msgType ,String time){
		if(TextUtils.isEmpty(time)){
			return null ;
		}
		String pathMd5 = EgmUtil.getMD5(time);
		if(TextUtils.isEmpty(pathMd5)){
			return null ;
		}
		String first = pathMd5.substring(0,2);
		String second = pathMd5.substring(2,4);
		String third = pathMd5.substring(4,pathMd5.length());
		File newFile = newFile(msgType,first,second,third) ;
		if(newFile != null && newFile.exists()){
			return newFile.getPath();
		}
		return null ;
	}
	
	/**
	 * 根据不同文件类型创建不同路径下的文件
	 * @param msgType
	 * @param first
	 * @param second
	 * @param fileName
	 * @return
	 */
	private File newFile(int msgType ,String first,String second ,String fileName){
		StringBuilder sb = new StringBuilder();
		String root = CacheManager.getRoot();
        File file = new File(root);
        if(!file.exists()){
            file.mkdirs();
        }
		File dir = null ;
		switch(msgType){
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
				sb.append(root)
				  .append("/")
				  .append(LOCAL_IMAGE_DIR)
				  .append("/")
				  .append(first)
				  .append("/")
				  .append(second)
				  .append("/");
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
				sb.append(root)
				  .append("/")
				  .append(AUDIO_DIR)
				  .append("/")
				  .append(first)
				  .append("/")
				  .append(second)
				  .append("/");
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
				sb.append(root)
				  .append("/")
				  .append(VIDEO_DIR)
				  .append("/")
				  .append(first)
				  .append("/")
				  .append(second)
				  .append("/");
				break;
		}
		
		dir = new File(sb.toString());
		if(!dir.exists()){
			dir.mkdirs();
		}
		
		File newFile = null ;
		switch(msgType){
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
				sb.append(fileName).append(".png");
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
				sb.append(fileName).append(".3gp");
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
				sb.append(fileName).append(".mp4");
				break;
		}
		newFile = new File(sb.toString());
		if(!newFile.exists()){
			try {
				newFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return newFile ;
	}
	
	public void clear(){
		if(manager != null){
			manager = null ;
		}
	}
}
