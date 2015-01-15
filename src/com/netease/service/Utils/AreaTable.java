package com.netease.service.Utils;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.netease.engagement.app.EgmConstants;
import com.netease.service.protocol.meta.OptionInfo;

/**
 * 区域（省-市-区/县）表管理类。
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class AreaTable {
    private static final String TABLE_NAME = "area";
    private static final String AREA_DB_NAME = "area.db";
    
    /** 
     * 通过省Id获取省名 
     * @return 获取成功则为省名称，否则为null
     */
    public static String getProvinceNameById(Context context, int provinceId) {
        String provindeName = "";
        
        String[] columns = new String[] { "name" };
        String whereClause = String.format("provinceid='%s' and cityid='%s' and districtid='%s'", provinceId, 0, 0);
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return null;
        
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                provindeName = cursor.getString(0);
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        
        return provindeName;
    }
    
    /** 
     * 通过省Id和市Id获取市名 
     * @return 获取成功则为市名称，否则为null
     */
    public static String getCityNameById(Context context, int provinceId, int cityId) {
        String cityName = "";
        
        String[] columns = new String[] { "name" };
        String whereClause = String.format("provinceid='%s' and cityid='%s' and districtid='%s'", provinceId, cityId, 0);
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return null;
        
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                cityName = cursor.getString(0);
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        
        return cityName;
    }

    /** 
     * 通过省Id市Id和县\区Id获取县\区名 
     * @return 获取成功则为县\区名称，否则为null
     */
    public static String getDistrictNameById(Context context, int provinceId, int cityId, int districtId) {
        String districtName = "";
        String[] columns = new String[] { "name" };
        String whereClause = String.format("provinceid='%s' and cityid='%s' and districtid='%s'", provinceId, cityId, districtId);
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return null;
        
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                districtName = cursor.getString(0);
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        
        return districtName;
    }
    
    /** 
     * 通过省名获取省Id 
     * @return 获取成功则为省Id，否则为0
     */
    public static int getProvinceIdByName(Context context, String provinceName) {
        int provindeId = 0;
        
        String[] columns = new String[] { "provinceid" };
        String whereClause = String.format("name like '%%%s%%'", provinceName);
        Cursor cursor = null;
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return 0;
        
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                provindeId = cursor.getInt(0);
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        
        return provindeId;
    }
    
    /** 
     * 通过省id和市名获取市Id 
     * @return 获取成功则为市Id，否则为0
     */
    public static int getCityIdByName(Context context, int provinceId, String cityName) {
        int cityId = 0;
        
        String[] columns = new String[] { "cityid" };
        String whereClause = String.format("provinceid=%s and name like '%%%s%%'", provinceId, cityName);
        Cursor cursor = null;
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return 0;
        
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                cityId = cursor.getInt(0);
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        
        return cityId;
    }
    
    /** 
     * 通过省市id和县\区名获取县\区Id 
     * @return 获取成功则为县\区Id，否则为0
     */
    public static int getDistrictIdByName(Context context, int provinceId, int cityId, String districtName) {
        int districtId = 0;
        String[] columns = new String[] { "districtid" };
        String whereClause = String.format("provinceid=%s and cityid=%s and name like '%%%s%%'", provinceId, cityId, districtName);
        Cursor cursor = null;
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return 0;
        
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                districtId = cursor.getInt(0);
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        
        return districtId;
    }
    
    /** 
     * 获取所有省名列表 
     * @return 获取成功则为所有省名列表，否则为null
     */
    public static ArrayList<String> getAllProvinces(Context context) {
        ArrayList<String> provinces = new ArrayList<String>();
        String[] columns = new String[] { "name" };
        String whereClause = String.format("cityid=%s and districtid=%s", 0, 0);
        String orderBy = "provinceid";
        Cursor cursor = null;
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return null;
        
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, orderBy);
            while (cursor != null && cursor.moveToNext()) {
                provinces.add(cursor.getString(0));
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        
        return provinces;
    }
    
    /** 
     * 获取所有省份key-value列表 
     * @return 获取成功则为所有省份key-value列表，否则为null
     */
    public static OptionInfo[] getAllProvinceOption(Context context) {
        OptionInfo[] provinces = null;
        
        String[] columns = new String[] { "name", "provinceid" };
        String whereClause = String.format("cityid=%s and districtid=%s", 0, 0);
        String orderBy = "provinceid";
        Cursor cursor = null;
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return null;
        
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, orderBy);
            if(cursor != null && cursor.getCount() > 0){
                provinces = new OptionInfo[cursor.getCount()];
                int i = 0;
                
                while (cursor.moveToNext()) {
                    provinces[i] = new OptionInfo(cursor.getInt(1), cursor.getString(0));
                    i++;
                }
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        
        return provinces;
    }
    
    /** 
     * 根据省Id获取所有市名列表
     * @return 获取成功则为所有市名列表，否则为null 
     */
    public static ArrayList<String> getAllCitys(Context context, int provinceId) {
        ArrayList<String> citys = new ArrayList<String>();
        String[] columns = new String[] { "name" };
        String whereClause = String.format("provinceid=%s and cityid>0 and districtid=0", provinceId);
        String orderBy = "cityid";
        Cursor cursor = null;
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return null;
        
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, orderBy);
            while (cursor != null && cursor.moveToNext()) {
                citys.add(cursor.getString(0));
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        return citys;
    }
    
    /** 
     * 根据省Id和市Id获取所有县\区名列表 
     * @return 获取成功则为所有县\区名列表，否则为null 
     */
    public static ArrayList<String> getAllDistricts(Context context, int provinceId, int cityId) {
        ArrayList<String> districts = new ArrayList<String>();
        String[] columns = new String[] { "name" };
        String whereClause = String.format("provinceid=%s and cityid=%s", provinceId, cityId);
        String orderBy = "districtid";
        Cursor cursor = null;
        SQLiteDatabase db = getAreaSQLiteDatabase(context);
        
        if(db == null)
            return null;
        
        try {
            cursor = db.query(TABLE_NAME, columns, whereClause, null, null, null, orderBy);
            while (cursor != null && cursor.moveToNext()) {
                districts.add(cursor.getString(0));
            }
        } 
        catch (Exception ex) {
            
        } 
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
                db.close();
            }
        }
        
        return districts;
    }
    
    private static SQLiteDatabase getAreaSQLiteDatabase(Context context){
        return DatabaseUtils.getSQLiteDatabase(context, AREA_DB_NAME, EgmConstants.AREA_DB_RAW_ID);
    }
}
