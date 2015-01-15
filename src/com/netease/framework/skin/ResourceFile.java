package com.netease.framework.skin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.netease.common.log.NTLog;
import com.netease.service.Utils.StackTraceUtil;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.text.TextUtils;

public class ResourceFile {
	public static final String TAG = ResourceFile.class.getSimpleName();
			
	public static final int BUFF_LEN = 8 * 1024;
	
	
	/**
	 * 从Asset读取文件到指定路径
	 * @param context
	 * @param assetPath
	 * @param destPath
	 * @return
	 */
	public static boolean copyAssetsToDest(Context context, String assetPath, String destPath) {
		boolean bRes = false;
		
		long start = System.currentTimeMillis();
		
		if (null == context 
				|| TextUtils.isEmpty(assetPath)
				|| TextUtils.isEmpty(destPath)) {
			return bRes;
		}
		
		/**
		 * 如果目标文件存在，判断文件大小是否相等，相等就不再拷贝，不相等删除拷贝。重建失败退出，成功进行文件读取。
		 */
		File file = new File(destPath);
		if (file.exists()) {
			AssetFileDescriptor fd = null;
			try {
				long len1 = file.length();
				long len2 = 0;
				fd = context.getAssets().openFd(assetPath);
				len2 = fd.getLength();
				if (len1 == len2) {
					return true;
				} else {
					file.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (null != fd) {
						fd.close();
					}
					fd = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			bRes = file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (!bRes) {
				return bRes;
			}
		}
		
		bRes = false;
		InputStream inputStream = null;
	    OutputStream outputStream = null;
	    
	    try {
			inputStream = context.getAssets().open(assetPath);
			outputStream = new FileOutputStream(file);
			
			int nLen = 0;
			byte[] buff = new byte[BUFF_LEN];
			while ((nLen = inputStream.read(buff)) > 0) {
				outputStream.write(buff, 0, nLen);
			}
			buff = null;
			bRes = true;
		} catch (IOException e) {
			e.printStackTrace();
			if (null != file) {
				file.delete();
			}
		} finally {
			try {
				if (null != inputStream) {
					inputStream.close();
				}
				inputStream = null;

				if (null != outputStream) {
					outputStream.close();
				}
				outputStream = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    
	    if (SkinConfig.DEBUG_TIME) {
	    	NTLog.d(TAG, StackTraceUtil.getMethodName() + " spend time=" + (System.currentTimeMillis() - start));
	    }
		
		return bRes;
	}

}
