package com.netease.engagement.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;

import com.handmark.pulltorefresh.compat.LoadingListView;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityImageBrowser;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.ProgerssImageView;
import com.netease.service.protocol.meta.PictureInfo;

/**
 * 男性会员公开照片列表
 */
public class FragmentImageListMan extends FragmentBase{
	
	public static FragmentImageListMan newInstance(long userid,
			ArrayList<PictureInfo> picList, int position){
		FragmentImageListMan fragment = new FragmentImageListMan();
		Bundle args = new Bundle();
		args.putLong(EgmConstants.BUNDLE_KEY.USER_ID, userid);
		args.putParcelableArrayList(EgmConstants.BUNDLE_KEY.IMAGE_URL, picList);
		args.putInt(EgmConstants.BUNDLE_KEY.POSITION, position);
		fragment.setArguments(args);
		return fragment ;
	}

	private CustomActionBar mCustomActionBar ;
	private LoadingListView mLoadingListView ;
	private ImageListAdapter mAdapter ;
	ArrayList<PictureInfo> mPicList ;
	private int position;
	private long mUserId;
	
	private int mScreenWidth ;
	private int mScreenHeight ;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if(args == null || args.getStringArrayList(EgmConstants.BUNDLE_KEY.IMAGE_URL) == null){
			return ;
		}
		
		mUserId = args.getLong(EgmConstants.BUNDLE_KEY.USER_ID);
		mPicList = args.getParcelableArrayList(EgmConstants.BUNDLE_KEY.IMAGE_URL);
		position = args.getInt(EgmConstants.BUNDLE_KEY.POSITION, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mLoadingListView = (LoadingListView) inflater.inflate(R.layout.view_loading_list,container,false);
		mLoadingListView.disableLoadingMore();
		mLoadingListView.disablePullToRefresh();
		mLoadingListView.setOnItemClickListener(mOnItemClickListener);
		mScreenWidth = getResources().getDisplayMetrics().widthPixels ;
		mScreenHeight = getResources().getDisplayMetrics().heightPixels ;
		return mLoadingListView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        
		mCustomActionBar.setLeftBackgroundResource(R.drawable.titlebar_c_selector);
		mCustomActionBar.setLeftAction(R.drawable.bar_btn_back_b, R.string.back);
		mCustomActionBar.setLeftTitleColor(getResources().getColor(R.color.white));
        mCustomActionBar.hideMiddleTitle();
        mCustomActionBar.setRightVisibility(View.GONE);
		mAdapter = new ImageListAdapter(getActivity());
		mLoadingListView.setAdapter(mAdapter);
		mLoadingListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mLoadingListView.getRefreshableView().setSelection(position);
			}
		}, 200);
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			//男性公开照，点击列表项进入左右滑动图集
			int index = arg2 - mLoadingListView.getRefreshableView().getHeaderViewsCount();
			//进入图集
			ArrayList<PictureInfo> picInfos = new ArrayList<PictureInfo>();
			for(PictureInfo item : mPicList){
				picInfos.add(item);
			}
			ActivityImageBrowser.startActivity(getActivity(), picInfos, index, 
					false, mUserId, true);
		}
	};
	
	private int imageHeight ;
	
	private class ImageListAdapter extends BaseAdapter{
		private LayoutInflater mInflater ;
		private FrameLayout.LayoutParams lp ;
		public ImageListAdapter(Context context){
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			imageHeight = (int) context.getResources().getDimension(R.dimen.info_private_image_list_height);
			lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,imageHeight);
		}
		@Override
		public int getCount() {
			return mPicList.size();
		}

		@Override
		public Object getItem(int position) {
			return mPicList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder viewHolder;
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.view_item_list_public_man,null);
				viewHolder = new ViewHolder();
				viewHolder.mProgressIv = (ProgerssImageView) convertView.findViewById(R.id.image);
				viewHolder.mProgressIv.mImageView.setDefaultResId(R.drawable.icon_photo_loaded_fail);
				viewHolder.mProgressIv.mImageView.setLayoutParams(lp);
				viewHolder.mProgressIv.mImageView.setScaleType(ScaleType.CENTER_CROP);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.mProgressIv.mImageView.setLoadingImage(null);
			viewHolder.mProgressIv.mImageView.setServerClipSize(mScreenWidth, mScreenHeight);
			viewHolder.mProgressIv.mImageView.setLoadingImage(mPicList.get(position).picUrl);
			return convertView;
		}
		
		public class ViewHolder {
			ProgerssImageView mProgressIv;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
