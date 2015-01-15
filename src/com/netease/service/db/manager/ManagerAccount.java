
package com.netease.service.db.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.util.LevelChangeStatusBean;
import com.netease.engagement.util.LevelChangeStatusBean.LevelChangeType;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.EgmDBTables;
import com.netease.service.db.EgmDBTables.AccountTable;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.stat.EgmStatService;
import com.netease.util.PDEEngine;


public class ManagerAccount {
    private static Context mContext = BaseApplication.getAppInstance().getApplicationContext();
    private Account mCurAccount;
    private static ManagerAccount sInstance = null;

    public static class Account {
    	private String mUserId;
        /** 用来表示通行证帐号，不是主账号，是主账号绑定的通行证帐号 */
        public String mAccount;
        /** 主账号 */
        public String mUserName;
        public String mPassword;
        /** token */
        public String mToken;
        public String mNickName;
        private int mSex;
        /** 头像链接 */
        private String mAvatar;
        /** 是否有头像 */
        public boolean mHasPortrait;
        /** 帐号类型，参见EgmConstants.AccountType */
        public int mUserType;
        private boolean mIsVip;
        /** 省份代码 */
        public int mProvinceId;
        /** 城市代码 */
        public int mCityId;
        /** 区域代码 */
        public int mDistrictId;
        /** 公开照片数 */
        public int mPublicPicCount;
        /** 私有照片数 */
        public int mPrivatePicCount;
        /** 是否是当前帐号 */
        public boolean mIsLastLogin;
    }

    public static synchronized ManagerAccount getInstance() {
        if (sInstance == null) {
            sInstance = new ManagerAccount();
            
        }
        if(mContext == null){
            mContext = BaseApplication.getAppInstance().getApplicationContext();
        }
        return sInstance;
    }
    
    public String getCurrentAccountToken(){
        Account acc = getCurrentAccount();
        
        if(acc != null){
            return acc.mToken;
        }
        else{
            return "";
        }
    }

    /** 获取最后一次登录帐号 */
    private Account getLastLoginAccount() {
        Account acc = null;
        String selection = AccountTable.C_LAST_LOGIN + " = 1";
        Cursor c = mContext.getContentResolver().query(AccountTable.CONTENT_URI, null, selection, null, null);
        
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    acc = getAccountFromCursor(c);
                }
            } 
            finally {
                c.close();
            }
        }
        
