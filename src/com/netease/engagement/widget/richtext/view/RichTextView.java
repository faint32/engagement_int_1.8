package com.netease.engagement.widget.richtext.view;

import android.content.Context;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;

import com.netease.engagement.widget.emot.EmotSpan;
import com.netease.engagement.widget.emot.EmoticonMgr;
import com.netease.engagement.widget.richtext.RichTextClickListener;
import com.netease.engagement.widget.richtext.span.LinkSpan;


public class RichTextView extends BaseRichTextView{

    public RichTextView(Context context) {
        this(context, null, 0);
    }

    public RichTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setMovementMethod(new LinkMovementMethod());
        addMatcher(LinkSpan.REGULAR);
        addMatcher(EmotSpan.REGULAR);
    }

    @Override
    public void setRichText(CharSequence str) {
        if (null == str) {
            return;
        }
        super.setRichText(str.toString());
    }

    @Override
    public void onMatched(String regularExp, int start, int end) {
    	Spannable sp = (Spannable)getText();
//    	if(regularExp.equals(LinkSpan.REGULAR)){
//    		new LinkSpan(sp.subSequence(start, end).toString(),mRichTextClickListener).setSpan(sp,start,end);
//    	}else 
    	if(regularExp.equals(EmotSpan.REGULAR)){
    		EmoticonMgr.getInstance(getContext()).setEmoticonSpan(sp,getTextSize());
    	}
    }
    
    RichTextClickListener mRichTextClickListener ;
    public void setOnRichTextClickListener(RichTextClickListener listener){
    	mRichTextClickListener = listener ;
    }
}
