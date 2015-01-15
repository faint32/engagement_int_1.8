package com.netease.engagement.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.adapter.ImagePagerAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.PictureInfo;

/**
 * 聊天界面发送私照viewpager
 */
public class FragmentPriPicImagePager extends FragmentBase{
	public static FragmentPriPicImagePager newInstance(
			ArrayList<PictureInfo> picInfos ,
			int selectIndex){
		FragmentPriPicImagePager fragment = new FragmentPriPicImagePager();
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST, picInfos);
		bundle.putInt(EgmConstants.BUNDLE_KEY.SELF_PAGE_SELECTINDEX, selectIndex);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	private ViewPager mPager ;
	
	private ImagePagerAdapter mAdapter ;
	private ArrayList<PictureInfo> mPicInfos ;
	private int mSelectIndex = 0 ;
	
	private TextView mImageBack ;
	private TextView mMiddleTitle ;
	private TextView mEditText ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		if(args == null || args.getParcelableArrayList(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST) == null){
			return ;
		}
		
		mPicInfos = args.getParcelableArrayList(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST);
		mSelectIndex = args.getInt(EgmConstants.BUNDLE_KEY.SELF_PAGE_SELECTINDEX, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_pri_pic_pager_layout, container,false);
		init(root);
		return root ;
	}
	
	private void init(View root){
		
		mImageBack = (TextView)root.findViewById(R.id.back);
		mImageBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				FragmentManager ft = getActivity().getSupportFragmentManager() ;
				ft.popBackStack();
			}
		});
		
		mMiddleTitle = (TextView)root.findViewById(R.id.middle_title);
		mMiddleTitle.setText(R.string.pri_pic_album);
		
		mEditText = (TextView)root.findViewById(R.id.edit);
		mEditText.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				LoopBack loopBack = new LoopBack();
				loopBack.mType = EgmConstants.LOOPBACK_TYPE.chat_send_pri_pic ;
				loopBack.mData = mPicInfos.get(mSelectIndex) ;
				EgmService.getInstance().doLoopBack(loopBack);
				getActivity().finish();
			}
		});
		
		ArrayList<String> urls = new ArrayList<String>();
		for(PictureInfo info : mPicInfos){
			urls.add(info.picUrl);
		}
		
		mPager = (ViewPager) root.findViewById(R.id.pager);
		mPager.setOnPageChangeListener(new OnPageChangeListener(){
			private int oldPosition = -1;
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageSelected(int arg0) {
				mSelectIndex = mPager.getCurrentItem();
				
				if (oldPosition != arg0) {
					if (mAdapter != null) {
						mAdapter.setScale(1);
					}
				}
				oldPosition = arg0;
			}
		});
		
		mAdapter = new ImagePagerAdapter(getActivity(), urls);
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(mSelectIndex);
	}
}
