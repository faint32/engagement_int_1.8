package com.netease.engagement.pushMsg;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.netease.common.log.NTLog;
import com.netease.pushservice.core.ServiceManager;
import com.netease.pushservice.event.Event;
import com.netease.pushservice.event.EventHandler;
import com.netease.pushservice.event.EventType;
import com.netease.pushservice.utils.LogUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.preferMgr.EgmPrefHelper;

/**
 * pushsdk的相关接口管理类
 * @author echo_chen
 * @since  2014-04-29
 */

public class MessagePushUtil {
    private static final String TAG = "MessagePushUtil";
    /**
     * 消息推送平台服务器地址
     */
    // online
    public static String host = "android.push.126.net";
    public static int port = 6002;
    // test
//      public static String host = "123.58.180.233";
//      public static int port = 6002;
	
	public static void init(Context context) {
		NTLog.i(TAG,"init");
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.init(host, port, context);
//		String domain = serviceManager.getProperty("NETEASE_DOMAIN");
//		NTLog.i(TAG,"init domain： " + domain);
//		setLogLevel(false);
	}
	
	
	public static void startService(Context context) {
	    NTLog.i(TAG,"startService");
		final ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.startService(context);
		
		serviceManager.addEventHandler(context, EventType.SERVICE_CONNECT,
				new EventHandler() {
					@Override
					public void processEvent(Event event) {
//						if(serviceManager!=null && context!=null) {
//							serviceManager.removeEventHandler(context);
//						}
					    NTLog.i(TAG,"startService: connect successfully");
					}
				});
		
		serviceManager.addEventHandler(context, EventType.SERVICE_CONNECT_FAILED,
				new EventHandler() {
					@Override
					public void processEvent(Event event) {
					    NTLog.e(TAG,"startService: connect failed!!!");
					}
				});
		
		serviceManager.addEventHandler(context, EventType.SERVICE_HEARTBEAT_FAILED,
				new EventHandler() {
					@Override
					public void processEvent(Event event) {
						NTLog.e(TAG,"startService: heart beat failed!!!");
					}
				});
		
		serviceManager.addEventHandler(context, EventType.SERVICE_DISCONNECT,
				new EventHandler() {
					@Override
					public void processEvent(Event event) {
						NTLog.e(TAG,"startService:disconnect from server!!!");
					}
				});
		
	}

	public static void restartService(Context context) {
        ServiceManager serviceManager = ServiceManager.getInstance();
        serviceManager.startService(context);
    }
	public static void removeEventHandler(Context context) {
		NTLog.i(TAG,"removeEventHandler");
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.removeEventHandler(context);
	}
	
	public static void register(final Context context) {
		NTLog.i(TAG,"register: 广播消息注册");
		
		ServiceManager serviceManager = ServiceManager.getInstance();
		
		if(serviceManager == null) {
			NTLog.e(TAG,"register: serviceManager is null");
			return;
		}
		
		String domain = serviceManager.getProperty("NETEASE_DOMAIN");
		String productKey = serviceManager.getProperty("NETEASE_PRODUCT_KEY");
		String productVersion = serviceManager.getProperty("NETEASE_PRODUCT_VERSION");
		
		if(context==null || TextUtils.isEmpty(domain) 
				|| TextUtils.isEmpty(productKey) || TextUtils.isEmpty(productVersion)) {
			NTLog.e(TAG,"register:输入参数为空");
			return;
		}
		serviceManager.register(context, domain, productKey, productVersion, null,
				new EventHandler() {
					@Override
					public void processEvent(Event event) {
						NTLog.i(TAG,"register: 广播消息注册返回");
						if (event != null) {
							if (event.isSuccess()) {
								NTLog.i(TAG,"register: 广播消息注册成功");
							} else {
								NTLog.e(TAG,"register:广播消息注册失败: " + event.getMsg());
								register(context);
							}
						}
						return;
					}
				});
	}
	
	public static boolean isBind = false;
	public static boolean isRegist = false;
	public static int reBindTimes = 0;
	
