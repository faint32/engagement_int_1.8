package com.netease.engagement.widget;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.LayoutParams;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.service.Utils.EgmUtil;

/**
 * 自定义actionbar
 */
public class CustomActionBar {

	private ActionBar mActionBar;
	
	private View mCustomView;
	
	private Context mContext ;
	
	private TextView mLeftTv, mRightTv;
	private TextView mTitle,mSubTitle;
	private TextView mRightIcon;
	
	public CustomActionBar(Context context, ActionBar actionBar, int resId) {
		mContext = context ;
		mActionBar = actionBar;
		mCustomView = LayoutInflater.from(context).inflate(resId, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mActionBar.setCustomView(mCustomView, lp);
		
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setDisplayHomeAsUpEnabled(false);
		
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(true);
		
		mActionBar.setBackgroundDrawable(context.getResources().getDrawable(R.color.transparent));
		mActionBar.setSplitBackgroundDrawable(context.getResources().getDrawable(R.color.transparent));
		mActionBar.setStackedBackgroundDrawable(context.getResources().getDrawable(R.color.transparent));
		
		mLeftTv = (TextView)mCustomView.findViewById(R.id.title_left);
		mTitle = (TextView)mCustomView.findViewById(R.id.title_title);
		mRightTv = (TextView) mCustomView.findViewById(R.id.title_right);
		mSubTitle = (TextView) mCustomView.findViewById(R.id.title_subtitle);
		mRightIcon = (TextView) mCustomView.findViewById(R.id.icon_right);
	}
	/**
	 * 隐藏actionbar
	 */
	public void hide(){
		mActionBar.hide();
	}
	
	/**
	 * 设置左边的点击监听
	 */
	public void setLeftClickListener(OnClickListener listener){
	    mLeftTv.setOnClickListener(listener);
	    if(listener != null){
	        mLeftTv.setVisibility(View.VISIBLE);
        }
	}
	
	/**
     * 设置右边的点击监听
     */
    public void setRightClickListener(OnClickListener listener){
        mRightTv.setOnClickListener(listener);
        if(listener != null){
            mRightTv.setVisibility(View.VISIBLE);
        }
    }
    
    public void setLeftVisibility(int visibility){
        mLeftTv.setVisibility(visibility);
    }
    
    public void setRightVisibility(int visibility){
        mRightTv.setVisibility(visibility);
    }
    
    /**
     * 左边文字
     */
    public void setLeftAction(int textId){
        if(textId > 0){
            mLeftTv.setText(textId);
        }
    }
	
	/**
	 * 左边图片加文字
	 */
	public void setLeftAction(int resId ,int textId){
		if(textId > 0){
		    mLeftTv.setText(textId);
		}
		
		if(resId > 0){
		    mLeftTv.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(resId), null, null, null);
		}
		else{
		    mLeftTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
	}
	
	public void setLeftAction(int resId ,String left_title){
        mLeftTv.setText(left_title);
        
        if(resId > 0){
            mLeftTv.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(resId), null, null, null);
        }
        else{
            mLeftTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
	}
	
	public void setLeftTitleColor(int color){
		mLeftTv.setTextColor(color);
	}
	
	public void setLeftTitleSize(int size){
		mLeftTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
	}
	
	/**
	 * 设置中间标题栏
	 */
	public void setMiddleTitle(int textId){
		mTitle.setText(textId);
	}
	
	public void setMiddleTitle(String title){
	    mTitle.setText(title);
	}
	
	public void setMiddleTitleColor(int color){
		mTitle.setTextColor(color);
	}
	
	public void setMiddleTitleSize(int size){
		mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
	}
	   /**
     * 设置中间子标题栏
     */
    public void setSubTitle(int textId){
        mSubTitle.setText(textId);
    }
    
    public void setSubTitle(String title){
        mSubTitle.setText(title);
    }
    
    public void setSubTitleColor(int color){
        mSubTitle.setTextColor(color);
    }
    
    public void setSubTitleSize(int size){
        mSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
    }
    
	
	public void hideLeftTitle(){
		mLeftTv.setVisibility(View.INVISIBLE);
	}
	
	public void hideMiddleTitle(){
		mTitle.setVisibility(View.GONE);
	}
	
	public void hideSubTitle(){
	    mSubTitle.setVisibility(View.GONE);
    }
	
	public void hideRightTitle(){
		mRightTv.setVisibility(View.INVISIBLE);
	}
	
	public void showSubTitle(){
        mSubTitle.setVisibility(View.VISIBLE);
    }
	
	
	
	/**
	 * 设置右边图标加文字
	 */
	public void setRightAction(int resId, int textId){
		if(textId > 0){
            mRightTv.setText(textId);
        }else{
        	mRightTv.setText("");
        }
        
        if(resId > 0){
            mRightTv.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(resId), null);
        }
        else{
            mRightTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
	}
	
	public void setRightTitleColor(int color){
		mRightTv.setTextColor(color);
	}
	
	public void setRightAction(int resId,String title){
	    if(!TextUtils.isEmpty(title)){
            mRightTv.setText(title);
        }else{
        	mRightTv.setText("");
        }
        
        if(resId > 0){
        	mRightTv.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(resId), null);
        }
        else{
        	mRightTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
	}
	public void setRightTitleSize(int size){
        mRightTv.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
    }
	
	public View getCustomView(){
		return mCustomView ;
	}
	
	public void hideWithAnim(){
		Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_to_top);
		mCustomView.startAnimation(anim);
	}
	
