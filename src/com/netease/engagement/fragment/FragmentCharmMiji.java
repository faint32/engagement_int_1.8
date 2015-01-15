package com.netease.engagement.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityAudioRecorder;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.activity.ActivityMyShow;
import com.netease.engagement.activity.ActivityPicUploadEntrance;
import com.netease.engagement.activity.ActivityPicUploadEntrance.PicType;
import com.netease.engagement.activity.ActivityUtil;
import com.netease.engagement.activity.ActivityYuanfen;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EgmConstants.IsCameraPhotoFlag;
import com.netease.engagement.image.explorer.FileExplorerActivity;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.EgmDBProviderExport;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.PictureInfo;


/**
 * 魅力秘籍
 */
public class FragmentCharmMiji extends FragmentBase{
	
	public static FragmentCharmMiji newInstance(int introduceType){
		FragmentCharmMiji fragment = new FragmentCharmMiji();
		Bundle args = new Bundle();
		args.putInt(EgmConstants.BUNDLE_KEY.SELF_INTRODUCE_TYPE, introduceType);
		fragment.setArguments(args);
		return fragment ;
	}

	private CustomActionBar mCustomActionBar ;
	private TextView mTxtUpload ;
	private TextView mTxtChat ;
	private TextView mTxtRecordIntr ;
	private TextView mTxtOpenYuanfen ;
	
	private int mIntroduceType;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		mIntroduceType = args.getInt(EgmConstants.BUNDLE_KEY.SELF_INTRODUCE_TYPE);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));

//        mCustomActionBar.setLeftAction(R.drawable.button_back_circle_selector,null);
        mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        mCustomActionBar.setMiddleTitle(R.string.charm_strategy);
        mCustomActionBar.setMiddleTitleSize(20);
        mCustomActionBar.hideRightTitle();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_charm_miji_layout,container,false);
		init(root);
		return root ;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(FragmentYuanfen.mIsYuanfenOpen) {
			mTxtOpenYuanfen.setText(R.string.peng_yuanfen_already_opend);
			mTxtOpenYuanfen.setBackgroundResource(R.drawable.btn_pgcenter_charm_cheats_unavailable);
			mTxtOpenYuanfen.setTextColor(getResources().getColor(R.color.content_text));
			mTxtOpenYuanfen.setEnabled(false);
		} else {
			mTxtOpenYuanfen.setOnClickListener(mOnClickListener);
			mTxtOpenYuanfen.setEnabled(true);
			mTxtOpenYuanfen.setBackgroundResource(R.drawable.btn_pri_center_cheat_selector);
		}
	}
	
	private void init(View root){
		if(root == null){
			return ;
		}
		mTxtUpload = (TextView)root.findViewById(R.id.upload_pri_pic);
		mTxtChat = (TextView)root.findViewById(R.id.find_to_chat);
		mTxtRecordIntr = (TextView)root.findViewById(R.id.record_intr);
		mTxtOpenYuanfen = (TextView)root.findViewById(R.id.open_yuanfen);
		
		
		mTxtUpload.setOnClickListener(mOnClickListener);
		mTxtChat.setOnClickListener(mOnClickListener);
		mTxtRecordIntr.setOnClickListener(mOnClickListener);
		if(mIntroduceType > 0) {
			mTxtRecordIntr.setText(R.string.record_audio_again);
		}
		
		if(FragmentYuanfen.mIsYuanfenOpen) {
			mTxtOpenYuanfen.setText(R.string.peng_yuanfen_already_opend);
			mTxtOpenYuanfen.setBackgroundResource(R.drawable.btn_pgcenter_charm_cheats_unavailable);
			mTxtOpenYuanfen.setTextColor(getResources().getColor(R.color.content_text));
			mTxtOpenYuanfen.setEnabled(false);
		} else {
			mTxtOpenYuanfen.setOnClickListener(mOnClickListener);
		}
	}

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.upload_pri_pic:
                    // 入口统一
                    ActivityPicUploadEntrance.startActivity(getActivity(),PicType.PRIVATE_PIC);
					break;
				case R.id.find_to_chat:
//					getActivity().setResult(Activity.RESULT_OK);
					ActivityHome.startActivity(getActivity(), FragmentHome.TAB_INDEX_RECOMMEND);
//					getActivity().finish();
					break;
				case R.id.record_intr:
//					ActivityAudioRecorder.startActivity(FragmentCharmMiji.this.getActivity());
					ActivityMyShow.startActivity(getActivity());
					break;
				case R.id.open_yuanfen:
					ActivityYuanfen.startActivity(getActivity(), false);
					break;
			}
		}
	};
	
	/**
	 * 上传照片
	 */
	private AlertDialog mUploadPicDialog ;
	private String mId = null; 
	
	private void showUploadPicDialog(){
		if(mUploadPicDialog == null){
			mUploadPicDialog = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					"上传私密照", 
					getActivity().getResources().getStringArray(R.array.send_pub_pic_array), 
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int which = (Integer  )view.getTag();
                            switch (which) {
	                            case 0: 
	                            	//拍照
	                            	mId = String.valueOf(System.currentTimeMillis());
							        ActivityUtil.capturePhotoForResult(FragmentCharmMiji.this, 
							        		 EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mId), 
											 EgmConstants.REQUEST_CAPTURE_PHOTO);
	                                break;
	                            case 1: 
	                            	//相册选择照片上传
	                            	FileExplorerActivity.startForSelectPicture(FragmentCharmMiji.this, 
	                            			EgmConstants.Photo_Type.TYPE_AVATAR, 
	                                        EgmConstants.REQUEST_SELECT_PICTURE, 
	                                        EgmConstants.SIZE_MAX_PICTURE, 
	                                        EgmConstants.SIZE_MIN_PICTURE);
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
						EgmService.getInstance().upLoadPicture(filePath,EgmConstants.Photo_Type.TYPE_PRIVATE, IsCameraPhotoFlag.OtherPhoto);
					}	
					break;
				case EgmConstants.REQUEST_CAPTURE_PHOTO:
					/**
					 * 拍照
					 */
					String picPath = EgmUtil.getFilePathByType(EgmUtil.TYPE_CAMERA, mId) ;
					if(!TextUtils.isEmpty(picPath)){
						//上传照片
						EgmService.getInstance().upLoadPicture(picPath,EgmConstants.Photo_Type.TYPE_PRIVATE, IsCameraPhotoFlag.CameraPhoto);
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
            srcFile = uri.getPath().replace("file://", "");
            
		} else {
			Cursor cursor = getActivity().getContentResolver().query(uri, null,null, null, null);
			cursor.moveToFirst();
			srcFile = cursor.getString(1);
			cursor.close();
		}
		return srcFile ;
	}
	

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
