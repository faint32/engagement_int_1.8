package com.netease.engagement.dataMgr;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.netease.engagement.app.EngagementApp;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.EmotConfigResult;


/**
 * 表情配置数据
 */
public class EmotConfigManager {
	
	private static EmotConfigManager mInstance ;
	private EmotConfigManager(){}
	
	public static EmotConfigManager getInstance(){
		if(mInstance == null){
			mInstance = new EmotConfigManager();
		}
		return mInstance ;
	}
	
	public static final String EMOT_CONFIG_FILE = "emot_config.json" ;
	
	public void getEmotConfig(){
		EgmService.getInstance().addListener(mCallBack);
		String version = EgmPrefHelper.getEmotionDataVersion(EngagementApp.getAppInstance().getApplicationContext());
		String faceVersion = EgmPrefHelper.getFaceDataVersion(EngagementApp.getAppInstance().getApplicationContext());
		EgmService.getInstance().doGetEmotConfig(version, faceVersion);
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetEmotConfigSucess(int transactionId,EmotConfigResult obj) {
			if(obj != null){
				EgmPrefHelper.putEmotionDataVersion(EngagementApp.getAppInstance().getApplicationContext(),obj.version);
				EgmPrefHelper.putFaceDataVersion(EngagementApp.getAppInstance().getApplicationContext(),obj.faceVersion);
				
				FaceDownLoadManager.getInstance().downLoadAllFaceZip();
			}
			EgmService.getInstance().removeListener(mCallBack);
		}

		@Override
		public void onGetEmotConfigError(int transactionId, int errCode,String err) {
			if(errCode == EgmServiceCode.TRANSACTION_RES_UNCHANGED){
				EgmService.getInstance().removeListener(mCallBack);
			}
		}
	};
	
	public void saveEmojiConfigToData(String data){
		try {
			FileOutputStream fos = EngagementApp.getAppInstance().getApplicationContext().openFileOutput(
					EMOT_CONFIG_FILE, Context.MODE_PRIVATE);
			if(fos != null){
				byte[] d = data.getBytes("utf-8");
				fos.write(d);
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public EmotConfigResult getEmotConfigFromData(){
		EmotConfigResult config = null ;
		try {
			FileInputStream fis = EngagementApp.getAppInstance().openFileInput(EMOT_CONFIG_FILE);
			if(fis == null){
				return null ;
			}
			String json = readString(fis , null);
			if(!TextUtils.isEmpty(json)){
				Gson gson = new Gson();
				config = gson.fromJson(json, EmotConfigResult.class);
				return config ;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return config ;
	}
	
	/**
	 * 读取user配置数据
	 */
	synchronized public static String readString(InputStream in, String charset){
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

}
