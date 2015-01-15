package com.netease.engagement.image.explorer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.image.explorer.adapter.GalleryPhotoUploadAdapter;

public class GalleryPhotoUploadFragment extends Fragment {
	
	private Activity mActivity;
	
	private CustomGallery uploadPhotoGallery;
	
	private GalleryPhotoUploadAdapter mAdapter;
	
	private OnClickListener onUploadClickForGalleryListener;
	
	private TextView picCountTv;
	
	private TextView uploadTv;
	
	private int photoType ;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.gallery_photo_upload, container, false);
		
		View titleBar = view.findViewById(R.id.imageUploadTitleBar);
		TextView backBtn = (TextView) titleBar.findViewById(R.id.backBtn);
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mActivity.getFragmentManager().popBackStack();
			}
		});
		
		uploadTv = (TextView) titleBar.findViewById(R.id.uploadTv);
		uploadTv.setOnClickListener(onUploadClickForGalleryListener);
		
		TextView displayNameTv = (TextView) titleBar.findViewById(R.id.displayNameTv);
		displayNameTv.setText(PhotoUploadActivity.getDisplayName());
		
		picCountTv = (TextView) titleBar.findViewById(R.id.selectedCountTv);
		setCurrentPicCount();
		
		uploadPhotoGallery = (CustomGallery) view.findViewById(R.id.uploadPhotoGallery);
		
		Bundle bundle = getArguments();
		//聊天界面发送公开照片
		if(bundle != null && bundle.containsKey("photoType")){
			photoType = bundle.getInt("photoType");
			if(photoType == EgmConstants.Photo_Type.TYPE_CHAT_PUBLIC_PIC){
				uploadTv.setText(R.string.send_txt);
			}
		}
		
		if(bundle != null && bundle.containsKey("pos")) {
			int pos = bundle.getInt("pos");
			mAdapter = new GalleryPhotoUploadAdapter(mActivity,photoType);
			uploadPhotoGallery.setAdapter(mAdapter);
			uploadPhotoGallery.setSelection(pos);
		}
		
		return view;
	}
	
	public void setOnUploadClickForGalleryListener(OnClickListener listener) {
		onUploadClickForGalleryListener = listener;
	}
	
	public void setCurrentPicCount() {
		picCountTv.setText("(" + PhotoUploadActivity.getCheckedCount() + ")");
	}
	
	public void setUploadTvEnable(boolean isEnable) {
		uploadTv.setEnabled(isEnable);
	}
	
}
