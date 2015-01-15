package com.netease.engagement.widget.richtext;

import android.view.View;

public interface RichTextClickListener {
	
	public static final int RICHTEXT_LINK = 1 ;
	public static final int RICHTEXT_EMOTICON = 2 ;
	
	/**
     * @param view
     * @param String
     * @param type
     * @return true,处理过, false, 未处理, 调用默认处理
     */
    public abstract boolean onRichTextClick(View view, String richTxt, int type);

}