	public static void bindAccount(final Context context) {
		NTLog.i(TAG,"bindAccount: 用户绑定");
		
//		String name = ManagerAccount.getInstance().getCurrentAccountMail();
		String name = ManagerAccount.getInstance().getCurrentAccountId();
		String signature = EgmPrefHelper.getSignature(context);
		String nonce = EgmPrefHelper.getNonce(context);
		String expire_time = String.valueOf(EgmPrefHelper.getExpire(context));
		
		
//		if(!StringUtil.isEmpty(name) && name.contains("@163.com")) {
//			name = name.substring(0, name.indexOf("@163.com"));
//		}

		ServiceManager serviceManager = ServiceManager.getInstance();
		
		if(serviceManager == null) {
			NTLog.e(TAG,"bindAccount: serviceManager is null");
			return;
		}
		
		String domain = serviceManager.getProperty("NETEASE_DOMAIN");
		String productKey = serviceManager.getProperty("NETEASE_PRODUCT_KEY");
		String productVersion = serviceManager.getProperty("NETEASE_PRODUCT_VERSION");
		
		NTLog.i(TAG,"name "+ name + " domain " + domain + " productKey " + productKey + " productVersion "+ productVersion
		        + " signature " + signature + " nonce " + nonce + " expire_time " + expire_time);
		
		if(context==null || TextUtils.isEmpty(name)) {
			NTLog.e(TAG,"bindAccount: 输入参数1为空");
			return;
		}
		
		if(TextUtils.isEmpty(domain) || TextUtils.isEmpty(productKey) || TextUtils.isEmpty(productVersion) ) {
			NTLog.e(TAG,"bindAccount: 输入参数2为空");
			init(context);
//			register(context);
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                
                @Override
                public void run() {
                    bindAccount(context);
                }
            }, 1000);
			return;
		}
		
		if(TextUtils.isEmpty(signature) || TextUtils.isEmpty(nonce) || TextUtils.isEmpty(expire_time)) {
			NTLog.e(TAG,"bindAccount: 输入参数3为空");
			return;
		}
		
		isBind = false;
		
		serviceManager.bindAccount(context, name, domain, productKey,
				productVersion, signature, nonce, expire_time, false, null, 
				new EventHandler() {
					@Override
					public void processEvent(Event event) {
						NTLog.i(TAG,"bindAccount: 用户绑定返回");
						if (event != null) {
							if (event.isSuccess()) {
								NTLog.i(TAG,"bindAccount: 用户绑定成功");
								isBind = true;
								int sex = ManagerAccount.getInstance().getCurrentGender() + 1;
							    reportInfoMask(context,String.valueOf(sex));
							} else {
								NTLog.e(TAG,"bindAccount: 用户绑定失败:" + event.getMsg());
								if(reBindTimes < 2){//失败最多重绑两次
    								bindAccount(context);
    								reBindTimes++;
								}
							}
						}
						return;
					}
				});
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
            	if(!isBind && reBindTimes == 0)
            		NTLog.e(TAG,"bindAccount: timeout");
            	    startService(context);
            }
        }, 30000);//保护，处理绑定超时不返回的情况,30s没有返回重启service
		
		handler.postDelayed((new Runnable() {
			@Override
			public void run() {
				if(!isBind && reBindTimes == 0)
				 bindAccount(context);
				}
		}), 33000);//启动service后延三秒再绑定
	}
	
	public static void cancelBind(Context context) {
		NTLog.i(TAG,"cancelBind: 解除用户绑定");
		
		String name = ManagerAccount.getInstance().getCurrentAccountId();
		
		ServiceManager serviceManager = ServiceManager.getInstance();
		
		if(serviceManager == null) {
			NTLog.e(TAG,"cancelBind: serviceManager is null");
			return;
		}
		
		String domain = serviceManager.getProperty("NETEASE_DOMAIN");
		
		if(context == null || TextUtils.isEmpty(name) || TextUtils.isEmpty(domain)) {
			NTLog.e(TAG,"cancelBind: 输入参数为空");
			return;
		}
		
		serviceManager.cancelBind(context, domain, name,
				new EventHandler() {
					@Override
					public void processEvent(Event event) {
						NTLog.i(TAG,"cancelBind: 解除用户绑定返回");
						if (event != null) {
							if (event!=null && event.isSuccess()) {
								NTLog.i(TAG,"cancelBind: 解除用户绑定成功");
							} else {
								NTLog.e(TAG,"cancelBind: 解除用户绑定失败:" + event.getMsg());
							}
						}
						return;
					}
				});
	}
	
	public static void setLogLevel(boolean open){
        
        String name = ManagerAccount.getInstance().getCurrentAccountMail();
        
        ServiceManager serviceManager = ServiceManager.getInstance();
        
        if(serviceManager == null) {
            NTLog.e(TAG,"setLogLevel: serviceManager is null");
            return;
        }
        if(open){
            serviceManager.setLoggerLevel(LogUtil.ASSERT);
        } else {
            serviceManager.setLoggerLevel(LogUtil.WARN);
        }
	}
	
	public static void reportInfoMask(final Context context,String mask){
	    NTLog.i(TAG,"reportInfoMask mask is " + mask);
        
        
        ServiceManager serviceManager = ServiceManager.getInstance();
        
        if(serviceManager == null) {
            NTLog.e(TAG,"reportInfoMask: serviceManager is null");
            return;
        }
        
        String domain = serviceManager.getProperty("NETEASE_DOMAIN");
        
        if(context == null || TextUtils.isEmpty(mask) || TextUtils.isEmpty(domain)) {
            NTLog.e(TAG,"reportInfoMask: 输入参数为空");
            return;
        }
        Map<String, String> map = new  HashMap<String, String>();
        map.put("mask", mask);
        
        serviceManager.reportInfo(context, domain, map,
                new EventHandler() {
                    @Override
                    public void processEvent(Event event) {
                        NTLog.i(TAG,"reportInfoMask:返回");
                        if (event != null) {
                            if (event!=null && event.isSuccess()) {
                                NTLog.i(TAG,"reportInfoMask: 成功");
                            } else {
                                NTLog.e(TAG,"reportInfoMask: 失败:" + event.getMsg());
                            }
                        }
                        return;
                    }
                });
	}

}
