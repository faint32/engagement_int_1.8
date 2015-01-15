package com.netease.engagement.widget.emot;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.Utils.EgmUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.EditText;

/**
 * 富文本EditText，显示表情，和剩余字数提示
 */
public class EmotEdit extends EditText {
	//文字高度
    private float mTextHeight;
    
    private boolean mTxtNumTip ;
    
    private int mPadding = 8;
    
    private int paddingBottom;
    private int paddingRight;
    private int mNum ;
    
    //剩余字数少于50时候显示提示
    private static final int TIP_NUM = 50 ;
    
    public EmotEdit(Context context) {
        this(context, null);
    }
    public EmotEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public EmotEdit(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
	private void init(Context context) {
    	
    	this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EgmConstants.PRIVATE_MSG_EDIT_MAX)});
        addTextChangedListener(mTextChange);
        
        mPadding = EgmUtil.dip2px(context, mPadding);
        
        setPadding(mPadding,mPadding,mPadding,mPadding);
        
        mRect = new Rect();
        mPaint = new TextPaint();
		mPaint.setTextSize(22);
		mPaint.setColor(getContext().getResources().getColor(R.color.info_audio_txt_color));
		mPaint.getTextBounds("00", 0, 2, mRect);
		
		paddingBottom = mRect.height();
		paddingRight = mRect.width() ;
		
		mTextHeight = getTextSize();
    }
    
    TextWatcher mTextChange = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }                                                                                                      
        
        @Override
        public void afterTextChanged(Editable s) {
        	//保护表情
        	if(s.length() > EgmConstants.PRIVATE_MSG_EDIT_MAX){
        		return ;
        	}
        	
        	//开始字数提示
        	if(s.length() > EgmConstants.PRIVATE_MSG_EDIT_MAX - TIP_NUM){
        		mNum = EgmConstants.PRIVATE_MSG_EDIT_MAX - s.length();
        		mTxtNumTip = true ;
        		setPadding(mPadding,mPadding,mPadding,paddingBottom + mPadding);
        	}else{
        		mTxtNumTip = false ;
        		setPadding(mPadding,mPadding,mPadding,mPadding);
        	}
        	invalidate();
        		
        	//表情替换
            int selBegin = getSelectionStart();
            int selEnd = getSelectionEnd();
            int[] newSelect = new int[]{selBegin, selEnd};
            EmoticonMgr.getInstance(getContext()).setEmoticonSpan(s, newSelect, mTextHeight);
            if(newSelect[0] != selBegin || newSelect[1] != selEnd){
            	setSelection(newSelect[0], newSelect[1]);
            }
        }
    };

    private TextPaint mPaint ;
    private int mWidth ;
    private int mHeight ;
    private Rect mRect ;
    
    @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mTxtNumTip){
			mWidth = getWidth();
			mHeight = getHeight();
			canvas.drawText(
					""+mNum, 
					mWidth - paddingRight - mPadding, 
					getScrollY() + mHeight - paddingBottom + mPadding/2,
					mPaint
			);
		}
	}
    
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            ViewParent parent = getParent();
            if(parent != null)
                parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(ev);
    }
}
