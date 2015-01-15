/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.engagement.image.cropimage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.netease.common.image.util.ImageUtil;
import com.netease.date.R;
import com.netease.framework.widget.ToastUtil;

/**
 * The activity can crop specific region of interest from an image.
 */
public class ActivityCropImage extends MonitoredActivity 
		implements View.OnClickListener {
	private static final String TAG = "ActivityCropImage";

	public static final String ACTION_SAVE = "save";
	public static final String ACTION_SET_WALLPAPER = "set_wallpaper";
	public static final String ACTION_CROP = "crop";

	public static final String EXTRA_FORMAT = "format";
	public static final String EXTRA_DATA = "data";
	public static final String EXTRA_DATA_FILE_PATH = "file_path";
	public static final String EXTRA_WALLPAPER = "wallpaper";
	public static final String EXTRA_FIXSCALE = "fixscale";
	public static final String CROP_COORDINATE = "coordinate";
	public static final String EXTRA_ROTATE_DEGRESS = "rotate_degress" ;
	public static final String EXTRA_MIN_SIZE = "extra_min_size";

	private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG; // only
																				// used
	private Uri mSaveUri = null;
	private boolean mSetWallpaper = false;
	private boolean mFixScal = false;

	private final Handler mHandler = new Handler();

	boolean mWaitingToPick; // Whether we are wait the user to pick a face.
	boolean mSaving; // Whether the "save" button is already clicked.

	private CropImageView mImageView;
	private ContentResolver mContentResolver;

	private Bitmap mBitmap;
	HighlightView mCrop;
	
	private int mDegree ;

	public static void actionSaveCropImage(Activity context, int requestCode,
			Bitmap bitmap, Uri outputPath, String format) {
		Intent intent = new Intent(context, ActivityCropImage.class);
		intent.setAction(ACTION_SAVE);
		Bundle bundle = new Bundle();
		bundle.putParcelable(EXTRA_DATA, bitmap);
		bundle.putParcelable(MediaStore.EXTRA_OUTPUT, outputPath);
		bundle.putString(EXTRA_FORMAT, format);
		intent.putExtras(bundle);
		context.startActivityForResult(intent, requestCode);
	}

	public static void actionSetWallpaperCropImage(Activity context,
			int requestCode, Bitmap bitmap) {
		Intent intent = new Intent(context, ActivityCropImage.class);
		intent.setAction(ACTION_SET_WALLPAPER);
		Bundle bundle = new Bundle();
		bundle.putParcelable(EXTRA_DATA, bitmap);
		bundle.putBoolean(EXTRA_WALLPAPER, true);
		intent.putExtras(bundle);
		context.startActivityForResult(intent, requestCode);
	}

	public static void actionGetCropImage(Activity context,
			int requestCode, Bitmap bitmap, boolean fixScal) {
		Intent intent = new Intent(context, ActivityCropImage.class);
		intent.setAction(ACTION_CROP);
		Bundle bundle = new Bundle();
		bundle.putParcelable(EXTRA_DATA, bitmap);
		bundle.putBoolean(EXTRA_WALLPAPER, false);
		bundle.putBoolean(EXTRA_FIXSCALE, fixScal);
		intent.putExtras(bundle);
		
		context.startActivityForResult(intent, requestCode);
	}
	
	public static void actionGetCropImage(Fragment fragment,
			int requestCode, Bitmap bitmap, boolean fixScal) {
		Intent intent = new Intent(fragment.getActivity(), ActivityCropImage.class);
		intent.setAction(ACTION_CROP);
		Bundle bundle = new Bundle();
		bundle.putParcelable(EXTRA_DATA, bitmap);
		bundle.putBoolean(EXTRA_WALLPAPER, false);
		bundle.putBoolean(EXTRA_FIXSCALE, fixScal);
		intent.putExtras(bundle);
		
		fragment.startActivityForResult(intent, requestCode);
	}
	
	public static void actionGetCropImage(Activity activity,
            int requestCode, String bitmapFilePath, boolean fixScal, int minSize) {
        Intent intent = new Intent(activity, ActivityCropImage.class);
        intent.setAction(ACTION_CROP);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_DATA_FILE_PATH, bitmapFilePath);
        bundle.putBoolean(EXTRA_WALLPAPER, false);
        bundle.putBoolean(EXTRA_FIXSCALE, fixScal);
        bundle.putInt(EXTRA_MIN_SIZE, minSize);
        intent.putExtras(bundle);
        
        activity.startActivityForResult(intent, requestCode);
    }
	
	public static void actionGetCropImage(Fragment fragment,
            int requestCode, String bitmapFilePath, boolean fixScal, int minSize) {
        Intent intent = new Intent(fragment.getActivity(), ActivityCropImage.class);
        intent.setAction(ACTION_CROP);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_DATA_FILE_PATH, bitmapFilePath);
        bundle.putBoolean(EXTRA_WALLPAPER, false);
        bundle.putBoolean(EXTRA_FIXSCALE, fixScal);
        bundle.putInt(EXTRA_MIN_SIZE, minSize);
        intent.putExtras(bundle);
        
        fragment.startActivityForResult(intent, requestCode);
    }
	
