package com.netease.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.netease.common.receiver.NetworkReceiver;
import com.netease.common.receiver.SdcardReceiver;
import com.netease.common.service.BaseService;

public class PlatformUtil {

	/******************************************************************
	 * 
	 * 常用的标志位判断，不建议用map等（虽然方便）
	 * 
	 *****************************************************************/
	
	private static boolean mCheckNetworkReceiver = false;
	
	private static boolean mCheckSdcardReceiver = false; // 
	
	private static boolean mHaveNetworkReceiver = false; // mCheckNetworkReceiver false 的情况下去做一次检查
	
	private static boolean mHaveSdcardReceiver = false; // mCheckSdcardReceiver false 的情况下去做一次检查
	
	private static NetworkInfo mNetworkInfo = null; // mHaveNetworkReceiver true 时有效
	
	private static boolean mSdcardMounted = false; // mHaveSdcardReceiver true 时有效
	
	/**
	 * 获取设备ID
	 * 
	 * @param context
	 * @return
	 */
    public static String getDeviceID(Context context) {
        String imei = null;
        String androidId = null;
        try {
            imei = getPhoneIMEI(context);
            if (TextUtils.isEmpty(imei)) {
                imei = getWifiMacAddress(context);
            }

            androidId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
            if (!TextUtils.isEmpty(androidId)) {
                imei += "_" + androidId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imei == null) {
            imei = "";
        }
        return imei;
    }
	
	/**
	 * 获取OS版本
	 * 
	 * @return
	 */
	public static String getOSVersion() {
		return android.os.Build.VERSION.RELEASE;
	}
	
	/**
	 * 获取设备名字
	 * 
	 * @return
	 */
	public static String getMobileName() {
		return android.os.Build.MODEL;
	}
	
    /**
     * 获取屏幕分辨率 
     * 
     * @return
     */
    public static String getResolution(Context context) {
    	DisplayMetrics dm = new DisplayMetrics();
		WindowManager WM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		WM.getDefaultDisplay().getMetrics(dm);
		StringBuffer buffer = new StringBuffer();
		buffer.append(dm.widthPixels).append('x').append(dm.heightPixels);
		return buffer.toString();	    	
    }
    
	/**
	 * 获取应用名称
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private static NetworkInfo getActiveNetworkInfo(Context context) {
		NetworkInfo info = null;

		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return info;
		}
		
		NetworkInfo[] infos = connectivity.getAllNetworkInfo();
		if (infos == null) {
			return info;
		}
		
		for (int i = 0; i < infos.length; i++) {
			NetworkInfo tmp = infos[i];
			if (null != tmp && tmp.isAvailable()
					&& tmp.isConnectedOrConnecting()) {
				info = tmp;
				break;
			}
		}

		return info;
	}
	
	/**
	 * 用于receiver 更新网络状态
	 * 
	 * @param context
	 */
	public static void updateCurrentNetworkInfo(Context context) {
		mNetworkInfo = getActiveNetworkInfo(context);
	}
	
	private static NetworkInfo getCurrentNetworkInfo(Context context) {
		if (! mCheckNetworkReceiver) {
			ComponentName name = new ComponentName(context, NetworkReceiver.class);
			int setting = context.getPackageManager().getComponentEnabledSetting(name);
			if (setting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
				mNetworkInfo = getActiveNetworkInfo(context);
				
				mHaveNetworkReceiver = true;
			}
			
			mCheckNetworkReceiver = true;
		}
		else if (! mHaveNetworkReceiver) {
			return getActiveNetworkInfo(context);
		}
		
		return mNetworkInfo; 
	}
	
	// status
	public static boolean hasConnected(Context context) {
		NetworkInfo info = getCurrentNetworkInfo(context);

		return info != null;
	}
	
	public static boolean isMobileNetWork(Context context) {
		NetworkInfo info = getCurrentNetworkInfo(context);
		
		return info != null && info.getType() == ConnectivityManager.TYPE_MOBILE;
	}
	
	public static boolean isWifiNetWork(Context context) {
		NetworkInfo info = getCurrentNetworkInfo(context);
		
		return info != null && info.getType() == ConnectivityManager.TYPE_WIFI;
	}
	/**
     * 获取网络类型名称
     * @param context
     * @return
     */
	public static String getNetWorkName(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null ) {
            return networkInfo.getTypeName();
        } else {
            return "";
        }
    }
	/**
     * 获取运营商名称
     * @param context
     * @return
     */
	public static String getOperatorName(Context context) {
	    String OperatorName = "";
	    try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imsi = tm.getSubscriberId();
            if(imsi!=null){
                if(imsi.startsWith("46000") || imsi.startsWith("46002")){//因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
                    OperatorName = "中国移动";
                }else if(imsi.startsWith("46001")){
                    OperatorName = "中国联通";
                }else if(imsi.startsWith("46003")){
                    OperatorName = "中国电信";
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
	    return OperatorName;
	}
	
	public static String getAppVersionName(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 获取应用版本号
	 * @param context
	 * @return
	 */
	public static String swSimpleVersionStr(Context context) {
		String str = "";
		try {
			str = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
			str = str.trim();
		} catch (NameNotFoundException e) {
		}

		if (str != null && str.length() > 0) {
			int index = str.indexOf(' ');
			if (index > 0) {
				return str.substring(0, index);
			}
		}
		return str;
	}
	
	public static long getAvailableExternalMemorySize() {
		if (hasStorage()) {
			File path = Environment.getExternalStorageDirectory();

			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		}
		return -1;
	}
	
	
	public static void updateSdcardMounted() {
		String state = Environment.getExternalStorageState();
		
		mSdcardMounted = Environment.MEDIA_MOUNTED.equals(state);
	}
	
	/**
	 * 判断是否sdcard mounted
	 * 
	 * @return
	 */
	private static boolean isSdcardMounted() {
		if (! mCheckSdcardReceiver) {
			Context context = BaseService.getServiceContext();
			if (context != null) {
				ComponentName name = new ComponentName(context, SdcardReceiver.class);
				int setting = context.getPackageManager().getComponentEnabledSetting(name);
				if (setting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
					mHaveSdcardReceiver = true;
				}
			}
			
			String state = Environment.getExternalStorageState();
			
			mSdcardMounted = Environment.MEDIA_MOUNTED.equals(state);
			
			mCheckSdcardReceiver = true;
		}
		else if (! mHaveSdcardReceiver) {
			String state = Environment.getExternalStorageState();
			
			return Environment.MEDIA_MOUNTED.equals(state);
		}
		
		return mSdcardMounted;
	}
	
	/**
	 * 
	 * @return
	 */
    public static boolean hasStorage() {
		return isSdcardMounted();
	}

	 /**
     * 会过滤不能写的分区
     * @return
     */
    public static HashSet<String> getExternalMounts() {
	    final HashSet<String> out = new HashSet<String>();
	    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
		    out.add(Environment.getExternalStorageDirectory().getPath());
	    }
	    String reg = ".* (vfat|ntfs|exfat|fat32|fuse) .*rw.*";
	    try {
	        final Process process = new ProcessBuilder().command("mount").redirectErrorStream(true).start();
	        process.waitFor();
	        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;
	        while ((line = br.readLine()) != null) {
	        	if (!line.toLowerCase(Locale.US).contains("asec") && !line.toLowerCase(Locale.US).contains("obb") && !line.toLowerCase(Locale.US).contains("secure")) {
		            if (line.matches(reg)) {
		                String[] parts = line.split(" ");
		                for (int i = 1; i < parts.length; i++) {
		                	String part = parts[i];
		                    if (part.startsWith("/")) {
		                    	if (new File(part).canWrite()) {
			                    	boolean needAdd = true;//nexus 4的两个目录非常奇怪，可能是内部做了替换 [/storage/emulated/legacy, /storage/emulated/0]
			                    	String tmpFilename = System.currentTimeMillis() + "";
			                    	File f = new File(part, tmpFilename);
			                    	f.createNewFile();
			                    	for (String o : out) {
			                    		if (new File(o, tmpFilename).exists()) {
			                    			needAdd = false;
			                    			break;
			                    		}
			                    	}
			                    	f.delete();
			                    	if (needAdd) {
				                    	out.add(part);
			                    	}
		                    	}
		                    	break;
		                    }
		                }
		            }
		        }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
        return out;
	}
    
    public static String getPhoneIMEI(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId();
		return imei;
	}
	
	public static String getWifiMacAddress(Context context) {
		try {
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();
			return info.getMacAddress();
		} catch (Exception e) {
			return "";
		}
	}
	
	// just support some devices and emulator, after connected network, maybe
	// get
	// some devices maybe return "" or null
	public static String getPhoneNumber(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		String phonenumber = mTelephonyMgr.getLine1Number();
		return phonenumber;
	}
	
	/**
	 * 检查是否有intent的接收者
	 * 
	 * @param context
	 * @param intent
	 * @return
	 */
	public static boolean hasIntentRecevicer(Context context, Intent intent) {
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
		return list != null && list.size() > 0;
	}
	
	
	/**
	 * 获取WIFI IP地址
	 * 
	 * @param context
	 * @return
	 */
	public static String getWifiIpAddress(Context context) {
		String ipAddress = null;
		
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			DhcpInfo info = wifi.getDhcpInfo();
			if (info != null) {
				ipAddress = toAddress(info.ipAddress);
			}
		}
		
		return ipAddress;
	}
	
	private static String toAddress(int i) {
		return ( i & 0xFF) + "." +
	        (( i >> 8 ) & 0xFF) + "." +
	        (( i >> 16 ) & 0xFF) + "." +
	        (( i >> 24 ) & 0xFF);
	}


	/*************** hardware ****************/

	/*************** screen ***************/
	/**
	 * return value like width/height
	 */
	private static int mDeviceWidth;
	private static int mDeviceHeight;
	private static float mDeviceDensity;
	
	static public float getDisplayDensity(Context context) {
		if (mDeviceDensity <= 0.1) {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(dm);
			mDeviceDensity = dm.density;
		}
		
		return mDeviceDensity;
	}
	
	public static int[] getDisplayMetrics(Context context) {
		if (mDeviceWidth > 0 && mDeviceHeight > 0) {
		}
		else {
			int width = 0;
			int height = 0;
			
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			display.getMetrics(dm);
			
			if(VersionUtils.getSDKVersionNumber() >= VersionUtils.Android_SDK_2_0) {
				width = dm.widthPixels;
				height = dm.heightPixels;
			}
			else {
				width = (int)(dm.widthPixels/dm.density);
				height =  (int)(dm.heightPixels/dm.density);
			}
			
			int sdkVersion;
			try {
				sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
			} catch (NumberFormatException e) {
				sdkVersion = 5;
			}
			if (sdkVersion >= 13) {
				try {
					Method mGetRawH = Display.class.getMethod("getRawWidth");
					Method mGetRawW = Display.class.getMethod("getRawHeight");
					width = (Integer)mGetRawW.invoke(display);
					height = (Integer)mGetRawH.invoke(display);
				} catch (Exception e) {
				}
			}
			
			if (width < height) {
				mDeviceWidth = width;
				mDeviceHeight = height;
			} else {
				mDeviceWidth = height;
				mDeviceHeight = width;
			}
		}
		
		return new int[] {mDeviceWidth, mDeviceHeight};
	}
	
	/*************** screen ****************/

	/************* Language *********************/

	/**
	 * return en-us
	 * 
	 * @param context
	 * @return
	 */
	static public String getLocalLanguage(Context context) {
		Locale local = Locale.getDefault();
		return local.getLanguage() + "-" + local.getCountry();

	}

	/************ Language ******************************/
	
	/*********Space**************/
	
	public static boolean enoughSpaceBySize(String path,long size)
	{
		if(TextUtils.isEmpty(path))
			return true;
		
		StatFs statFS = new StatFs(path);
		return (long) statFS.getAvailableBlocks()*statFS.getBlockSize() > size;
	}
	
	public static long availableSize(String path) {
		StatFs statFS = new StatFs(path);
		return (long) statFS.getAvailableBlocks() * statFS.getBlockSize();
	}
	
	public static boolean isSupportMultiPointer() {
		boolean multiTouchAvailable1 = false;
		boolean multiTouchAvailable2 = false;
		// Not checking for getX(int), getY(int) etc 'cause I'm lazy
		Method methods[] = MotionEvent.class.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equals("getPointerCount")) {
				multiTouchAvailable1 = true;
			}
			if (m.getName().equals("getPointerId")) {
				multiTouchAvailable2 = true;
			}
		}

		if (multiTouchAvailable1 && multiTouchAvailable2) {
			return true; // 支持多点触摸
		} else {
			return false;
		}
	}
}
