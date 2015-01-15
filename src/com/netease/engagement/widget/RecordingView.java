package com.netease.engagement.widget;

import java.io.File;

import android.R.mipmap;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.media.AudioRecorderWrapper;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.media.MediaPlayerWrapper.MediaListener;

public class RecordingView extends RelativeLayout implements OnTouchListener{
	
	private ImageView mImageInit ; 
	private ImageView mToPlayImg ;
	private ImageView mPlayEnd ;
	
	//背景颜色
	private Paint mBgPaint ;
	//扇形宽度
	private int mStrokeWidth;
	//扇形所在矩形
	private RectF mDrawArcRectF;
	//扇形弧绘制相关
	private float mStartDegree ;
	//扫过弧度
	private float mSweepDegree ;
    //最大录音时间
	private static final int MAX_DURATION = 60 * 1000;
	//录音成功最短时间
	public static final int LEAST_DURATION = 3 * 1000 ;
	//开始时间
	private long mStartTime ;
	
	private boolean mInited = false ;
	private boolean mRecMode = true ;
	private boolean mRecSucces = false ;
	private boolean mIsPlaying = false;
	
	private Drawable mBgFirstState ;
	private Drawable mBgSecondState;
	private Drawable mBgThirdState ;
	
	//录音相关
	private AudioRecorderWrapper mAudioRecorderWrapper ;
	private String mFileId ;
	private String mFilePath ;
	private Handler mHandler ;
	
//	private boolean max_duration = false ;
	
	public RecordingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public RecordingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RecordingView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		mHandler = new Handler();
		//初始化资源
		initDrawables(context);
		ViewCompat.setBackground(this, mBgFirstState);
		this.setOnTouchListener(this);
		
		mImageInit = new ImageView(context);
		ViewCompat.setBackground(mImageInit, context.getResources().getDrawable(R.drawable.icon_pgrecommendlist_record));
		this.addView(mImageInit, getLp());
		
		mToPlayImg = new ImageView(context);
		ViewCompat.setBackground(mToPlayImg, context.getResources().getDrawable(R.drawable.btn_to_play_selector));
		this.addView(mToPlayImg, getLp());
		mToPlayImg.setVisibility(View.GONE);
		
		mPlayEnd = new ImageView(context);
		ViewCompat.setBackground(mPlayEnd, context.getResources().getDrawable(R.drawable.btn_play_end_selector));
		this.addView(mPlayEnd, getLp());
		mPlayEnd.setVisibility(View.GONE);
		
		mBgPaint = new Paint();
		mBgPaint.setColor(Color.parseColor("#e4e4e4"));
		mBgPaint.setStyle(Paint.Style.STROKE);
		mBgPaint.setAntiAlias(true);
		
		mDrawArcRectF = new RectF(0, 0, 0, 0);
		