//	public static void actionGetCropImage(Fragment fragment,
//            int requestCode, String bitmapFilePath, boolean fixScal,int degress) {
//        Intent intent = new Intent(fragment.getActivity(), ActivityCropImage.class);
//        intent.setAction(ACTION_CROP);
//        Bundle bundle = new Bundle();
//        bundle.putString(EXTRA_DATA_FILE_PATH, bitmapFilePath);
//        bundle.putBoolean(EXTRA_WALLPAPER, false);
//        bundle.putBoolean(EXTRA_FIXSCALE, fixScal);
//        bundle.putInt(EXTRA_ROTATE_DEGRESS, degress);
//        intent.putExtras(bundle);
//        fragment.startActivityForResult(intent, requestCode);
//    }
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContentResolver = getContentResolver();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cropimage);

		mImageView = (CropImageView) findViewById(R.id.image);

		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		if (extras == null) {
			finish();
			return;
		}
		
		// Make UI fullscreen.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		findViewById(R.id.discard).setOnClickListener(this);

		findViewById(R.id.save).setOnClickListener(this);
		
		new Handler().post(new Runnable() {
			
			@Override
			public void run() {
				init(intent, extras);
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.discard:
			setResult(RESULT_CANCELED);
			finish();
			break;
			
		case R.id.save:
			onSaveClicked();
			break;
		}
	}
	
	private void init(Intent intent, Bundle extras) {
		if(extras.containsKey(EXTRA_DATA)){
		    mBitmap = (Bitmap) extras.getParcelable(EXTRA_DATA);
		}
		else {
		    String filePath = extras.getString(EXTRA_DATA_FILE_PATH);
		    mDegree = extras.getInt(EXTRA_ROTATE_DEGRESS,0);
		    mBitmap = ImageUtil.getBitmapFromFile(filePath);
		    if(mDegree > 0){
		        mBitmap = ImageUtil.rotateBitmap(mBitmap, mDegree);
		    }
		}
		
		mMinSize = extras.getInt(EXTRA_MIN_SIZE, 0);
		
		mFixScal = extras.getBoolean(EXTRA_FIXSCALE, false);
		if (ACTION_SAVE.equals(intent.getAction())) {
			// save image
			mSaveUri = (Uri) extras.getParcelable(MediaStore.EXTRA_OUTPUT);

			String outputFormatString = extras.getString(EXTRA_FORMAT);
			if (outputFormatString != null) {
				mOutputFormat = Bitmap.CompressFormat
						.valueOf(outputFormatString);
			}

		} else if (ACTION_SET_WALLPAPER.equals(intent.getAction())) {
			// set wallpaper
			mSetWallpaper = extras.getBoolean(EXTRA_WALLPAPER);
		} else if (ACTION_CROP.equals(intent.getAction())) {
			// crop
			mSetWallpaper = false;
		}

		if (mBitmap == null) {
			finish();
			return;
		}

		startFaceDetection();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	private void startFaceDetection() {
		if (isFinishing()) {
			return;
		}

		mImageView.setImageBitmapResetBase(mBitmap, true);

		Util.startBackgroundJob(this, null, "请稍等...", new Runnable() {
			@Override
			public void run() {
				final CountDownLatch latch = new CountDownLatch(1);
				final Bitmap b = mBitmap;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (b != mBitmap && b != null) {
							mImageView.setImageBitmapResetBase(b, true);
							mBitmap.recycle();
							mBitmap = b;
						}
						if (mImageView.getScale() == 1F) {
							mImageView.center(true, true);
						}
						latch.countDown();
					}
				});
				try {
					latch.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				mRunFaceDetection.run();
			}
		}, mHandler);
	}

	private int mMinSize;
	private void onSaveClicked() {
		// TODO this code needs to change to use the decode/crop/encode single
		// step api so that we don't require that the whole (possibly large)
		// bitmap doesn't have to be read into memory
		if (mCrop == null) {
			return;
		}
		

		Bitmap croppedImage = null;

		Rect r = mCrop.getCropRect();
	
		final int width = r.width();
		final int height = r.height();
		final int left = r.left;
		final int top = r.top;
		
//		 int min = EgmConstants.SIZE_MIN_AVATAR_MALE;
//		 Account acc = ManagerAccount.getInstance().getCurrentAccount();
//	     if(acc != null){
//	         int gender = acc.mSex ;
//             if(acc.mSex == EgmConstants.SexType.Female){
//                 min = EgmConstants.SIZE_MIN_AVATAR_FEMALE;
//             }else{
//                 min = EgmConstants.SIZE_MIN_AVATAR_MALE;
//             }
//	     }
	     
		if(width < mMinSize || height < mMinSize){
		    ToastUtil.showToast(this, R.string.reg_tip_avatar_crop_small);
		    return;
		}
		
		if (mSaving)
            return;
        mSaving = true;

		// If we are circle cropping, we want alpha channel, which is the
		// third param here.
		try {
			croppedImage = Bitmap.createBitmap(width, height,
					Bitmap.Config.RGB_565);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

		Canvas canvas = new Canvas(croppedImage);
		Rect dstRect = new Rect(0, 0, width, height);
		canvas.drawBitmap(mBitmap, r, dstRect, null);

		// Release bitmap memory as soon as possible
		mImageView.clear();
		mBitmap.recycle();

		mImageView.setImageBitmapResetBase(croppedImage, true);
		mImageView.center(true, true);
		mImageView.mHighlightViews.clear();

		final Bitmap b = croppedImage;

		Util.startBackgroundJob(this, null, mSetWallpaper ? "wallpaper"
				: "请稍等...", new Runnable() {
			@Override
			public void run() {
				saveOutput(b, left, top, width, height);
			}
		}, mHandler);
	}

	private void saveOutput(Bitmap croppedImage, int left, int top, int width, int height) {
		if (mSaveUri != null) {
			OutputStream outputStream = null;
			boolean ret = true;
			try {
				outputStream = mContentResolver.openOutputStream(mSaveUri);
				if (outputStream != null) {
					croppedImage.compress(mOutputFormat, 75, outputStream);
				}
			} catch (IOException ex) {
				// TODO: report error to caller
				Log.e(TAG, "Cannot open file: " + mSaveUri, ex);
				ret = false;
			} finally {
				Util.closeSilently(outputStream);
			}
			Bundle extras = new Bundle();
			if (ret) {
				setResult(RESULT_OK,
						new Intent(mSaveUri.toString()).putExtras(extras));
			} else {
				setResult(RESULT_CANCELED);
			}
		} else if (mSetWallpaper) {

			try {
				setWallpaper(croppedImage);
				setResult(RESULT_OK);
			} catch (IOException e) {
				e.printStackTrace();
				setResult(RESULT_CANCELED);
			}
		} else {
			Bundle extras = new Bundle();

			String path = ImageUtil.getBitmapFilePath(croppedImage, "crop_image");
			extras.putString(EXTRA_DATA, path);

			// 传回裁剪坐标
			StringBuilder sb = new StringBuilder();
			sb.append(left).append('&').append(top).append('&').append(width)
					.append('&').append(height);
			extras.putString(CROP_COORDINATE, sb.toString());

			setResult(RESULT_OK, new Intent().putExtras(extras));
		}

		final Bitmap b = croppedImage;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mImageView.clear();
				b.recycle();
			}
		});
		finish();
	}
	
	Runnable mRunFaceDetection = new Runnable() {
		@SuppressWarnings("hiding")
		float mScale = 1F;
		Matrix mImageMatrix;
		FaceDetector.Face[] mFaces = new FaceDetector.Face[3];
		int mNumFaces;

		// For each face, we create a HightlightView for it.
		private void handleFace(FaceDetector.Face f) {
			PointF midPoint = new PointF();

			int r = ((int) (f.eyesDistance() * mScale)) * 2;
			f.getMidPoint(midPoint);
			midPoint.x *= mScale;
			midPoint.y *= mScale;

			int midX = (int) midPoint.x;
			int midY = (int) midPoint.y;

			HighlightView hv = new HighlightView(mImageView);

			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			RectF faceRect = new RectF(midX, midY, midX, midY);
			faceRect.inset(-r, -r);
			if (faceRect.left < 0) {
				faceRect.inset(-faceRect.left, -faceRect.left);
			}

			if (faceRect.top < 0) {
				faceRect.inset(-faceRect.top, -faceRect.top);
			}

			if (faceRect.right > imageRect.right) {
				faceRect.inset(faceRect.right - imageRect.right, faceRect.right
						- imageRect.right);
			}

			if (faceRect.bottom > imageRect.bottom) {
				faceRect.inset(faceRect.bottom - imageRect.bottom,
						faceRect.bottom - imageRect.bottom);
			}

			hv.setup(mImageMatrix, imageRect, faceRect, false && false);

			mImageView.add(hv);
		}

		// Create a default HightlightView if we found no face in the picture.
		private void makeDefault() {
			HighlightView hv = new HighlightView(mImageView);

			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			// make the default size about 4/5 of the width or height
			int cropWidth = Math.min(width, height) * 4 / 5;
			int cropHeight = cropWidth;

			int x = (width - cropWidth) / 2;
			int y = (height - cropHeight) / 2;

			RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
			hv.setup(mImageMatrix, imageRect, cropRect, mFixScal);
			mImageView.add(hv);
		}

		// Scale the image down for faster face detection.
		private Bitmap prepareBitmap() {
			if (mBitmap == null) {
				return null;
			}
			// 256 pixels wide is enough.
			if (mBitmap.getWidth() > 256) {
				mScale = 256.0F / mBitmap.getWidth();
			}
			Matrix matrix = new Matrix();
			matrix.setScale(mScale, mScale);
			Bitmap faceBitmap = null;
			try {
				faceBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
						mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			return faceBitmap;
		}

		@Override
		public void run() {
			mImageMatrix = mImageView.getImageMatrix();
			Bitmap faceBitmap = prepareBitmap();

			mScale = 1.0F / mScale;
			if (faceBitmap != null) {
				FaceDetector detector = new FaceDetector(faceBitmap.getWidth(),
						faceBitmap.getHeight(), mFaces.length);
				mNumFaces = detector.findFaces(faceBitmap, mFaces);
			}

			if (faceBitmap != null && faceBitmap != mBitmap) {
				faceBitmap.recycle();
			}

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mWaitingToPick = mNumFaces > 1;
					if (mNumFaces > 0) {
						for (int i = 0; i < mNumFaces; i++) {
							handleFace(mFaces[i]);
						}
					} else {
						makeDefault();
					}
					mImageView.invalidate();
					if (mImageView.mHighlightViews.size() == 1) {
						mCrop = mImageView.mHighlightViews.get(0);
						mCrop.setFocus(true);
					}

					if (mNumFaces > 1) {
						Toast t = Toast.makeText(ActivityCropImage.this,
								"multiface_crop_help", Toast.LENGTH_SHORT);
						t.show();
					}
				}
			});
		}
	};
}
