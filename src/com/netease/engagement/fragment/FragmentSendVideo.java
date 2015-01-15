package com.netease.engagement.fragment;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityVideoPlay;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.VideoInfo;
import com.netease.framework.widget.ToastUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 单个视频发送页面
 */
public class FragmentSendVideo extends FragmentBase{
	
	public static FragmentSendVideo newInstance(VideoInfo info){
		FragmentSendVideo fragment = new FragmentSendVideo();
		Bundle bundle = new Bundle();
		bundle.putParcelable(EgmConstants.BUNDLE_KEY.VIDEO_INFO, info);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	private ImageView mTitleBack ;
	private TextView mTitleMiddle ;
	private TextView mTitleRight ;
	
	private ImageView mVideoImage ;
	private ImageView mVideoPlay ;
	
	private VideoInfo mVideoInfo ;
	private int mFromType = EgmConstants.SELEC_VIDEO_TYPE.TYPE_CHAT;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getArguments() == null
				|| getArguments().getParcelable(EgmConstants.BUNDLE_KEY.VIDEO_INFO) == null){
			return ;
		}
		
		mVideoInfo = getArguments().getParcelable(EgmConstants.BUNDLE_KEY.VIDEO_INFO);
		Intent intent =  getActivity().getIntent();
		if(intent != null){
			mFromType = intent.getIntExtra(EgmConstants.BUNDLE_KEY.SELECT_VIDEO_TYPE, EgmConstants.SELEC_VIDEO_TYPE.TYPE_CHAT);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_send_video,container,false);
		init(root);
		return root;
	}
	
	private void init(View root){
		if(root == null){
			return ;
		}
		mTitleBack = (ImageView)root.findViewById(R.id.back);
		mTitleBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().getSupportFragmentManager().popBackStack();
			}
		});
		mTitleMiddle = (TextView)root.findViewById(R.id.title_middle);
		mTitleMiddle.setVisibility(View.GONE);
		mTitleRight = (TextView)root.findViewById(R.id.title_right);
		if(mFromType == EgmConstants.SELEC_VIDEO_TYPE.TYPE_CHAT){
			mTitleRight.setText(R.string.send_txt);
		} else{
			mTitleRight.setText(R.string.confirm);
		}
		mTitleRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mListener != null){
					mListener.onSendVideo(mVideoInfo);
				}
			}
		});
		
		mVideoImage = (ImageView)root.findViewById(R.id.video_image);
		mVideoPlay = (ImageView)root.findViewById(R.id.video_play);
		mVideoPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!TextUtils.isEmpty(mVideoInfo.getFilePath())){
					ActivityVideoPlay.startActivity(getActivity(),mVideoInfo.getFilePath());
				}
			}
		});
		
		Bitmap thumb = getVideoThumb(mVideoInfo.getFilePath());
		if(thumb != null){
			mVideoImage.setImageBitmap(thumb);
		}
	}
	
	/**
	 * 获取视频文件缩略图
	 * @param filePath
	 * @return
	 */
	private Bitmap getVideoThumb(String filePath){
		Bitmap thumb = null ;
		if(!TextUtils.isEmpty(filePath)){
			thumb = ThumbnailUtils.createVideoThumbnail(filePath,
					MediaStore.Video.Thumbnails.MINI_KIND);
		}
		return thumb ;
	}
	

	private OnSendVideoListener mListener ;
	public interface OnSendVideoListener{
		public void onSendVideo(VideoInfo info);
	}
	public void setOnSendVideoListener(OnSendVideoListener listener){
		mListener = listener ;
	}
	
	private OnLeftBackListener mBackListener ;
	public interface OnLeftBackListener{
		public void onLeftBack();
	}
	public void setOnLeftBackListener(OnLeftBackListener listener){
		mBackListener = listener ;
	}
	
	public void removeLeftBackListener(){
		if(mBackListener != null){
			mBackListener = null ;
		}
	}
}
