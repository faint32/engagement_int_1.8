package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.PictureInfos;

public class FragmentChatPriPicList extends FragmentBase{
	
	public static FragmentChatPriPicList newInstance(String uid){
		FragmentChatPriPicList fragment = new FragmentChatPriPicList();
		Bundle bundle = new Bundle();
		bundle.putString(EgmConstants.BUNDLE_KEY.USER_ID, uid);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	private PullToRefreshGridView mPullToRefreshGridView ;
	private GridView mGridView ;
	
	private PhotoListAdapter mAdapter ;
	private ArrayList<PictureInfo> mPicList ;
	private String uid ;
	//列宽
	private int mItemWidth ;
	//当前页码
	private int mPageNo = 1;
	//每页的数量
	private int mPageNum ;
	//总数
	private int mTotalCount ;
	//列数
	private static final int CLOUM_NUM = 3 ;
	
	private TextView mImageBack ;
	private TextView mMiddleTitle ;
	private TextView mEditText ;
	
	private LinearLayout mNoContentLayout ;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(this.getArguments() == null
				|| TextUtils.isEmpty(this.getArguments().getString(EgmConstants.BUNDLE_KEY.USER_ID))){
			return ;
		}
		
		uid = this.getArguments().getString(EgmConstants.BUNDLE_KEY.USER_ID);
		EgmService.getInstance().addListener(mCallBack);
	}
	
