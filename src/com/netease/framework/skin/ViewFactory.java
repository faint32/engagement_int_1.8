package com.netease.framework.skin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.View;

public class ViewFactory implements Factory {
	private static final String[] sClassPrefixList;
	
	// 是否在皮肤设置项改变时通知已经生成的view进行皮肤切换
	private boolean mNeedSkinChange = true;
	private LayoutInflater _inflater;
	private SkinManager _skinMgr;
	private Context mContext;
//	private ViewFilterActionBar mFilterActionBar;
//	private ViewFilterContent mFilterContent;

	static {
		String[] arrayOfString = new String[3];
		arrayOfString[0] = "android.widget.";
		arrayOfString[1] = "android.view.";
		arrayOfString[2] = "android.webkit.";
		sClassPrefixList = arrayOfString;
	}
	
	public ViewFactory(Context context) {
		mContext = context;
		_skinMgr = SkinManager.getInstance(mContext.getApplicationContext());
		_skinMgr.setNeedSkinChange(mNeedSkinChange);
		// 由于使用了弱引用，每次都必须设置过滤器
//		mFilterActionBar = new ViewFilterActionBar();
//		mFilterContent = new ViewFilterContent();
//		_skinMgr.registerFilter(mFilterActionBar);
//		_skinMgr.registerFilter(mFilterContent);
	}

	public ViewFactory(Context context, boolean need) {
		this(context);
		mNeedSkinChange = need;
	}

	public void destroy() {
		mContext = null;
		_skinMgr = null;
		_inflater = null;
//		mFilterActionBar = null;
//		mFilterContent = null;
	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		View view = null;
		_inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		String[] prefixes = sClassPrefixList;
		int length = prefixes.length;
		int j = 0;

		while (null == view) {
			if (j >= length) {
				break;
			}
			try {
				if (-1 == name.indexOf('.')) {
					view = _inflater.createView(name, prefixes[j], attrs);
				} else {
					view = _inflater.createView(name, null, attrs);
				}
			} catch (InflateException e) {
				e.printStackTrace();
				throw e;
			} catch (ClassNotFoundException ee) {
				j++;
			}
		}

		if (null == view) {
			return null;
		}

		if (null != _skinMgr) {
			_skinMgr.addView(name, view, attrs);
		}

		return view;
	}
}
