package com.netease.engagement.activity;

import java.util.ArrayList;


import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentImageBrowser;
import com.netease.service.protocol.meta.PictureInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;

public class ActivityImageBrowser extends ActivityEngagementBase{

	private static String TAG = ActivityImageBrowser.class.getSimpleName();
	
	private ArrayList<PictureInfo> mPicInfos ;
	private int mSelectIndex ;
	private boolean mFromGirlPage ;
	//图集中的图片可以删除
	private boolean mCanDel ;
	
	// user id
	private long mUserId;
	// 统计标记位
	private boolean mStatistic;
	
	public static void startActivity(Context context, 
			ArrayList<PictureInfo> picInfos ,
			int selectedIndex, boolean fromGirlPage,
			long userid, boolean statistic) {
        Intent intent = new Intent(context, ActivityImageBrowser.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putParcelableArrayListExtra(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST,picInfos);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_SELECTINDEX, selectedIndex);
        intent.putExtra(EgmConstants.BUNDLE_KEY.IMAGE_LIST_GIRL_PAGE, fromGirlPage);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, userid);
        intent.putExtra(EgmConstants.BUNDLE_KEY.NEED_STATISTIC, statistic);
        context.startActivity(intent);
    }
	
	public static void startActivity(FragmentBase fragment ,
			ArrayList<PictureInfo> picInfos ,
			int selectedIndex,
			boolean fromGirlPage,
			boolean canDelete) {
        Intent intent = new Intent(fragment.getActivity(), ActivityImageBrowser.class);
        intent.putParcelableArrayListExtra(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST,picInfos);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_SELECTINDEX, selectedIndex);
        intent.putExtra(EgmConstants.BUNDLE_KEY.IMAGE_LIST_GIRL_PAGE, fromGirlPage);
        intent.putExtra(EgmConstants.BUNDLE_KEY.IMAGE_LSIT_CAN_DELETE, canDelete);
        fragment.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FrameLayout linear = new FrameLayout(this);
        linear.setId(R.id.activity_image_browser_id);
        setContentView(linear);
        
        Intent intent = getIntent();
        
        mPicInfos = intent.getParcelableArrayListExtra(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST);
        mSelectIndex = intent.getIntExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_SELECTINDEX, -1);
        mFromGirlPage = intent.getBooleanExtra(EgmConstants.BUNDLE_KEY.IMAGE_LIST_GIRL_PAGE,false);
        mCanDel = intent.getBooleanExtra(EgmConstants.BUNDLE_KEY.IMAGE_LSIT_CAN_DELETE,false);
        mStatistic = intent.getBooleanExtra(EgmConstants.BUNDLE_KEY.NEED_STATISTIC, false);
        mUserId = intent.getLongExtra(EgmConstants.BUNDLE_KEY.USER_ID, 0);
        
        if(mPicInfos == null || mPicInfos.size() == 0 || mSelectIndex == -1){
        	finish() ;
        }
        
        if (findViewById(R.id.activity_image_browser_id) != null && savedInstanceState == null) {
            Fragment fragment = null;
            fragment = FragmentImageBrowser.newInstance(mPicInfos,
            		mSelectIndex, mUserId, mFromGirlPage, mCanDel, mStatistic);
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_image_browser_id, fragment, TAG).commit();
        }
    }
	
}
