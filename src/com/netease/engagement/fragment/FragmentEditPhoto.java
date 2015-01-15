package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityImageBrowser;
import com.netease.engagement.activity.ActivityUtil;
import com.netease.engagement.adapter.PhotoListAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EgmConstants.IsCameraPhotoFlag;
import com.netease.engagement.image.explorer.FileExplorerActivity;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.EgmDBProviderExport;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.PictureInfos;
import com.netease.service.protocol.meta.UserInfo;


/**
 * 公开照片和私密照片列表
 */
public class FragmentEditPhoto extends FragmentBase{
	
	public static FragmentEditPhoto newInstance(String userinfo,boolean isPrivate){
		FragmentEditPhoto fragment = new FragmentEditPhoto();
		Bundle args = new Bundle();
		args.putString(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO,userinfo);
		args.putBoolean(EgmConstants.BUNDLE_KEY.PRIVATE_IMAGE_LIST,isPrivate);
		fragment.setArguments(args);
		return fragment ;
	}
	
	private UserInfo mUserInfo ;
	//私照标识
	private boolean isPrivate = false ;
	
	private PullToRefreshGridView mPullToRefreshGridView ;
	private GridView mGridView ;
	private PhotoListAdapter mAdapter ;
	private boolean mPullFromEnd = false ;
	
	private boolean mSinglePicUpload;
	
	private PictureInfos mPictureInfos ;
	private ArrayList<PictureInfo> mPicList ;
	
	public static final int BROWSE_MODE = 1 ;
	public static final int EDIT_MODE = 2 ;
	public static int mMode = BROWSE_MODE ;
	
	//GridView padding
	private int mPadding = 4 ;
	//列宽
	private int mItemWidth ;
	//当前页码
	private int mPageNo = 1 ;
	//每页的数量
	private int mPageNum ;
	//总数
	private int mTotalCount ;
	//列数
	private static final int CLOUM_NUM = 4 ;
	
	
	private TextView mCancel ;
	private TextView mBack;
	private TextView mMiddleTitle ;
	private TextView mEditText ;
	
	//private LinearLayout mTipLayout ;
	//private ImageView mTipIcon ;
	//private TextView mTipTxt ;
	
	private TextView mLoadingTip ;
	
	private String mTitleType;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(this.getArguments() == null 
				|| TextUtils.isEmpty(this.getArguments().getString(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO))){
			return ;
		}
		
		isPrivate = this.getArguments().getBoolean(EgmConstants.BUNDLE_KEY.PRIVATE_IMAGE_LIST);
		if(isPrivate) {
			mTitleType = "私密";
		} else {
			mTitleType = "公开照";
		}
		
