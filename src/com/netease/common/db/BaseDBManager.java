package com.netease.common.db;

import java.util.Collection;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import com.netease.common.debug.CheckAssert;


public abstract class BaseDBManager {

	/**
	 * 关闭Cursor
	 * @param cursor
	 */
	public static void closeCursor(Cursor cursor) {
		if (cursor != null) {
			try {
				cursor.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 一般查询方式，对查询关键字进行AND连接查询
	 * 
	 * @param context
	 * @param uri
	 * @param projection
	 * @param where
	 * @param whereOp
	 * @param whereArgs
	 * @param sortOrder
	 * @return
	 */
	public static Cursor query(Context context, Uri uri, String[] projection,
			String[] where, String[] whereOp, String[] whereArgs, String sortOrder) {
		CheckAssert.checkNull(context);
		CheckAssert.checkNull(uri);
		
		Cursor cursor = null;
		
		if (where != null) {
			CheckAssert.checkNull(whereOp);
			CheckAssert.checkNull(whereArgs);
			
			CheckAssert.checkValue(where.length, "!=", whereOp.length);
			CheckAssert.checkValue(where.length, "!=", whereArgs.length);
			
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < where.length; i++) {
				if (i > 0) {
					builder.append(" AND ");
				}
				builder.append(where[i]).append(' ').append(whereOp[i])
					.append(' ').append('?');
			}
			
			cursor = context.getContentResolver().query(uri, projection, 
					builder.toString(), whereArgs, sortOrder);
		} else {
			CheckAssert.checkNotNull(whereArgs);
			
			cursor = context.getContentResolver().query(uri, projection, 
					null, null, sortOrder);
		}
		
		return cursor;
	}
	
	/**
	 * 批量插入更新
	 * 
	 * @param db
	 * @param sql
	 * @param data
	 * @return
	 * @throws SQLException
	 */
	public static void bulkExecute(SQLiteDatabase db, String sql, 
			Collection<Object[]> datas) {
		CheckAssert.checkNull(db);
		CheckAssert.checkNull(sql);
		CheckAssert.checkNull(datas);
		
		SQLiteStatement stat = db.compileStatement(sql);
		if (stat != null) {
			try {
				for (Object[] data : datas) {
					for (int i = data.length - 1; i > -1; i--) {
						if (data[i] == null) {
							bindString(stat, i + 1, null);
						} else {
							String name = data[i].getClass().getName().intern();
							if (name == "java.lang.String") {
								bindString(stat, i + 1, (String)data[i]);
							} else if (name == "java.lang.Byte") {
								stat.bindLong(i + 1, ((Byte)data[i]).longValue());
							} else if (name == "java.lang.Short") {
								stat.bindLong(i + 1, ((Short)data[i]).longValue());
							} else if (name == "java.lang.Integer") {
								stat.bindLong(i + 1, ((Integer)data[i]).longValue());
							} else if (name == "java.lang.Long") {
								stat.bindLong(i + 1, ((Long)data[i]).longValue());
							} else if (name == "java.lang.Float") {
								stat.bindDouble(i + 1, ((Float)data[i]).doubleValue());
							} else if (name == "java.lang.Double") {
								stat.bindDouble(i + 1, ((Double)data[i]).doubleValue());
							} else if (name == "[B") { // byte[]
								stat.bindBlob(i + 1, (byte[])data[i]);
							} else {
								bindString(stat, i + 1, data[i].toString());
							} 
						}
						stat.execute();
					}
				}
			} catch (SQLException e) {
				throw e;
			} finally {
				try {
					stat.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * SQLiteStatement bind String
	 * 
	 * @param stat
	 * @param index
	 * @param value
	 */
	public static void bindString(SQLiteStatement stat, int index, String value) {
		if (value == null) {
			stat.bindNull(index);
		}
		else {
			stat.bindString(index, value);
		}
	}
	
	
}
