package com.netease.engagement.pushMsg;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.netease.common.log.NTLog;
import com.netease.pushservice.core.ServiceManager;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmService;


public class MessageListener extends BroadcastReceiver {
    private static final String  TAG = "MessageListener";
    public static final int MSG_TYPE_BROADCAST = 1;//广播
    public static final int MSG_TYPE_PRIVATE_MSG = 2;//私信
    public static final int MSG_TYPE_ATTACHMENT = 3;//附件
    public static final int MSG_TYPE_MULTICAST = 4;//组播
    
	@Override
	public void onReceive(Context context, Intent intent) {
	    
	    String topic = intent.getStringExtra("topic");
	    String message = intent.getStringExtra("message");
	  
	    NTLog.i(TAG, "receive topic:" + topic + " receive message:" + message);

	    //对于私信需要产品ack到SDK
	    if (topic.endsWith("specify")) {
	        ServiceManager serviceManager = ServiceManager.getInstance();
	        serviceManager.init(context);
	        serviceManager.ackMessage(context, serviceManager.getProperty("NETEASE_DOMAIN"), message);
	    }
	    
        if(null == ManagerAccount.getInstance().getCurrentAccount()) { // 当前登录帐号为空，代表没有登录，不对消息进行提醒
            return;
        }
        
        EgmService.getInstance().doHandlePushMsg(message);

	}
}