	public void showWithAnim(){
		Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_from_top);
		mCustomView.startAnimation(anim);
	}
	
	public void setBackgroundColor(int resid){
	    this.getCustomView().setBackgroundColor(mContext.getResources().getColor(resid));
	}
	
	public void setLeftBackgroundResource(int resId) {
		mLeftTv.setBackgroundResource(resId);
	}
	
	public void setRightBackgroundResource(int resId) {
		mRightTv.setBackgroundResource(resId);
	}
	
	public void setLeftTitleCenter() {
		mLeftTv.setGravity(Gravity.CENTER);
	}
	public TextView getmLeftTv() {
		return mLeftTv;
	}
	public void setmLeftTv(TextView mLeftTv) {
		this.mLeftTv = mLeftTv;
	}
	public TextView getmRightTv() {
		return mRightTv;
	}
	public void setmRightTv(TextView mRightTv) {
		this.mRightTv = mRightTv;
	}
	
	public void setCustomBarBackground(int resId) {
		mCustomView.setBackgroundResource(resId);
	}
	/**
	 * 设置title居左
	 * @param leftPadingDp 距离左侧返回按钮的距离，单位:dp
	 */
	public void setTitleAlignLeft(int leftPadingDp){
	  mTitle.setGravity(Gravity.LEFT);
	  mTitle.setPadding(EgmUtil.dip2px(mContext, leftPadingDp), 0, 0, 0);
	  mSubTitle.setGravity(Gravity.LEFT);
	  mSubTitle.setPadding(EgmUtil.dip2px(mContext, leftPadingDp), 0, 0, 0);
	}
	/**
	 * 设置title居中
	 */
	public void setTitleAlignCenter(){
	  mTitle.setGravity(Gravity.CENTER);
	  mTitle.setPadding(0, 0, 0, 0);
	  mSubTitle.setGravity(Gravity.CENTER);
	  mSubTitle.setPadding(0, 0, 0, 0);
	}
	/**
	 * 设置右边按钮旁边的图标
	 */
	public void setRightIcon(int resId){
        if(resId > 0){
        		mRightIcon.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(resId), null);
        }
        else{
        		mRightIcon.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
	}
	/**
	 * 设置右边按钮旁边的图标是否显示
	 */
	public void setRightIconShow(boolean show){
		if(show){
			mRightIcon.setVisibility(View.VISIBLE);
		} else{
			mRightIcon.setVisibility(View.GONE);
		}
	}
}
