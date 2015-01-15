package com.netease.common.task;

public interface TransTypeCode {

	/*************************************************************************
	 * Type 0, -1 ~ -200 为保留类型，建议应用相关的type取值 1 ~ 30000
	 ************************************************************************/
	// 数据回应通知事务，例如获取到底http数据后进行回调通知
	public static final int TYPE_NOTIFY = -1;
	
	// 图片处理事务
	public static final int TYPE_IMAGE = -2;
	
	// 清除缓存
	public static final int TYPE_CLEAR_CACHE = -3;
    
    // 文件下载
    public static final int TYPE_FILE_DOWNLOAD = -4;
	
	/*************************************************************************
	 * Code -0xE000 ~ -0xEFFF 为保留Code，建议应用相关Code取值 0 ～ 0xEFFF
	 ************************************************************************/
	// 用于父子事务
	public static final int CODE_PARENT_CONTIUE = - 0xE000;
	
	// 取消
	public static final int ERR_CODE_CANCEL = - 0xE001;
	
	// Error code
	public static final int ERR_CODE_NO_NETWORK = - 0xEFE0; // 出错，无网络
	public static final int ERR_CODE_NETWORK_IOEXCEPTION = - 0xEFE1; // 网络IO异常
	public static final int ERR_CODE_NETWORK_EXCEPTION = - 0xEFE2; // 网络异常
	public static final int ERR_CODE_FILE_CREATE_EXCEPTION = - 0xEFE3; // 文件创建异常
	
	/****** Code -0xE100 ~ -0xE500 为Http Error Code (-0xE100 - ResponceCode) ******/
	public static final int ERR_CODE_HTTP = -0xE100;
	
	public static final int ERR_CODE_HTTP_301 = ERR_CODE_HTTP - 301; // http 301
	public static final int ERR_CODE_HTTP_302 = ERR_CODE_HTTP - 302; // http 302
	public static final int ERR_CODE_HTTP_400 = ERR_CODE_HTTP - 400; // http 400
	public static final int ERR_CODE_HTTP_401 = ERR_CODE_HTTP - 401; // http 401
	public static final int ERR_CODE_HTTP_403 = ERR_CODE_HTTP - 403; // http 403
	public static final int ERR_CODE_HTTP_404 = ERR_CODE_HTTP - 404; // http 404
	public static final int ERR_CODE_HTTP_500 = ERR_CODE_HTTP - 500; // http 500
	
	/****** Code -0xE600 Data Parse Exception ******/
	public static final int ERR_CODE_DATA_PARSE_EXCEPTION = -0xE600; // 数据解析错误
	
}