	private int getChoosedIndex(){
		int index = -1 ;
		for(int i = 0; i< mPicList.size();i++){
			if(mPicList.get(i).choosed){
				index = i;
				break;
			}
		}
		return index ;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		EgmService.getInstance().doGetPictureList(uid,EgmConstants.Photo_Type.TYPE_PRIVATE, mPageNo);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_pri_pic_layout,container,false);
		init(root);
		return root ;
	}
	
	private void init(View root){
		
		mImageBack = (TextView)root.findViewById(R.id.back);
		mImageBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		
		mMiddleTitle = (TextView)root.findViewById(R.id.middle_title);
		mMiddleTitle.setText(R.string.pri_pic_album);
		
		mEditText = (TextView)root.findViewById(R.id.edit);
		mEditText.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//发送图片
				int index = getChoosedIndex();
				if(index >= 0){
					LoopBack loopBack = new LoopBack();
					loopBack.mType = EgmConstants.LOOPBACK_TYPE.chat_send_pri_pic ;
					loopBack.mData = mPicList.get(index) ;
					EgmService.getInstance().doLoopBack(loopBack);
					getActivity().finish();;
				}else{
					ToastUtil.showToast(getActivity(),"请选择要上传的照片");
				}
			}
		});
		
		mNoContentLayout = (LinearLayout)root.findViewById(R.id.no_pic_tip_layout);
		ImageView tipIcon = (ImageView) mNoContentLayout.findViewById(R.id.tip_icon);
		ViewCompat.setBackground(tipIcon, getResources().getDrawable(R.drawable.icon_photo_gray));
		TextView tipTxt = (TextView)mNoContentLayout.findViewById(R.id.tip_txt);
		tipTxt.setText(R.string.no_pri_pic_tip);
		
		mPullToRefreshGridView = (PullToRefreshGridView)root.findViewById(R.id.pull_refresh_grid);
		
		mPicList = new ArrayList<PictureInfo>();
		
		mGridView = mPullToRefreshGridView.getRefreshableView() ;
		mGridView.setBackgroundColor(Color.BLACK);
		mGridView.setNumColumns(CLOUM_NUM);
		mItemWidth = getActivity().getResources().getDisplayMetrics().widthPixels/CLOUM_NUM ;
		
		mPullToRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>(){
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
			}
			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				EgmService.getInstance().doGetPictureList(uid,EgmConstants.Photo_Type.TYPE_PRIVATE,mPageNo);
			}
		});
		mPullToRefreshGridView.setMode(Mode.PULL_FROM_END);
		
		mAdapter = new PhotoListAdapter(getActivity(),mPicList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(mOnItemClickListener);
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			FragmentPriPicImagePager fragment = FragmentPriPicImagePager.newInstance(mPicList,position);
	        	ft.add(R.id.activity_chat_pri_piclist_container_id, fragment);
	        	ft.addToBackStack(null);
	        	ft.commit();
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK && requestCode == EgmConstants.REQUEST_GET_PRI_PIC){
			if(data == null){
				return ;
			}
			PictureInfo info = data.getParcelableExtra(EgmConstants.BUNDLE_KEY.PICTURE_INFO);
			LoopBack lp = new LoopBack();
			lp.mType = EgmConstants.LOOPBACK_TYPE.chat_send_pri_pic ;
			lp.mData = info ;
			EgmService.getInstance().doLoopBack(lp);
			getActivity().getSupportFragmentManager().popBackStack();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetPictureListSucess(int transactionId, PictureInfos obj) {
			mPullToRefreshGridView.onRefreshComplete();
			if(obj != null){

				if(obj.pictureInfos.length == 0){
					mNoContentLayout.setVisibility(View.VISIBLE);
					mPullToRefreshGridView.setMode(Mode.DISABLED);
					return ;
				}
				
				mTotalCount = obj.totalCount ;
				mPageNum = obj.count ;
				
				//取消上拉加载更多功能
				if(mPageNo*mPageNum >= mTotalCount){
					mPullToRefreshGridView.setMode(Mode.DISABLED);
				}
				
				mPageNo ++ ;
				
				mPicList.addAll(Arrays.asList(obj.pictureInfos));
				mAdapter.notifyDataSetChanged();
			}
		}
		@Override
		public void onGetPictureListError(int transactionId, int errCode,String err) {
			Toast.makeText(getActivity(), err, Toast.LENGTH_SHORT).show();
		}
	};
	
	class PhotoListAdapter extends BaseAdapter{
		private List<PictureInfo> list ;
		private LayoutInflater inflater ;
		private GridView.LayoutParams lp;
		private Context context ;
		
		public PhotoListAdapter(Context context ,List<PictureInfo> list){
			this.context = context ;
			this.list = list ;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			lp = new GridView.LayoutParams(mItemWidth, mItemWidth);
		}
		@Override
		public int getCount() {
			return list == null ? 0 : list.size() ;
		}

		@Override
		public Object getItem(int position) {
			return list == null ? null : list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null ;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.item_view_photo_list,null);
				holder = new ViewHolder();
				holder.image = (LoadingImageView) convertView.findViewById(R.id.photo);
				holder.image.setScaleType(ScaleType.CENTER_CROP);
				holder.image.setScaleTop(true);
				holder.selected = (ImageView)convertView.findViewById(R.id.selected);
				holder.unSelected = (ImageView)convertView.findViewById(R.id.unselected);
				
				final ImageView image_selected = holder.selected ;
				final ImageView image_unselected = holder.unSelected ;
				image_selected.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						int p = (Integer) image_selected.getTag();
						list.get(p).choosed = false ;
						image_selected.setVisibility(View.GONE);
						image_unselected.setVisibility(View.VISIBLE);
					}
				});
				image_unselected.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						int p = (Integer) image_selected.getTag();
						for(int i = 0 ; i < list.size() ; i++){
							if(list.get(i).choosed && i != p){
								ToastUtil.showToast(context,R.string.send_one_pic_only);
								return ;
							}
						}
						list.get(p).choosed = true ;
						image_selected.setVisibility(View.VISIBLE);
						image_unselected.setVisibility(View.GONE);
					}
				});
				convertView.setLayoutParams(lp);
				convertView.setTag(holder);
			}
			
			holder = (ViewHolder) convertView.getTag();
			ViewCompat.setBackground(holder.image,context.getResources().getDrawable(R.drawable.bg_photo_placeholder));
			holder.image.setImageBitmap(null);
			holder.image.setServerClipSize(mItemWidth, mItemWidth);
			holder.image.setLoadingImage(list.get(position).smallPicUrl);
			
			// bug fix #141117
			holder.selected.setTag(Integer.valueOf(position));
			holder.unSelected.setTag(Integer.valueOf(position));
			if(list.get(position).choosed) {
				holder.selected.setVisibility(View.VISIBLE);
				holder.unSelected.setVisibility(View.GONE);
			} else {
				holder.selected.setVisibility(View.GONE);
				holder.unSelected.setVisibility(View.VISIBLE);
			}
			
			return convertView;
		}
		
		class ViewHolder{
			LoadingImageView image ;
			ImageView selected ;
			ImageView unSelected ;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
}
