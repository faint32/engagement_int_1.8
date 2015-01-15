package com.netease.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.animation.Animation;

public class Util {
	static byte[] GUID = { (byte) 0xf6, 0x56, (byte) 0xc1, (byte) 0xc5, 0x6c, (byte) 0xbd, 0x46, (byte) 0xc0, (byte) 0xbd, 0x37, (byte) 0x9b, (byte) 0xe5, 0x36, (byte) 0xc8, (byte) 0xdb, 0x00};
	
    private static final String DATE_FORMAT_IN = "EEE MMM dd HH:mm:ss Z yyyy";
    private static final String DATE_FORMAT_OUT = "yyyy-MM-dd HH:mm";
    private static SimpleDateFormat mInputDateFormat = null;
    private static SimpleDateFormat mOutDateFormat = null;
    public static String swVersion = null;

    private static DateFormat getInputDateFormat() {
    	if (mInputDateFormat == null) {
    		mInputDateFormat = new SimpleDateFormat(
    	            DATE_FORMAT_IN, Locale.US);
    	}
    	return mInputDateFormat;
    }
    
    private static DateFormat getOutDateFormat() {
    	if (mOutDateFormat == null) {
    		mOutDateFormat = new SimpleDateFormat(
    				DATE_FORMAT_OUT, Locale.CHINA);
    	}
    	return mOutDateFormat;
    }
    
    public static boolean isArrivedLimit(int count, int base, int mod) {
    	if (count <= 0 || base <= 0 || mod <= 0) {
    		return false;
    	}
    	
    	if (count >= base) {
    		if (0 == (count-base)%mod) {
    			return true;
    		} else {
    			return false;
    		}
    	} else {
    		return false;
    	}
    }
    
    public static String nullStr(String str) {
        if (TextUtils.isEmpty(str) || str.equalsIgnoreCase("null"))
            return null;

        return str;

    }
    
    public static int parseInt(String str) {
    	int result = 0;
    	try {
    		result = Integer.parseInt(str);
    	}
    	catch (NumberFormatException e) {
    		
    	}
    	return result;
    }

    public static float parseFloat(String str) {
    	float result = 0;
    	try {
    		result = Float.parseFloat(str);
    	}
    	catch (NumberFormatException e) {
    		
    	}
    	return result;
    }
    
    public static long parseLong(String str) {
    	long result = 0;
    	try {
    		result = Long.parseLong(str);
    	}
    	catch (NumberFormatException e) {
    		
    	}
    	return result;
    }
    
    public static String formatDateYMDHM(String dateCreated) {
        Date dateRes = new Date();
        if (nullStr(dateCreated) == null)
            return getOutDateFormat().format(dateRes);

        try {
            dateRes = getInputDateFormat().parse(dateCreated);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dateRes == null)
            return dateCreated;
        else {
            return getOutDateFormat().format(dateRes);
        }
    }

    public static String formatMS2YMDHM(long ms) {
        return getOutDateFormat().format(ms);
    }
    
    public static long formatDate2MS(String dateCreated) {
        Date dateRes = null;
        if (nullStr(dateCreated) == null)
            return 0;

        try {
            dateRes = getInputDateFormat().parse(dateCreated);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dateRes == null)
            return 0;
        else
            return dateRes.getTime();
    }
    
    static final String URLCharTable = "!#$%&'()*+,-./:;=?@[\\]^_`{|}~";

    public static String getHttpLink(String str, int offset) {
        int len = 0;
        if (Util.startsWithIgnoreCase(str, offset, "http://")) {
            len = "http://".length();
        } else if (Util.startsWithIgnoreCase(str, offset, "www.")) {
            len = "www.".length();
        } else if (Util.startsWithIgnoreCase(str, offset, "wap.")) {
            len = "wap.".length();
        } else if (Util.startsWithIgnoreCase(str, offset, "https://")) {
            len = "https://".length();
        } else {
            return null;
        }

        int strLen = str.length();

        while (offset + len < strLen) {
            char c = str.charAt(offset + len);
            if ((c >= 'A' && c <= 'Z') // 'a' - 'z'
                    || (c >= 'a' && c <= 'z') // 'A' - 'Z'
                    || (c >= '0' && c <= '9')) { // '0' - '9'
                len++;
            } else {
                if (URLCharTable.indexOf(c) >= 0) {
                    len++;
                } else {
                    break;
                }
            }
        }

        return str.substring(offset, offset + len);
    }
    
 	public static String getStringEraseSuffix(String str) {
 		if (str != null && str.length() > 0) {
 			int loc = str.lastIndexOf(".");
 			if (loc > 0 && loc < str.length()) {
 				String suffix = str.substring(loc + 1);
 				if (suffix.matches("[a-zA-Z]*")) {
 					str = str.substring(0, loc);
 				}
 			}
 		}
 		return str;
 	}

