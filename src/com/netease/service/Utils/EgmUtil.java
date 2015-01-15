package com.netease.service.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.common.cache.CacheManager;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityDownload;
import com.netease.engagement.app.EgmConstants;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.VersionInfo;
import com.netease.util.EnctryUtil;

public class EgmUtil {

    public static String nullStr(String str) {
        if (TextUtils.isEmpty(str))
            return null;

        return str;
    }

    /**
     * dip转pixel
     * 
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * pixel转dip
     * 
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static boolean saveObjectToFile(Context context, Object o,
            String filename) {
        if (context == null) {
            return false;
        }
        try {
            FileOutputStream fos = context.openFileOutput(filename,
                    Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
            oos.flush();
            oos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Object readObjectFromFile(Context context, String filename) {
        if (context == null) {
            return null;
        }
        try {
            FileInputStream fis = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String swVersion = null;

    public static String getSwVersion(Context context) {
        if (swVersion == null || swVersion.length() == 0)
            swVersion = getApplicationMetaInfo(context, "VERSION");
        return swVersion;
    }

    public static String getApplicationMetaInfo(Context context, String metaName) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(context.getPackageName(), 128);
            if (info != null && info.metaData != null) {
                return info.metaData.getString(metaName);
            } else {
                return null;
            }
        } catch (NameNotFoundException e) {
            return null;
        }

    }

    public static String numberVersion = null;
    public static String getNumberVersion(Context context) {
        if (TextUtils.isEmpty(numberVersion) && context != null) {
            try {
                numberVersion = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0).versionName;
            } catch (NameNotFoundException e) {
            }
            if (null != numberVersion) {
                numberVersion = numberVersion.trim();
            }
        }
        return TextUtils.isEmpty(numberVersion) ? "1.0.0" : numberVersion;
    }

    /** 获取渠道id */
    private static  String swChannel = null;

    public static String getAppChannelID(Context context) {
        if (TextUtils.isEmpty(swChannel))
            swChannel = getApplicationMetaInfo(context, "Channel");
        
        return TextUtils.isEmpty(swChannel) ? "normal":swChannel;

    }
    /** 判断是否是首发渠道 */
    public static boolean isShoufa(Context context) {
        String   sf = getApplicationMetaInfo(context, "isShouFa");
        return sf!=null && sf.equals("y");
    }

    // 判断邮件地址是否合法
    public static boolean isValidAddress(String address) {
        // Note: Some email provider may violate the standard, so here we only
        // check that
        // address consists of two part that are separated by '@', and domain
        // part contains
        // at least one '.'.
        int len = address.length();
        int firstAt = address.indexOf('@');
        int lastAt = address.lastIndexOf('@');
        int firstDot = address.indexOf('.', lastAt + 1);
        int lastDot = address.lastIndexOf('.');
        return firstAt > 0 && firstAt == lastAt && lastAt + 1 < firstDot
                && firstDot <= lastDot && lastDot < len - 1;
    }

    // 获取编码格式
    public static String getCharset(String ContentType) {
        if (ContentType == null) {
            return "UTF-8";
        } else {
            int i = ContentType.indexOf("=");
            String charset = null;
            if (i < 0) {
                charset = "UTF-8";
            } else {
                charset = ContentType.substring(i + 1);
            }
            return charset;
        }
    }
    //判断手机号码合法性
    public static boolean isValidatePhoneNum(String phoneNum) {
        return phoneNum.matches("1[0-9]{10}");
    }
    /**一个汉字算2个字符*/
    public static int getNicknameLen(String nickname) {
        int len = 0;
        char[] chars = nickname.toCharArray();
        for (char c : chars) {
            String s = Character.toString(c);
            if (s.matches("[\\u4E00-\\u9FA5]")) {
                len += 2;
            } else {
                len++;
            }
        }
        return len;
    }
    
