
package com.netease.service.db;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.netease.common.db.provider.BaseDBOpenHelper;
import com.netease.common.log.NTLog;
import com.netease.service.db.EgmDBTables.AccountTable;
import com.netease.util.KeyValuePair;


/**
 * 数据库表的创建及升级等相关管理
 * @author echo_chen
 * @since  2014-03-18
 */

public class EgmDBOpenHelper extends BaseDBOpenHelper {

    public static final String TAG = "EgmDBOpenHelper";

    public static String DataBaseName = "egm.db";
    private static final int DB_VERSION = 1;

    private static EgmDBOpenHelper instance = null;

    static public EgmDBOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new EgmDBOpenHelper(context);
        }

        return instance;
    }

    public EgmDBOpenHelper(Context context) {
        super(context, DataBaseName, null, DB_VERSION);
    }

    @Override
    protected void onDestroyOldDB(SQLiteDatabase db) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        NTLog.i(TAG, "onCreate");
        onCreateAccountTable(db);
//        onCreateMsgCenterTable(db);
//        onCreatePrivateMsgTable(db);
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

    /** 创建帐号数据库表 */
    private void onCreateAccountTable(SQLiteDatabase db) {
        List<KeyValuePair> columns = null;
        columns = new LinkedList<KeyValuePair>();
        columns.add(new KeyValuePair(BaseColumns._ID, INTEGER_KEY_AUTO_INC));
        columns.add(new KeyValuePair(AccountTable.C_USER_ID, TEXT_NOT_NULL));
        columns.add(new KeyValuePair(AccountTable.C_USER_ACCOUNT, TEXT_NOT_NULL));
        columns.add(new KeyValuePair(AccountTable.C_USER_MAIL_ACCOUNT, TEXT));
        columns.add(new KeyValuePair(AccountTable.C_USER_PASSWORD, TEXT));
        columns.add(new KeyValuePair(AccountTable.C_USER_TOKEN, TEXT));
        columns.add(new KeyValuePair(AccountTable.C_USER_SEX, INT));
        columns.add(new KeyValuePair(AccountTable.C_USER_NICKNAME, TEXT));
        columns.add(new KeyValuePair(AccountTable.C_USER_AVATAR, TEXT));
        columns.add(new KeyValuePair(AccountTable.C_USER_HAS_AVATAR, INT));
        columns.add(new KeyValuePair(AccountTable.C_USER_TYPE, INT));
        columns.add(new KeyValuePair(AccountTable.C_USER_IS_VIP, INT));
        columns.add(new KeyValuePair(AccountTable.C_USER_PROVINCE_ID, INT));
        columns.add(new KeyValuePair(AccountTable.C_USER_CITY_ID, INT));
        columns.add(new KeyValuePair(AccountTable.C_USER_DISTRICT_ID, INT));
        columns.add(new KeyValuePair(AccountTable.C_USER_PUBLIC_PIC_COUNT, INT));
        columns.add(new KeyValuePair(AccountTable.C_USER_PRIVATE_PIC_COUNT, INT));
        columns.add(new KeyValuePair(AccountTable.C_LAST_LOGIN, INT));
        columns.add(new KeyValuePair(AccountTable.C_UPDATE_TIME, LONG));

        columns.add(new KeyValuePair(AccountTable.C_RESERVED1, TEXT));
        columns.add(new KeyValuePair(AccountTable.C_RESERVED2, TEXT));
        columns.add(new KeyValuePair(AccountTable.C_RESERVED3, INT));
        columns.add(new KeyValuePair(AccountTable.C_RESERVED4, LONG));
        
        createTable(db, AccountTable.TABLE_NAME, columns, "UNIQUE (" + AccountTable.C_USER_ID + ") ON CONFLICT REPLACE");
    }

    
}
