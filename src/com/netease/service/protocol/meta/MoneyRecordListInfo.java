package com.netease.service.protocol.meta;

import java.util.ArrayList;


public class MoneyRecordListInfo {
    /** 每页数量 */
    public int count;
    /** 总数量 */
    public int totalCount;
    public ArrayList<MoneyRecordInfo> records;
    
    public class MoneyRecordInfo{
        /** 订单ID */
        public long recordId; 
        /** 订单日期 */
        public long date;
        /** 金额 */
        public float amount;
        /** 描述 */
        public String message;
        /** 记录类型 参加EgmConstants.MoneyRecordType */
        public int recordType;
    }
}