    public static boolean isNicknameContainsSpecialChar(String nickname) {
        boolean result = false;
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9_\\-\\u4E00-\\u9FA5]");
        Matcher matcher = pattern.matcher(nickname);
        if (matcher.find()) {
            result = true;
        }
        return result;
    }
    
    /**
     * 检查是否包含该APP
     * @param context
     * @param packageName App包名
     * @return
     */
    public static boolean hasApp(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_ACTIVITIES);
            if(pi == null)return false;
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);
            List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(
                    resolveIntent, 0);
            ResolveInfo ri = apps.iterator().next();
            if(ri == null)return false;
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
    /**
     * 根据包名启动一个外部App。
     * 若该app已经安装且启动成功返回true，否则返回false。
     * @param context
     * @param packageName App包名
     * @return
     */
    public static boolean startOtherApp(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_ACTIVITIES);
            if(pi == null)return false;
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);
            List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(
                    resolveIntent, 0);
            ResolveInfo ri = apps.iterator().next();
            if(ri == null)return false;
            String className = ri.activityInfo.name;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            context.startActivity(intent);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
    
    private final static long SIZE_KB = 1024;
    private final static long SIZE_MB = SIZE_KB * 1024;
    private final static long SIZE_GB = SIZE_MB * 1024;
    private final static long SIZE_TB = SIZE_GB * 1024;
    
    private final static long SIZE_KB_X = 1000;
    private final static long SIZE_MB_X = SIZE_KB * 1000;
    private final static long SIZE_GB_X = SIZE_MB * 1000;
    private final static long SIZE_TB_X = SIZE_GB * 1000;
    
    public final static int SIZE_UINT_DEFAULT = -1;
    public final static int SIZE_UINT_B = 0;
    public final static int SIZE_UINT_KB = 1;
    public final static int SIZE_UINT_MB = 2;
    public final static int SIZE_UINT_GB = 3;
    public final static int SIZE_UINT_TB = 4;
    public static String getSizeStr(long size, int num, int unit) {
        if(size <= 0)return "0B";
        StringBuffer sb = new StringBuffer("");
        final Float s = getSize(size, num, unit);
        if (unit == SIZE_UINT_B) {
            sb.append(s.intValue()).append("B");
        } else if (unit == SIZE_UINT_KB) {
            sb.append(s.intValue()).append("KB");
        } else if (unit == SIZE_UINT_MB) {
            sb.append(s).append("MB");
        } else if (unit == SIZE_UINT_GB) {
            sb.append(s).append("GB");
        } else if (unit == SIZE_UINT_TB) {
            sb.append(s).append("TB");
        } else {
            if (size < SIZE_KB_X) {
                sb.append(s.intValue()).append("B");
            } else if (size < SIZE_MB_X) {
                sb.append(s.intValue()).append("KB");
            } else if (size < SIZE_GB_X) {
                sb.append(s.intValue()).append("MB");
            } else if (size < SIZE_TB_X) {
                sb.append(s).append("GB");
            } else {
                sb.append(s).append("TB");
            }
        }
        
        return sb.toString();
    }
    
    public static String getSizeStrNoB(long size, int num, int unit) {
        StringBuffer sb = new StringBuffer(size < 0 ? "-" : "");
        final Float s = getSize(size, num, unit);
        
        if (unit == SIZE_UINT_B) {
            sb.append(s.intValue()).append("B");
        } else if (unit == SIZE_UINT_KB) {
            sb.append(s.intValue()).append("K");
        } else if (unit == SIZE_UINT_MB) {
            sb.append(s).append("M");
        } else if (unit == SIZE_UINT_GB) {
            sb.append(s).append("G");
        } else if (unit == SIZE_UINT_TB) {
            sb.append(s).append("T");
        } else {
            if (size < SIZE_KB_X) {
                sb.append(s.intValue()).append("B");
            } else if (size < SIZE_MB_X) {
                sb.append(s.intValue()).append("K");
            } else if (size < SIZE_GB_X) {
                sb.append(s.intValue()).append("M");
            } else if (size < SIZE_TB_X) {
                sb.append(s).append("G");
            } else {
                sb.append(s).append("T");
            }
        }
        
        return sb.toString();
    }
    public static float getSize(long size, int num, int unit) {
        double s = size;
        if (unit == SIZE_UINT_B) {
            return size;
        } else if (unit == SIZE_UINT_KB) {
            return formatDoubleNum((s /= SIZE_KB), num);
        } else if (unit == SIZE_UINT_MB) {
            return formatDoubleNum((s /= SIZE_MB), num);
        } else if (unit == SIZE_UINT_GB) {
            return formatDoubleNum((s /= SIZE_GB), num);
        } else if (unit == SIZE_UINT_TB) {
            return formatDoubleNum((s /= SIZE_TB), num);
        } else {
            if (size < SIZE_KB_X) {
                return size;
            } else if (size < SIZE_MB_X) {
                return formatDoubleNum((s /= SIZE_KB), num);
            } else if (size < SIZE_GB_X) {
                return formatDoubleNum((s /= SIZE_MB), num);
            } else if (size < SIZE_TB_X) {
                return formatDoubleNum((s /= SIZE_GB), num);
            } else {
                return formatDoubleNum((s /= SIZE_TB), num);
            }
        }
    }
    /**
     * 获取小数点位数
     * @param d 原来数据
     * @param num 小数点位数
     * @return
     */
    public static Float formatDoubleNum(double d, int num){
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale (num, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    
    /**
     * 判断一个应用是否在前台
     * @param Context
     */
    public static boolean isTopActivity(Context context){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo>  tasksInfo = am.getRunningTasks(1);
        if(tasksInfo.size() > 0){
            //应用程序位于堆栈的顶层
            if(context.getPackageName().equals(tasksInfo.get(0).topActivity.getPackageName())){
                return true;
            }
        }
        return false;
    }
    
	public static <T> void reverseList(List<T> list){
        if(list == null || list.size() < 2)
            return;
        for(int i = 0; i < list.size() / 2; i++){
            T t = list.get(i);
            list.set(i, list.get(list.size() - i - 1));
            list.set(list.size() - i - 1, t);
        }
    }
	
    // 短信（彩信）分享
    public static void share2MMSByPath(Context context, String content, String path) {
        if (path == null || path.equals("")) {
            share2MMSByUri(context, content, null);
            return;
        }
        File file = new File(path);
        if (file.exists())
            share2MMSByUri(context, content, Uri.fromFile(file));
        else {
            ToastUtil.showToast(context, "多媒体路径无效");
            share2MMSByUri(context, content, null);
        }
    }

    public static void share2MMSByUri(Context context, String content, Uri uri) {
        if (uri == null) {
            share2SMS(context, content);
            return;
        }

        Intent it = new Intent(Intent.ACTION_SEND);
        it.putExtra("sms_body", content);
        it.putExtra(Intent.EXTRA_STREAM, uri);
        it.setType("image/png");
        context.startActivity(it);
    }

    public static void share2SMS(Context context, String toShare) {
        Uri uri = Uri.parse("smsto:");
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        it.putExtra("sms_body", toShare);
        context.startActivity(it);
    }
	
	//邮件分享
    public static void share2EmailByPath(Context context, String shareTxt, String title, String path) {
        if (path == null || path.equals("")) {
            share2EmailByUri(context, shareTxt, title, null);
            return;
        }
        File file = new File(path);
        if (file.exists())
            share2EmailByUri(context, shareTxt, title, Uri.fromFile(file));
        else {
            ToastUtil.showToast(context, "多媒体路径无效");
            share2EmailByUri(context, shareTxt, title, null);
        }
    }
    
    public static void share2EmailByUri(Context context, String shareTxt, String title, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(shareTxt));
//        intent.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(shareTxt));
//        intent.putExtra(Intent.EXTRA_STREAM, "");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {});
        intent.setType("text/html");
        context.startActivity(intent);
    }
    
    public static String nullToZero(String src){
        if(src == null) {
            src = "0";
        }
        return src;
    }
    
    public static String getIdFromPath(String path) {
    	String id = null;
    	
    	File file = new File(path);
    	String name = file.getName();
    	int index = name.lastIndexOf('.');
        if (index > 0) {
        	name = name.substring(0, index);
        	index = name.lastIndexOf("_");
        	if (index > 0) {
        		id = name.substring(index+1);
        	}
        }
        
    	return id;
    }
    
    /**
     * 删除目录（文件夹）以及目录下的文件
     * @param   sPath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        if(TextUtils.isEmpty(sPath))
            return true;
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 删除单个文件
     * @param   sPath    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
    
    /**  
     * 复制单个文件  
     * @param oldPath 原文件路径  
     * @param newPath 复制后路径  
     */   
    public static boolean copyFile(String oldPath, String newPath) {  
        boolean result = false;
        
        if(!TextUtils.isEmpty(oldPath) && !TextUtils.isEmpty(newPath)){
        	InputStream inStream = null;
        	FileOutputStream fs = null;
            try {   
                int byteread = 0;   
                File oldfile = new File(oldPath);   
                if (oldfile.exists()) { //文件存在时   
                    inStream = new FileInputStream(oldPath); //读入原文件   
                    fs = new FileOutputStream(newPath);   
                    
                    byte[] buffer = new byte[1024];   
                    while ( (byteread = inStream.read(buffer)) != -1) {   
                        fs.write(buffer, 0, byteread);   
                    }   
                    inStream.close(); 
                    
                    result = true;
                }   
            } catch (Exception e) {   
                e.printStackTrace();   
            } finally {
            	if (inStream != null) {
            		try {
						inStream.close();
						inStream = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            	if (fs != null) {
            		try {
            			fs.close();
            			fs = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            }
        }
        
        return result;
    }   
    
    // status
 	public static boolean hasConnected(Context context) {
 		ConnectivityManager connectivity = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (connectivity == null) {

 		} else {
 			NetworkInfo[] info = connectivity.getAllNetworkInfo();
 			if (info != null) {
 				for (int i = 0; i < info.length; i++) {
 					if (null != info[i] && info[i].isAvailable()
 							&& info[i].isConnected()) {
 						return true;
 					}
 				}
 			}
 		}

 		return false;
 	}
 	
   /**
     * 该路径用于保存一些临时文件（用于预览或上传的图片、音频、视频或草稿等），在缓存子目录下，不会随清理缓存而清除，需要删除的内容调用者自己管理
     */
    public static String getCacheDir() {
        File file = null; 
        String path = null;
        path = CacheManager.getRoot() + File.separator;
        file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }
    
    public static String getMD5(String message){
        String digest = message;
        try{
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(message.getBytes("UTF-8"));
            digest = EnctryUtil.bytesToHexString(algorithm.digest());
        }
        catch (Exception e){
            e.printStackTrace();
        }   
        
        return digest.toLowerCase();
    }

    public static byte[] AEStoByte(String hexString) {      
        int len = hexString.length()/2;      
        byte[] result = new byte[len]; 
        
        for (int i = 0; i < len; i++)      
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();   
        
        return result;      
    } 
    
    public static final int TYPE_BASE = 0;
	//本地图片或者相机图片预览
	public static final int TYPE_PREVIEW  = TYPE_BASE;
	public static final int TYPE_CAMERA   = TYPE_BASE + 1;
	public static final int TYPE_RECORDER = TYPE_BASE + 2;
	/**
     * 根据不同的文件类型生成存储地址
     */
    public static String getFilePathByType(int type, String id) {
		String path = null;
		String filePath = null;
		path = EgmUtil.getCacheDir().toString();
		if (!new File(path).exists()) {
			new File(path).mkdirs();
		}
		
		if (TYPE_PREVIEW == type) {
			filePath = path + "image_preview_"+ id + ".png";
		} else if (TYPE_CAMERA == type) {
			filePath = path + "pic_camera_"+ id + ".png";
		} else if (TYPE_RECORDER == type) {
		    filePath = path + "media_recorder_"+ id + ".3gp";
		}
		return filePath;
	}
    
    private static String Version;
    
    public static String getVersionStr(Context context) {
    	if (TextUtils.isEmpty(Version)) {
            try {
            	Version = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0).versionName;
            } catch (Exception e) {
            }
    	}
        
        return Version;
    }
    
    /**
     * 创建自定义的Menu对话框，分隔线是紫色的，menu文字是紫色的。
     * listener中通过view的getTag，转为Integer，即为item，从0开始。另外，需要在listener的点击中设置整个dialog消失，否则点击后对话框不会消失。
     * @param context
     * @param title
     * @param items
     * @param listener
     * @return
     */
    public static AlertDialog createEgmMenuDialog(Context context, String title, 
    		CharSequence[] items, View.OnClickListener listener){
    	return createEgmMenuDialog(context, title, items, listener, false);
    }
    
    /**
     * 创建自定义的Menu对话框，分隔线是紫色的，menu文字是紫色的。
     * listener中通过view的getTag，转为Integer，即为item，从0开始。另外，需要在listener的点击中设置整个dialog消失，否则点击后对话框不会消失。
     * @param context
     * @param title
     * @param items
     * @param listener
     * @param autoDismiss 自动关闭dialog
     * @return
     */
    public static AlertDialog createEgmMenuDialog(Context context, String title, 
    		CharSequence[] items, View.OnClickListener listener, boolean autoDismiss){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.view_custom_egm_dialog, null, false);
        TextView titleTv = (TextView)layout.findViewById(R.id.dialog_title);
        LinearLayout list = (LinearLayout)layout.findViewById(R.id.dialog_list);
        
        if(TextUtils.isEmpty(title)){
        	titleTv.setVisibility(View.GONE);
        }
        titleTv.setText(title);
        
        int height = (int)context.getResources().getDimension(R.dimen.dialog_item_height);
        int dividreHeight = (int)context.getResources().getDimension(R.dimen.divider_height_1);
        
        AutoDismissListener adListener = new AutoDismissListener(listener);
        if (autoDismiss) {
        	listener = adListener;
        }
        
        for(int i = 0; i < items.length; i++){
            TextView itemTv = (TextView)inflater.inflate(R.layout.view_egm_dialog_item, null, false);
            
            itemTv.setText(items[i]);
            itemTv.setTag(Integer.valueOf(i));
            itemTv.setOnClickListener(listener);
            
            list.addView(itemTv, LayoutParams.MATCH_PARENT, height);
            if(i != items.length - 1){  // 最后一个没有分隔线
                ImageView divider = new ImageView(context);
                divider.setBackgroundColor(context.getResources().getColor(R.color.grey));
                list.addView(divider, LayoutParams.MATCH_PARENT, dividreHeight);
            }
        }
        
        
        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        
        adListener.setDialog(dialog);
        
        return dialog;
    }
    
    /**
     * 创建自定义的对话框，分隔线是紫色的，文字是紫色的。
     * 要在listener的点击中设置整个dialog消失，否则点击后对话框不会消失。
     * @param context
     * @param title
     * @param content
     * @param left
     * @param right
     * @param listener
     * @return
     */
    public static AlertDialog createEgmBtnDialog(Context context, String title, 
    		String content, String left, String right, View.OnClickListener listener){
    	return createEgmBtnDialog(context, title, content, left, right, listener, false);
    }
    
    public static AlertDialog createEgmBtnDialog(Context context, String title,
    		String content, String left, String right,
    		View.OnClickListener listener, boolean autoDismiss){
    	
    	AutoDismissListener autoListener = new AutoDismissListener(listener);
    	if (autoDismiss) {
    		listener = autoListener;
    	}
    	
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.view_custom_btn_dialog, null, false);
        TextView titleTv = (TextView)layout.findViewById(R.id.dialog_title);
        ImageView titleDiv = (ImageView)layout.findViewById(R.id.dialog_divider1);
        TextView leftBtn = (TextView)layout.findViewById(R.id.dialog_btn_left);
        TextView rightBtn = (TextView)layout.findViewById(R.id.dialog_btn_right);
        TextView contentTv = (TextView)layout.findViewById(R.id.dialog_content);
        ImageView btnDiv = (ImageView)layout.findViewById(R.id.dialog_divider3);
        
        if(TextUtils.isEmpty(title)){
            titleTv.setVisibility(View.GONE);
            titleDiv.setVisibility(View.GONE);
        } else {
            titleTv.setText(title);
        }
        contentTv.setText(content);
        
        leftBtn.setText(left);
        leftBtn.setTag(Integer.valueOf(DialogInterface.BUTTON_NEGATIVE));
        leftBtn.setOnClickListener(listener);
        
        rightBtn.setText(right);
        rightBtn.setTag(Integer.valueOf(DialogInterface.BUTTON_POSITIVE));
        rightBtn.setOnClickListener(listener);
        
        if(TextUtils.isEmpty(left)){
            leftBtn.setVisibility(View.GONE);
            btnDiv.setVisibility(View.GONE);
        }
        
        if(TextUtils.isEmpty(right)){
            rightBtn.setVisibility(View.GONE);
            btnDiv.setVisibility(View.GONE);
        }

        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        
        autoListener.setDialog(dialog);
        dialog.setCanceledOnTouchOutside(false);
        
        return dialog;
    }
    
    /**
     * 创建自定义的对话框，分隔线是紫色的，文字是紫色的。
     * 要在listener的点击中设置整个dialog消失，否则点击后对话框不会消失。
     * @param context
     * @param title
     * @param content
     * @param image
     * @param left
     * @param right
     * @param listener
     * @return
     */
    public static AlertDialog createEgmBtnWithImageDialog(Context context, String title, String content, int imageRes, String left, String right,View.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.view_custom_btn_with_image_dialog, null, false);
        TextView titleTv = (TextView)layout.findViewById(R.id.dialog_title);
        ImageView titleDiv = (ImageView)layout.findViewById(R.id.dialog_divider1);
        TextView leftBtn = (TextView)layout.findViewById(R.id.dialog_btn_left);
        TextView rightBtn = (TextView)layout.findViewById(R.id.dialog_btn_right);
        TextView contentTv = (TextView)layout.findViewById(R.id.dialog_content);
        ImageView btnDiv = (ImageView)layout.findViewById(R.id.dialog_divider3);
        ImageView imageIv = (ImageView)layout.findViewById(R.id.dialog_image);
        
        if(TextUtils.isEmpty(title)){
            titleTv.setVisibility(View.GONE);
            titleDiv.setVisibility(View.GONE);
        } else {
            titleTv.setText(title);
        }
        contentTv.setText(content);
        
        imageIv.setBackgroundResource(imageRes);
        
        leftBtn.setText(left);
        leftBtn.setTag(Integer.valueOf(DialogInterface.BUTTON_NEGATIVE));
        leftBtn.setOnClickListener(listener);
        
        rightBtn.setText(right);
        rightBtn.setTag(Integer.valueOf(DialogInterface.BUTTON_POSITIVE));
        rightBtn.setOnClickListener(listener);
        
        if(TextUtils.isEmpty(left)){
            leftBtn.setVisibility(View.GONE);
            btnDiv.setVisibility(View.GONE);
        }
        
        if(TextUtils.isEmpty(right)){
            rightBtn.setVisibility(View.GONE);
            btnDiv.setVisibility(View.GONE);
        }

        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        
        return dialog;
    }
    
    public static AlertDialog createEgmNoticeDialog(Context context, String content, View.OnClickListener listener) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	
    	LayoutInflater inflater = LayoutInflater.from(context);
    	View layout = inflater.inflate(R.layout.view_custom_notice_dialog, null, false);
    	TextView contentTv = (TextView)layout.findViewById(R.id.dialog_content);
    	TextView btnTv = (TextView) layout.findViewById(R.id.dialog_btn);
    	
    	contentTv.setText(content);
    	btnTv.setOnClickListener(listener);
    	
    	builder.setView(layout);
    	
    	final AlertDialog dialog = builder.create();
    	return dialog;
    }
    
    /**
     * 创建自定义的Menu对话框，分隔线是灰色的，menu文字是紫色的，menu文字是居中的，可以有title和content,也可以没有。
     * listener中通过view的getTag，转为Integer，即为item，从0开始。
     * @param context
     * @param title
     * @param content
     * @param items
     * @param listener
     * @param autoDismiss 自动关闭dialog
     * @return
     */
    public static AlertDialog createEgmContentMenuDialog(Context context, String title,String content, 
    		CharSequence[] items, View.OnClickListener listener, boolean autoDismiss){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.view_custom_content_menu_dialog, null, false);
        TextView titleTv = (TextView)layout.findViewById(R.id.dialog_title);
        ImageView titleDiv = (ImageView)layout.findViewById(R.id.dialog_title_devider);
        TextView contentTv = (TextView)layout.findViewById(R.id.dialog_content);
        ImageView contentDiv = (ImageView)layout.findViewById(R.id.dialog_content_devider);
        LinearLayout list = (LinearLayout)layout.findViewById(R.id.dialog_list);
        
        if(TextUtils.isEmpty(title)){
        	titleTv.setVisibility(View.GONE);
        	titleDiv.setVisibility(View.GONE);
        } else {
        	titleTv.setText(title);
        }
        
        if(TextUtils.isEmpty(content)){
        	contentTv.setVisibility(View.GONE);
        	contentDiv.setVisibility(View.GONE);
        } else {
        	contentTv.setText(content);
        }
       
        int height = (int)context.getResources().getDimension(R.dimen.dialog_item_height);
        int dividreHeight = (int)context.getResources().getDimension(R.dimen.divider_height_1);
        
        AutoDismissListener adListener = new AutoDismissListener(listener);
        if (autoDismiss) {
        	listener = adListener;
        }
        
        for(int i = 0; i < items.length; i++){
            TextView itemTv = (TextView)inflater.inflate(R.layout.view_egm_dialog_item, null, false);
            itemTv.setGravity(Gravity.CENTER);
            itemTv.setText(items[i]);
            itemTv.setTag(Integer.valueOf(i));
            itemTv.setOnClickListener(listener);
            
            list.addView(itemTv, LayoutParams.MATCH_PARENT, height);
            if(i != items.length - 1){  // 最后一个没有分隔线
                ImageView divider = new ImageView(context);
                divider.setBackgroundColor(context.getResources().getColor(R.color.grey));
                list.addView(divider, LayoutParams.MATCH_PARENT, dividreHeight);
            }
        }
        
        
        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        
        adListener.setDialog(dialog);
        
        return dialog;
    }
    
    /** 是否安装了app */
    public static boolean isAppInstall(Context context, String packageName){
        PackageInfo packageInfo = null;

        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } 
        catch (NameNotFoundException e) {}
        
        return packageInfo != null;
    }
    
    /** 通过包名启动app */
    public static boolean startAppByPackageName(Context context, String name){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(name); 
        
        if(intent != null){
            context.startActivity(intent); 
            return true;
        }
        else{
            return false;
        }
    }
    
    /** 截取一定的文字长度，剩下的用省略号表示 */
    public static String trimWithEllipsis(String s, int length){
        if(s == null || s.length() <= length){
            return s;
        }
        else{
            return s.substring(0, length) + "...";
        }
    }
    
    /** 处理版本升级，以*.apk文件包URL，按正常的下载安装处理，除此以外的其他URL，都跳浏览器打开 */
    public static void startUpdateApp(Context context, String url,boolean cancelAble){
        if(context == null || TextUtils.isEmpty(url))
            return;
        if(url.toLowerCase().endsWith(".apk")){
            ActivityDownload.lunch(context, url,cancelAble);
        } else {
            try {
                Uri uri = Uri.parse(url);
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(it);
            } catch (Exception e) {
                e.printStackTrace();                    
            }
        }
    }
    /** 显示版本升级的对话框 */
    public static void showUpdateDialog(final Context context, final VersionInfo version){
    	 String left = context.getResources().getString(R.string.setting_update_later);
     if (version.forceUpdate) {
     	left = "";
     }
    	AlertDialog mDialog = EgmUtil.createEgmBtnDialog(context, 
    				 context.getResources().getString(R.string.setting_new_version), 
    				 version.description,
                  left,
                  context.getResources().getString(R.string.setting_update_now), new View.OnClickListener() {

                      @Override
                      public void onClick(View view) {
                          int which = (Integer)view.getTag();
                          switch (which) {
                              case DialogInterface.BUTTON_POSITIVE:
                                  EgmUtil.startUpdateApp(context, version.downUrl,!version.forceUpdate);
                                  break;
                              default:
                                  break;
                          }
                      }
                  },true);
          if (version.forceUpdate) {
          	mDialog.setCancelable(false);
          }
          mDialog.show();
    }
    
    public static void changeAudioStreamType(Context context){
    		if(EgmPrefHelper.getReceiverModeOn(context)){
			EgmPrefHelper.putReceiverModeOn(context, false);
		} else{
			EgmPrefHelper.putReceiverModeOn(context, true);
		}
    		LoopBack lp = new LoopBack();
		lp.mType = EgmConstants.LOOPBACK_TYPE.change_audiostrem;
		EgmService.getInstance().doLoopBack(lp);
    }
    
public static class AutoDismissListener implements View.OnClickListener {
    	
    	private Dialog mDialog;
    	private View.OnClickListener mListener;
    	
    	public AutoDismissListener(View.OnClickListener listener) {
    		mListener = listener;
    	}
    	
    	public void setDialog(Dialog dialog) {
    		mDialog = dialog;
    	}

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				mListener.onClick(v);
			}
			
			if (mDialog != null) {
				mDialog.dismiss();
			}
		}
    	
    }
}
