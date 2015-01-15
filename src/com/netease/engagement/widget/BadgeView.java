package com.netease.engagement.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * 简单的标志控件。它可以依附到需要标记的目标控件上，即按指定的相对位置显示在需要标记的目标控件上。
 * 它没有别的复杂功能，仅仅是为做标记提供一个封装后的便捷：使用者只要直接传入需要标记的控件及其它一些参数，无需自己布局，即可完成标记。<p>
 * 注意：目标控件如果不是TabWidget，那么它会和标志一起被装到FrameLayout容器中，然后该容器取代目标控件。
 */
public class BadgeView extends TextView {
    public static final int POSITION_TOP_LEFT = 1;
    public static final int POSITION_TOP_RIGHT = 2;
    public static final int POSITION_BOTTOM_LEFT = 3;
    public static final int POSITION_BOTTOM_RIGHT = 4;
    public static final int POSITION_CENTER_VERTICAL_LEFT = 5;
    public static final int POSITION_CENTER_VERTICAL_RIGHT = 6;
    public static final int POSITION_CENTER_HORIZONTAL_TOP = 7;
    public static final int POSITION_CENTER_HORIZONTAL_BOTTOM = 8;
    
    private static final int DEFAULT_CORNER_RADIUS_DIP = 8;
    private static final int DEFAULT_POSITION = POSITION_TOP_RIGHT;
    private static final int DEFAULT_BADGE_COLOR = Color.RED;
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    
    private static Animation fadeIn;
    private static Animation fadeOut;
    
    private Context context;
    /** BadgeView所要依附的对象 */
    private View target;
    
    private int badgePosition;  // BadgeView在依附对象中的相对位置
    private int badgeMargin;    // 边距
    private int badgeColor;     // 背景颜色
    
    private boolean isShown;    // 当前BadgeView是否显示
    
    private ShapeDrawable badgeBg;  // 背景
    private int targetTabIndex;     // TabWidget的当前Tab index
    
    public BadgeView(Context context) {
        this(context, (AttributeSet) null, android.R.attr.textViewStyle);
    }
    
    public BadgeView(Context context, AttributeSet attrs) {
         this(context, attrs, android.R.attr.textViewStyle);
    }
    
    /**
     * Constructor -
     * 
     * create a new BadgeView instance attached to a target {@link android.view.View}.
     *
     * @param context context for this view.
     * @param target the View to attach the badge to.
     */
    public BadgeView(Context context, View target) {
         this(context, null, android.R.attr.textViewStyle, target, 0);
    }
    
    /**
     * Constructor -
     * 
     * create a new BadgeView instance attached to a target {@link android.widget.TabWidget}
     * tab at a given index.
     *
     * @param context context for this view.
     * @param target the TabWidget to attach the badge to.
     * @param index the position of the tab within the target.
     */
    public BadgeView(Context context, TabWidget target, int index) {
        this(context, null, android.R.attr.textViewStyle, target, index);
    }
    
