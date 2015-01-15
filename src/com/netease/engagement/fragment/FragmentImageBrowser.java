package com.netease.engagement.fragment;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.adapter.ImagePagerAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.stat.EgmStat;


/**
 * 图集fragment，传入图片url列表和当前显示图片index
 */
public class FragmentImageBrowser extends FragmentBase{
	
	private ViewPager mViewPager ;
	private TextView mTxt_tip ;
	private ImageView mDelImage ;
	
	private ImagePagerAdapter mImagePagerAdapter ;
	private ArrayList<PictureInfo> mPicInfos ;
	private ArrayList<String> mImgUrls ;
	
	private int mSelectIndex ;
	private boolean mFromGirlPage ;
	
	//图集中的图片可以删除
	private boolean mCanDel ;
	// 统计
	private boolean mStatistic;
	// 用户id
	private long mUserId;
	
	private static final int ZOOM_IN = 0;
	private static final int ZOOM_OUT = 1;
	private int currentZoomStatus = 1;
	
	public static FragmentImageBrowser newInstance(
			ArrayList<PictureInfo> picInfos ,
			int selectIndex,
			long userid,
			boolean fromGirlPage,
			boolean canDel,
			boolean statistic){
		FragmentImageBrowser fragment = new FragmentImageBrowser();
		Bundle args = new Bundle();
		args.putParcelableArrayList(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST, picInfos);
		args.putInt(EgmConstants.BUNDLE_KEY.SELF_PAGE_SELECTINDEX, selectIndex);
		args.putLong(EgmConstants.BUNDLE_KEY.USER_ID, userid);
		args.putBoolean(EgmConstants.BUNDLE_KEY.IMAGE_LIST_GIRL_PAGE, fromGirlPage);
		args.putBoolean(EgmConstants.BUNDLE_KEY.IMAGE_LSIT_CAN_DELETE, canDel);
		args.putBoolean(EgmConstants.BUNDLE_KEY.NEED_STATISTIC, statistic);
		fragment.setArguments(args);
		return fragment ;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().hide();
		Bundle args = getArguments();
		if(args == null || args.getParcelableArrayList(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST) == null){
			//此处仅仅return不够，还是会走到下面的onCreateView。调用finish则不会向下走
			getActivity().finish();
		}
		
		mPicInfos = args.getParcelableArrayList(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST);
		mSelectIndex = args.getInt(EgmConstants.BUNDLE_KEY.SELF_PAGE_SELECTINDEX, 0);
		mFromGirlPage = args.getBoolean(EgmConstants.BUNDLE_KEY.IMAGE_LIST_GIRL_PAGE);
		mCanDel = args.getBoolean(EgmConstants.BUNDLE_KEY.IMAGE_LSIT_CAN_DELETE);
		mStatistic = args.getBoolean(EgmConstants.BUNDLE_KEY.NEED_STATISTIC, false);
		mUserId = args.getLong(EgmConstants.BUNDLE_KEY.USER_ID);
		
		EgmService.getInstance().addListener(mCallBack);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		FrameLayout root = (FrameLayout) inflater.inflate(R.layout.fragment_image_browser,container,false);
		init(root);
		return root;
	}
	
