package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.netease.common.image.util.ImageUtil;
import com.netease.date.R;
import com.netease.engagement.image.cropimage.ActivityCropImage;
import com.netease.engagement.view.imageviews.NeteaseCropImageView;
import com.netease.service.Utils.EgmUtil;

/***
 * New image crop activity for clipping circle image 
 * @author user
 *
 */
public class ImageCropActivity extends ActivityEngagementBase implements android.support.v4.app.LoaderManager.LoaderCallbacks<Bitmap>, View.OnClickListener{
	private final static String EXTRA_OUT_IMAGE_PATH = "file_out_path_extra";
	private final static String EXTRA_RETURN_DATA = "return_data_extra";
	private final static String EXTRA_SRC_IMAGE_PATH = "file_src_path_extra";
	public final static String EXTRA_DATA = "data";
	public final static String EXTRA_RETURN_DATA_PATH = "return_path_extra";
	public final static String EXTRA_MIN_WIDTH_HEIGHT ="min_extra_width";
	public static final String CROP_COORDINATE = "coordinate";
	
	private boolean mNeedData = true;
	private NeteaseCropImageView mCropImageView;
	private String mFileOutPath;
	private String mFileSrcPath;
	private int mMinWidth = 0;
	
	public static Intent actionImageCrop(Activity context, String imageSrcPath,
			String imageOutPath, int minWidth) {
		Intent intent = new Intent();
		intent.setClass(context, ImageCropActivity.class);
		intent.putExtra(EXTRA_SRC_IMAGE_PATH, imageSrcPath);
		intent.putExtra(EXTRA_OUT_IMAGE_PATH, imageOutPath);
		intent.putExtra(EXTRA_RETURN_DATA, true);
		intent.putExtra(EXTRA_MIN_WIDTH_HEIGHT, minWidth);
		return intent;
	}

	public static void actionGetCropImage(Fragment fragment,
            int requestCode, String bitmapFilePath, int minSize) {
        Intent intent = new Intent(fragment.getActivity(), ActivityCropImage.class);
        intent.setClass(fragment.getActivity(), ImageCropActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SRC_IMAGE_PATH, bitmapFilePath);
        bundle.putInt(EXTRA_MIN_WIDTH_HEIGHT, minSize);
        intent.putExtras(bundle);
        
        fragment.startActivityForResult(intent, requestCode);
    }
	
	
	public static void actionGetCropImage(Activity activity,
            int requestCode, String bitmapFilePath, int minSize) {
		Intent intent = new Intent(activity, ActivityCropImage.class);
        intent.setClass(activity, ImageCropActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SRC_IMAGE_PATH, bitmapFilePath);
        bundle.putInt(EXTRA_MIN_WIDTH_HEIGHT, minSize);
        intent.putExtras(bundle);
        
        activity.startActivityForResult(intent, requestCode);
    }
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setOveride(false);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crop_image_layout);
		Intent intent = getIntent();
		mFileOutPath = intent.getStringExtra(EXTRA_OUT_IMAGE_PATH);
		mFileSrcPath = intent.getStringExtra(EXTRA_SRC_IMAGE_PATH);
		mNeedData = intent.getBooleanExtra(EXTRA_RETURN_DATA, true);
		mMinWidth = intent.getIntExtra(EXTRA_MIN_WIDTH_HEIGHT, 60);
		
		
		mFileOutPath = EgmUtil.getCacheDir() + "crop";
		mCropImageView = (NeteaseCropImageView) findViewById(R.id.crop_image_view);
		mCropImageView.setOutput(mMinWidth, mMinWidth);
		findViewById(R.id.cancel_btn).setOnClickListener(this);
		findViewById(R.id.ok_btn).setOnClickListener(this);
		
		getSupportLoaderManager().initLoader(0, null, this).startLoading();
	}

	@Override
	protected void onDestroy() {
		mCropImageView.clear();
		super.onDestroy();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ok_btn:
				if (mCropImageView == null) {
					return;
				}
				
				final int[] widthHeight = mCropImageView.isMeetRequired();
				if (widthHeight[0] < mMinWidth || widthHeight[1] < mMinWidth) {
					Toast.makeText(this, R.string.avater_clip_size_too_small, Toast.LENGTH_SHORT).show();
					return;
				}
				
				Bundle extras = new Bundle();
				if (mCropImageView.saveOriginalImage(mFileOutPath)) {
					extras.putString(EXTRA_DATA, mFileOutPath);
					extras.putString(CROP_COORDINATE, mCropImageView.getOriginalCoordInfo());
				}
				
				setResult(RESULT_OK, new Intent().putExtras(extras));
				finish();
//				if (mNeedData) {
//					byte[] data = mCropImageView.getCroppedImage();
//					if (data != null) {
//						Intent intent = new Intent();
//						intent.putExtra(EXTRA_DATA, data);
//						setResult(RESULT_OK, intent);
//					}
//					finish();
//				} else {
//					if (mCropImageView.saveCroppedIamge(mFileOutPath)) {
//						Intent intent = new Intent();
//						intent.putExtra(EXTRA_RETURN_DATA_PATH, mFileOutPath);
//						setResult(RESULT_OK, intent);
//					}
//					finish();
//				}
				break;
			case R.id.cancel_btn:
				finish();
				break;
	
			default:
				break;
		}
	}

	@Override
	public android.support.v4.content.Loader<Bitmap> onCreateLoader(int arg0,
			Bundle arg1) {
		// TODO Auto-generated method stub
		AsyncTaskLoader<Bitmap> loader = new AsyncTaskLoader<Bitmap>(this) {
			
			@Override
			public Bitmap loadInBackground() {
				// TODO Auto-generated method stub
				Bitmap src = ImageUtil.decodeSampledForDisplay(mFileSrcPath);
				src = ImageUtil.rotateBitmapInNeeded(mFileSrcPath, src);
				return src;
			}
			
			@Override
		    protected void onStartLoading() {
		        cancelLoad();
		        forceLoad();
		    }

		    @Override
		    protected void onStopLoading() {
		        cancelLoad();
		    }

		    @Override
		    protected void onReset() {
		        stopLoading();
		    }
		};
		return loader;
	}

	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Bitmap> arg0,
			Bitmap arg1) {
		// TODO Auto-generated method stub
		mCropImageView.setImageBitmap(arg1);
		mCropImageView.invalidate();
		getSupportLoaderManager().destroyLoader(0);
	}

	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Bitmap> arg0) {
		// TODO Auto-generated method stub
		
	}
}
