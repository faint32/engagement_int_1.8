package com.netease.service.transactions;


import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.PortraitInfo;
import com.netease.service.protocol.meta.VersionInfo;

/**
 * 修改会员头像
 */
public class ModifyProfileTransaction extends EgmBaseTransaction {

	private static final boolean DEBUG = false ;
	
	String mFilePath;
	String x ;
	String y ;
	String w ;
	String h ;
	
	public ModifyProfileTransaction(String filePath,String x ,String y,String w ,String h) {
		super(TARNSACTION_TYPE_MODIFY_PROFILE);
		this.mFilePath = filePath ;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createModifyProfile(mFilePath,x,y,w,h);
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		PortraitInfo portrait = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            portrait = gson.fromJson(json, PortraitInfo.class);
        }
        if(portrait != null){
        	    String uid = ManagerAccount.getInstance().getCurrentAccountId();
        	    if(!TextUtils.isEmpty(uid)){
        	    		ManagerAccount.getInstance().updateAvatarByUserId(uid, portrait.portraitUrl192);
        	    }
        		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, portrait);
        }else {
            notifyDataParseError();
        }
    }
}
