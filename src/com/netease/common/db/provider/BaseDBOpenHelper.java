package com.netease.common.db.provider;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.netease.common.debug.CheckAssert;
import com.netease.util.KeyValuePair;

/**
 * 
 * 为了减少sdk对数据库升级降级的影响已经约定创建表等字符串常量
 * 
 * @author dingding
 *
 */
public abstract class BaseDBOpenHelper extends SQLiteOpenHelper {
	// drop table String format
	private static final String DROP_STAT = "DROP %s IF EXISTS %s";
	
	// create table 
	private static final String CREATE_TABLE = "CREATE TABLE";
	// alter table
	private static final String ALTER_TABLE = "ALTER TABLE";
	// create index
	private static final String CREATE_INDEX = "CREATE INDEX";
	// create trigger
	private static final String CREATE_TRIGGER = "CREATE TRIGGER";
	
	// update table content
	private static final String UPDATE = "UPDATE";
	// delete table content
	private static final String DELETE_TABLE_CONTENT = "DELETE FROM";
	
	// 一般情况下使用unique conflict replace
	protected static final String UNIQUE_REPLACE_FORMAT = "UNIQUE (%s) CONFLICT REPLACE";
	// unique conflict ignore
	protected static final String UNIQUE_INGNORE_FORMAT = "UNIQUE (%s) CONFLICT IGNORE";
	// 其他还有abort fail和rollback
	
	// 常用整型自增主键key
	protected static final String INT_PRIMARY_KEY_AUTOINCREMENT = 
		"INTEGER PRIMARY KEY AUTOINCREMENT";
	
	// if not exist
	protected static final String IF_NOT_EXIST = "IF NOT EXIST";
	
	// int key auto inc
	protected static final String INTEGER_KEY_AUTO_INC = "INTEGER PRIMARY KEY AUTOINCREMENT";
	// text
	protected static final String TEXT = "TEXT";
	// text not null
	protected static final String TEXT_NOT_NULL = "TEXT NOT NULL";
	// integer default 0
	protected static final String INT = "INTEGER";
	// integer default 1
	protected static final String INT_DEFAULT_1 = "INTEGER DEFAULT 1";
	// long default 0
	protected static final String LONG = "INTEGER";
	// 
	protected static final String WHERE = "WHERE";
	
	
	public BaseDBOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	/**
	 * 3.0以后的数据库开始区分数据库升级和降级，默认
	 * 
	 * since sdk 3.0 api level  
	 * 
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 */
	@SuppressLint("Override") 
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 先删除老的数据库表
		onDestroyOldDB(db);
		// 然后重新创建数据库表
		onCreate(db);
	}
	
	/**
	 * 删除所有原有old的数据库表，可能在数据库版本回退的时候，未能完全删除，因此通过
	 * 更新方式添加的数据库表必须通过updateCreateTable方式进行添加
	 * 
	 * @param db
	 */
	protected void onDestroyOldDB(SQLiteDatabase db) {
        String[] columns = {
        		"type", "name"
        };
        
        Cursor cursor = db.query("sqlite_master", columns, null, null, null, null, null);
        if (cursor == null) {
            return;
        }
        try {
            while (cursor.moveToNext()) {
                final String name = cursor.getString(1);
                if (!name.startsWith("sqlite_")) {
                    // If it's not a SQL-controlled entity, drop it
                    final String sql = "DROP " + cursor.getString(0) + " IF EXISTS " + name;
                    try {
                        db.execSQL(sql);
                    } catch (SQLException e) {
                    }
                }
            }
        } finally {
            cursor.close();
        }
	}
	
	/**
	 * 删除单张表
	 * 
	 * @param db
	 * @param tableName
	 */
	protected static final void destroyTable(SQLiteDatabase db, String tableName) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert tableName == null;
		CheckAssert.checkNull(tableName);
		
		db.execSQL(String.format(DROP_STAT, "TABLE", tableName));
	}
	
	/**
	 * 删除多张表
	 * 
	 * @param db
	 * @param tableName
	 */
	protected static final void destroyTables(SQLiteDatabase db, String[] tableNames) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert tableName == null;
		CheckAssert.checkNull(tableNames);
		
		for (String tableName : tableNames) {
			CheckAssert.checkNull(tableName);
			
			db.execSQL(String.format(DROP_STAT, "TABLE", tableName));
		}
	}
	
	/**
	 * 删除索引
	 * 
	 * @param db
	 * @param indexName
	 */
	protected static final void destroyIndex(SQLiteDatabase db, String indexName) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert indexName == null;
		CheckAssert.checkNull(indexName);
		
		db.execSQL(String.format(DROP_STAT, "INDEX", indexName));
	}
	
	/**
	 * 删除触发器
	 * 
	 * @param db
	 * @param triggerName
	 */
	protected static final void destroyTrigger(SQLiteDatabase db, String triggerName) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert triggerName == null;
		CheckAssert.checkNull(triggerName);
		
		db.execSQL(String.format(DROP_STAT, "TRIGGER", triggerName));
	}
	
	/**
	 * onCreate中创建数据库表
	 * 
	 * @param db
	 * @param tableName
	 * @param col
	 * @param desc
	 */
	protected static final void createTable(SQLiteDatabase db, String tableName, 
			List<KeyValuePair> columns, String desc) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert tableName == null;
		CheckAssert.checkNull(tableName);