	private void init(View root){
		mImgUrls = getPhotoUrls();
		
		mTxt_tip = (TextView)root.findViewById(R.id.tip);
		mDelImage = (ImageView)root.findViewById(R.id.delete_image);
		if(mCanDel){
			mDelImage.setVisibility(View.VISIBLE);
			mDelImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDelPicDialog();
				}
			});
		}
		
		mViewPager = (ViewPager)root.findViewById(R.id.pager);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
			
			@Override
			public void onPageScrollStateChanged(int arg0) {}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageSelected(int arg0) {
				if (oldPosition != arg0) {
					if (mImagePagerAdapter != null) {
						mImagePagerAdapter.setScale(1);
					}
				}
				oldPosition = arg0;
				
				mTxt_tip.setText(""+(arg0+1)+"/" + mImgUrls.size());
			}
		});
		
		mImagePagerAdapter = new ImagePagerAdapter(
				getActivity(),
				mImgUrls);
		
		mImagePagerAdapter.setOnPhotoTapListener(mTapListener);
		
		mViewPager.setAdapter(mImagePagerAdapter);
		
		mViewPager.setCurrentItem(mSelectIndex);
		mTxt_tip.setText(""+(mSelectIndex+1)+"/" + mImgUrls.size());
		
		oldPosition = 0;
		statClickPublicPic();
	}
	
	private int oldPosition = -1;
	
	private void statClickPublicPic() {
		int pos = oldPosition;
		if (pos >= 0 && pos < mPicInfos.size()) {
			PictureInfo info = mPicInfos.get(pos);
			
			EgmStat.log(EgmStat.LOG_CLICK_PHOTO_DETAIL, 
					EgmStat.SCENE_USER_DETAIL, mUserId, 
					info.id, EgmStat.TYPE_PUB_FREE);
		}
	}
	
	OnPhotoTapListener mTapListener = new OnPhotoTapListener() {
		
		@Override
		public void onPhotoTap(View view, float x, float y) {
			FragmentImageBrowser.this.getActivity().finish();
		}
	};
	
	/**
	 * 获取要删除的照片的地址
	 * @return
	 */
	private ArrayList<String> getPhotoUrls(){
		mImgUrls = new ArrayList<String>();
		for(PictureInfo item : mPicInfos){
			mImgUrls.add(item.picUrl);
		}
		return mImgUrls;
	}
	
	/**
	 * 删除照片
	 */
	private AlertDialog mDelPicDialog ;
	private void showDelPicDialog(){
		if(mDelPicDialog == null){
			LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(
					R.layout.view_delete_pics_dialog_layout,null);
			
			TextView cancel = (TextView) layout.findViewById(R.id.cancel);
			cancel.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					mDelPicDialog.dismiss();
				}
			});
			
			TextView confirm = (TextView) layout.findViewById(R.id.ok);
			confirm.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					EgmService.getInstance().deletePicture(
							EgmConstants.Photo_Type.TYPE_PUBLIC,
							new long[]{mPicInfos.get(mViewPager.getCurrentItem()).id});
					mDelPicDialog.dismiss();
					showWatting("删除中...");
				}
			});
			mDelPicDialog = new AlertDialog.Builder(getActivity()).setView(layout).create();
		}
		mDelPicDialog.show();
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onDeletePicSucess(int transactionId, int code) {
			stopWaiting();
			int currentItem = mViewPager.getCurrentItem();
			mViewPager.removeAllViews();
			mViewPager.setAdapter(null);
			mImgUrls.remove(currentItem);
			mPicInfos.remove(currentItem);
			
			if(mPicInfos.size() == 0){
				getActivity().finish();
				return ;
			}
			
			mImagePagerAdapter = new ImagePagerAdapter(
					getActivity(),
					mImgUrls);	
			
			mImagePagerAdapter.setOnPhotoTapListener(mTapListener);
			mViewPager.setAdapter(mImagePagerAdapter);
			
			int count = mImagePagerAdapter.getCount();
            if (currentItem < count) {
                
                mViewPager.setCurrentItem(currentItem);
                mTxt_tip.setText("" + (currentItem + 1) + "/" + mImgUrls.size());

            } else if (currentItem == count) {

                mViewPager.setCurrentItem(currentItem - 1);
                mTxt_tip.setText("" + (currentItem) + "/" + mImgUrls.size());
            }
		}

		@Override
		public void onDeletePicError(int transactionId, int errCode, String err) {
			stopWaiting();
			ToastUtil.showToast(getActivity(),err);
		}
	};
	
//	private class ImgTouchGesListener extends GestureDetector.SimpleOnGestureListener{
//		@Override
//		public boolean onDoubleTap(MotionEvent e) {
//			View view = mImagePagerAdapter.getCurrentView();
//			ImageViewTouch mImageView = (ImageViewTouch) view.findViewById(R.id.viewpager_image_browse);
//			
//			if(currentZoomStatus == ZOOM_IN) {
//				mImageView.zoomOut();
//				currentZoomStatus = ZOOM_OUT;
//			} else {
//				mImageView.zoomIn();
//				currentZoomStatus = ZOOM_IN;
//			}
//			return super.onDoubleTap(e);
//		}
//
//		@Override
//		public boolean onSingleTapConfirmed(MotionEvent e) {
//			FragmentImageBrowser.this.getActivity().finish();
//			return super.onSingleTapConfirmed(e);
//		}
//	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
}
