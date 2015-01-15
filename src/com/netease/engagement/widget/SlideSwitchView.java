package com.netease.engagement.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.date.R;

/**
 * @author lishang SlideSwitcher：
 * 
 *         原理：为了支持滑动的时候，左右TextView渐变，采用了自定义布局，并利用3个TextView实现
 *         其中一个作为滑块背景。另两个作为显示。
 *         采用了属性动画，为了防止点击过快，动画混乱，一个动画未结束的时候，必须手动end才能开启下一轮动画
 *         可以设置滑动的持续时间、文字渐隐、渐现的持续时间 支持属性中设置文字颜色
 * 
 */
public class SlideSwitchView extends RelativeLayout {

	private View rootView;

	/**
	 * 背景滑块
	 */
	TextView bgTextView;
	/**
	 * 左侧显示文字
	 */
	TextView lefTextView;
	/**
	 * 右侧显示文字
	 */
	TextView righTextView;
	
	View contentView;

	private int selectColor;
	private int unSelectColor;
	private int disapperingDuration;
	private int appearingDuration;
	private int slideDuration;
	
	/**
	 * 相关动画
	 */
	private ObjectAnimator slideAnimation;
	private ObjectAnimator dispearingAnimatorL;
	private ObjectAnimator appearingAnimatorL;
	private ObjectAnimator dispearingAnimatorR;
	private ObjectAnimator appearingAnimatorR;
	

	/**
	 * 当期位置
	 */
	private boolean mPosition;
	private GestureDetector.SimpleOnGestureListener mGestureListener;
	private GestureDetector mDetector;
	/**
	 * 监听，切换完成时的处理
	 */
	private StateChangerListener stateChangerListener;
	private AnimatorSet setL;
	private AnimatorSet setR;
	private AnimatorSet animatorSet;

	public interface Position {
		boolean RIGHT = true;
		boolean LEFT = false;
	}

	public interface StateChangerListener {
		void onStateChanged(boolean position);
	}

