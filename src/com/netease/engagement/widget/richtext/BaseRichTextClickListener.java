package com.netease.engagement.widget.richtext;

import android.view.View;

public class BaseRichTextClickListener implements RichTextClickListener{

	@Override
	public boolean onRichTextClick(View view, String richTxt, int type) {
		switch (type) {
	        case RichTextClickListener.RICHTEXT_LINK:
	        	System.out.println("test-------------");
	            break;
	    }
	    return true;
	}

}