//		assert columns == null;
		CheckAssert.checkNull(columns);
		
		StringBuilder builder = new StringBuilder();
		builder.append(CREATE_TABLE).append(' ').append(tableName)
			.append(' ').append('(');
		
		for (KeyValuePair pair : columns) {
			builder.append(pair.getKey()).append(' ')
				.append(pair.getValue()).append(',');
		}
		
		if (! TextUtils.isEmpty(desc)) {
			// unique or index
			builder.append(desc);
		} else {
			// delete last char ','
			builder.deleteCharAt(builder.length() - 1);
		}
		
		builder.append(')');
		
		db.execSQL(builder.toString());
	}
	
	/**
	 * onUpgrade中创建数据库表，需要先执行删除后重新创建
	 * 
	 * @param db
	 * @param tableName
	 * @param col
	 * @param desc
	 */
	protected static final void updateCreateTable(SQLiteDatabase db, String tableName,
			List<KeyValuePair> columns, String desc) {
		// 执行删除原有的数据库表
		destroyTable(db, tableName);
		
		// 然后重新创建
		createTable(db, tableName, columns, desc);
	}
	
	/**
	 * 重命名数据库表
	 * 
	 * @param db
	 * @param newName
	 * @param oldName
	 */
	protected static final void alterTableName(SQLiteDatabase db, String newName, 
			String oldName) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert newName == null;
		CheckAssert.checkNull(newName);
//		assert oldName == null;
		CheckAssert.checkNull(oldName);
		
		StringBuilder builder = new StringBuilder();
		builder.append(ALTER_TABLE).append(' ').append(oldName)
			.append(' ').append("RENAME TO").append(' ').append(newName);
		// 执行 ALTER TABLE TableName RENAME TO newTableName
		db.execSQL(builder.toString());
	}
	
	/**
	 * 增加数据库表栏
	 * 
	 * @param db
	 * @param tableName
	 * @param column
	 * @param desc
	 */
	protected static final void addTableColumn(SQLiteDatabase db, String tableName, 
			String column, String desc) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert tableName == null;
		CheckAssert.checkNull(tableName);
//		assert column == null;
		CheckAssert.checkNull(column);
//		assert desc == null;
		CheckAssert.checkNull(desc);
		
		StringBuilder builder = new StringBuilder();
		builder.append(ALTER_TABLE).append(' ').append(tableName)
			.append(' ').append("ADD").append(' ').append(column)
			.append(' ').append(desc);
		// 执行 ALTER TABLE TableName ADD column desc
		db.execSQL(builder.toString());
	}
	
	/**
	 * 创建index索引
	 * 
	 * @param db
	 * @param indexName
	 * @param tableName
	 * @param indexColumns [index-column,]*index-column
	 */
	protected static final void createIndex(SQLiteDatabase db, String indexName,
			String tableName, String indexColumns) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert indexName == null;
		CheckAssert.checkNull(indexName);
//		assert tableName == null;
		CheckAssert.checkNull(tableName);
