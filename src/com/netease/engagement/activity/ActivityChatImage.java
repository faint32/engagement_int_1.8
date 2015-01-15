package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentChatImage;

public class ActivityChatImage extends ActivityEngagementBase{
	
	public static void startActivity(
			Context context,
			String imageUrl,
			boolean isPrivate,
			long userId,
			long picId ) {
        Intent intent = new Intent(context, ActivityChatImage.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_URL,imageUrl);
        intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_IS_PRIVATE,isPrivate);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID,userId);
        intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_PRIVATE_IMAGE_ID,picId);
        ((Activity)context).startActivity(intent);
    }
	
	public static void startActivity(Context context,String imageUrl,int isCameraPhoto) {
        Intent intent = new Intent(context, ActivityChatImage.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_IS_CAMERA_PHOTO, isCameraPhoto);
        intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_URL,imageUrl);
        ((Activity)context).startActivity(intent);
    }

	private String mImageUrl ;
	
	//for private image
	private boolean isPrivate ;
	private long userId ;
	private long picId ;
	int isCameraPhoto;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setCustomActionBar();
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_chat_image_container_id);
		setContentView(container);
		
		Intent intent = this.getIntent();
		mImageUrl = intent.getStringExtra(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_URL);
		isPrivate = intent.getBooleanExtra(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_IS_PRIVATE,false);
		userId = intent.getLongExtra(EgmConstants.BUNDLE_KEY.USER_ID,0);
		picId = intent.getLongExtra(EgmConstants.BUNDLE_KEY.CHAT_PRIVATE_IMAGE_ID,0);
		isCameraPhoto=intent.getIntExtra(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_IS_CAMERA_PHOTO, -1);
				
		if(this.findViewById(R.id.activity_chat_image_container_id) != null && savedInstanceState == null){
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			FragmentChatImage fragment = FragmentChatImage.newInstance(
					mImageUrl,
					isPrivate,
					userId,
					picId,
					isCameraPhoto);
			ft.replace(R.id.activity_chat_image_container_id, fragment).commit();
		}
	}
}