    public BadgeView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, null, 0);
    }
    
    public BadgeView(Context context, AttributeSet attrs, int defStyle, View target, int tabIndex) {
        super(context, attrs, defStyle);
        
        setTextSize(0.1f);
        
        init(context, target, tabIndex);
    }

    private void init(Context context, View target, int tabIndex) {
        if(target.getParent() == null)
            throw new IllegalStateException("target view must have a parent view");
        
        this.context = context;
        this.target = target;
        this.targetTabIndex = tabIndex;
        
        // apply defaults
        badgePosition = DEFAULT_POSITION;
        badgeColor = DEFAULT_BADGE_COLOR;
        
        setTypeface(Typeface.DEFAULT_BOLD);
        setTextColor(DEFAULT_TEXT_COLOR);
        
        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(200);

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(200);
        
        isShown = false;
        
        if (this.target != null) {
            applyTo(this.target);
        } 
        else {
            show();
        }
    }

    /**
     * 依附到目标对象上
     * @param target 目标对象
     */
    private void applyTo(View target) {
        LayoutParams lp = target.getLayoutParams();
        ViewParent parent = target.getParent();
        FrameLayout container = new FrameLayout(context);
        
        if (target instanceof TabWidget) {  // 依附对象为Tab时，BadgeView装在一个FrameLayout里放到TabWidget的Tab上
            // set target to the relevant tab child container
            target = ((TabWidget) target).getChildTabViewAt(targetTabIndex);
            this.target = target;
            
            ((ViewGroup) target).addView(container, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            
            this.setVisibility(View.GONE);
            container.addView(this, new FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT));
        } 
        else {  // 其它依附对象，BadgeView和依附对象一起装在一个FrameLayout里取代依附对象
            if(lp instanceof LinearLayout.LayoutParams){
                LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams)lp;
                lp = new LinearLayout.LayoutParams(ll.width, ll.height, ll.weight);
                ((LinearLayout.LayoutParams) lp).bottomMargin = ll.bottomMargin;
                ((LinearLayout.LayoutParams) lp).leftMargin = ll.leftMargin;
                ((LinearLayout.LayoutParams) lp).rightMargin = ll.rightMargin;
                ((LinearLayout.LayoutParams) lp).topMargin = ll.topMargin;
            }
            
            // TODO verify that parent is indeed a ViewGroup
            ViewGroup group = (ViewGroup) parent; 
            int index = group.indexOfChild(target);
            
            group.removeView(target);
            group.addView(container, index, lp);    // 新容器替代目标对象
            
            container.addView(target, new FrameLayout.LayoutParams( // 新容器装载目标对象
                    android.widget.FrameLayout.LayoutParams.FILL_PARENT,
                    android.widget.FrameLayout.LayoutParams.FILL_PARENT));
    
            this.setVisibility(View.GONE);
            container.addView(this, new FrameLayout.LayoutParams(   // 新容器装载标志图像
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT));
            
            group.invalidate();
        }
    }
    
    /**
     * Make the badge visible in the UI.
     */
    public void show() {
        show(false, null);
    }
    
    /**
     * Make the badge visible in the UI.
     * @param animate flag to apply the default fade-in animation.
     */
    public void show(boolean animate) {
        show(animate, fadeIn);
    }
    
    /**
     * Make the badge visible in the UI.
     * @param anim Animation to apply to the view when made visible.
     */
    public void show(Animation anim) {
        show(true, anim);
    }
    
    /**
     * Make the badge non-visible in the UI.
     */
    public void hide() {
        hide(false, null);
    }
    
    /**
     * Make the badge non-visible in the UI.
     * @param animate flag to apply the default fade-out animation.
     */
    public void hide(boolean animate) {
        hide(animate, fadeOut);
    }
    
    /**
     * Make the badge non-visible in the UI.
     * @param anim Animation to apply to the view when made non-visible.
     */
    public void hide(Animation anim) {
        hide(true, anim);
    }
    
    /**
     * 根据当前情况切换BadgeView的显示和隐藏，例如当前显示，调用后则隐藏
     */
    public void toggle() {
        toggle(false, null, null);
    }
    
    /**
     * 根据当前情况切换BadgeView的显示和隐藏，例如当前显示，调用后则隐藏
     * @param animate flag to apply the default fade-in/out animation.
     */
    public void toggle(boolean animate) {
        toggle(animate, fadeIn, fadeOut);
    }
    
    /**
     * 根据当前情况切换BadgeView的显示和隐藏，例如当前显示，调用后则隐藏
     * @param animIn Animation to apply to the view when made visible.
     * @param animOut Animation to apply to the view when made non-visible.
     */
    public void toggle(Animation animIn, Animation animOut) {
        toggle(true, animIn, animOut);
    }
    
    /**
     * 根据指定的位置显示BadgeView，有动画则带动画
     * @param animate
     * @param anim
     */
    private void show(boolean animate, Animation anim) {
        if (getBackground() == null) {
            if (badgeBg == null) {
                badgeBg = getDefaultBackground();
            }
            setBackgroundDrawable(badgeBg);
        }
        applyLayoutParams();
        
        if (animate) {
            this.startAnimation(anim);
        }
        this.setVisibility(View.VISIBLE);
        isShown = true;
    }
    
    /**
     * 隐藏BadgeView，有动画则带动画
     * @param animate
     * @param anim
     */
    private void hide(boolean animate, Animation anim) {
        this.setVisibility(View.GONE);
        if (animate) {
            this.startAnimation(anim);
        }
        isShown = false;
    }
    
    /**
     * 根据当前情况切换BadgeView的显示和隐藏，例如当前显示，调用后则隐藏。
     * @param animate
     * @param animIn
     * @param animOut
     */
    private void toggle(boolean animate, Animation animIn, Animation animOut) {
        if (isShown) {
            hide(animate && (animOut != null), animOut);    
        } else {
            show(animate && (animIn != null), animIn);
        }
    }
    
    /**
     * 获取默认背景
     * @return
     */
    private ShapeDrawable getDefaultBackground() {
        int r = dipToPixels(DEFAULT_CORNER_RADIUS_DIP);
        float[] outerR = new float[] {r, r, r, r, r, r, r, r};
        
        RoundRectShape rr = new RoundRectShape(outerR, null, null);
        ShapeDrawable drawable = new ShapeDrawable(rr);
        drawable.getPaint().setColor(badgeColor);
        
        return drawable;
    }
    
    /**
     * 根据BadgeView被指定的位置设置LayoutParams
     */
    private void applyLayoutParams() {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        switch (badgePosition) {
        case POSITION_TOP_LEFT:
            lp.gravity = Gravity.LEFT | Gravity.TOP;
            lp.setMargins(badgeMargin, badgeMargin, 0, 0);
            break;
        case POSITION_TOP_RIGHT:
            lp.gravity = Gravity.RIGHT | Gravity.TOP;
            lp.setMargins(0, badgeMargin, badgeMargin, 0);
            break;
        case POSITION_BOTTOM_LEFT:
            lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
            lp.setMargins(badgeMargin, 0, 0, badgeMargin);
            break;
        case POSITION_BOTTOM_RIGHT:
            lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            lp.setMargins(0, 0, badgeMargin, badgeMargin);
            break;
        case POSITION_CENTER_VERTICAL_LEFT:
            lp.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            lp.setMargins(badgeMargin, 0, 0, 0);
            break;
        case POSITION_CENTER_VERTICAL_RIGHT:
            lp.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            lp.setMargins(0, 0, badgeMargin, 0);
            break;
        case POSITION_CENTER_HORIZONTAL_BOTTOM:
            lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            lp.setMargins(0, 0, 0, badgeMargin);
            break;
        case POSITION_CENTER_HORIZONTAL_TOP:
            lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            lp.setMargins(0, badgeMargin, 0, 0);
            break;
        default:
            break;
        }
        
        setLayoutParams(lp);
    }

    /**
     * Returns the target View this badge has been attached to.
     */
    public View getTarget() {
        return target;
    }

    /**
     * Is this badge currently visible in the UI?
     */
    @Override
    public boolean isShown() {
        return isShown;
    }

    /**
     * Returns the positioning of this badge.
     * one of POSITION_TOP_LEFT, POSITION_TOP_RIGHT, POSITION_BOTTOM_LEFT, POSITION_BOTTOM_RIGHT.
     */
    public int getBadgePosition() {
        return badgePosition;
    }

    /**
     * Set the positioning of this badge.
     * @param layoutPosition one of POSITION_TOP_LEFT, POSITION_TOP_RIGHT, POSITION_BOTTOM_LEFT, POSITION_BOTTOM_RIGHT.
     */
    public void setBadgePosition(int layoutPosition) {
        this.badgePosition = layoutPosition;
    }

    /**
     * Returns the horizontal/vertical margin from the target View that is applied to this badge.
     */
    public int getBadgeMargin() {
        return badgeMargin;
    }

    /**
     * Set the horizontal/vertical margin from the target View that is applied to this badge.
     * @param badgeMargin the margin in pixels.
     */
    public void setBadgeMargin(int badgeMargin) {
        this.badgeMargin = badgeMargin;
    }
    
    /**
     * Returns the color value of the badge background.
     */
    public int getBadgeBackgroundColor() {
        return badgeColor;
    }

    /**
     * Set the color value of the badge background.
     * @param badgeColor the badge background color.
     */
    public void setBadgeBackgroundColor(int badgeColor) {
        this.badgeColor = badgeColor;
        badgeBg = getDefaultBackground();
    }
    
    private int dipToPixels(int dip) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        return (int) px;
    }
}
