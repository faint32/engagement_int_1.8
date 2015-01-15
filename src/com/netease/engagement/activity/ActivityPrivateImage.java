package com.netease.engagement.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentPrivateImage;
import com.netease.service.protocol.meta.PictureInfo;

public class ActivityPrivateImage extends ActivityEngagementBase{

	public static void startActivity(FragmentBase fragment,String userId , ArrayList<PictureInfo> picList, int pos) {
        Intent intent = new Intent(fragment.getActivity(), ActivityPrivateImage.class);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, userId);
        intent.putParcelableArrayListExtra(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST, picList);
        intent.putExtra(EgmConstants.BUNDLE_KEY.POSITION, pos);
        fragment.startActivity(intent);
    }
	
	public static void startActivityForResult(FragmentBase fragment,String userId , ArrayList<PictureInfo> picList, int pos) {
        Intent intent = new Intent(fragment.getActivity(), ActivityPrivateImage.class);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, userId);
        intent.putParcelableArrayListExtra(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST, picList);
        intent.putExtra(EgmConstants.BUNDLE_KEY.POSITION, pos);
        fragment.startActivityForResult(intent,EgmConstants.REQUEST_GET_PRI_PIC);
    }
	
	private String userId ;//为空的情况下为聊天界面发送私密照片查看大图
	private ArrayList<PictureInfo> mUnLockList;
	private int mPos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setCustomActionBar();
		
		userId = getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.USER_ID);
		mUnLockList = getIntent().getParcelableArrayListExtra(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST);
		mPos = getIntent().getIntExtra(EgmConstants.BUNDLE_KEY.POSITION, 0);
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_private_image_container_id);
		setContentView(container);
		
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		Fragment newFragment = FragmentPrivateImage.newInstance(userId ,mUnLockList, mPos) ;
		ft.replace(R.id.activity_private_image_container_id, newFragment);
		ft.commit();
	}
}