    public static boolean startsWithIgnoreCase(String str, int offset,
            String anObject) {
        int length = anObject.length();

        if (offset + length > str.length()) {
            return false;
        }

        int idx = 0;

        while (idx < length) {
            char c = str.charAt(offset + idx);
            if (c >= 'A' && c <= 'Z') {
                c += 32;
            }
            if (c != anObject.charAt(idx)) {
                break;
            } else {
                idx++;
            }
        }
        if (idx == length && idx > 0) {
            return true;
        }
        return false;
    }

    public static String toString(String str) {
        return str == null ? "" : str;
    }

    public static boolean isStringEmpty(String v) {
        if (v == null || v.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String replace(String str, String oldStr, String newStr) {
        return str.replace(oldStr, newStr);
    }

    public static Vector split(String txt, String splitStr) {
        if (txt == null || txt.length() <= 0 || splitStr == null
                || splitStr.length() <= 0) {

            return null;
        }

        String[] strArr = txt.split(splitStr);
        if (strArr.length > 0) {
            Vector<String> strings = new Vector<String>();
            for (int i = 0; i < strArr.length; i++)
                strings.addElement(strArr[i]);
            return strings;
        }

        return null;
    }

    public static Hashtable cloneHashtable(Hashtable table) {

        if (table != null) {
            return (Hashtable) table.clone();
        } else {
            return null;
        }
    }

    private static final int INSERTIONSORT_THRESHOLD = 7;

    public static void sort(Vector list) {
        if (list != null && list.size() > 0) {
            Object[] src = new Object[list.size()];
            Object[] dest = new Object[list.size()];
            list.copyInto(src);
            list.copyInto(dest);
            mergeSort(src, dest, 0, src.length, 0);
            for (int i = 0; i < dest.length; i++) {
                list.setElementAt(dest[i], i);
            }
        }
    }

    private static void mergeSort(Object[] src, Object[] dest, int low,
            int high, int off) {
        int length = high - low;

        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low
                        && ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid
                    && ((Comparable) src[p]).compareTo(src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    private static void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

//    public static void delFile(String filePath) {
//        File file = new File(filePath);
//        try {
//            if (file != null && file.exists()) {
//                file.delete();
//                file = null;
//            }
//        } catch (Exception e) {
//
//        }
//    }

    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getSwVersion(Context context) {
        if (swVersion == null || swVersion.length() == 0)
            swVersion = swVersionStr(context);
        return swVersion;
    }

    public static String swVersionStr(Context context) {
        String str = "";
        try {
            str = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {

            e.printStackTrace();
        }

        return str;
    }
    
    /**
     * get display String for File Size
     * 
     * @param size
     * @return
     */
    public static String getFileSizeString(long size) {
    	if (size == 0) {
    		return "0.0B";
		}
    	// < 1K
    	if (size < 1024) {
            return size + "B";
        }
        // 1K -- 1M
        else if (size >= 1024 && size < 1048576) {
            int countK = (int) (size / 1024);
            int smallK = (int) ((size * 10 / 1024) % 10);

            if (smallK > 0) {
                return countK + "." + smallK + "KB";
            } else {
                return countK + "KB";
            }
        }
        // > 1M
        else if (size >= 1048576 && size < 1073741824){
            int countM = (int) (size / (1048576));
            int smallM = (int) ((size * 10 / (1048576)) % 10);

            if (smallM > 0) {
                return countM + "." + smallM + "MB";
            } else {
                return countM + "MB";
            }
        }
        else {
        	int countM = (int) (size / (1073741824));
            int smallM = (int) ((size * 10 / (1073741824)) % 10);

            if (smallM > 0) {
                return countM + "." + smallM + "GB";
            } else {
                return countM + "GB";
            }
        }
    }
    
    /**
     * Given the absolute path , then it will delete all files and folders under the specified path.
     * @param path
     * @return
     */
	public static boolean delAllFiles(String path) {
		File file = new File(path);
		if (file.exists()) {
			path = file.getPath();
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			
			String tmp_path = path + "_tmp";
			try {
				File tmpFile = new File(tmp_path);
				if (tmpFile.exists()) {
					Runtime.getRuntime().exec(new String[]{"mv", path, tmp_path}).waitFor();
				}
				else {
					file.renameTo(tmpFile);
				}
				
				Runtime.getRuntime().exec(new String[]{"rm", "-r", tmp_path});
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	
	   public static int[] toRGB(String color) {
			int[] argb = new int[4]; 
			if (color.length() == 8) {
				argb[0] = Integer.valueOf(color.substring(0, 2), 16);
				argb[1] = Integer.valueOf(color.substring(2, 4), 16);
				argb[2] = Integer.valueOf(color.substring(4, 6), 16);
				argb[3] = Integer.valueOf(color.substring(6, 8), 16);
			}
			
			return argb;
		}
	 
		
		 private static final char[] DIGITS = {
		        '0', '1', '2', '3', '4', '5', '6', '7',
		           '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
		    };
		 
		 public static char[] encodeHex(byte[] data) {

		        int l = data.length;

		           char[] out = new char[l << 1];

		           // two characters form the hex value.
		           for (int i = 0, j = 0; i < l; i++) {
		               out[j++] = DIGITS[(0xF0 & data[i]) >>> 4 ];
		               out[j++] = DIGITS[ 0x0F & data[i] ];
		           }

		           return out;
		    }
		 
		 public static String removeCharater(String originalStr,char c){
		        if(originalStr==null || originalStr.length()==0){
		            return null;
		        }
		        int matchStart = originalStr.indexOf(c, 0);
		        if (matchStart == -1) {
		            return originalStr;
		        }
		        int count=originalStr.length();
		        StringBuilder result = new StringBuilder(originalStr.length());
		        int searchStart = 0;
		        do {           
		            result.append(originalStr.substring(searchStart, matchStart));                        
		            searchStart = matchStart + 1;
		        } while ((matchStart = originalStr.indexOf(c,searchStart)) != -1);        
		        if(searchStart<count)
		            result.append(originalStr.substring(searchStart, count));        
		        return result.toString();
		    }
		    
		    public static String removeCharater(String originalStr,String str){
		        if(originalStr==null || originalStr.length()==0){
		            return null;
		        }
		        int matchStart = originalStr.indexOf(str, 0);
		        if (matchStart == -1) {
		            return originalStr;
		        }
		        int count=originalStr.length();
		        int strLeng=str.length();
		        StringBuilder result = new StringBuilder(originalStr.length());
		        int searchStart = 0;
		        do {            
		            result.append(originalStr.substring(searchStart, matchStart));                       
		            searchStart = matchStart + strLeng;
		        } while ((matchStart = originalStr.indexOf(str,searchStart)) != -1);
		        if(searchStart<count)
		            result.append(originalStr.substring(searchStart, count));        
		        return result.toString();
		    }
		    
		    public static String filterReplace(String originalStr,String[] filter,String target){
		        if(originalStr==null || originalStr.length()==0 || filter==null || filter.length==0){
		            return "";
		        }
		        String result=originalStr;
		        for(String fl:filter){
		            result=result.replace(fl,target);
		        }
		        if(result==null)
		            result="";
		        return result;
		    }
		    
		    public static String contentProcess(String original){
		        //String[] filter= new String[]{" ","　","\r","\n"};
		    	String[] filter= new String[]{"　","\r","\n"};
		        return filterReplace(original,filter,"");
		    }

			public static boolean checkMD5(InputStream is, String verfyMD5) {
				try {
					MessageDigest md = MessageDigest.getInstance("MD5");
					
					byte[] data = new byte[1024 * 4];
					int length = 0;
					while ((length = is.read(data)) > 0) {
						md.update(data, 0, length);
					}
					
					is.close();
					
					BigInteger number = new BigInteger(1, md.digest());
					String md5 = number.toString(16);

					while (md5.length() < 32)
						md5 = "0" + md5;

					return verfyMD5.equals(md5);
					
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		 
			public static int dip2px(Context context, float dpValue) {
				final float scale = context.getResources().getDisplayMetrics().density;
				return (int) (dpValue * scale + 0.5f);
			}
			
			public static int px2dip(Context context, float dpValue) {
				final float scale = context.getResources().getDisplayMetrics().density;
				return (int) (dpValue / scale + 0.5f);
			}
			
	public static boolean isCurrentProcess(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager activityMag = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		String processName = context.getApplicationInfo().processName;
//		NTLog.i("isCurrentProcess", "Mypid = " +  pid + "|MyprecessName = " + processName);
		for (RunningAppProcessInfo appProcess : activityMag.getRunningAppProcesses()) {
//			NTLog.i("isCurrentProcess", "pid = " +  appProcess.pid + "|precessName = " + appProcess.processName);
			if (appProcess.pid == pid 
					&& appProcess.processName.equalsIgnoreCase(processName)) {
				return true;
			}
		}

		return false;
	}
	
	
	 /**
     * Cancel an {@link AsyncTask}.  If it's already running, it'll be interrupted.
     */
    public static void cancelTaskInterrupt(AsyncTask<?, ?, ?> task) {
        cancelTask(task, true);
    }

    /**
     * Cancel an {@link AsyncTask}.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     *        task should be interrupted; otherwise, in-progress tasks are allowed
     *        to complete.
     */
    public static void cancelTask(AsyncTask<?, ?, ?> task, boolean mayInterruptIfRunning) {
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(mayInterruptIfRunning);
        }
    }
    
    public static void cancelAnimation(Object receiver) {
	    if (VersionUtils.getSDKVersionNumber() >=8 ) {
			try {
				Method cancel = Animation.class.getMethod("cancel");
				cancel.invoke(receiver);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
    }
    
    //判断邮件地址合法性
    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern
            .compile("[a-zA-Z0-9\\+\\.\\_\\%\\-]{1,256}" + "\\@"
                    + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
                    + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

    public static boolean isEmailAddress(String s) {
        Matcher match = EMAIL_ADDRESS_PATTERN.matcher(s);
        return match.matches();
    }
    
    /**
     * 获取当前设备的ip地址
     * @return
     */
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
		}

		return null;
	}
}