//		assert indexColumns == null;
		CheckAssert.checkNull(indexColumns);
		
		StringBuilder builder = new StringBuilder();
		builder.append(CREATE_INDEX).append(' ').append(IF_NOT_EXIST)
			.append(' ').append(indexName).append(' ').append("ON").append(' ')
			.append(tableName).append(' ').append('(').append(indexColumns).append(')');
		// 执行CREAT INDEX IF NOT EXIST IndexName ON TableName ( [index-column,]*index-column)
		db.execSQL(builder.toString());
	}
	
	protected static final int TRIGGER_TIME_BEFORE = 0x01;
	protected static final int TRIGGER_TIME_AFTER = 0x02;
	protected static final int TRIGGER_TIME_INSTEAD_OF = 0x03;
	
	protected static final String getTriggerTime(int triggerTime) {
//		assert triggerTime > TRIGGER_TIME_INSTEAD_OF;
		CheckAssert.checkValue(triggerTime, '>', TRIGGER_TIME_INSTEAD_OF);
//		assert triggerTime < 0;
		CheckAssert.checkValue(triggerTime, '<', 0);
		
		String value = "";
		switch (triggerTime) {
		case TRIGGER_TIME_BEFORE:
			value = "BEFORE";
			break;
		case TRIGGER_TIME_AFTER:
			value = "AFTER";
			break;
		case TRIGGER_TIME_INSTEAD_OF:
			value = "INSTEAD OF";
			break;
		}
		
		return value;
	}
	
	protected static final int TRIGGER_OP_DELETE = 0x01;
	protected static final int TRIGGER_OP_INSERT = 0x02;
	protected static final int TRIGGER_OP_UPDATE = 0x03;
	
	protected static final String getTriggerOp(int triggerOp) {
//		assert triggerOp > TRIGGER_OP_UPDATE;
		CheckAssert.checkValue(triggerOp, '>', TRIGGER_OP_UPDATE);
//		assert triggerOp < TRIGGER_OP_DELETE;
		CheckAssert.checkValue(triggerOp, '<', TRIGGER_OP_DELETE);
		
		String value = "";
		switch (triggerOp) {
		case TRIGGER_OP_DELETE:
			value = "DELETE";
			break;
		case TRIGGER_OP_INSERT:
			value = "INSERT";
			break;
		case TRIGGER_OP_UPDATE:
			value = "UPDATE";
			break;
		}
		
		return value;
	}
	
	/**
	 * 
	 * @param db
	 * @param triggerName
	 * @param triggerTime
	 * @param triggerOp
	 * @param columns triggerOp为TRIGGER_OP_UPDATE时有效
	 * @param tableName
	 * @param eachRow
	 * @param when
	 * @param statments
	 * 
	 * @see <a href='http://www.sqlite.org/lang_createtrigger.html'>http://www.sqlite.org/lang_createtrigger.html</a>
	 */
	protected static final void createTrigger(SQLiteDatabase db, String triggerName,
			int triggerTime, int triggerOp, List<String> columns, String tableName,
			boolean eachRow, String when, List<String> statments) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert triggerName == null;
		CheckAssert.checkNull(triggerName);
		
//		assert tableName == null;
		CheckAssert.checkNull(tableName);
//		assert indexColumns == null;
		CheckAssert.checkNull(statments);
		
		StringBuilder builder = new StringBuilder();
		builder.append(CREATE_TRIGGER).append(' ').append(IF_NOT_EXIST)
			.append(' ').append(triggerName).append(' ')
			.append(getTriggerTime(triggerTime)).append(' ')
			.append(getTriggerOp(triggerOp));
		if (triggerOp == TRIGGER_OP_UPDATE && columns != null
				&& columns.size() > 0) {
			builder.append(' ').append("OF").append(' ');
			for (String column : columns) {
				builder.append(column).append(',');
			}
			builder.deleteCharAt(builder.length() - 1);
		}
		builder.append(' ').append("ON").append(' ').append(tableName)
			.append(eachRow ? "FOR EACH ROW" : "");
		if (! TextUtils.isEmpty(when)) {
			builder.append(' ').append("WHEN").append(' ').append(when);
		}
		builder.append(' ').append("BEGIN").append(' ');
		for (String stat : statments) {
			builder.append(stat).append(';');
		}
		builder.append(' ').append("END");
		
		db.execSQL(builder.toString());
	}
	
	/**
	 * 更新数据库表内容
	 * 
	 * @param db
	 * @param tableName
	 * @param columns
	 * @param desc
	 */
	protected static final void updateTableContent(SQLiteDatabase db, String tableName,
			List<KeyValuePair> columns, String where) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert tableName == null;
		CheckAssert.checkNull(tableName);
//		assert columns == null;
		CheckAssert.checkNull(columns);	
		
		StringBuilder builder = new StringBuilder();
		builder.append(UPDATE).append(' ').append(tableName)
			.append(' ').append("SET").append(' ');
		
		for (KeyValuePair pair : columns) {
			builder.append(pair.getKey()).append(' ').append(pair.getValue());
		}
		
		db.execSQL(builder.toString());
	}
	
	/**
	 * 仅用于清除数据库表内容，删除数据库表请参考destroyTable
	 * 
	 * @param db
	 * @param tableName
	 * @param desc where
	 */
	protected static final void deleteTableContent(SQLiteDatabase db, String tableName, 
			String where) {
//		assert db == null;
		CheckAssert.checkNull(db);
//		assert tableName == null;
		CheckAssert.checkNull(tableName);
		
		StringBuilder builder = new StringBuilder();
		builder.append(DELETE_TABLE_CONTENT).append(' ').append(tableName);
		
		if (! TextUtils.isEmpty(where)) {
			builder.append(' ').append(WHERE).append(' ').append(where);
		}
		
		// 执行 DELETE FROM TableName WHERE where
		db.execSQL(builder.toString());
	}
	
	
	/**
	 * 将Cursor中index位置的内容放回ContentValues cv
	 * 
	 * @param cv
	 * @param cursor
	 * @param index
	 */
	protected static final void setGetCursorString(ContentValues cv, 
			Cursor cursor, int index) {
		String value = cursor.getString(index);
		if (value != null) {
			cv.put(cursor.getColumnName(index), value);
		}
	}
}
