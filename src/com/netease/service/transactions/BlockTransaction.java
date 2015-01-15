package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.EgmProtocolConstants.Block_Type;


/**
 * 加黑
 */
public class BlockTransaction extends EgmBaseTransaction{

	public int type ;
	public long bid ;
	
	public BlockTransaction(int type,long bid) {
		super(TRANSACTION_BLOCK);
		this.type = type ;
		this.bid = bid ;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createBlock(type,bid);
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, bid);
        if(type == Block_Type.BLOCK_CONFIRM){
            LastMsgDBManager.delMsgByUid(bid);
            MsgDBManager.delMsgByUid(bid);
        }
    }

}
