package com.netease.framework.skin;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.netease.common.log.NTLog;
import com.netease.service.Utils.StackTraceUtil;

public class ViewManager implements IResources{
	public static final String TAG = ViewManager.class.getSimpleName();
	
	private static int[] resArrayBackground;
	private static int[] resArrayTextColor;
	private static int[] resArrayTextSize;
	private static int[] resArrayDivider;
	private static int[] resArraySrc;
	private static int[] resArrayDrawableLeft;
	private static int[] resArrayDrawableRight;
	private static int[] resArrayCBbg;
	private static int[] resArrayTextColorHint;
	static {
		resArrayBackground = new int[] { android.R.attr.background };
		resArrayTextColor = new int[] { android.R.attr.textColor };
		resArrayTextColorHint = new int[] { android.R.attr.textColorHint };
		resArrayTextSize = new int[] { android.R.attr.textSize };
		resArrayDivider = new int[] { android.R.attr.divider };
		resArraySrc = new int[] { android.R.attr.src };
		resArrayDrawableLeft = new int[] { android.R.attr.drawableLeft };
		resArrayDrawableRight = new int[] { android.R.attr.drawableRight };
		resArrayCBbg = new int[] { android.R.attr.button };
	}
	
	private IResources mSkinRes;
	private Resources mDefaultRes;
	private Context mContext = null;
	
	// 为了防止内存泄露使用WeakHashMap
	private WeakHashMap<View, ViewSkinResource> mViewResMap = null;

	public ViewManager(Context context, IResources skinRes, Resources res) {
		mContext = context;
		mSkinRes = skinRes;
		mDefaultRes = res;
		mViewResMap = new WeakHashMap<View, ViewSkinResource>();
	}
	
	public void setSkinRes(IResources skinRes) {
		mSkinRes = skinRes;
	}
	
	private int getResourceId(AttributeSet attrs, int[] checkAttrs) {
		if (null == attrs || null == checkAttrs || checkAttrs.length == 0) {
			return -1;
		}

		TypedArray a = mContext.obtainStyledAttributes(attrs, checkAttrs);
		TypedValue value = a.peekValue(0);

		int resId = -1;
		if (value != null) {
			resId = value.resourceId;
		}
		a.recycle();

		return resId;
	}
	
