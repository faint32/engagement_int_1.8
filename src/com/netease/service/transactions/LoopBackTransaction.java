package com.netease.service.transactions;


import com.netease.common.task.Transaction;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.LoopBack;


public class LoopBackTransaction extends Transaction {
    private LoopBack mObj = null;

    public LoopBackTransaction(LoopBack obj) {
        super(EgmBaseTransaction.TRANSACTION_UI_BROADCAST);
        this.mObj = obj;
    }

    @Override
    public void onTransact() {
        final LoopBack obj = mObj;
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, obj);
        mObj = null;
    }
}