		Gson gson = new Gson();
		mUserInfo = gson.fromJson(this.getArguments().getString(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO),UserInfo.class);
		EgmService.getInstance().addListener(mEgmCallback);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_photo_list,container,false);
		init(root);
		return root;
	}
	
	private void init(View root){
		mPadding = EgmUtil.dip2px(getActivity(),mPadding);
		
		mBack = (TextView) root.findViewById(R.id.back);
		mBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getActivity().setResult(Activity.RESULT_OK);
				getActivity().finish();
			}
		});
		
		
		mCancel = (TextView)root.findViewById(R.id.cancel);
		mCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mMode = BROWSE_MODE;
				if(mPullFromEnd){
					mPullToRefreshGridView.setMode(Mode.PULL_FROM_END);
				}
				mBack.setVisibility(View.VISIBLE);
				mCancel.setVisibility(View.GONE);
				mEditText.setText(R.string.edit);
				mAdapter.notifyDataSetChanged();
			}
		});
		
		mMiddleTitle = (TextView)root.findViewById(R.id.middle_title);
		mMiddleTitle.setText(mTitleType + String.format(getResources().getString(R.string.album_title),0));
		
		mEditText = (TextView)root.findViewById(R.id.edit);
		mEditText.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				switch(mMode){
					case BROWSE_MODE:
						if(mPicList.size() == 0){
							return ;
						}
						mPullToRefreshGridView.setMode(Mode.DISABLED);
						mMode = EDIT_MODE ;
						mEditText.setText(R.string.delete_audio);
						mBack.setVisibility(View.GONE);
						mCancel.setVisibility(View.VISIBLE);
						mAdapter.notifyDataSetChanged();
						break;
					case EDIT_MODE:
						if(mPullFromEnd){
							mPullToRefreshGridView.setMode(Mode.PULL_FROM_END);
						}
						long[] choosedIds = getChoosedPicIds();
						if(choosedIds == null || choosedIds.length == 0){
							ToastUtil.showToast(getActivity(),R.string.choose_del_pic);
							return ;
						}
						mEditText.setText(R.string.edit);
						showDelPicDialog();
						break;
				}
			}
		});
		
		TextView tips = (TextView) root.findViewById(R.id.edit_photo_tips);
		if (isPrivate) {
			tips.setText(R.string.edit_photo_private_tips);
		}
		else {
			tips.setText(R.string.edit_photo_public_tips);
		}
		
		mPullToRefreshGridView = (PullToRefreshGridView)root.findViewById(R.id.pull_refresh_grid);
		mGridView = mPullToRefreshGridView.getRefreshableView();
		mGridView.setBackgroundColor(Color.BLACK);
		mGridView.setNumColumns(CLOUM_NUM);
		mGridView.setPadding(mPadding, mPadding, mPadding, mPadding);
		
		mItemWidth = getActivity().getResources().getDisplayMetrics().widthPixels/CLOUM_NUM ;
		
		mPullToRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>(){
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
			}
			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				if(isPrivate){
					EgmService.getInstance().doGetPictureList(String.valueOf(mUserInfo.uid),EgmConstants.Photo_Type.TYPE_PRIVATE,mPageNo);
				}else{
					EgmService.getInstance().doGetPictureList(String.valueOf(mUserInfo.uid),EgmConstants.Photo_Type.TYPE_PUBLIC,mPageNo);
				}
			}
		});
		
		mPullToRefreshGridView.setOnItemClickListener(mOnItemClickListener);
		
		mLoadingTip = new TextView(getActivity());
		mLoadingTip.setGravity(Gravity.CENTER);
		mLoadingTip.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
		mLoadingTip.setTextColor(Color.BLACK);
		mLoadingTip.setText(R.string.loading);
		mPullToRefreshGridView.setEmptyView(mLoadingTip);
		
		mPicList = new ArrayList<PictureInfo>();
		mAdapter = new PhotoListAdapter(getActivity(),mPicList,mItemWidth,isPrivate);
		mGridView.setAdapter(mAdapter);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(isPrivate){
			EgmService.getInstance().doGetPictureList(String.valueOf(mUserInfo.uid),EgmConstants.Photo_Type.TYPE_PRIVATE,mPageNo);
		}else{
			EgmService.getInstance().doGetPictureList(String.valueOf(mUserInfo.uid),EgmConstants.Photo_Type.TYPE_PUBLIC,mPageNo);
		}
	}
	
	/**
	 * 删除照片
	 */
	private AlertDialog mDelPicDialog ;
	private LinearLayout layout;
	private void showDelPicDialog(){
		
		if(mDelPicDialog == null){
			layout = (LinearLayout) getActivity().getLayoutInflater().inflate(
					R.layout.view_delete_pics_dialog_layout,null);
			
			TextView cancel = (TextView) layout.findViewById(R.id.cancel);
			cancel.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					mMode = EDIT_MODE ;
					mEditText.setText(R.string.delete_audio);
					mAdapter.notifyDataSetChanged();
					mDelPicDialog.dismiss();
					clearChoosedTag();
				}
			});
			
			TextView confirm = (TextView) layout.findViewById(R.id.ok);
			confirm.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					int type = isPrivate ? EgmConstants.Photo_Type.TYPE_PRIVATE : EgmConstants.Photo_Type.TYPE_PUBLIC ;
					long[] choosedPicIds = getChoosedPicIds();
					if(choosedPicIds != null && choosedPicIds.length != 0){
						EgmService.getInstance().deletePicture(type, choosedPicIds);
						showWatting(null, "删除中...", false);
					}
					mDelPicDialog.dismiss();
				}
			});
			mDelPicDialog = new AlertDialog.Builder(getActivity()).setView(layout).create();
		}
		TextView title = (TextView) layout.findViewById(R.id.title);
		if(getChoosedPicIds().length > 1) {
			title.setText("删除这些照片?");
		} else {
			title.setText("删除这张照片?");
		}
		mDelPicDialog.show();
	}
	
	
	

    /**
     * @author lishang 
     * 图片上传模式
     * 默认是最多选择9张，支持多张选择 
     * 0：默认模式 
     * PIC_SHOWOFF_MODE：晒照片模式
     * PIC_PRIVATE_MIRROR_MODE：私照魔镜 
     * OTHER_MODE：其他模式，支持任意张,这里有不合理的地方，
     * OTHER_MODE需要设置字段来支持 mSerialMaxCount,mMaxSize,mMinSize,mType
     */
	public interface PicUpLoadMode{
	    
	    int DEFAULT_MODE=0;
	    int PIC_SHOWOFF_MODE=1;
	    int PIC_PRIVATE_MIRROR_MODE=2;
	    int OTHER_MODE=3;
	}
	
	private int picUpLoadMode;
	
    public void setPicUpLoadMode(int picUpLoadMode) {
        this.picUpLoadMode = picUpLoadMode;
    }

    public int getPicUpLoadMode() {
        return this.picUpLoadMode;
    }
    
    private int mSerialMaxCount;
    private int mMaxSize;
    private int mMinSize;
    private int mType;

    public void setPicUpLoadLimites(int maxCount, int maxSize, int minSize, int type) {
        this.mSerialMaxCount = maxCount;
        this.mMaxSize = maxSize;
        this.mMinSize = minSize;
        this.mType = type;
    }
    /**
     * 图片上传，可以控制一次上传的数量
     * by lishang
     * @param context
     * @param type 公照 or 私照
     * @param maxSerialCount 最大连续选择数量
     * @param maxPic 图片最大尺寸
     * @param minPic 图片最小存
     */
    private void showUploadPicDialogForPicShowOff(final Context context, final int type,
            final int maxSerialCount, final int maxPicSize, final int minPicSize) {
        
	        if(mUploadPicDialog == null){
	            mUploadPicDialog = EgmUtil.createEgmMenuDialog(
	                    getActivity(), 
	                    getActivity().getString(R.string.upload_pic), 
	                    getActivity().getResources().getStringArray(R.array.send_pub_pic_array), 
	                    new View.OnClickListener() {
	                        @Override
	                        public void onClick(View view) {
	                            int which = (Integer)view.getTag();
	                            switch (which) {
	                                case 0: 
	                                    //拍照
	                                    mId = String.valueOf(System.currentTimeMillis());
	                                    ActivityUtil.capturePhotoForResult(FragmentEditPhoto.this, 
	                                             EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mId), 
	                                             EgmConstants.REQUEST_CAPTURE_PHOTO);
	                                    break;
	                                case 1: 
	                                  //相册选择照片上传
                                    FileExplorerActivity.startForUploadPicture(context, type,
                                            maxSerialCount, maxPicSize, minPicSize);
	                        break;
	                    }

	                            if(mUploadPicDialog.isShowing()){
	                                mUploadPicDialog.dismiss();
	                            }
	                        }
	                    });
	        }
	        mUploadPicDialog.show();
	    }
	    
	
	
	/**
	 * 上传照片
	 */
	private AlertDialog mUploadPicDialog ;
	private String mId = null; 
	private void showUploadPicDialog(){
		if(mUploadPicDialog == null){
			mUploadPicDialog = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					getActivity().getString(R.string.upload_pic), 
					getActivity().getResources().getStringArray(R.array.send_pub_pic_array), 
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int which = (Integer)view.getTag();
                            switch (which) {
	                            case 0: 
	                            	//拍照
	                            	mId = String.valueOf(System.currentTimeMillis());
							        ActivityUtil.capturePhotoForResult(FragmentEditPhoto.this, 
							        		 EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mId), 
											 EgmConstants.REQUEST_CAPTURE_PHOTO);
	                                break;
	                            case 1: 
//	                            	//相册选择照片上传
						if (!isPrivate)
							FileExplorerActivity.startForUploadPicture(getActivity(), EgmConstants.Photo_Type.TYPE_PUBLIC, EgmConstants.COUNT_MAX_UPLOAD_PICTURE, EgmConstants.SIZE_MAX_PICTURE, EgmConstants.SIZE_MIN_PICTURE);
						else {
							FileExplorerActivity.startForUploadPicture(getActivity(), EgmConstants.Photo_Type.TYPE_PRIVATE, EgmConstants.COUNT_MAX_UPLOAD_PICTURE, EgmConstants.SIZE_MAX_PICTURE, EgmConstants.SIZE_MIN_PICTURE);
						}
						break;
					}

                            if(mUploadPicDialog.isShowing()){
                            	mUploadPicDialog.dismiss();
                            }
                        }
                    });
		}
		mUploadPicDialog.show();
	}
	
	private EgmCallBack mEgmCallback = new EgmCallBack(){
		@Override
		public void onGetPictureListSucess(int transactionId, PictureInfos obj) {
			mPullToRefreshGridView.onRefreshComplete();
			if(obj == null){
				return ;
			}
			
			mPictureInfos = obj ;
			mPageNum = mPictureInfos.count ;
			mTotalCount = mPictureInfos.totalCount ;
			
			mMiddleTitle.setText(mTitleType + String.format(getResources().getString(R.string.album_title), mTotalCount));
			
			//取消上拉加载更多功能
			if(mPageNo*mPageNum >= mTotalCount){
				mPullFromEnd = false ;
				mPullToRefreshGridView.setMode(Mode.DISABLED);
			}else{
				mPullFromEnd = true ;
				mPullToRefreshGridView.setMode(Mode.PULL_FROM_END);
			}
			
			mPageNo ++ ;
			
			mPicList.addAll(Arrays.asList(mPictureInfos.pictureInfos));
			mAdapter.notifyDataSetChanged();
		}
		
		@Override
		public void onGetPictureListError(int transactionId, int errCode,String err) {
			Toast.makeText(getActivity(), err, Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onUploadPicSucess(int transactionId, PictureInfo obj) {
			if(obj != null){
			    EgmPrefHelper.putUpdatePicTime(getActivity(), java.lang.System.currentTimeMillis());
			    mPicList.add(0,obj);
				mAdapter.notifyDataSetChanged();
			}
			mTotalCount = mTotalCount + 1 ;
			mMiddleTitle.setText(mTitleType + String.format(getResources().getString(R.string.album_title),mTotalCount));
			stopWaiting();
			if (mSinglePicUpload) {
			    ToastUtil.showToast(getActivity(),R.string.upload_pic_suc);
			}
			// 如果是活动则关闭Activity,刷新个人数据
            if (picUpLoadMode == PicUpLoadMode.PIC_SHOWOFF_MODE
                    || picUpLoadMode == PicUpLoadMode.PIC_PRIVATE_MIRROR_MODE) {
                
                getActivity().finish();
            }
            EgmService.getInstance().doGetPrivateData();
		}
		
		@Override
		public void onUploadPicError(int transactionId, int errCode, String err) {
			stopWaiting();
            if (errCode != EgmServiceCode.TRANSACTION_PICTURE_PUBLIC_LIMIT
                    && errCode != EgmServiceCode.TRANSACTION_PICTURE_PRIVATE_LIMIT) {
                Toast.makeText(getActivity(), err, Toast.LENGTH_SHORT).show();
            }
		}
		
		@Override
		public void onDeletePicSucess(int transactionId, int code) {
			stopWaiting();
			int num = deletePics();
			mTotalCount = mTotalCount - num ;
			mMiddleTitle.setText(mTitleType + String.format(getResources().getString(R.string.album_title), mTotalCount));
			ToastUtil.showToast(getActivity(),R.string.del_pic_success);
			mMode = BROWSE_MODE ;
			mCancel.setVisibility(View.GONE);
			mBack.setVisibility(View.VISIBLE);
			mAdapter.notifyDataSetChanged();
		}
		
		@Override
		public void onDeletePicError(int transactionId, int errCode, String err) {
			stopWaiting();
			Toast.makeText(getActivity(), err, Toast.LENGTH_SHORT).show();
		}
	};
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			switch(mMode){
				case BROWSE_MODE:
					if(arg2 == 0){
					    switch (picUpLoadMode) {
                            case PicUpLoadMode.DEFAULT_MODE:
                                showUploadPicDialog();
                                break;
                            case PicUpLoadMode.PIC_SHOWOFF_MODE:
                                showUploadPicDialogForPicShowOff(getActivity(),
                                        EgmConstants.Photo_Type.TYPE_PUBLIC, 1,
                                        EgmConstants.SIZE_MAX_PICTURE,
                                        EgmConstants.SIZE_MIN_PICTURE);
                                break;
                            case PicUpLoadMode.PIC_PRIVATE_MIRROR_MODE:
                                showUploadPicDialogForPicShowOff(getActivity(),
                                        EgmConstants.Photo_Type.TYPE_PRIVATE,
                                        EgmConstants.COUNT_MAX_UPLOAD_PICTURE,
                                        EgmConstants.SIZE_MAX_PICTURE,
                                        EgmConstants.SIZE_MIN_PICTURE);
                                break;
                            case PicUpLoadMode.OTHER_MODE:
                                showUploadPicDialogForPicShowOff(getActivity(), mType,
                                        mSerialMaxCount, mMaxSize, mMinSize);
                                break;
                            default:
                                break;
                        }

					}else{
						ActivityImageBrowser.startActivity(
								FragmentEditPhoto.this, 
								mPicList, 
								arg2 -1,
								false,
								false);
					}
					break;
				case EDIT_MODE:
					mPicList.get(arg2).choosed = !mPicList.get(arg2).choosed ;
					if(mPicList.get(arg2).choosed){
						((ImageView)arg1.findViewById(R.id.unselected)).setVisibility(View.GONE);
						((ImageView)arg1.findViewById(R.id.selected)).setVisibility(View.VISIBLE);
					}else{
						((ImageView)arg1.findViewById(R.id.unselected)).setVisibility(View.VISIBLE);
						((ImageView)arg1.findViewById(R.id.selected)).setVisibility(View.GONE);
					}
					break;
				}
		}
	};
	
	/**
	 * 删除本地照片列表
	 */
	private int deletePics(){
		Iterator<PictureInfo> iterator = mPicList.iterator();
		PictureInfo item = null ;
		int num = 0 ;
		while(iterator.hasNext()){
			item = iterator.next() ;
			if(item.choosed){
				iterator.remove();
				num ++ ;
			}
		}
		return num ;
	}
	
	private void clearChoosedTag(){
		for(PictureInfo item : mPicList){
			item.choosed = false ;
		}
	}
	
	/**
	 * 获取当前选中的照片的id
	 */
	private long[] getChoosedPicIds(){
		int num = 0 ;
		for(PictureInfo item : mPicList){
			if(item.choosed){
				num ++ ;
			}
		}
		if(num == 0){
			return null ;
		}
		
		long[] ids = new long[num];
		int k = 0 ;
		for(int i = 0 ;i< mPicList.size() ;i++){
			if(mPicList.get(i).choosed){
				ids[k++] = mPicList.get(i).id ;
			}
		}
		return ids ;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case EgmConstants.REQUEST_SELECT_PICTURE:
					/**
					 * 上传照片
					 */
					if(data == null){
						return ;
					}
					String filePath = getAlbumImagePath(data.getData());
					if(!TextUtils.isEmpty(filePath)){
						//上传照片
						int type = isPrivate ? EgmConstants.Photo_Type.TYPE_PRIVATE : EgmConstants.Photo_Type.TYPE_PUBLIC ;
						EgmService.getInstance().upLoadPicture(filePath, type, IsCameraPhotoFlag.OtherPhoto);
					}	
					break;
				case EgmConstants.REQUEST_CAPTURE_PHOTO:
					/**
					 * 拍照
					 */
					String picPath = EgmUtil.getFilePathByType(EgmUtil.TYPE_CAMERA, mId) ;
					if(!TextUtils.isEmpty(picPath)){
						//上传照片
						int type = isPrivate ? EgmConstants.Photo_Type.TYPE_PRIVATE : EgmConstants.Photo_Type.TYPE_PUBLIC ;
						EgmService.getInstance().upLoadPicture(picPath, type, IsCameraPhotoFlag.CameraPhoto);
					}	
					break;
			}
			showWatting(null, "上传中...", false);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 从相册中选照片的时候的真实路径
	 * @param uri
	 * @return
	 */
	private String getAlbumImagePath(Uri uri){
		String srcFile = null ;
		if (uri.toString().startsWith("file://")) {
			srcFile = uri.toString().replace("file://", "");
		} else {
			Cursor cursor = getActivity().getContentResolver().query(uri, null,null, null, null);
			cursor.moveToFirst();
			srcFile = cursor.getString(0);
			cursor.close();
		}
		return srcFile ;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPicList.clear();
		mPicList = null ;
		mAdapter = null ;
		mGridView = null ;
		mMode = BROWSE_MODE ;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mEgmCallback);
	}
}
