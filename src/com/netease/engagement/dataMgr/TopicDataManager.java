package com.netease.engagement.dataMgr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.filedownload.FileDownloadListener;
import com.netease.common.http.filedownload.FileDownloadManager;
import com.netease.engagement.app.EngagementApp;
import com.netease.service.Utils.ZipUtil;
import com.netease.service.Utils.ZipUtil.IZip;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.TopicConfigResult;

/**
 * 聊天和碰缘分话题数据管理
 */
public class TopicDataManager {
	
	private static TopicDataManager manager ;
	
	private TopicDataManager(){
		File file = new File(TOPIC_ZIP_DIR);
		if(!file.exists()){
			file.mkdir();
		}
	}
	public static TopicDataManager getInstance(){
		if(manager == null){
			manager = new TopicDataManager();
		}
		return manager ;
	}
	
	public static final String TOPIC_ZIP_DIR = EngagementApp.getAppInstance().getFilesDir().getPath()+"/topic_dir";
	
	public void getTipicConfig(OnUnZipEndListener listener){
		mListener = listener ;
		EgmService.getInstance().addListener(mCallBack);
		String version = EgmPrefHelper.getTopicDataVersion(EngagementApp.getAppInstance());
		EgmService.getInstance().doGetTopicData(version);
	}
	
	private OnUnZipEndListener mListener ;
	public interface OnUnZipEndListener{
		public void OnUnZipEnd();
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetTopicSucess(int transactionId, TopicConfigResult obj) {
			if(obj != null){
				String version = obj.version ;
				if(!TextUtils.isEmpty(version)){
					if(version.equals(EgmPrefHelper.getTopicDataVersion(EngagementApp.getAppInstance()))){
						//do nothing
					}else{
						//开始下载新的数据
						downLoadTopicZip(version,obj.url);
					}
				}
			}
			EgmService.getInstance().removeListener(mCallBack);
		}

		@Override
		public void onGetTopicError(int transactionId, int errCode, String err) {
			if(errCode == EgmServiceCode.TRANSACTION_RES_UNCHANGED){
				//
			}
			EgmService.getInstance().removeListener(mCallBack);
		}
	};
	
