package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.engagement.util.LevelChangeStatusBean;
import com.netease.engagement.util.LevelChangeStatusBean.LevelChangeType;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.service.protocol.meta.SendGiftResult;


public class SendGiftTransaction extends EgmBaseTransaction{

	private String toUserId ;
	private String giftId ;
	private String picId ;
	private int type ;
	
	public SendGiftTransaction(String toUserId ,String giftId ,String picId ,int type) {
		super(TARNSACTION_SEND_GIFT);
		this.toUserId = toUserId ;
		this.giftId = giftId ;
		this.picId = picId ;
		this.type = type ;
	}
	
	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createSendGift(toUserId ,giftId ,picId ,type);
	    sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		SendGiftResult result = null ;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            result = gson.fromJson(json,SendGiftResult.class);
        }
        if (result != null) {
        	
        	long uid = ManagerAccount.getInstance().getCurrentId();
        	int oldLevel = EgmPrefHelper.getUserLevel(BaseApplication.getAppInstance().getApplicationContext(), uid);
        	int newLevel = result.userLevel;
        	if(oldLevel!=0 && oldLevel<newLevel) { // 判断男性是否升级
        		LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
        		status.set(uid, LevelChangeType.Male_Level_Up, oldLevel, newLevel);
        	}
        	EgmPrefHelper.putUserLevel(BaseApplication.getAppInstance().getApplicationContext(), uid, newLevel);
        	
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, result);
            ChatItemInfo itemInfo = result.chatItemInfo;
            LastMsgDBManager.handelNewMsg(itemInfo);
            if(itemInfo != null){
                MessageInfo msgInfo = itemInfo.message;
                MsgDBManager.insertMsg(msgInfo);
            }
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
