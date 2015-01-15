package com.netease.pkgRelated;


import com.netease.date.R;
import com.netease.service.app.BaseApplication;
import com.netease.service.protocol.EgmServiceCode;

public class ErrorToString {
	public static String getString(int errCode){
		int errorStr;
		switch(errCode){
		case EgmServiceCode.TRANSACTION_SUCCESS:
			return null;
		case EgmServiceCode.TRANSACTION_FAIL:
    		errorStr = R.string.transaction_fail;
    		break;
		case EgmServiceCode.NETWORK_ERR_COMMON:
            errorStr = R.string.network_error_common;
            break;
		case EgmServiceCode.TRANSACTION_COMMON_SERVER_ERROR:
		    errorStr = R.string.sever_err;
            break;
		case EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION:
            errorStr = R.string.parse_err;
            break;
		case EgmServiceCode.ERR_CODE_FILE_CREATE_EXCEPTION:
			 errorStr = R.string.file_create_error;
			break;
		default:
    			errorStr = R.string.unkown_err;
    		break;
		}
		return BaseApplication.getAppInstance().getResources().getString(errorStr);
	}
}
