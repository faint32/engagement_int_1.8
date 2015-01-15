package com.netease.engagement.widget.richtext.view;

import com.netease.engagement.widget.richtext.IRichText;
import com.netease.engagement.widget.richtext.RichTextParser;
import com.netease.engagement.widget.richtext.RichTextParser.OnRichTextMatchListener;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public abstract class BaseRichTextView extends TextView implements IRichText,
		OnRichTextMatchListener {

	protected RichTextParser mPaser;

	public BaseRichTextView(Context context) {
		this(context, null, 0);
	}

	public BaseRichTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BaseRichTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaser = new RichTextParser();
		mPaser.setOnRichTextMatchListener(this);
	}

	@Override
	public void setRichText(CharSequence str) {
		
		Spannable sp = null;
		if(str instanceof Spannable){
			sp = (Spannable)str ;
		}else{
			sp = new SpannableStringBuilder(str);
		}
		
		toRichText(sp);

		setMovementMethod(ClickMovementMethod.getInstance());
		setFocusable(false);
		setClickable(false);
		//setLongClickable(false);
		setLongClickable(true);
	}

	/*public void setText(String value) {
		super.setText(value);
		setMovementMethod(ClickMovementMethod.getInstance());
		setFocusable(false);
		setClickable(false);
		setLongClickable(false);
	}*/

	@Override
	public void addMatcher(String regular) {
		mPaser.addMatcher(regular);
	}

	@Override
	public void setRichTextEnable(boolean enable) {
		if (enable) {
			mPaser.setOnRichTextMatchListener(this);
		} else {
			mPaser.setOnRichTextMatchListener(null);
		}
	}

	@Override
	public void toRichText(Spannable sp) {
		setText(sp, BufferType.SPANNABLE);
		mPaser.parseRichText(sp);
	}

	/**
	 * 自定义对格式化字段文本的点击操作效果
	 */
	public static class ClickMovementMethod extends LinkMovementMethod {
		private static ClickMovementMethod sInstance;

		public static ClickMovementMethod getInstance() {
			if (sInstance == null)
				sInstance = new ClickMovementMethod();

			return sInstance;
		}

		@Override
		public boolean onTouchEvent(TextView widget, Spannable buffer,
				MotionEvent event) {
			int action = event.getAction();

			if (action == MotionEvent.ACTION_UP
					|| action == MotionEvent.ACTION_DOWN) {
				int x = (int) event.getX();
				int y = (int) event.getY();

				x -= widget.getTotalPaddingLeft();
				y -= widget.getTotalPaddingTop();

				x += widget.getScrollX();
				y += widget.getScrollY();

				Layout layout = widget.getLayout();
				int line = layout.getLineForVertical(y);
				int off = layout.getOffsetForHorizontal(line, x);

				ClickableSpan[] link = buffer.getSpans(off, off,
						ClickableSpan.class);

				if (link.length != 0) {
					if (action == MotionEvent.ACTION_UP) {
						link[0].onClick(widget); // 触发点击事件
					} else if (action == MotionEvent.ACTION_DOWN) {
						Selection.setSelection(
								buffer, // 选中（高亮）字段文本
								buffer.getSpanStart(link[0]),
								buffer.getSpanEnd(link[0]));
					}

					return true;
				} else {
					Selection.removeSelection(buffer); // 取消选中效果
				}
			}
			return false;
		}
	}
}
