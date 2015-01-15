package com.netease.engagement.widget.emot;

import java.util.ArrayList;

import com.netease.date.R;
import com.netease.engagement.view.PagerIndicator;
import com.netease.engagement.widget.emot.EmotGridView.OnEmoticonClickListener;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * 表情键盘
 */
public class EmoticonView extends LinearLayout {
	// 表情组名
	public static String[] mEmoticon_group_name;

	private ViewPager mEmoticonPager;
	private PagerAdapter[] mEmoticonPagerAdapters;
	// 表情分组
	private RadioGroup mEmotGroup;
	private EmoticonPagerChangeListener mEmoticionPagerChangeListener;

	// 表情按下事件监听器
	private OnEmoticonClickListener mListener;

	private PagerIndicator mPagerIndicator;
	private RadioGroup.LayoutParams lp;

	public EmoticonView(Context context) {
		super(context);
		init();
	}

	public EmoticonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	/**
	 * 初始化布局
	 */
	private void init() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.emoticon_view, this, true);

		mEmoticon_group_name = EmoticonMgr.EMOTICON_GROUP_NAME;

		if(mEmoticon_group_name == null || mEmoticon_group_name.length == 0){
			return ;
		}
		
		mEmoticonPagerAdapters = new PagerAdapter[mEmoticon_group_name.length];

		mEmoticonPager = (ViewPager) findViewById(R.id.pager);
		
		mEmoticionPagerChangeListener = new EmoticonPagerChangeListener();
		mEmoticonPager.setOnPageChangeListener(mEmoticionPagerChangeListener);

		mPagerIndicator = (PagerIndicator) findViewById(R.id.indicator);

		mEmotGroup = (RadioGroup) findViewById(R.id.emoticon_group);
		mEmotGroup.setOnCheckedChangeListener(mOnCheckChangeListener);

		if (mEmoticon_group_name.length >= 2) {
			mEmotGroup.setVisibility(View.VISIBLE);
			mEmotGroup.removeAllViews();
			lp = new RadioGroup.LayoutParams(0, LayoutParams.MATCH_PARENT);
			lp.weight = 1.0f;
			for (int i = 0; i < mEmoticon_group_name.length; i++) {
				RadioButton rb = (RadioButton) inflater.inflate(R.layout.view_item_radio_button, null);
				rb.setId(i);
				rb.setText(mEmoticon_group_name[i]);
				mEmotGroup.addView(rb, lp);
			}
		}
		mEmotGroup.check(0);
	}

	private OnCheckedChangeListener mOnCheckChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			int index = checkedId;
			if (index < 0 || index > mEmoticon_group_name.length - 1) {
				return;
			}
			if (mEmoticonPagerAdapters[index] == null) {
				mEmoticonPagerAdapters[index] = new EmoticonPagerAdapter(
						mEmoticon_group_name[index]);
			}
			mEmoticonPager.setAdapter(mEmoticonPagerAdapters[index]);
			if(mEmoticonPagerAdapters[index].getCount() > 1){
				mPagerIndicator.setCount(mEmoticonPagerAdapters[index].getCount());
				mPagerIndicator.setVisibility(View.VISIBLE);
				mPagerIndicator.setCurrentItem(0);
			}
		}
	};

	public void setOnEmoticonClickListener(OnEmoticonClickListener l) {
		mListener = l;
	}

	private final class EmoticonPagerChangeListener extends
			ViewPager.SimpleOnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
			mPagerIndicator.setCurrentItem(position);
		}
	}

	private final class EmoticonPagerAdapter extends PagerAdapter {
		private ArrayList<String> mEmoList;

		public EmoticonPagerAdapter(String groupName) {
			this.mEmoList = EmoticonMgr.getInstance(getContext()).getNamesByGroup(groupName);
		}

		@Override
		public int getCount() {
			if (mEmoList != null) {
				int size = mEmoList.size();
				int pageNum = size / (EmotGridView.CHILD_NUM - 1);
				if (size % (EmotGridView.CHILD_NUM - 1) == 0) {
					return pageNum;
				} else {
					return pageNum + 1;
				}
			}
			return 0;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			EmotGridView gridView = new EmotGridView(getContext());
			gridView.setEmoticonList(mEmoList);
			gridView.setPageNo(position);
			container.addView(gridView);
			gridView.setOnEmoticonClickListener(mListener);
			return gridView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}
}
