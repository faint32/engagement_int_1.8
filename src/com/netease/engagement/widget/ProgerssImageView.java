package com.netease.engagement.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.netease.engagement.widget.LoadingImageView.IUiGetImage;

/**
 * 封装了进度条的LoadingImageView，加载图片的时候显示进度条，加载完成则隐藏
 * @version 1.0
 */
public class ProgerssImageView extends FrameLayout{
    public ProgressBar mProgressBar;
    public LoadingImageView mImageView;
    
    private boolean mAutoInvisible;

    public ProgerssImageView(Context context) {
        this(context, null, 0);
    }

    public ProgerssImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgerssImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    public void setAutoInvisible(boolean value) {
    	mAutoInvisible = value;
    }
    
    public void showProgressBar() {
    	mProgressBar.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.INVISIBLE);
    }
    
    public void showImageView() {
    	mProgressBar.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.VISIBLE);
    }
    
    public void showImageViewWithProgess() {
    	mProgressBar.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.VISIBLE);
    }
    
    public void restoreState(){
        mProgressBar.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.INVISIBLE);
    }
    
    private void init(){
        mImageView = new LoadingImageView(this.getContext()); 
        mImageView.setScaleTop(true);
        
        mAutoInvisible = true;
        
        LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.addView(mImageView, lp1);
        
        mProgressBar = new ProgressBar(this.getContext());
        LayoutParams lp2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp2.gravity = Gravity.CENTER;
        this.addView(mProgressBar, lp2);
        
        mImageView.setNeedLoadImageErrorCallBack(true);
        mImageView.setUiGetImageListener(new IUiGetImage(){
            @Override
            public void onLoadImageStar(String url) {
            	if (mAutoInvisible) {
            		showProgressBar();
            	}
            }

            @Override
            public void onLoadImageFinish() {
            	showImageView();
            }

            @Override
            public void onLoadImageError() {
            	showImageView();
            }
        });
    }
}
