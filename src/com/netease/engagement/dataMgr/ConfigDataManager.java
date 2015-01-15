package com.netease.engagement.dataMgr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.netease.engagement.app.EngagementApp;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.UserInfoConfig;


/**
 * 管理用户配置文件数据
 */
public class ConfigDataManager {
	
	private static ConfigDataManager mInstance ;
	private ConfigDataManager(){}
	
	public static ConfigDataManager getInstance(){
		if(mInstance == null){
			mInstance = new ConfigDataManager();
		}
		return mInstance ;
	}
	
	//用户信息配置数据文件名
	public static final String USER_CONFIG_FILE = "user_config.json" ;
	
	public void getUserConfig(){
		EgmService.getInstance().addListener(mCallBack);
		String version = EgmPrefHelper.getUserConfigVersion(EngagementApp.getAppInstance().getApplicationContext());
		EgmService.getInstance().doGetUserInfoConfig(version);
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetUserInfoConfigSucess(int transactionId,UserInfoConfig obj) {
			if(obj != null){
				EgmPrefHelper.putUserConfigVersion(EngagementApp.getAppInstance().getApplicationContext(),obj.version);
			}
			EgmService.getInstance().removeListener(mCallBack);
		}

		@Override
		public void onGetUserInfoConfigError(int transactionId, int errCode,String err) {
			if(errCode == EgmServiceCode.TRANSACTION_RES_UNCHANGED){
				EgmService.getInstance().removeListener(mCallBack);
			}
		}
	};
	
	/**
	 * 保存user配置数据，存储到sdcard
	 */
	public void saveUserInfoConfig(String fileName, String data) {
		String dir = EgmUtil.getCacheDir()+"userinfo/";
		File dirFile = new File(dir);
		if(!dirFile.exists()){
			dirFile.mkdir();
		}
        File file = new File(dir + fileName);
        
        if (file != null) {
            if (file.exists()) {
            	return ;
            }else{
            	OutputStream outStream = null;
            	try {
					file.createNewFile();
					outStream = new FileOutputStream(file);
					if (outStream != null) {
						try {
							byte[] d = data.getBytes("utf-8");
							outStream.write(d);
							outStream.flush();
							outStream.close();
						} catch (Exception e) {
							e.printStackTrace();
							try {
								outStream.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
    }
	
	/**
	 * 读取user配置数据
	 */
	public String readString(InputStream in, String charset){
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
	 * 将存储的user配置项转化成UserInfoConfig
	 */
	public UserInfoConfig getUserInfoConfig(){
		UserInfoConfig config = null ;
		String dir = EgmUtil.getCacheDir()+"userinfo/";
		try {
			File file = new File(dir + ConfigDataManager.USER_CONFIG_FILE);
			if(!file.exists()){
				return null ;
			}
			FileInputStream fis = new FileInputStream(dir + ConfigDataManager.USER_CONFIG_FILE);
			String json = readString(fis , null);
			if(!TextUtils.isEmpty(json)){
				Gson gson = new Gson();
				config = gson.fromJson(json, UserInfoConfig.class);
				return config ;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return config ;
	}
	 
	synchronized public static void saveUserConfigToData(String data){
		try {
			//文件不存在的时候会自动创建文件
			FileOutputStream fos = EngagementApp.getAppInstance().getApplicationContext().openFileOutput(
					USER_CONFIG_FILE, Context.MODE_PRIVATE);
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
	
	public UserInfoConfig getUConfigFromData(){
		UserInfoConfig config = null ;
		try {
			FileInputStream fis = EngagementApp.getAppInstance().openFileInput(USER_CONFIG_FILE);
			if(fis == null){
				return null ;
			}
			String json = readString(fis , null);
			if(!TextUtils.isEmpty(json)){
				Gson gson = new Gson();
				config = gson.fromJson(json, UserInfoConfig.class);
				return config ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return config ;
	}
}