//        mCurAccount = acc;  // 存到内存中
        
        return acc;
    }

    /**
     * 设置当前登录帐号。将传入的帐号设为当前登录帐号，如果在表中不存在，则增加一个帐号。
     * @return
     */
    public boolean setLoginAccount(UserInfo info, String userName, String password, String token, int accountType) {
        boolean ret = false;
        
        if (null == info) {
            return ret;
        }
        
        ManagerAccount.Account acc = new ManagerAccount.Account();
        acc.mUserId = String.valueOf(info.uid);
        acc.mAccount = info.account;
        acc.mPassword = password;
        acc.mUserName = userName;
        acc.mToken = token;
        acc.mNickName = info.nick;
        acc.mHasPortrait = info.hasPortrait;
        acc.mAvatar = info.portraitUrl192;
        acc.mIsLastLogin = true;
        acc.mSex = info.sex;
        acc.mUserType = accountType;
        acc.mIsVip = info.isVip;
        
        acc.mProvinceId = info.province;
        acc.mCityId = info.city;
        acc.mDistrictId = info.district;
        
        acc.mPublicPicCount = info.photoCount;
        acc.mPrivatePicCount = info.privatePhotoCount;
        
        Account oldacc = null;
        
        if (acc.mUserId != null) {
            oldacc = getAccount(acc.mUserId);
        }
        
        if (null != oldacc) {
            ret = updateAccountByUserId(acc);
        } 
        else {
            ret = addAccount(acc);
        }
        
        if (ret) {
        	Account tmp = mCurAccount;
        	if (tmp != null) {
	        	if (acc.mUserId.equals(tmp.mUserId)) {
	        		if (TextUtils.isEmpty(acc.mPassword)) {
	        			acc.mPassword = tmp.mPassword;
	        		}
	        		if (TextUtils.isEmpty(acc.mToken)) {
	        			acc.mToken = tmp.mToken;
	        		}
	        		
	        		acc.mIsLastLogin = tmp.mIsLastLogin;
	        		mCurAccount = acc;
	            }
	        	else {
	        		logout();
	        	}
        	}
        	
        	int oldLevel = EgmPrefHelper.getUserLevel(BaseApplication.getAppInstance().getApplicationContext(), info.uid);
        	int newLevel = info.level;
        	if (oldLevel != 0) {
        		if (info.sex == EgmConstants.SexType.Male) {
        			if (oldLevel < newLevel) { // 男性升级
        				LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
    	        		status.set(info.uid, LevelChangeType.Male_Level_Up_1, oldLevel, newLevel);
        			} else if (oldLevel > newLevel) { // 男性降级
        				if (newLevel >= 7) {
        					LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
        	        		status.set(info.uid, LevelChangeType.Male_Level_Down, oldLevel, newLevel);
        				}
        			}
        		} else if (info.sex == EgmConstants.SexType.Female) {
        			if (oldLevel < newLevel) { // 女性升级
        				LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
    	        		status.set(info.uid, LevelChangeType.Female_Level_Up, oldLevel, newLevel);
        			}
        		}
        	}
        	EgmPrefHelper.putUserLevel(BaseApplication.getAppInstance().getApplicationContext(), info.uid, newLevel);
        }
        
//        if(ret){    // 更新或添加成功了
//            mCurAccount = acc;  // 存到内存中
//        }
        
        // 记录更新用户信息的时间
        long now = System.currentTimeMillis();
        EgmPrefHelper.putUpdateUserInfoTime(mContext, now);
        
        return ret;
    }
    
    /**
     * 用userinfo更新当前登录帐号。
     * @return
     */
    public boolean updateLoginAccount(UserInfo info) {
        boolean result = false;
        
        if (info == null)
            return result;
        
        Account account = getAccount(String.valueOf(info.uid));
        if(account != null){
            result = setLoginAccount(info, account.mUserName, account.mPassword, account.mToken, account.mUserType);
        }
        
        return result;
    }

    /** 清除当前登录状态 */
    public int logout() {
        mCurAccount = null;
        
        EgmStatService.init(0);
        
        ContentValues values = new ContentValues();
        values.put(AccountTable.C_LAST_LOGIN, "0");
        
        String selection = AccountTable.C_LAST_LOGIN + "=?";
        String[] selectionArgs = new String[] {
            String.valueOf(1)
        };
        
        return mContext.getContentResolver().update(AccountTable.CONTENT_URI, values, selection, selectionArgs);
    }

    /**
     * 获取帐号
     * @param user_name
     * @return
     */
    public Account getAccount(String user_id) {
        Account acc = null;
        
        if (!TextUtils.isEmpty(user_id)) {
            String selection = AccountTable.C_USER_ID + "=?";
            String[] selectionArgs = new String[] {
              user_id
            };
            
            Cursor c = mContext.getContentResolver().query(AccountTable.CONTENT_URI, null, selection, selectionArgs, null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        acc = getAccountFromCursor(c);
                    }
                } finally {
                    c.close();
                }
            }
        }
        return acc;
    }

    private Account getAccountFromCursor(Cursor c){
        Account acc = new Account();
        
        acc.mUserId = c.getString(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_ID));
        acc.mAccount = c.getString(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_MAIL_ACCOUNT));
        acc.mUserName = c.getString(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_ACCOUNT));
        acc.mPassword = c.getString(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_PASSWORD));
        acc.mToken = c.getString(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_TOKEN));
        acc.mSex = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_SEX));
        acc.mNickName = c.getString(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_NICKNAME));
        acc.mAvatar = c.getString(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_AVATAR));
        
        int has = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_HAS_AVATAR));
        if(has == 1){
            acc.mHasPortrait = true;
        }
        else{
            acc.mHasPortrait = false;
        }
        
        acc.mUserType = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_TYPE));
        acc.mIsVip = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_IS_VIP)) == 1 ? true : false;
        acc.mProvinceId = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_PROVINCE_ID));
        acc.mCityId = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_CITY_ID));
        acc.mDistrictId = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_DISTRICT_ID));
        acc.mPublicPicCount = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_PUBLIC_PIC_COUNT));
        acc.mPrivatePicCount = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_USER_PRIVATE_PIC_COUNT));
        acc.mIsLastLogin = c.getInt(c.getColumnIndex(EgmDBTables.AccountTable.C_LAST_LOGIN)) == 1 ? true : false;
        
        String pwEnc = PDEEngine.PDecrypt(mContext, acc.mPassword);
        if (!TextUtils.isEmpty(pwEnc)) {
            acc.mPassword = pwEnc;
        }
        
        String tokenDec = PDEEngine.PDecrypt(mContext, acc.mToken);
        if(!TextUtils.isEmpty(acc.mToken)){
            acc.mToken = tokenDec;
        }
        
        return acc;
    }
    
    /** 更新当前登录帐号的信息 */
    public boolean updateAccountByUserInfo(UserInfo info){
        ManagerAccount.Account acc = new ManagerAccount.Account();
        acc.mUserId = String.valueOf(info.uid);
        acc.mAccount = info.account;
        acc.mNickName = info.nick;
        if(!TextUtils.isEmpty(info.portraitUrl192)){
            acc.mAvatar = info.portraitUrl192;
        }
        acc.mHasPortrait = info.hasPortrait;
        acc.mIsLastLogin = true;
        acc.mSex = info.sex;
        acc.mIsVip = info.isVip;
        
        if(info.province > 0){
            acc.mProvinceId = info.province;
        }
        if(info.city > 0){
            acc.mCityId = info.city;
        }
        if(info.district > 0){
            acc.mDistrictId = info.district;
        }
        
        acc.mPublicPicCount = info.photoCount;
        acc.mPrivatePicCount = info.privatePhotoCount;
        
        return updateAccountByUserId(acc);
    }
    
    /**
     * 更新用户帐号表中某一user_account所对应的数据项
     * @param Account
     * @return
     */
    public boolean updateAccountByUserId(Account user) {
        ContentValues values = new ContentValues();
        values.clear();
        
        if (null != user.mPassword) {
            String pwEnc = PDEEngine.PEncrypt(mContext, user.mPassword);
            values.put(AccountTable.C_USER_PASSWORD, pwEnc);
        }
        
        if(null != user.mToken){
            String tokenEnc = PDEEngine.PEncrypt(mContext, user.mToken);
            values.put(AccountTable.C_USER_TOKEN, tokenEnc);
        }
        
        if (null != user.mAccount) {
            values.put(AccountTable.C_USER_MAIL_ACCOUNT, user.mAccount);
        }
        
        if (null != user.mNickName) {
            values.put(AccountTable.C_USER_NICKNAME, user.mNickName);
        }
        
        if (null != user.mUserName) {
            values.put(AccountTable.C_USER_ACCOUNT, user.mUserName);
        }
        
        if (null != user.mAvatar) {
            values.put(AccountTable.C_USER_AVATAR, user.mAvatar);
        }
        
        values.put(AccountTable.C_USER_HAS_AVATAR, user.mHasPortrait ? 1 : 0);
        values.put(AccountTable.C_USER_SEX, user.mSex);
        values.put(AccountTable.C_USER_TYPE, user.mUserType);
        values.put(AccountTable.C_USER_IS_VIP, user.mIsVip ? 1 : 0);
        
        values.put(AccountTable.C_USER_PROVINCE_ID, user.mProvinceId);
        values.put(AccountTable.C_USER_CITY_ID, user.mCityId);
        values.put(AccountTable.C_USER_DISTRICT_ID, user.mDistrictId);
        
        values.put(AccountTable.C_USER_PUBLIC_PIC_COUNT, user.mPublicPicCount);
        values.put(AccountTable.C_USER_PRIVATE_PIC_COUNT, user.mPrivatePicCount);
        
        values.put(AccountTable.C_LAST_LOGIN, user.mIsLastLogin ? 1 : 0);
        values.put(AccountTable.C_UPDATE_TIME, System.currentTimeMillis());

        String selection = AccountTable.C_USER_ID + "=?";
        String[] selectionArgs = new String[] {
            user.mUserId
        };
        
        int ret = mContext.getContentResolver().update(AccountTable.CONTENT_URI, values, selection, selectionArgs);
        return ret > 0 ? true : false;
    }

    /**
     * 向用户帐号表中插入一项
     *
     * @param Account
     * @return
     */
    private boolean addAccount(Account user) {
        if (null == user) {
            return false;
        }
        
        ContentValues values = new ContentValues();
        if (null != user.mUserName) {
            values.put(AccountTable.C_USER_ACCOUNT, user.mUserName);
        }
        
        if (null != user.mPassword) {
            String pwEnc = PDEEngine.PEncrypt(mContext, user.mPassword);
            values.put(AccountTable.C_USER_PASSWORD, pwEnc);
        }
        
        if(null != user.mToken){
            String tokenEnc = PDEEngine.PEncrypt(mContext, user.mToken);
            values.put(AccountTable.C_USER_TOKEN, tokenEnc);
        }
        
        if (null != user.mNickName) {
            values.put(AccountTable.C_USER_NICKNAME, user.mNickName);
        }
        
        if (null != user.mUserId) {
            values.put(AccountTable.C_USER_ID, user.mUserId);
        }
        
        if (null != user.mAccount) {
            values.put(AccountTable.C_USER_MAIL_ACCOUNT, user.mAccount);
        }
        
        if (null != user.mAvatar) {
            values.put(AccountTable.C_USER_AVATAR, user.mAvatar);
        }
        
        values.put(AccountTable.C_USER_HAS_AVATAR, user.mHasPortrait ? 1 : 0);
        values.put(AccountTable.C_USER_SEX, user.mSex);
        values.put(AccountTable.C_USER_TYPE, user.mUserType);
        values.put(AccountTable.C_USER_IS_VIP, user.mIsVip ? 1 : 0);
        
        values.put(AccountTable.C_USER_PROVINCE_ID, user.mProvinceId);
        values.put(AccountTable.C_USER_CITY_ID, user.mCityId);
        values.put(AccountTable.C_USER_DISTRICT_ID, user.mDistrictId);
        
        values.put(AccountTable.C_USER_PUBLIC_PIC_COUNT, user.mPublicPicCount);
        values.put(AccountTable.C_USER_PRIVATE_PIC_COUNT, user.mPrivatePicCount);
        
        values.put(AccountTable.C_LAST_LOGIN, user.mIsLastLogin ? 1 : 0);
        values.put(AccountTable.C_UPDATE_TIME, System.currentTimeMillis());
        
        Uri uri = mContext.getContentResolver().insert(AccountTable.CONTENT_URI, values);
        try {
            if (uri == null || ContentUris.parseId(uri) < 0) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }

    /**
     * 获取登录过的所有账号
     *
     * @return
     */
    public List<Account> getAllAccountsList() {
        List<Account> list = new ArrayList<Account>();

        Cursor c = mContext.getContentResolver().query(AccountTable.CONTENT_URI, null, null, null, "_id" + " DESC");
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                Account acc = getAccountFromCursor(c);
                list.add(acc);
            }
            c.close();
        }

        return list;
    }

    /** 按显示需求获取登录过的所有帐号 */
    public Cursor getAllAccounts() {
        return mContext.getContentResolver().query(AccountTable.CONTENT_URI, null, null, null, AccountTable.C_LAST_LOGIN + " DESC");
    }
    
    /** 是否有登录过的账户 */
    public boolean hasAccount(){
        Cursor c = getAllAccounts();
        
        if(c != null && c.getCount() > 0){
            c.close();
            return true;
        }
        else{
            c.close();
            return false;
        }
    }

    /**
     * 删除指定用户名的帐号
     *
     * @param String
     * @return
     */
    public void deleteAccountByName(String user_name) {
        if (TextUtils.isEmpty(user_name))
            return;
        
        String where = AccountTable.C_USER_ACCOUNT + " =?";
        String[] selectionArgs = new String[] {
            user_name
        };
        mContext.getContentResolver().delete(AccountTable.CONTENT_URI, where, selectionArgs);
        
        if(mCurAccount != null && mCurAccount.mUserName.equals(user_name)) {
            mCurAccount = null;
        }
    }

    /**
     * 删除指定userid的帐号
     *
     * @param String
     * @return
     */
    public void deleteAccountByUserId(String user_id) {
        if (TextUtils.isEmpty(user_id))
            return;
        String where = AccountTable.C_USER_ID + " =?";
        String[] selectionArgs = new String[] {
                user_id
        };
        mContext.getContentResolver().delete(AccountTable.CONTENT_URI, where, selectionArgs);
        
        if(mCurAccount != null && mCurAccount.mUserId.equals(user_id)) {
            mCurAccount = null;
        }
    }

    /**
     * 删除所有账号
     *
     * @return
     */
    public void deleteAllAccount() {
        mContext.getContentResolver().delete(AccountTable.CONTENT_URI, null, null);
        mCurAccount = null;
    }

    /**
     * 获取当前登录帐号的用户名，若不存在，则返回匿名
     *
     * @return
     */
    public String getCurrentAccountName() {
        String name = null;
        
        Account acc = getCurrentAccount();
        if(acc != null){
            name = acc.mUserName;
        }
        
        return name;
    }

    /**
     * 获取当前登录帐号的用户id
     *
     * @return
     */
    public String getCurrentAccountId() {
        String userId = null;
        
        Account acc = getCurrentAccount();
        if(acc != null){
            userId = acc.mUserId;
        }
        
        return userId;
    }
    /**
     * 获取当前登录帐号的用户id
     *
     * @return
     */
    public long getCurrentId() {
    	  	 String userId = null;
    	  	 long id = 0;
          Account acc = getCurrentAccount();
          if(acc != null){
              userId = acc.mUserId;
          }
          if(!TextUtils.isEmpty(userId)){
        	  	id = Long.parseLong(userId);
          }
          return id;
    }
    
    /**
     * 获取当前登录帐号的用户id
     *
     * @return
     */
    public String getCurrentIdString() {
          return String.valueOf(getCurrentId());
    }
    
    
    /**
     * 获取当前登录帐号的用户通行证帐号
     *
     * @return
     */
    public String getCurrentAccountMail() {
        String mail = null;
        
        Account acc = getCurrentAccount();
        if(acc != null){
            mail = acc.mAccount;
        }
        
        return mail;
    }

    /**
     * 获取当前登录帐号的用户
     *
     * @return
     */
    public synchronized Account getCurrentAccount() {
        Account acc;
        if (mCurAccount != null) {
            acc = mCurAccount;
        } 
        else {
            acc = getLastLoginAccount();
            mCurAccount = acc;
            
            if (acc != null) {
            	try {
					EgmStatService.init(Integer.parseInt(acc.mUserId));
				} catch (Exception e) {
				}
            }
        }
        
        return acc;
    }

    /**
     * 根据userId更新昵称和头像
     *
     * @return
     */
    private boolean updateNickAndAvatarByUserId(String userId, String nickName, String avatar){
        if(TextUtils.isEmpty(userId)){
            return false;
        }
        
        ContentValues values = new ContentValues();
        values.clear();

        if (!TextUtils.isEmpty(nickName)) {
            values.put(AccountTable.C_USER_NICKNAME, nickName);
        }
        
        if (!TextUtils.isEmpty(avatar)) {
            values.put(AccountTable.C_USER_AVATAR, avatar);
        }
        
        values.put(AccountTable.C_UPDATE_TIME, System.currentTimeMillis());

        String selection = AccountTable.C_USER_ID + "=?";
        String[] selectionArgs = new String[] {
                userId
        };
        int ret = mContext.getContentResolver().update(AccountTable.CONTENT_URI, values, selection, selectionArgs);
        if (ret > 0) {
            if(mCurAccount != null && mCurAccount.mUserId.equals(userId)) {
                mCurAccount = null;
            }
            return true;
        }
        else
            return false;
    }
    /**
     * 根据userId更新昵称和头像
     *
     * @return
     */
    public boolean updateAvatarByUserId(String userId,String avatar){
        if(TextUtils.isEmpty(userId)){
            return false;
        }
        
        ContentValues values = new ContentValues();
        values.clear();

        if (!TextUtils.isEmpty(avatar)) {
            values.put(AccountTable.C_USER_AVATAR, avatar);
        }
        
        values.put(AccountTable.C_UPDATE_TIME, System.currentTimeMillis());

        String selection = AccountTable.C_USER_ID + "=?";
        String[] selectionArgs = new String[] {
                userId
        };
        int ret = mContext.getContentResolver().update(AccountTable.CONTENT_URI, values, selection, selectionArgs);
        if (ret > 0) {
            if(mCurAccount != null && mCurAccount.mUserId.equals(userId)) {
                mCurAccount.mAvatar = avatar;
            }
            return true;
        }
        else
            return false;
    }
    /**
     * 判断当前是否男性帐号登录
     *
     * @return
     */
    public boolean isMale(){
    		Account acc = getCurrentAccount();
    	    if(acc != null && acc.mSex == EgmConstants.SexType.Male){
    	    	  return true;
    	    }
    	    return false;
    }
    /**
     * 判断当前帐号性别
     *
     * @return 0:女，1：男
     */
    public int getCurrentGender(){
     	int gender = EgmConstants.SexType.Male;
    		Account acc = getCurrentAccount();
	    if(acc != null){
	    		gender = acc.mSex;
	    }
	    return gender;
    }
    /**
     * 获取当前登录帐号头像
     */
    public String getCurrentAvatar(){
     	String avatar = null;
     	Account acc = getCurrentAccount();
	    if(acc != null){
	    		avatar = acc.mAvatar;
	    }
	    return avatar;
    }
    
    public boolean isVip() {
    	boolean b = false;
    	Account acc = getCurrentAccount();
	    if(acc != null){
	    	b = acc.mIsVip;
	    }
	    return b;
    }
    
    public void setVip(boolean b) {
    	Account acc = getCurrentAccount();
	    if(acc != null){
	    	acc.mIsVip = b;
	    }
    }
}
