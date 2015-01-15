package com.netease.service.stat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;

import com.netease.common.cache.CacheManager;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.Entities.FilePart;
import com.netease.common.http.Entities.MultipartEntity;
import com.netease.common.http.Entities.Part;
import com.netease.common.service.BaseService;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.transactions.EgmBaseTransaction;


public class EgmStatTransaction extends EgmBaseTransaction {

	private static final String LOG_DIR = "/.log_dir";
	private static final String DEFAULT_FILE = "default.log";
	
	private boolean mSend;
	private long mUserId;
	private List<JSONObject> mLogs;
	private File mFile;
	
	public EgmStatTransaction(boolean send, long userid, List<JSONObject> logs) {
		super(0);
		
		mSend = send;
		mUserId = userid;
		mLogs = logs;
	}

	@Override
	public void onTransact() {
		String root = CacheManager.getRoot() + LOG_DIR;
		
		File file = new File(root, DEFAULT_FILE);
		
		if (mLogs != null && mLogs.size() > 0) {
			appendLogs(mUserId, file, mLogs);
			mLogs.clear();
		}
		
		if (mSend && file.exists() && file.length() > 0) {
			File tmp = new File(root, DEFAULT_FILE + "_tmp");
			file.renameTo(tmp);
			file = tmp;
			
			tmp = new File(root, System.currentTimeMillis() + ".log");
			
			byte[] data = BaseService.getByteBuf(1024);
			
			try {
				FileOutputStream fos = new FileOutputStream(tmp);
				ZipOutputStream zos = new ZipOutputStream(fos);
				zos.putNextEntry(new ZipEntry("log.txt"));
				
				FileInputStream fis = new FileInputStream(file);
				
				int len = 0;
				while ((len = fis.read(data)) != -1) {
					zos.write(data, 0, len);
				}
				
				zos.closeEntry();
				
				zos.close();
				fos.close();
				
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			BaseService.returnByteBuf(data);
			
			file.delete();
			
			mFile = tmp;
		}
		
		if (mSend && mFile == null) {
			mFile = getNextLogFile(root);
		}
		
		if (mSend && mFile != null && mFile.length() > 0) {
			sendLogFile(mFile);
			return ;
		}
		
		doEnd();
	}
	
	private static File getNextLogFile(String root) {
		File rootFile = new File(root);
		
		long time = System.currentTimeMillis() - 5 * 24 * 3600 * 1000; 
		
		String[] names = rootFile.list();
		
		if (names != null && names.length > 0) {
			int size = names.length;
			for (int i = 0; i < size; i++) {
				String name = names[i];
				
				File file = new File(root, name);
				
				if (name.endsWith(".log")) {
					if (file.lastModified() < time) {
						file.delete();
					}
					else if (! name.equals(DEFAULT_FILE)) {
						return file;
					}
				}
				else {
					file.delete();
				}
			}
		}
		
		return null;
	}

	private static void appendLogs(long userId, File file, List<JSONObject> logs) {
		if (logs == null || logs.size() == 0) {
			return ;
		}
		
		File parent = file.getParentFile();
		if (! parent.exists()) {
			parent.mkdirs();
		}
		
		StringBuffer buffer = new StringBuffer();
		
		synchronized (EgmStatTransaction.class) {
			try {
				FileOutputStream fos = new FileOutputStream(file, true);
				
				int size = logs.size();
				for (int i = 0; i < size; i++) {
					buffer.setLength(0);
					
					JSONObject json = logs.get(i);
					
					buffer.append(json.optLong(EgmStatService.TIME))
						.append(EgmStatService.SPLIT);
					json.remove(EgmStatService.TIME);
					buffer.append(json.toString()).append("\r\n");
					
					byte[] data = buffer.toString().getBytes("utf-8");
					fos.write(data, 0, data.length);
				}
				
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendLogFile(File file) {
		if (file.length() == 0) {
			if (file.exists()) {
				file.delete();
			}
			
			doEnd();
			return ;
		}
		
		THttpRequest request = EgmProtocol.getInstance().createRecommendLogger();
		
		try {
			Part[] parts = new Part[1];
			parts[0] = new FilePart("logs", file);
			
			request.setHttpEntity(new MultipartEntity(parts));
		} catch (Exception e) {
		}
		
		sendRequest(request);
	}

	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		if (mFile != null) {
			mFile.delete();
		}
		
		mFile = getNextLogFile(CacheManager.getRoot() + LOG_DIR);
		
		if (mFile != null) {
			sendLogFile(mFile);
		}
	}
	
}
