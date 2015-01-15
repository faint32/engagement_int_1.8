package com.netease.engagement.activity;


import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;

import com.netease.engagement.fragment.FragmentBase;

public class ActivityUtil {
	public static void selectPictureForResult(Activity a, int requestCode){
		Uri uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        Intent intent = new Intent(Intent.ACTION_PICK, uri);
        a.startActivityForResult(intent, requestCode);
	}

	public static void captureSmallPhotoForResult(Activity a, int requestCode){
		Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		a.startActivityForResult(it, requestCode);
	}

	public static void selectPictureForResult(FragmentBase a, int requestCode){
		Uri uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        Intent intent = new Intent(Intent.ACTION_PICK, uri);
        a.startActivityForResult(intent, requestCode);
    }

    public static void captureSmallPhotoForResult(FragmentBase a, int requestCode){
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        a.startActivityForResult(it, requestCode);
    }

	/**
	 * 调用系统的拍照界面去拍照，并获取结果
	 * @param a
	 * @param filePath 保存相片的全路径
	 * @param requestCode
	 */
	public static void capturePhotoForResult(Activity a, String filePath, int requestCode){
		Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
		try {
			a.startActivityForResult(it, requestCode);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
     * 调用系统的拍照界面去拍照，并获取结果
     * @param
     * @param filePath 保存相片的全路径
     * @param requestCode
     */
    public static void capturePhotoForResult(FragmentBase a, String filePath, int requestCode){
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
        try {
            a.startActivityForResult(it, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static void capturePhotoForResult(FragmentBase a, Uri uri, int requestCode){
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        try {
            a.startActivityForResult(it, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static void capturePhotoForResult(Activity a, Uri uri, int requestCode){
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        try {
            a.startActivityForResult(it, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

	public static void shareTextToSms(Context context, String msg){
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
		intent.putExtra("sms_body", msg);
		context.startActivity(intent);
	}

	public static String getFilePathFromUrl(Context context, Uri uri){
		String path = null;
		String scheme = uri.getScheme();
		if(ContentResolver.SCHEME_CONTENT.equals(scheme)){
			String projection[] = {ImageColumns.DATA,};
			Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
			if(c != null && c.moveToFirst()){
				path = c.getString(0);
			}
			if(c != null)
				c.close();
		}
		else if(ContentResolver.SCHEME_FILE.equals(scheme)){
			path = uri.getPath();
		}

		return path;
	}

	private static final int CAMEAR_DEFAULT = 0;
	private static final int CAMEAR_NONE = -1;
	private static final int CAMEAR_HAVE = 1;
	private static int	mCameraCheck = CAMEAR_DEFAULT;
	public static boolean haveCamera(){
		if(mCameraCheck != CAMEAR_DEFAULT)
			return (mCameraCheck == CAMEAR_HAVE);

		try {
			Camera camera = Camera.open();
			if(camera != null){
				mCameraCheck = CAMEAR_HAVE;
				camera.release();
			}
			else{
				mCameraCheck = CAMEAR_NONE;
			}
		} catch (Exception e) {
			mCameraCheck = CAMEAR_NONE;
			e.printStackTrace();
		}

		return (mCameraCheck == CAMEAR_HAVE);
	}
}
