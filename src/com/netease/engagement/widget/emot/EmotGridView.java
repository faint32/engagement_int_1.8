package com.netease.engagement.widget.emot;

import java.util.List;

import com.netease.date.R;
import com.netease.service.Utils.EgmUtil;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * 抽象类，用于装载表情
 */
public class EmotGridView extends GridView {

	//通过计算得到高度
	public static int GridView_Height ;
	// 包含删除键
	public static int CHILD_NUM = 32;

	/* 表情图标索引的列表 */
	private List<String> mEmoNameList;

	// 这个grid要显示的page no
	private int mPageNo;

	/* 表情View被Click的监听 */
	private OnEmoticonClickListener mListener;

	private EmotAdapter mAdapter;
	
	private int mClounNum = 8 ;
	
	private int mItemWidth ;
	
	private int mImagePadding = 25 ;
	
	public EmotGridView(Context context) {
		super(context);
		init(context);
	}

	public EmotGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EmotGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setSelector(this.getContext().getResources().getDrawable(R.drawable.icon_emot_selector));
		setGravity(Gravity.CENTER);
		setNumColumns(mClounNum);
		setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

		mItemWidth = getContext().getResources().getDisplayMetrics().widthPixels/mClounNum ;
		mImagePadding = EgmUtil.dip2px(getContext(),mImagePadding);

		mAdapter = new EmotAdapter(context);
		setAdapter(mAdapter);

		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				if (position == CHILD_NUM - 1 && mListener != null) {// 点击删除键
					mListener.onClick(null, true);
				}
				EmotAdapter adapter = (EmotAdapter) parent.getAdapter();
				String info = adapter.getItem(position);
				if (info != null && mListener != null) {// 点击表情
					mListener.onClick(info, false);
				}
			}
		});
	}

	public void setEmoticonList(List<String> emotNameList) {
		mEmoNameList = emotNameList;
	}

	/**
	 * 设置页码
	 */
	public void setPageNo(int pageNo) {
		mPageNo = pageNo;
	}

	public void setOnEmoticonClickListener(OnEmoticonClickListener listener) {
		mListener = listener;
	}

	public interface OnEmoticonClickListener {
		public void onClick(String phrase, boolean isDelete);
	}

	private class EmotAdapter extends BaseAdapter {
		private GridView.LayoutParams lp ;
		private LayoutInflater inflater ;
		
		public EmotAdapter(Context context){
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return CHILD_NUM;
		}

		@Override
		public String getItem(int position) {
			if (position == CHILD_NUM - 1) {// 最右下角的格子
				return null;
			}
			// 计算这个item在List中的位置
			int index = (CHILD_NUM - 1) * mPageNo + position;
			if (mEmoNameList != null) {
				if (index >= 0 && index < mEmoNameList.size()) {
					return mEmoNameList.get(index);
				}
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_view_emot_list, null);
			}
			ImageView emotIcon = (ImageView)convertView.findViewById(R.id.emot);
			
			lp = new GridView.LayoutParams(mItemWidth,mItemWidth);
			convertView.setLayoutParams(lp);
			
			String phraseInfo = getItem(position);
			emotIcon.setEnabled(true);
			
			if (!TextUtils.isEmpty(phraseInfo)) {// 格子里面是有内容的
				emotIcon.setImageResource(EmoticonMgr.getInstance(getContext()).getIcon(phraseInfo));
				emotIcon.setTag(phraseInfo);
				
			} else if (position == CHILD_NUM - 1) {// 最右下角，显示一个删除按钮
				emotIcon.setImageResource(R.drawable.icon_keyboard_face_delete);
				emotIcon.setTag(null);
				
			} else {
				emotIcon.setImageResource(0);
				emotIcon.setTag(null);
				emotIcon.setBackgroundResource(0);
				emotIcon.setEnabled(false);
			}
			return convertView;
		}

		/**
		 * 用于控制EmoticonGrid中的空格子不能选择
		 */
		@Override
		public boolean isEnabled(int position) {
			if (position == CHILD_NUM - 1) {// 最右下角的格子
				return true;
			} else {
				String info = getItem(position);
				return info != null;
			}
		}
	}
}