		mAudioRecorderWrapper = AudioRecorderWrapper.getInstance();
		MediaPlayerWrapper.getInstance().registerMediaListener(mMediaPlayListener);
	}
	
	private void initDrawables(Context context){
		mBgFirstState = context.getResources().getDrawable(R.drawable.bg_pgrecommendlist_bottom_gray_circle);
		mBgSecondState = context.getResources().getDrawable(R.drawable.bg_pgrecommendlist_color_circle_red);
		mBgThirdState = context.getResources().getDrawable(R.drawable.bg_pgrecommendlist_color_circle_blue);
	}
	
	private RelativeLayout.LayoutParams getLp(){
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		return lp ;
	}
	
	private void startRecord(){
		mAudioRecorderWrapper.stop();
		MediaPlayerWrapper.getInstance().stop();
		
		mInited = true ;
		removeCallbacks(mRunnable);
		
		mStartTime = System.currentTimeMillis();
		mFileId = String.valueOf(System.currentTimeMillis());
		
		mAudioRecorderWrapper.setFilePath(EgmUtil.getFilePathByType(EgmUtil.TYPE_RECORDER, mFileId));
		mAudioRecorderWrapper.start();
		
		post(mRunnable);
		
		ViewCompat.setBackground(this,mBgSecondState);
		mStrokeWidth = (this.getWidth() - mImageInit.getWidth())/2;
		
		mBgPaint.setStrokeWidth(2 * mStrokeWidth);
		mDrawArcRectF.set(0, 0, 
				getMeasuredWidth() - 2 * mStrokeWidth,
				getMeasuredHeight() - 2 * mStrokeWidth);
		
		invalidate();
	}
	
	/**
	 * 停止录音
	 */
	private void stopRecord(boolean success){
//		max_duration = false ;
		removeCallbacks(mRunnable);
		mAudioRecorderWrapper.stop();
		if(success){
			/**
			 * 成功录制语音
			 */
			mRecSucces = true ;
			mFilePath = mAudioRecorderWrapper.getFilePath();
			ViewCompat.setBackground(this, mBgThirdState);
			mImageInit.setVisibility(View.GONE);
			mToPlayImg.setVisibility(View.VISIBLE);
			//替换背景后重绘边缘
			invalidate();
		}else{
			ViewCompat.setBackground(this,mBgFirstState);
			/**
			 * 删除本地文件
			 */
			String filePath = mAudioRecorderWrapper.getFilePath();
			if(!TextUtils.isEmpty(filePath)){
				File audioFile = new File(filePath);
				if(audioFile.exists()){
					audioFile.delete();
				}
			}
		}
	}
	
	/**
	 * 播放语音
	 */
	private void playAudio(){
	    mIsPlaying = true;
	    
		mToPlayImg.setVisibility(View.GONE);
		mPlayEnd.setVisibility(View.VISIBLE);
		MediaPlayerWrapper.getInstance().play(mFilePath);
	}
	
	/** 停止播放 */
	public void stopPlay(){
	    mIsPlaying = false;
        MediaPlayerWrapper.getInstance().stop();
	}

	/** 设置播放的初始状态 */
    public void setPlayInitState(int duration, String url){
        mFilePath = url;
        
        mStrokeWidth = (this.getWidth() - mImageInit.getWidth())/2;
        mBgPaint.setStrokeWidth(2 * mStrokeWidth);
        mDrawArcRectF.set(0, 0, 
                getMeasuredWidth() - 2 * mStrokeWidth,
                getMeasuredHeight() - 2 * mStrokeWidth);
        
        int degree = (int) ((duration * 1.0 / MAX_DURATION) * 360) ;
        mStartDegree = -90 + degree ;
        mSweepDegree = 360 - degree ;
        
        mRecSucces = true ;
        ViewCompat.setBackground(this, mBgThirdState);
        mImageInit.setVisibility(View.GONE);
        mToPlayImg.setVisibility(View.VISIBLE);
        
        invalidate();   //重绘
    }
    
    /**
     * 设置为录制状态
     */
    public void setRecordState(){
        mRecSucces = false ;
        mInited = false ;
        mRecMode = true ;
        ViewCompat.setBackground(this,mBgFirstState);
        mImageInit.setVisibility(View.VISIBLE);
        mToPlayImg.setVisibility(View.GONE);
        mPlayEnd.setVisibility(View.GONE);
    }
    
	/**
	 * 重新录制
	 */
	public void reRecord(){
		reRecordRetainFile();
		deleteAudioFile();    // 删除本地文件
	}
	
	/** 重新录制，但是不删除之前留住本地的录音文件 */
    public void reRecordRetainFile(){
//      max_duration = false ;
        mRecSucces = false ;
        mInited = false ;
        mRecMode = true ;
        ViewCompat.setBackground(this,mBgFirstState);
        mImageInit.setVisibility(View.VISIBLE);
        mToPlayImg.setVisibility(View.GONE);
        mPlayEnd.setVisibility(View.GONE);
        
        stopPlay();   // 如果正在播放的话，停止播放
    }
    
    /** 删除本地录音文件 */
    public void deleteAudioFile(){
        String filePath = mAudioRecorderWrapper.getFilePath();
        if(!TextUtils.isEmpty(filePath)){
            File audioFile = new File(filePath);
            if(audioFile.exists()){
                audioFile.delete();
            }
        }
    }
	
	private MediaListener mMediaPlayListener = new MediaListener(){
		@Override
		public void onMediaPrePare() {
		}

		@Override
		public void onMediaPlay() {
		    mIsPlaying = true;
		}

		@Override
		public void onMediaPause() {
		    mIsPlaying = false;
		}

		@Override
		public void onMediaRelease() {
		    mIsPlaying = false;
		    
		    if(!mRecMode){
		        mHandler.postDelayed(new Runnable(){
	                @Override
	                public void run() {
	                    mToPlayImg.setVisibility(View.VISIBLE);
	                    mPlayEnd.setVisibility(View.GONE);
	                }
	            }, 10);
		    }
		}

		@Override
		public void onMediaCompletion() {
		    mIsPlaying = false;
		    
			mHandler.postDelayed(new Runnable(){
				@Override
				public void run() {
					mToPlayImg.setVisibility(View.VISIBLE);
					mPlayEnd.setVisibility(View.GONE);
				}
			}, 10);
		}
	};
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		//原代码有问题，不显示的话mImageInit.getWidth()为0
		
		int measureSpec=MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
		mImageInit.measure(measureSpec, measureSpec);

        mStrokeWidth = (this.getWidth() - mImageInit.getMeasuredWidth())/2;
        mBgPaint.setStrokeWidth(2 * mStrokeWidth);
        mDrawArcRectF.set(0, 0, getMeasuredWidth() - 2 * mStrokeWidth, 
                getMeasuredHeight() - 2* mStrokeWidth);
        
		if(mInited && mRecMode || mRecSucces){
			canvas.save();
			canvas.translate(getPaddingLeft() + mStrokeWidth, getPaddingTop()+ mStrokeWidth);
			canvas.drawArc(mDrawArcRectF,mStartDegree,mSweepDegree, false,mBgPaint);
			canvas.restore();
		}
		if(mRecSucces){
			mRecMode = false ;
		}
	}
	
	private Runnable mRunnable = new Runnable(){
		@Override
		public void run() {
			long duration = System.currentTimeMillis() - mStartTime ;
			if(duration >= MAX_DURATION){
				//停止录音
				mAudioRecorderWrapper.stop();
				removeCallbacks(mRunnable);
				
				ViewCompat.setBackground(RecordingView.this, mBgThirdState);
//				max_duration = true ;
				return ;
			}
			if(mListener != null){
				mListener.onRecording(duration);
			}
			int degree = (int) ((duration*1.0/MAX_DURATION)*360) ;
			mStartDegree = -90 + degree ;
			mSweepDegree = 360 - degree ;
			invalidate();
			postDelayed(mRunnable,5);
		}
	};
	
	private long lastDownTime ;
	private long lastUpTime ;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			
			lastDownTime = System.currentTimeMillis();
			if(lastDownTime - lastUpTime <= 1000){
				return false ;
			}
			
			if(mRecMode){    // 当前是录音状态
			    ViewCompat.setBackground(mImageInit, getContext().getResources().getDrawable(R.drawable.btn_pgrecommendlist_record_prs));
				startRecord();
				if(mListener != null){
					mListener.onRecordStart();
				}
			}else{   // 非录音，播放或者停止播放
			    if(mIsPlaying){  // 正在播放，则停止播放
			        stopPlay();
			    }
			    else{    //开始播放录音
			        playAudio();
			    }
				
				return false ;
			}
			break;
			
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			
//			if(max_duration){
//				return true ;
//			}
		    
			long now = System.currentTimeMillis() ;
			lastUpTime = now;
			
			long interval = lastUpTime - lastDownTime ;
			if(interval < 500){
				try {
					Thread.sleep(500 - interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			ViewCompat.setBackground(mImageInit, getContext().getResources().getDrawable(R.drawable.icon_pgrecommendlist_record));
			if(!checkDuration(now) && mListener != null){    // 录音时长不够
				stopRecord(false);
				mListener.onRecordEnd(false ,-1,mAudioRecorderWrapper.getFilePath());
			}
			else if(mListener != null){
				long duration = (now - mStartTime) >= MAX_DURATION ? MAX_DURATION : now - mStartTime ;
				mListener.onRecordEnd(true,duration,mAudioRecorderWrapper.getFilePath());
				stopRecord(true);
			}
			break;
		}
		return true;
	}
	
	public interface OnRecordListener{
		//开始录音
		void onRecordStart();
		//录音时间显示接口
		void onRecording(long milSec);
		//标记录音失败或者成功，录音时间太短表示录音不成功
		void onRecordEnd(boolean suc,long duration,String filePath);
	}
	
	public OnRecordListener mListener ;
	public void setOnRecordListener(OnRecordListener listener){
		mListener = listener ;
	}
	
	private boolean checkDuration(long now){
		return (now - mStartTime) >= LEAST_DURATION ;
	}
}