	/**
	 * 下载话题压缩包
	 */
	private void downLoadTopicZip(final String version ,String url){
		if(TextUtils.isEmpty(version) || TextUtils.isEmpty(url)){
			return ;
		}
		final File desFile = new File(TOPIC_ZIP_DIR + "/" + generateFileName(version));
		if(desFile.exists()){
			return ;
		}else{
			try {
				desFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(FileDownloadManager.getInstance().checkIsDownloading(url)){
			return ;
		}
		
		FileDownloadManager.getInstance().downloadFile(url, null,null, new FileDownloadListener() {
			@Override
			public void onSuccess(String path) {
				if(copyToData(path,desFile.getPath())){
					//解压文件
					new Thread(
						new Runnable(){
							@Override
							public void run() {
								try {
									ZipUtil.upZipFile(desFile, TOPIC_ZIP_DIR,new IZip() {
										@Override
										public void onProgress(long current, long total, int percent) {
											if(percent == 100){
												//存储版本信息
												EgmPrefHelper.putTopicDataVersion(EngagementApp.getAppInstance().getApplicationContext(), version);
												if(mListener != null){
													mListener.OnUnZipEnd();
												}
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
			}});
	}
	
	private String generateFileName(String version){
		if(TextUtils.isEmpty(version)){
			return null ;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("topic_").append(version).append(".zip");
		return sb.toString();
	}
	
	private boolean copyToData(String srcPath ,String desPath){
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
	
	private String getJson(String filePath){
		if(TextUtils.isEmpty(filePath)){
			return null ;
		}
		File file = new File(filePath);
		if(file.exists()){
			try {
				FileInputStream fis = new FileInputStream(file);
				String result = readString(fis,"utf-8");
				if(!TextUtils.isEmpty(result)){
					return result ;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null ;
	}
	
	private String readString(InputStream in, String charset){
		BufferedReader reader = null;
		char[] data = null;
		try {
			if (charset != null) {
				charset = charset.toLowerCase();
                if ("utf-8".equals(charset)) {
                	charset = null;
                }
			}
			if (charset != null) {
				reader = new BufferedReader(new InputStreamReader(in, charset));
			} else {
				reader = new BufferedReader(new InputStreamReader(in));
			}
			
			StringBuffer buffer = new StringBuffer();
			int length = 0;
			
			data = new char[1024];
			while (( length = reader.read(data)) != -1) {
				buffer.append(data, 0, length);
			}
			return buffer.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null ;
	}
	
	/**
	 * 碰缘分
	 */
	public List<String> getYuanFen(){
		List<String> yuanFen = new ArrayList<String>();
		String json = getJson(TOPIC_ZIP_DIR +"/0.txt");
		if(!TextUtils.isEmpty(json)){
			Gson gson = new Gson();
			yuanFen = gson.fromJson(json,new TypeToken<List<String>>(){}.getType());
			if(yuanFen.size()!=0){
				return yuanFen ;
			}
		}
		return null ;
	}
	
	/**
	 * 女 打招呼
	 */
	public List<String> getHelloGirl(){
		List<String> yuanFen = new ArrayList<String>();
		String json = getJson(TOPIC_ZIP_DIR +"/1.txt");
		if(!TextUtils.isEmpty(json)){
			Gson gson = new Gson();
			yuanFen = gson.fromJson(json,new TypeToken<List<String>>(){}.getType());
			if(yuanFen.size()!=0){
				return yuanFen ;
			}
		}
		return null ;
	}
	
	/**
	 * 女 要礼物
	 */
	public List<String> getWannaGift(){
		List<String> yuanFen = new ArrayList<String>();
		String json = getJson(TOPIC_ZIP_DIR +"/2.txt");
		if(!TextUtils.isEmpty(json)){
			Gson gson = new Gson();
			yuanFen = gson.fromJson(json,new TypeToken<List<String>>(){}.getType());
			if(yuanFen.size()!=0){
				return yuanFen ;
			}
		}
		return null ;
	}
	
	/**
	 * 男 打招呼
	 */
	public List<String> getHelloMan(){
		List<String> yuanFen = new ArrayList<String>();
		String json = getJson(TOPIC_ZIP_DIR +"/3.txt");
		if(!TextUtils.isEmpty(json)){
			Gson gson = new Gson();
			yuanFen = gson.fromJson(json,new TypeToken<List<String>>(){}.getType());
			if(yuanFen.size()!=0){
				return yuanFen ;
			}
		}
		return null ;
	}
	
	/**
	 * 男 幽默
	 */
	public List<String> getHummerMan(){
		List<String> yuanFen = new ArrayList<String>();
		String json = getJson(TOPIC_ZIP_DIR +"/4.txt");
		if(!TextUtils.isEmpty(json)){
			Gson gson = new Gson();
			yuanFen = gson.fromJson(json,new TypeToken<List<String>>(){}.getType());
			if(yuanFen.size()!=0){
				return yuanFen ;
			}
		}
		return null ;
	}
	
	/**
	 * 分享文字
	 */
	public JSONObject getShareText(String targetName) {
		JSONObject jsonObject = null;
		
		JSONArray array = null;
		String json = getJson(TOPIC_ZIP_DIR + "/5.txt");
		if(!TextUtils.isEmpty(json)) {
			try {
				array = new JSONArray(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (array != null) {
			for(int i=0; i<array.length(); i++) {
				JSONObject object = array.optJSONObject(i);
				String currentName = object.optString("name");
				if (targetName.equalsIgnoreCase(currentName)) {
					jsonObject = object;
					break;
				}
			}
		}
		
		return jsonObject;
	}
	
	/**
	 * 聊天技文字
	 */
	public String getTaklSkillById(int targetId) {
		String txt = "";
		
		JSONArray array = null;
		String json = getJson(TOPIC_ZIP_DIR + "/6.txt");
		if(!TextUtils.isEmpty(json)) {
			try {
				array = new JSONArray(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (array != null) {
			for(int i=0; i<array.length(); i++) {
				JSONObject object = array.optJSONObject(i);
				int skillId = Integer.valueOf(object.optString("skillId"));
				if (targetId == skillId) {
					JSONArray tips = object.optJSONArray("tips");
					if (tips!=null && tips.length()>0) {
						Random random = new Random(System.currentTimeMillis());
						int index = random.nextInt(tips.length());
						if (index>=0 && index<tips.length()) {
							txt = tips.optString(index);
						}
					}
					break;
				}
			}
		}
		
		return txt;
	}
}
