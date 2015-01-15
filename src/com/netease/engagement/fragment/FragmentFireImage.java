package com.netease.engagement.fragment;

import java.util.Timer;
import java.util.TimerTask;

import uk.co.senab.photoview.PhotoView;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.adapter.MsgListCursorAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.MessageInfo;

public class FragmentFireImage extends FragmentBase {
	
	public static FragmentFireImage newInstance(MessageInfo msgInfo, long startTime){
		FragmentFireImage fragment = new FragmentFireImage();
		Bundle bundle = new Bundle();
		bundle.putSerializable(EgmConstants.BUNDLE_KEY.MESSAGE_INFO, msgInfo);
		bundle.putLong(EgmConstants.BUNDLE_KEY.FIRE_START_TIME, startTime);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	private MessageInfo msgInfo;
	private String mediaUrl;
	private long startTime;
	
	private PhotoView mImageView;
	private TextView mTitleLeft;
	private ProgressBar mProgressBar ;
	private TextView mXianpaiTv;
	private ProgressBar mFireProgressbar;
	
	public static final int DEFAULT_FIRE_DURATION = 10 * 1000;
	private Timer timer;
	
	private boolean isGetFireMessageMediaUrlFail = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		msgInfo = (MessageInfo) this.getArguments().getSerializable(EgmConstants.BUNDLE_KEY.MESSAGE_INFO);
		startTime = this.getArguments().getLong(EgmConstants.BUNDLE_KEY.FIRE_START_TIME);

		EgmService.getInstance().addListener(mCallBack);
		
		EgmService.getInstance().doGetFireMessageMediaUrl(msgInfo);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
//	    CustomActionBar mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
//	    mCustomActionBar.hide();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = (View) inflater.inflate(R.layout.fragment_fire_image_layout,container,false);
		init(view);
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (isGetFireMessageMediaUrlFail) {
			MsgListCursorAdapter.setFireStart(msgInfo, 0);
		}
	}
	
	private void init(View root){
		if(root == null){
			return ;
		}
		
		mImageView = (PhotoView) root.findViewById(R.id.container);
		mProgressBar = (ProgressBar)root.findViewById(R.id.progressbar);
		mFireProgressbar = (ProgressBar)root.findViewById(R.id.fire_progressbar);
		mTitleLeft = (TextView)root.findViewById(R.id.title_left);
		mTitleLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentFireImage.this.getActivity().finish();
			}
		});
		
		if (msgInfo.isCameraPhoto == 1) {
			mXianpaiTv = (TextView)root.findViewById(R.id.xianpaiTv);
			mXianpaiTv.setVisibility(View.VISIBLE);
		}
		
		if (startTime != 0) {
			startTimer();
		}
	}
	
	private void startTimer() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				long time = System.currentTimeMillis() - startTime;
				int progress = 100 - (int) ((100 * time) / DEFAULT_FIRE_DURATION);
				if (progress < 0 || progress > 100) {
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					FragmentFireImage.this.getActivity().finish();
				} else {
					mFireProgressbar.setProgress(progress);
				}
			}
		}, 0, 50);
	}
	
	private void renderView() {
		final PhotoView image = mImageView;
		image.setZoomable(true);
		image.setMediumScale(2.0f);
		image.setMaximumScale(4.0f);
		image.setScaleType(ScaleType.FIT_CENTER);
		
		ImageViewAsyncCallback callback = new ImageViewAsyncCallback(image, mediaUrl) {
			@Override
			public void onUiGetImage(int tid, Bitmap bitmap) {
				mProgressBar.setVisibility(View.GONE);
				super.onUiGetImage(tid, bitmap);
				
				image.setScaleType(ScaleType.FIT_CENTER);
				
				if (startTime == 0) {
					startTime = MsgListCursorAdapter.setFireStart(msgInfo);
					startTimer();
				}
			}
			
			@Override
			public void onUiGetImageNull(int tid, ImageView imageView) {
				mProgressBar.setVisibility(View.GONE);
				image.setScaleType(ScaleType.CENTER);
				image.setImageResource(R.drawable.icon_photo_loaded_fail_with_bg);
			}
		};
		image.setTag(callback);
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetFireMessageMediaUrlSucess(int transactionId, String mediaUrl) {
			FragmentFireImage.this.mediaUrl = mediaUrl;
			renderView();
		}
		
		@Override
		public void onGetFireMessageMediaUrlError(int transactionId, int errCode, String err) {
			Toast.makeText(FragmentFireImage.this.getActivity(), err, Toast.LENGTH_SHORT).show();
			mProgressBar.setVisibility(View.GONE);
			
			isGetFireMessageMediaUrlFail = true;
		}
	};
}
