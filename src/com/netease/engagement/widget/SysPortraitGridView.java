package com.netease.engagement.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.common.ui.view.ViewHolder;
import com.netease.date.R;
import com.netease.service.protocol.meta.SysPortraitInfo;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 系统头像表格
 * @author gordondu
 *
 */
public class SysPortraitGridView extends GridView {

	// 每页个数
	public static final int PAGE_SIZE = 8;
	// 列数
	private static final int COLUMN_NUM = 4;
	// 当前在哪一页，从0开始
	private int mPageNum = 0;
	// 总页数
	private int mTotalPage = 0;
	private int mTotalItem = 0;
	
	private int mDiameter;
	private RelativeLayout.LayoutParams mCircleImageLayoutParams;

	private SysPortraitGridAdapter mAdapter;
	private SysPortraitInfo[] mSysPortraitInfoList;

	public SysPortraitGridView(Context context, SysPortraitInfo[] sysPortraitInfoList) {
		super(context);
		mSysPortraitInfoList = sysPortraitInfoList;
		this.setSelector(R.drawable.gridview_sysportrait_selector);
		this.setGravity(Gravity.CENTER);
		this.setNumColumns(COLUMN_NUM);
		mTotalItem = mSysPortraitInfoList == null ? 0 : mSysPortraitInfoList.length;
		mTotalPage = Math.round(Float.valueOf(mTotalItem) / PAGE_SIZE);
		mDiameter = getContext().getResources().getDimensionPixelSize(R.dimen.pripage_sysportrait_width)
				- getContext().getResources().getDimensionPixelSize(R.dimen.info_margin_20dp);
		mCircleImageLayoutParams = new RelativeLayout.LayoutParams(mDiameter, mDiameter);
		mAdapter = new SysPortraitGridAdapter();
		this.setAdapter(mAdapter);
		this.setOnItemClickListener(mOnItemClickListener);
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> root, View view, int position, long id) {
			ViewPager viewPager = (ViewPager) root.getParent();
			int listPos = viewPager.getCurrentItem() * PAGE_SIZE + position;
			resetSysPortraitChoosed(listPos);
			refreshAllAdapter(viewPager);
		}
	};
	
	private void resetSysPortraitChoosed(int pos) {
		if (mSysPortraitInfoList != null) {
			for (int i=0; i<mSysPortraitInfoList.length; i++) {
				mSysPortraitInfoList[i].choosed = false;
			}
			mSysPortraitInfoList[pos].choosed = true;
		}
	}
	
	private void refreshAllAdapter(ViewPager viewPager) {
		for(int i=0 ;i < viewPager.getChildCount();i++){
			((SysPortraitGridView)viewPager.getChildAt(i)).getAdapter().notifyDataSetChanged();
		}
	}

	public void setPageNum(int pageNum) {
		mPageNum = pageNum;
	}

	public SysPortraitGridAdapter getAdapter() {
		if (mAdapter != null) {
			return mAdapter;
		}
		return null;
	}

	public class SysPortraitGridAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public SysPortraitGridAdapter() {
			inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return (mPageNum + 1) < mTotalPage ? PAGE_SIZE : (mTotalItem - mPageNum * PAGE_SIZE);
		}

		@Override
		public Object getItem(int position) {
			int pos = mPageNum * PAGE_SIZE + position;
			SysPortraitInfo obj = null;
			if (mSysPortraitInfoList != null && pos < mSysPortraitInfoList.length) {
				obj = mSysPortraitInfoList[pos];
			}
			return obj;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CircleImageView sysPortraitImg = null;
			TextView choosedTag = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.view_item_sysportrait_grid, null);
				sysPortraitImg = ViewHolder.get(convertView, R.id.sysportrait);
				sysPortraitImg.setLayoutParams(mCircleImageLayoutParams);
			} else {
				sysPortraitImg = ViewHolder.get(convertView, R.id.sysportrait);
			}
			choosedTag = ViewHolder.get(convertView, R.id.choosed_tag);
			choosedTag.setVisibility(View.GONE);
			int pos = mPageNum * PAGE_SIZE + position;
			if (mSysPortraitInfoList != null && pos < mSysPortraitInfoList.length) {
				SysPortraitInfo sysPortrait = mSysPortraitInfoList[pos];
				sysPortraitImg.setTag(new ImageViewAsyncCallback(sysPortraitImg, sysPortrait.url));
				if (sysPortrait.choosed) {
					choosedTag.setVisibility(View.VISIBLE);
				}
			}
			return convertView;
		}
	}
}
