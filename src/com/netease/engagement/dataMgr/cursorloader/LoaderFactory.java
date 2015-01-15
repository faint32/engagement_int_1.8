package com.netease.engagement.dataMgr.cursorloader;

import android.content.Context;

/**
 * Loader工厂类
 */
public class LoaderFactory {
	
	public static final int CHAT_LIST_SORT_TIME = 0 ;
	public static final int CHAT_LIST_SORT_RICH = 1 ;
	public static final int CHAT_LIST_SORT_INIT = 2 ;
	
	public static Loader getLoader(Context context ,int loaderType,boolean onlyNew){
			switch(loaderType){
				case CHAT_LIST_SORT_TIME:
					return new LastMsgTimeLoader(context,onlyNew);
				case CHAT_LIST_SORT_RICH:
					return new LastMsgRichLoader(context ,onlyNew) ;
				case CHAT_LIST_SORT_INIT:
					return new LastMsgInitLoader(context ,onlyNew) ;
					default :
						return null ;
		}
	}
}