	public SlideSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		init(context, attrs);

	}

	public SlideSwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public SlideSwitchView(Context context) {
		super(context);
		init(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 *            初始化，主要设置文字颜色、动画时长
	 */
	public void init(Context context, AttributeSet attrs) {

		mPosition = Position.LEFT;
		rootView = LayoutInflater.from(context).inflate(
				R.layout.view_slide_switch_layout, this, true);

		bgTextView = (TextView) rootView.findViewById(R.id.bg_purple);
		lefTextView = (TextView) rootView.findViewById(R.id.left_text);
		righTextView = (TextView) rootView.findViewById(R.id.right_text);
		contentView =rootView.findViewById(R.id.content);
		
		selectColor = Color.WHITE;
		unSelectColor = Color.GRAY;
		disapperingDuration = 100;
		appearingDuration = 100;
		slideDuration = 200;

		if (attrs != null) {
			TypedArray tArray = context.obtainStyledAttributes(attrs,
					R.styleable.slide_switch);// 获取配置属性
			lefTextView.setText(tArray
					.getString(R.styleable.slide_switch_left_text));
			righTextView.setText(tArray
					.getString(R.styleable.slide_switch_right_text));
			selectColor = tArray.getColor(
					R.styleable.slide_switch_select_color, selectColor);
			unSelectColor = tArray.getColor(
					R.styleable.slide_switch_unselect_color, unSelectColor);

			lefTextView.setTextColor(selectColor);
			righTextView.setTextColor(unSelectColor);
		}

		mGestureListener = new SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {

				setPositionState(!mPosition);
				return super.onSingleTapUp(e);
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				if (distanceX < -10)
					setPositionState(Position.RIGHT);
				if (distanceX > 10)
					setPositionState(Position.LEFT);
				
				return super.onScroll(e1, e2, distanceX, distanceY);
			}
			
		};
		mDetector = new GestureDetector(context, mGestureListener);

		setUpAnimation();
	};

	/**
	 * 创建属性动画
	 */
	private void setUpAnimation() {

		slideAnimation = new ObjectAnimator();
		slideAnimation.setTarget(bgTextView);
		dispearingAnimatorL = ObjectAnimator.ofFloat(lefTextView, "alpha",
				1.0f, 0.0f);
		appearingAnimatorL = ObjectAnimator.ofFloat(lefTextView, "alpha", 0.0f,
				1.0f);
		dispearingAnimatorR = ObjectAnimator.ofFloat(righTextView, "alpha",
				1.0f, 0.0f);
		appearingAnimatorR = ObjectAnimator.ofFloat(righTextView, "alpha",
				0.0f, 1.0f);

		dispearingAnimatorL.setDuration(disapperingDuration);
		appearingAnimatorL.setDuration(appearingDuration);
		dispearingAnimatorR.setDuration(disapperingDuration);
		appearingAnimatorR.setDuration(appearingDuration);
		slideAnimation.setDuration(slideDuration);

		setL = new AnimatorSet();
		setL.play(dispearingAnimatorL).before(appearingAnimatorL);
		setR = new AnimatorSet();
		setR.play(dispearingAnimatorR).before(appearingAnimatorR);
		animatorSet = new AnimatorSet();
		animatorSet.play(slideAnimation).with(setR).with(setL);

	}


	/**
	 * 重置动画，防止多个动画的混乱。如果有动画在运行，则结束它
	 */
	void endAllAnimation() {

		animatorSet.end();
		slideAnimation.end();
		dispearingAnimatorL.end();
		appearingAnimatorL.end();
		dispearingAnimatorR.end();
		appearingAnimatorR.end();


	}

	void removeAllListener() {

		animatorSet.removeAllListeners();
		slideAnimation.removeAllListeners();
		dispearingAnimatorL.removeAllListeners();
		appearingAnimatorL.removeAllListeners();
		dispearingAnimatorR.removeAllListeners();
		appearingAnimatorR.removeAllListeners();
	}
	/**
	 * @param position
	 *            利用属性动画设置动效 position=LEFT 向左滑动 position=RIGHT 向右滑动
	 */
	public void setPositionState(boolean position) {

		if (mPosition == position)
			return;
		mPosition = position;

		if (animatorSet.isRunning()) {
			endAllAnimation();
		}

		removeAllListener();
		
		if (mPosition == Position.RIGHT) {
			int right = bgTextView.getRight();
            int padding = contentView.getPaddingRight() + contentView.getPaddingLeft();
            
			slideAnimation.setPropertyName("x");
			slideAnimation.setTarget(bgTextView);
			slideAnimation.setFloatValues(0, getMeasuredWidth()-padding - right);

			appearingAnimatorL.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationStart(Animator animation) {
					super.onAnimationStart(animation);
					lefTextView.setTextColor(unSelectColor);
				}

			});
			appearingAnimatorR.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationStart(Animator animation) {
					super.onAnimationStart(animation);
					righTextView.setTextColor(selectColor);
				}

			});
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					lefTextView.setTextColor(unSelectColor);
					righTextView.setTextColor(selectColor);
					
					if(stateChangerListener!=null){
						stateChangerListener.onStateChanged(Position.RIGHT);
					}
				}
			});
			animatorSet.start();

		} else {

			int right = bgTextView.getRight();
            int padding = contentView.getPaddingRight() + contentView.getPaddingLeft();
            
			slideAnimation.setPropertyName("x");
			slideAnimation.setTarget(bgTextView);
            slideAnimation.setFloatValues(getMeasuredWidth() - padding - right, 0);

			appearingAnimatorL.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationStart(Animator animation) {
					super.onAnimationStart(animation);
					lefTextView.setTextColor(selectColor);
				}

			});
			appearingAnimatorR.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationStart(Animator animation) {
					super.onAnimationStart(animation);
					righTextView.setTextColor(unSelectColor);
				}

			});
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					righTextView.setTextColor(unSelectColor);
					lefTextView.setTextColor(selectColor);
					if(stateChangerListener!=null){
						stateChangerListener.onStateChanged(Position.LEFT);
					}
				}
			});
			animatorSet.start();

		}
	}

	/**
	 * @param stateChangerListener
	 *            设置切换完成监听
	 */
	public void setOnStateChangerListener(
			StateChangerListener stateChangerListener) {

		this.stateChangerListener = stateChangerListener;
	}

	/**
	 * @param stateChangerListener
	 *            设置渐隐动画时长
	 */
	public void setDisappearingDuration(int duration) {
		this.disapperingDuration = duration;
		dispearingAnimatorL.setDuration(disapperingDuration);
		dispearingAnimatorR.setDuration(disapperingDuration);
	}

	/**
	 * @param stateChangerListener
	 *            设置渐现动画时长
	 */
	public void setAppearingDuration(int duration) {
		this.appearingDuration = duration;

		appearingAnimatorL.setDuration(appearingDuration);
		appearingAnimatorR.setDuration(appearingDuration);

	}

	/**
	 * @param stateChangerListener
	 *            设置滑动动画时长
	 */
	public void setSlideDuration(int duration) {
		this.slideDuration = duration;
		slideAnimation.setDuration(duration);
	}

	/*
	 * 触摸事件的处理 利用GestureDector
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		mDetector.onTouchEvent(event);
		return true;
	}

	/* 拦截事件 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}
	
    public boolean getPostion() {
        return mPosition;
    }
    
    /**
     * 舍弃动画直接设置状态，为了兼容一些初始化的设置
     * @param position
     */
    public void setPositionStateInstance(boolean position) {
        if (position == Position.RIGHT) {
            int right = bgTextView.getRight();
            int padding = contentView.getPaddingRight() + contentView.getPaddingLeft();
            
            bgTextView.setTranslationX(getMeasuredWidth() - padding - right);
            righTextView.setTextColor(selectColor);
            lefTextView.setTextColor(unSelectColor);
        } else {
            bgTextView.setTranslationX(0);
            lefTextView.setTextColor(selectColor);
            righTextView.setTextColor(unSelectColor);
        }
        mPosition = position;
    }
    
 
    /**
     * 进度设定的支持，由左向右
     */
    public void setProgress(float percent) {
        int right = bgTextView.getRight();
        int padding = contentView.getPaddingRight() + contentView.getPaddingLeft();

        bgTextView.setTranslationX((getMeasuredWidth() - padding - right) * percent / 100);

        if (percent < 50) {
            lefTextView.setTextColor(selectColor);
            righTextView.setTextColor(unSelectColor);

            lefTextView.setAlpha(percent / 50);
            righTextView.setAlpha(percent / 50);
        } else {
            righTextView.setTextColor(selectColor);
            lefTextView.setTextColor(unSelectColor);

            lefTextView.setAlpha(percent / 50 - 1);
            righTextView.setAlpha(percent / 50 - 1);
        }

    }

    /**
     * 进度设定的支持，由右向左
     */
    public void setAntiProgress(float percent) {
        int right = bgTextView.getRight();
        int padding = contentView.getPaddingRight() + contentView.getPaddingLeft();

        bgTextView.setTranslationX((getMeasuredWidth() - padding - right) * (100 - percent) / 100);

        if (percent > 50) {
            lefTextView.setTextColor(selectColor);
            righTextView.setTextColor(selectColor);
            lefTextView.setAlpha(percent / 50);
            righTextView.setAlpha(percent / 50);
        } else {
            righTextView.setTextColor(selectColor);
            lefTextView.setTextColor(selectColor);
            lefTextView.setAlpha(percent / 50 - 1);
            righTextView.setAlpha(percent / 50 - 1);
        }

    }
    
}
