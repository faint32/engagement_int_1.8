package com.netease.android.widget.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;

import com.netease.android.util.DpAndPxUtils;
import com.netease.date.R;

public class LofterPopupMenu extends PopupWindow {

    private View window;
    private ViewGroup menu;
    private ViewGroup actionList;
    private Button closeBtn;
    private Context context;

    private Animation hideAnim;

    private List<Item> items = new ArrayList<Item>();

    public LofterPopupMenu(Context context) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        window = inflater.inflate(R.layout.video_lofter_popup_window, null);
        setContentView(window);

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);

        ColorDrawable dw = new ColorDrawable(0xb0000000);
        setBackgroundDrawable(dw);

        hideAnim = AnimationUtils.loadAnimation(context, R.anim.video_popup_window_hide);
        final View view = window;
        hideAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	view.post(new Runnable() {
					@Override
					public void run() {
						LofterPopupMenu.super.dismiss();
					}
				});
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        menu = (ViewGroup) window.findViewById(R.id.popup_menu);

        actionList = (ViewGroup) window.findViewById(R.id.action_list);

        closeBtn = (Button) window.findViewById(R.id.close_btn);
        closeBtn.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
        closeBtn.getPaint().setFakeBoldText(true);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void show() {
    	actionList.removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.view != null) {
                addSplit(i == 0);
                actionList.addView(item.view, new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                Button view = new Button(actionList.getContext());
                if (i == items.size() - 1) {
                    if (i == 0) {
                        view.setBackgroundResource(R.drawable.video_popup_window_selector_single);
                    } else {
                        view.setBackgroundResource(R.drawable.video_popup_window_selector_last);
                    }
                } else {
                    if (i != 0) {
                        view.setBackgroundResource(R.drawable.video_popup_window_selector);
                    } else {
                        view.setBackgroundResource(R.drawable.video_popup_window_selector_first);
                    }
                }
                view.setText(item.text);
                view.setTextColor(context.getResources().getColor(item.textColor));
                view.setTextSize(18);
                addSplit(i == 0);
                actionList.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, DpAndPxUtils.dip2px(44)));
                view.setOnClickListener(item.listener);
            }
        }
        menu.setAnimation(AnimationUtils.loadAnimation(context, R.anim.video_popup_window_show));
        showAtLocation(window, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        window.requestFocus();
        
    }

    public void addMenuItem(String text, View.OnClickListener listener) {
        items.add(new Item(text, R.color.popup_btn_font, listener));
    }

    public void addMenuItem(String text, View.OnClickListener listener, int textColor) {
        items.add(new Item(text, textColor, listener));
    }

    public void addMenuItem(View view) {
        items.add(new Item(view));
    }

    private void addSplit(boolean isFirst) {
        if (!isFirst) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View split = inflater.inflate(R.layout.video_popup_window_split, null);
            actionList.addView(split, new LayoutParams(LayoutParams.MATCH_PARENT, DpAndPxUtils.dip2px(1)));
        }
    }

    private static class Item {
        String text;
        int textColor;
        View.OnClickListener listener;
        View view;

        Item(String text, int textColor, View.OnClickListener listener) {
            this.text = text;
            this.textColor = textColor;
            this.listener = listener;
        }

        Item(View view) {
            this.view = view;
        }
    }

    @Override
    public void dismiss() {
        menu.startAnimation(hideAnim);
    }
}
