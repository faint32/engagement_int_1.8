package com.netease.service.db;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.netease.common.db.provider.BaseDBOpenHelper;
import com.netease.common.log.NTLog;
import com.netease.service.db.MsgDBTables.LastMsgTable;
import com.netease.service.db.MsgDBTables.MsgTable;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.util.KeyValuePair;


/**
 * 消息数据库表的创建及升级等相关管理
 * @author echo_chen
 * @since  2014-05-20
 */

public class MsgDBOpenHelper extends BaseDBOpenHelper {

    public static final String TAG = "MsgDBOpenHelper";

    public static String DataBaseName = "msg.db";
    private static final int DB_VERSION = 1;

    private static MsgDBOpenHelper instance = null;
    
    private static String mUid = null;

    synchronized static public MsgDBOpenHelper getInstance(Context context) {
        if (mUid != null && instance != null && ( !mUid.equals(ManagerAccount.getInstance().getCurrentAccountId()))) {
            instance.close();
            instance = null;
            NTLog.i(TAG,"Recreate ");
        }
        if (instance == null) {
            instance = new MsgDBOpenHelper(context);
            mUid = ManagerAccount.getInstance().getCurrentAccountId();
            NTLog.i(TAG,"new MsgDBOpenHelper  mUid is " + mUid);
        }

        return instance;
    }

    public MsgDBOpenHelper(Context context) {
        super(context, DataBaseName, null, DB_VERSION);
    }

    @Override
    protected void onDestroyOldDB(SQLiteDatabase db) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        NTLog.i(TAG, "onCreate");
        onCreateMsgTable(db);
        onCreateLastMsgTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == newVersion) {
            return;
        }
        switch (oldVersion) {
            case 1:
                break;
        }

    }
    private void onCreateMsgTable(SQLiteDatabase db) {
        List<KeyValuePair> columns = null;

        columns = new LinkedList<KeyValuePair>();
        columns.add(new KeyValuePair(BaseColumns._ID, INTEGER_KEY_AUTO_INC));
        columns.add(new KeyValuePair(MsgTable.C_MSGID, LONG));
        columns.add(new KeyValuePair(MsgTable.C_ANOTHERID, LONG));
        columns.add(new KeyValuePair(MsgTable.C_FROMID, LONG));
        columns.add(new KeyValuePair(MsgTable.C_TYPE, INT));
        columns.add(new KeyValuePair(MsgTable.C_CONTENT, TEXT));
        columns.add(new KeyValuePair(MsgTable.C_STATUS, INT));
        columns.add(new KeyValuePair(MsgTable.C_TIME, LONG));
        columns.add(new KeyValuePair(MsgTable.C_MEDIA_URL, TEXT));
        columns.add(new KeyValuePair(MsgTable.C_DURATION, INT));
        columns.add(new KeyValuePair(MsgTable.C_EXTRA_ID, LONG));
        columns.add(new KeyValuePair(MsgTable.C_EXTRA, TEXT));
        columns.add(new KeyValuePair(MsgTable.C_USERCP, INT));
        columns.add(new KeyValuePair(MsgTable.C_ATTACH, TEXT));

        columns.add(new KeyValuePair(MsgTable.C_RESERVED1, TEXT));
        columns.add(new KeyValuePair(MsgTable.C_RESERVED2, TEXT));
        columns.add(new KeyValuePair(MsgTable.C_RESERVED3, INT));
        columns.add(new KeyValuePair(MsgTable.C_RESERVED4, LONG));
        createTable(db, MsgTable.TABLE_NAME, columns, "UNIQUE (" + MsgTable.C_MSGID + ", " + MsgTable.C_FROMID +  ") ON CONFLICT REPLACE");  
    }
    
    private void onCreateLastMsgTable(SQLiteDatabase db) {
        List<KeyValuePair> columns = null;

        columns = new LinkedList<KeyValuePair>();
        columns.add(new KeyValuePair(BaseColumns._ID, INTEGER_KEY_AUTO_INC));
        columns.add(new KeyValuePair(LastMsgTable.C_ANOTHERID, LONG));
        columns.add(new KeyValuePair(LastMsgTable.C_ISVIP, INT));
        columns.add(new KeyValuePair(LastMsgTable.C_ISNEW, INT));
        columns.add(new KeyValuePair(LastMsgTable.C_NICK, TEXT));
        columns.add(new KeyValuePair(LastMsgTable.C_AVATAR, TEXT));
        columns.add(new KeyValuePair(LastMsgTable.C_CROWNID, INT));
        columns.add(new KeyValuePair(LastMsgTable.C_MSGID, LONG));
        columns.add(new KeyValuePair(LastMsgTable.C_FROMID, LONG));
        columns.add(new KeyValuePair(LastMsgTable.C_TYPE, INT));
        columns.add(new KeyValuePair(LastMsgTable.C_CONTENT, TEXT));
        columns.add(new KeyValuePair(LastMsgTable.C_STATUS, INT));
        columns.add(new KeyValuePair(LastMsgTable.C_TIME, LONG));
        columns.add(new KeyValuePair(LastMsgTable.C_UNREADNUM, INT));
        columns.add(new KeyValuePair(MsgTable.C_MEDIA_URL, TEXT));
        columns.add(new KeyValuePair(MsgTable.C_DURATION, INT));
        columns.add(new KeyValuePair(MsgTable.C_EXTRA_ID, LONG));
        columns.add(new KeyValuePair(MsgTable.C_EXTRA, TEXT));
        columns.add(new KeyValuePair(MsgTable.C_USERCP, INT));
        columns.add(new KeyValuePair(LastMsgTable.C_ATTACH, TEXT));  
        
        columns.add(new KeyValuePair(LastMsgTable.C_RICH, LONG));  
        columns.add(new KeyValuePair(LastMsgTable.C_INTIMACY, LONG));  

        columns.add(new KeyValuePair(LastMsgTable.C_RESERVED1, TEXT));
        columns.add(new KeyValuePair(LastMsgTable.C_RESERVED2, TEXT));
        columns.add(new KeyValuePair(LastMsgTable.C_RESERVED3, INT));
        columns.add(new KeyValuePair(LastMsgTable.C_RESERVED4, LONG));
        createTable(db, LastMsgTable.TABLE_NAME, columns, "UNIQUE (" + LastMsgTable.C_ANOTHERID + ") ON CONFLICT REPLACE");
    }
       
    public static File getDbFile(Context context, String uid, String name) {
        File dir;
        File f;
        String path = context.getApplicationInfo().dataDir + "/" + uid + "/";
        dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        f = new  File(path + name);
        return f;
    }
}
