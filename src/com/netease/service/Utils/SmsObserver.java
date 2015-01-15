package com.netease.service.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

public class SmsObserver extends ContentObserver {
	 public static Uri SMS_INBOX = Uri.parse("content://sms/"); 
	 public static final int MSG_VERIFYCODE = 1;
	 public static String address = "10690163";//106571203801630，10690163
	 private Context mContext;
	 private Handler mHandler;
	 public SmsObserver(Context context, Handler handler) {  
         super(handler);  
         mContext = context;
         mHandler = handler;
     }  

     @Override  
     public void onChange(boolean selfChange) {  
         super.onChange(selfChange);  
         getSmsFromPhone();  
     }  
     public void getSmsFromPhone() {  
         ContentResolver cr = mContext.getContentResolver();  
         String[] projection = new String[] { "body" };//"_id", "address", "person",, "date", "type  
//         String where = " address = '10690163' AND date >  "  
//                 + (System.currentTimeMillis() - 10 * 60 * 1000);  
//         StringBuilder selection = new StringBuilder();
//         selection.append("read").append("=?");
// 		selection.append("address").append("=?").append(" AND ").append("read").append("=?");
 		
// 		String[] args = new String[] { 
// 				"0"
// 		};
         Cursor cur = null;
 		try{
	         cur = cr.query(SMS_INBOX, projection, "read=0", null, "date desc");
	         if (null == cur)  
	             return;  
	//		int index = cur.getColumnIndex("body");
			if (cur.moveToFirst()) {
				do {
					String body = cur.getString(0);
					String code = getCode(body);
					if (!TextUtils.isEmpty(code)) {
						if (mHandler != null) {
							Message msg = new Message();
							msg.what = MSG_VERIFYCODE;
							msg.obj = code;
							mHandler.sendMessage(msg);
						}
						break;
					}
				} while (cur.moveToNext());
			}
 		} catch (Exception e){
 			
 		} finally{
 			if(cur != null && !cur.isClosed()){
 				cur.close();
 			}
 		}
         
     }  
 	private static String getCode(String msg) {
		String code = null;
		
		Pattern p = Pattern.compile("(([0-9]+).*女神来了)|(女神来了\\D*([0-9]+))");
		
		Matcher matcher = p.matcher(msg);
		
		if (matcher.find()) {
			code = matcher.group(2);
			if (code != null) {
				return code;
			}
			
			code = matcher.group(4);
			if (code != null) {
				return code;
			}
		}
		
		return code;
	}

}
