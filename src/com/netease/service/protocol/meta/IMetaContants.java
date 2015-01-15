package com.netease.service.protocol.meta;

import android.provider.BaseColumns;

import com.netease.service.db.MsgDBTables.MsgTable;

public class IMetaContants {

	public static interface IDBBase {
		public static final int _ID = 0;
	}
	
	public static interface IMessageInfo extends IDBBase {
		public static final int T_C_DEFAULT = 0;
		public static final int T_C_SIMPLE = 1;
		
		public static final String[] Projection = { BaseColumns._ID,
				MsgTable.C_MSGID, 
				MsgTable.C_ANOTHERID, 
				MsgTable.C_FROMID,
				MsgTable.C_TYPE, 
				MsgTable.C_CONTENT, 
				MsgTable.C_STATUS,
				MsgTable.C_TIME, 
				MsgTable.C_MEDIA_URL, 
				MsgTable.C_DURATION,
				MsgTable.C_EXTRA_ID, 
				MsgTable.C_EXTRA, 
				MsgTable.C_USERCP,
				MsgTable.C_ATTACH, };
		
		public static final int C_MSGID = 1;
		public static final int C_ANOTHERID = 2;
		public static final int C_FROMID = 3;
		public static final int C_TYPE = 4;
		public static final int C_CONTENT = 5;
		public static final int C_STATUS = 6;
		public static final int C_TIME = 7;
		public static final int C_MEDIA_URL = 8;
		public static final int C_DURATION = 9;
		public static final int C_EXTRA_ID = 10;
		public static final int C_EXTRA = 11;
		public static final int C_USERCP = 12;
		public static final int C_ATTACH = 13;
		
		public static final String[] SimpleProject = {
			MsgTable.C_MSGID, 
			MsgTable.C_ANOTHERID,
			MsgTable.C_TIME,
			MsgTable.C_STATUS,
			MsgTable.C_TYPE,
			MsgTable.C_CONTENT,
			MsgTable.C_RESERVED3,
		};
		
		public static final int S_C_MSGID = 0;
		public static final int S_C_ANOTHERID = 1;
		public static final int S_C_TIME = 2;
		public static final int S_C_STATUS = 3;
		public static final int S_C_TYPE = 4;
		public static final int S_C_CONTENT = 5;
		public static final int S_C_RESERVED3 = 6;
		
		public static final int RESERVED3_ISCAMERA_PHOTO = 0x01; // isCameraPhoto << 8
		public static final int RESERVED3_ISFIREMSG_OPENED = 0x02; // isFireMsgOpened << 8
	}
	
}