	public static void setBackgroundDrawable(View v, Drawable d) {
		if (null == v || null == d) {
			return;
		}

		int pl = v.getPaddingLeft();
		int pt = v.getPaddingTop();
		int pr = v.getPaddingRight();
		int pb = v.getPaddingBottom();
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			v.setBackground(d);
		} else {
			v.setBackgroundDrawable(d);
		}
		v.setPadding(pl, pt, pr, pb);
	}
	
	public ViewSkinResource addView(View v, AttributeSet attrs) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " v=" + v);
		}
		if (null == v || null == attrs) {
			return null;
		}
		
		ViewSkinResource resMap = new ViewSkinResource();
		int resId = -1;

		resId = getResourceId(attrs, resArrayBackground);
		if (resId > 0) {
			resMap.setBackgroundResId(resId);
		}

		if (v instanceof TextView) {
			resId = getResourceId(attrs, resArrayTextColor);
			if (resId > 0) {
				resMap.setTextColorResId(resId);
			}
			
			resId = getResourceId(attrs, resArrayTextColorHint);
			if (resId > 0) {
				resMap.setTextColorHintResId(resId);
			}
			
			resId = getResourceId(attrs, resArrayTextSize);
			if (resId > 0) {
				resMap.setTextSizeResId(resId);
			}
			
			resId = getResourceId(attrs, resArrayDrawableLeft);
			if (resId > 0) {
				resMap.setDrawableLeftResId(resId);
			}
			
			resId = getResourceId(attrs, resArrayDrawableRight);
			if (resId > 0) {
				resMap.setDrawableRightResId(resId);
			}
		} else if (v instanceof ListView) {
			resId = getResourceId(attrs, resArrayDivider);
			if (resId > 0) {
				resMap.setDividerResId(resId);
			}
		} else if (v instanceof ImageView) {
			resId = getResourceId(attrs, resArraySrc);
			if (resId > 0) {
				resMap.setSrcResId(resId);
			}
		} else if (v instanceof CheckBox) {
			resId = getResourceId(attrs, resArrayCBbg);
			if (resId > 0) {
				resMap.setCheckBoxBtnResId(resId);
			}
		} 
		
		synchronized (mViewResMap) {
			mViewResMap.put(v, resMap);
		}
		
		return resMap;
	}
	
	public void setViewSkin(View v, ViewSkinResource resMap) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " v=" + v);
		}
		if (null == v || null == resMap) {
			return;
		}

		int id = -1;
		id = resMap.getBackgroundResId();
		if (id > 0) {
			Drawable d = getDrawable(id);
			setBackgroundDrawable(v, d);
		}
		
		if (v instanceof TextView) {
			id = resMap.getTextColorResId();
			if (id > 0) {
				// 由于textcolor是可能用selector，这里要进行保护
				int color = getColor(id);
				if (color < 0 ) {
					ColorStateList colors = getColorStateList(id);
					if (null != colors) {
						((TextView) v).setTextColor(colors);
					}
				} else {
					((TextView) v).setTextColor(id);
				}
			}
			
			id = resMap.getTextColorHintResId();
			if (id > 0) {
				((TextView) v).setHintTextColor(getColor(id));
			}
			
			id = resMap.getTextSizeResId();
			if (id > 0) {
				// 注意这里一定要使用TypedValue.COMPLEX_UNIT_PX进行设置，因为
				// getDimension读取出来是像素值
				((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimension(id));
			}
			
			id = resMap.getDrawableLeftResId();
			int id2 = resMap.getDrawableRightResId();
			if (id > 0 && id2 > 0) {
				((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
						getDrawable(id), null, getDrawable(id2), null);
			} else {
				if (id > 0) {
					((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
							getDrawable(id), null, null, null);
				}
				
				if (id2 > 0) {
					((TextView) v).setCompoundDrawablesWithIntrinsicBounds(
							null, null, getDrawable(id2), null);
				}
			}
		} else if (v instanceof ListView) {
			id = resMap.getDividerResId();
			if (id > 0) {
				((ListView) v).setDivider(getDrawable(id));
			}
		} else if (v instanceof ImageView) {
			id = resMap.getSrcResId();
			if (id > 0) {
				((ImageView) v).setImageDrawable(getDrawable(id));
			}
		} else if (v instanceof CheckBox) {
			id = resMap.getCheckBoxBtnResId();
			if (id > 0) {
				((CheckBox) v).setButtonDrawable(getDrawable(id));
			}
		} 
	}
	
	public void onSkinChange() {
		synchronized (mViewResMap) {
			Set<Entry<View, ViewSkinResource>> keys = mViewResMap.entrySet();
			Iterator<Entry<View, ViewSkinResource>> iter = keys.iterator();
			Map.Entry<View, ViewSkinResource> en = null;
			View key = null;
			ViewSkinResource value = null;

			while (iter.hasNext()) {
				en = iter.next();
				key = (View) en.getKey();
				value = (ViewSkinResource) en.getValue();

				if (null == value) {
					continue;
				}

				setViewSkin(key, value);
			}
		}
	}
	
	@Override
	public Drawable getDrawable(int id) throws Resources.NotFoundException {
		Drawable d = null;
		if (null == mSkinRes) {
			d = mDefaultRes.getDrawable(id);
			return d;
		}

		try {
			d = mSkinRes.getDrawable(id);
		} catch (Resources.NotFoundException e) {
			d = mDefaultRes.getDrawable(id);
		}

		return null == d ? mDefaultRes.getDrawable(id) : d;
	}

	@Override
	public int getColor(int id) throws Resources.NotFoundException {
		int color = -1;
		if (null == mSkinRes) {
			color = mDefaultRes.getColor(id);
			return color;
		}

		try {
			color = mSkinRes.getColor(id);
		} catch (Resources.NotFoundException e) {
			color = mDefaultRes.getColor(id);
		}

		return color;
	}

	@Override
	public ColorStateList getColorStateList(int id) {
		ColorStateList list = null;
		if (null == mSkinRes) {
			list = mDefaultRes.getColorStateList(id);
			return list;
		}

		try {
			list = mSkinRes.getColorStateList(id);
		} catch (Resources.NotFoundException e) {
			list = mDefaultRes.getColorStateList(id);
		}

		return list;
	}

	@Override
	public float getDimension(int id) throws NotFoundException {
		
		float dim = -1;
		if (null == mSkinRes) {
			dim = mDefaultRes.getDimension(id);
			return dim;
		}

		try {
			dim = mSkinRes.getDimension(id);
		} catch (Resources.NotFoundException e) {
			dim =  mDefaultRes.getDimension(id);
		}

		return dim;
	}
	
	@Override
	public boolean getBoolean(int id) throws NotFoundException {
		boolean b = false;
		if (null == mSkinRes) {
			b = mDefaultRes.getBoolean(id);
			return b;
		}

		try {
			b = mSkinRes.getBoolean(id);
		} catch (Resources.NotFoundException e) {
			b =  mDefaultRes.getBoolean(id);
		}

		return b;
	}
	
	@Override
	public int getInteger(int id) throws NotFoundException {
		int i = 0;
		if (null == mSkinRes) {
			i = mDefaultRes.getInteger(id);
			return i;
		}

		try {
			i = mSkinRes.getInteger(id);
		} catch (Resources.NotFoundException e) {
			i =  mDefaultRes.getInteger(id);
		}

		return i;
	}
	
	@Override
	public int getIdentifier(int id) {
		if (null == mSkinRes) {
			return id;
		}

		int newId = mSkinRes.getIdentifier(id);
		if (newId <= 0) {
			return id;
		}

		return newId;
	}

	public void destroy() {
		mContext = null;
		mSkinRes = null;
		mDefaultRes = null;
		if (null != mViewResMap) {
			mViewResMap.clear();
		}
		mViewResMap = null;
	}
}
