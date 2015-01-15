package com.netease.engagement.image.explorer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.image.explorer.adapter.GridPhotoUploadAdapter;
import com.netease.engagement.image.explorer.utils.BitmapExecutorService;

public class GridPhotoUploadFragment extends Fragment {
	
	private Activity mActivity;
	
	private GridPhotoUploadAdapter mAdapter;
	
	private GridView mGridView;
	
	private OnClickListener onGridPhotoClickListener;
	
	private OnClickListener onUploadClickListener;
	
	private TextView picCountTv;
	
	private TextView uploadTv;
	
	private int mPictureType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.grid_photo_upload, container, false);
		
		View titleBar = view.findViewById(R.id.imageUploadTitleBar);
		TextView backBtn = (TextView) titleBar.findViewById(R.id.backBtn);
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				BitmapExecutorService.closeService();
				mActivity.finish();
			}
		});
		
		uploadTv = (TextView) titleBar.findViewById(R.id.uploadTv);
		if(mPictureType == EgmConstants.Photo_Type.TYPE_AVATAR){
		    uploadTv.setVisibility(View.INVISIBLE);
		}
		else{
		    uploadTv.setVisibility(View.VISIBLE);
		    uploadTv.setOnClickListener(onUploadClickListener);
		    if(mPictureType == EgmConstants.Photo_Type.TYPE_CHAT_PUBLIC_PIC){
		    		uploadTv.setText(R.string.send_txt);
		    }
		}
		
		TextView displayNameTv = (TextView) titleBar.findViewById(R.id.displayNameTv);
		displayNameTv.setText(PhotoUploadActivity.getDisplayName());
		
		picCountTv = (TextView) titleBar.findViewById(R.id.selectedCountTv);
		setCurrentPicCount();
		
		mGridView = (GridView) view.findViewById(R.id.multiPhotoUploadGridView);
		
		initData();
		return view;
	}
	
	public void setPictureType(int type){
	    mPictureType = type;
	}
	
	private void initData() {
		if(PhotoUploadActivity.getPicList() != null && PhotoUploadActivity.getPicList().size() > 0) {
			if(mAdapter == null) {
				mAdapter = new GridPhotoUploadAdapter(mActivity, onGridPhotoClickListener, mPictureType);
				PhotoUploadActivity.initCheckStatus(PhotoUploadActivity.getPicList().size());
			} 
			mGridView.setAdapter(mAdapter);
		}	
	}
	
	public void setOnGridPhotoClickListener(OnClickListener listener) {
		this.onGridPhotoClickListener = listener;
	}
	
	public void setOnUploadClickListener(OnClickListener listener) {
		this.onUploadClickListener = listener;
	}
	
	public void setCurrentPicCount() {
		picCountTv.setText("(" + PhotoUploadActivity.getCheckedCount() + ")");
	}
	
	public void setUploadTvEnable(boolean isEnable) {
		uploadTv.setEnabled(isEnable);
	}
	
}
