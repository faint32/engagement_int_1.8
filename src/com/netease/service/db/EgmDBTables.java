package com.netease.service.db;

import android.net.Uri;

/**
 * 数据库表的名称及字段定义
 * @author echo_chen
 * @since  2014-03-18
 */

public class EgmDBTables {
	public static final String AUTHORITY = EgmDBProvider.AUTHORITY;
	public static final String[] TableNames = new String[]{
		AccountTable.TABLE_NAME,
	};
	
	public static interface AccountTable {
		public static final String TABLE_NAME = "account";
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		
		/**===================数据库字段==================*/
		/**id*/
        public static final String C_USER_ID = "user_id";
        /** 邮箱帐号 */
        public static final String C_USER_MAIL_ACCOUNT = "user_mail_account";
		/**帐号*/
    	public static final String C_USER_ACCOUNT = "user_account";
    	/**密码*/
    	public static final String C_USER_PASSWORD = "user_pwd";
    	/**token*/
        public static final String C_USER_TOKEN = "user_token";
    	/** 性别 定义见EgmConstants.SexType */
    	public static final String C_USER_SEX = "user_sex";
    	/**nickname*/
    	public static final String C_USER_NICKNAME = "user_nickname";
    	/**avatar*/
        public static final String C_USER_AVATAR = "user_avatar";
        /** 是否有头像 */
        public static final String C_USER_HAS_AVATAR = "user_has_avatar";
    	/** type 定义见EgmConstants.AccountType */
    	public static final String C_USER_TYPE = "user_type";
    	/**是否是vip*/
        public static final String C_USER_IS_VIP = "user_is_vip";
        /**省份代码*/
        public static final String C_USER_PROVINCE_ID = "user_province_id";
        /**城市代码*/
        public static final String C_USER_CITY_ID = "user_city_id";
        /**区县代码*/
        public static final String C_USER_DISTRICT_ID = "user_district_id";
        /**公有照片数*/
        public static final String C_USER_PUBLIC_PIC_COUNT = "user_public_pic_count";
        /** 私有照片数 */
        public static final String C_USER_PRIVATE_PIC_COUNT = "user_private_pic_count";
        
    	/**是否最近一次登录 1为登录，其他为未登录*/
    	public static final String C_LAST_LOGIN = "last_login";
    	/**更新时间*/
        public static final String C_UPDATE_TIME = "update_time";
        
        // 预留字段
        public static final String C_RESERVED1 = "reserved1";
        public static final String C_RESERVED2 = "reserved2";
        public static final String C_RESERVED3 = "reserved3";
        public static final String C_RESERVED4 = "reserved4";
	}
}
