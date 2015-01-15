package com.netease.engagement.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;

public class RecordingViewDoubleClick extends RelativeLayout {

	private String TAG = "DoubleClickRecordingView";
	private boolean DEBUG = true;
	private ImageView sendRecordImg;
	private ImageView recordingCicleImg;
	private TextView pressSendVoice;
	private OnRecordListener onRecordListener;
	private Context context;
	private long recordDuration;
	private float mStartDegree;
	private float mSweepDegree;
	private static final int MAX_DURATION = 60 * 1000;
	private long mStartTime;
	private Paint paint;
	private RectF rectF;
	private Path path;
	/**
	 * 圆形渐变背景BitmapDrawable
	 */
	private BitmapDrawable bitmapDrawable;

	public RecordingViewDoubleClick(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
	}

	public RecordingViewDoubleClick(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public RecordingViewDoubleClick(Context context) {
		super(context);
		this.context = context;
		init();
	}

	private void init() {

		// 关闭当前View的硬件加速 ，否则Android4.0 Ice Cream 出现问题 小米1
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		paint = new Paint();
		paint.setAntiAlias(true);
		rectF = new RectF();
		path = new Path();
		recordingCicleImg = new ImageView(context);
		Animation scaleInBgAnimation = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.scale_in);
		scaleInBgAnimation.setDuration(300);
		scaleInBgAnimation.setStartOffset(200);
		ViewCompat.setBackground(recordingCicleImg,
				context.getResources().getDrawable(R.drawable.bg_pgrecommendlist_bottom_white_circle));
		recordingCicleImg.startAnimation(scaleInBgAnimation);
		scaleInBgAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				recordingCicleImg.setVisibility(View.INVISIBLE);
				ViewCompat.setBackground(RecordingViewDoubleClick.this,
						context.getResources().getDrawable(R.drawable.bg_pgrecommendlist_bottom_white_circle));
				startRecord();
			}
		});
		this.addView(recordingCicleImg, getLp());

		sendRecordImg = new ImageView(context);
		ViewCompat.setBackground(sendRecordImg,
				context.getResources().getDrawable(R.drawable.button_send_voice_selector));
		Animation scaleInAnimation = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.scale_in);
		scaleInAnimation.setDuration(200);
		scaleInAnimation.setStartOffset(200);
		sendRecordImg.startAnimation(scaleInAnimation);
		this.addView(sendRecordImg, getLp());
		sendRecordImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (recordDuration / 1000 < 1) {
					Toast.makeText(context, "录音时间太短", 1).show();
				} else {
					stopRecord();
					if (onRecordListener != null)
						onRecordListener.onRecordSend();
				}
			}
		});
		sendRecordImg.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					pressSendVoice.setTextColor(Color.WHITE);
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					pressSendVoice.setTextColor(Color.parseColor("#787878"));
					break;
				default:
					break;
				}
				return false;
			}
		});

		sendRecordImg.setId(R.id.dialog_double_click_record_id);
		float scale = context.getResources().getDisplayMetrics().density;
		scale = context.getResources().getDisplayMetrics().scaledDensity;
		pressSendVoice = new TextView(context);
		pressSendVoice.setText("点击发送");
		pressSendVoice.setTextColor(Color.parseColor("#787878"));
		pressSendVoice.setTextSize(14);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layoutParams.setMargins(0, 0, 0, (int) (30 * scale + 0.5f));
		layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.dialog_double_click_record_id);
		this.addView(pressSendVoice, layoutParams);

		bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_pgrecommendlist_color_circle_red);
	}

	private RelativeLayout.LayoutParams getLp() {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		return lp;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float center_X = getWidth() / 2;
		float center_Y = getHeight() / 2;
		float r = getWidth() / 2;
		try {
			canvas.save();
			rectF.set(center_X - r, center_Y - r, center_X + r, center_Y + r);
			path.reset();
			path.arcTo(rectF, mStartDegree, mSweepDegree);
			path.lineTo(center_X, center_Y);
			path.close();
			canvas.clipPath(path);
 
			bitmapDrawable.setBounds(0, 0, getWidth(), getHeight());
			bitmapDrawable.draw(canvas);
			// canvas.drawBitmap(bitmapCirecle, 0.0f, 0.0f, paint);
			canvas.restore();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
	}

	private void startRecord() {
		removeCallbacks(mRunnable);
		mStartTime = System.currentTimeMillis();
		post(mRunnable);
		if (onRecordListener != null)
			onRecordListener.onRecordStart();
		invalidate();
	}

	void stopRecord() {
		removeCallbacks(mRunnable);
		if (onRecordListener != null)
			onRecordListener.onRecordEnd();
	}

	/**
	 * 刷新画弧，显示录音进度
	 */
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			recordDuration = System.currentTimeMillis() - mStartTime;
			if (recordDuration >= MAX_DURATION) {
				removeCallbacks(mRunnable);
				if (onRecordListener != null) {
					onRecordListener.onRecordOverFlow();
				}
				return;
			}

			if (onRecordListener != null) {
				onRecordListener.onRecording(recordDuration);
			}
			int degree = (int) ((recordDuration * 1.0 / MAX_DURATION) * 360);
			mStartDegree = -90;
			mSweepDegree = degree;
			invalidate();
			postDelayed(mRunnable, 50);
		}
	};

	public void setOnSendClickListener(OnRecordListener listener) {
		this.onRecordListener = listener;
	}

	/**
	 * 回调接口
	 * 
	 */
	public interface OnRecordListener {

		// 点击发送
		void onRecordSend();

		// 开始录音
		void onRecordStart();

		// 录音结束
		void onRecordEnd();

		// 录音时间达到最大
		void onRecordOverFlow();

		// 录音时间显示接口
		void onRecording(long milSec);
	}

}
