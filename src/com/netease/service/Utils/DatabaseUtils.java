package com.netease.service.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.netease.engagement.app.EgmConstants;

/**
 * 数据库工具类。
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class DatabaseUtils {
    private static final int FILE_BLOCK_SIZ = 8192;
  
    /** 获取指定数据库文件的文件地址 */
    public static String getDatabasePath(Context context, String dbName){
        return "/data/data/" + context.getPackageName() + "/" + EgmConstants.LOCAL_DATABASE_FOLDER + "/" + dbName;
    }
  
    /** 
     * 根据数据库名字获取放在文件系统上的数据库。如果该数据库还未拷贝到文件系统，那么先拷贝后读取。 
     * @param dbName 数据库名称
     * @param rawId res/raw下的数据库文件的res id
     * @return 数据库获取成功，则返回其实例；否则为null.
     */  
    public static SQLiteDatabase getSQLiteDatabase(Context context, String dbName, int rawId) {  
        SQLiteDatabase db = null;  
        String dbFilename = getDatabasePath(context, dbName);
        
        // 数据库文件如果还没有拷贝到文件系统中，那么需要执行拷贝操作
        File file = new File(dbFilename);
        if(!file.exists()){
            try {
                copyDataBase(context, rawId, dbName);
            } 
            catch (IOException e) {
                return null;
            }
        }
        
        // 数据库已经在文件系统
        try {  
            db = SQLiteDatabase.openDatabase(dbFilename, null, SQLiteDatabase.OPEN_READONLY);  
        } 
        catch (SQLiteException e) {  
            return null;
        }  
        
        return db;  
    }  
    
    /** 
     * 根据数据库名字获取放在文件系统上的数据库。如果该数据库还未拷贝到文件系统，那么返回null。 
     * @param dbName 数据库名称
     * @return 如果该数据库还未拷贝到文件系统或者数据库获取失败，那么返回null；否则返回其实例.
     */  
    public static SQLiteDatabase getSQLiteDatabase(Context context, String dbName) {  
        SQLiteDatabase db = null;  
        String dbFilename = getDatabasePath(context, dbName);
        
        // 数据库已经在文件系统
        try {  
            db = SQLiteDatabase.openDatabase(dbFilename, null, SQLiteDatabase.OPEN_READONLY);  
        } 
        catch (SQLiteException e) {  
            return null;
        }  
        
        return db;  
    }  
  
    /** 
     * 复制res/raw里的数据库到文件系统中 
     * @param rawId raw中的数据库的res id
     * @param dbName 数据库在文件系统里存放的文件名称 
     */  
    public static void copyDataBase(Context context, int rawId, String dbName) throws IOException {  
        String dbFolderPath = "/data/data/" + context.getPackageName() + "/" + EgmConstants.LOCAL_DATABASE_FOLDER;
        String dbFilename = getDatabasePath(context, dbName);
        
        // 判断文件夹是否存在，不存在就新建一个
        File folderDir = new File(dbFolderPath);  
        if (!folderDir.exists()){  
            folderDir.mkdir();  
        }
        
        FileOutputStream os = new FileOutputStream(dbFilename); 
        InputStream is = context.getResources().openRawResource(rawId); 
        byte[] buffer = new byte[FILE_BLOCK_SIZ];  
        int count = 0;  
        while ((count = is.read(buffer)) > 0) {  
            os.write(buffer, 0, count);  
            os.flush();  
        } 
        
        is.close();  
        os.close();  
    }  
}
