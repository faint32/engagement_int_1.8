package com.netease.engagement.fragment;

import java.util.List;

import com.netease.engagement.app.EngagementApp;
import com.netease.framework.widget.ToastUtil;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

public class VideoRecorderTools {
	private static int max_width = 720 ;
	
	private int mCurCamId ;
	
	private static VideoRecorderTools mInstance ;
	
	private VideoRecorderTools(){}
	
	public static VideoRecorderTools getInstance(){
		if(mInstance == null){
			mInstance = new VideoRecorderTools();
		}
		return mInstance ;
	}

	public boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	public Camera getCameraInstance(boolean front) {
		Camera c = null;
		try{
			if(front){
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				for(int camIdx = 0; camIdx < Camera.getNumberOfCameras(); camIdx++){
					Camera.getCameraInfo(camIdx, cameraInfo);
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
						try{
							c = Camera.open(camIdx);
							mCurCamId = camIdx ;
						}catch (RuntimeException e){
							e.printStackTrace();
						}
					}
				}
				if(c == null){
					mCurCamId = 0 ;
					c = Camera.open(mCurCamId);
				}
			}else{
				c = Camera.open(0);
				mCurCamId = 0 ;
			}
		}catch(Exception e) {
			ToastUtil.showToast(
					EngagementApp.getAppInstance().getApplicationContext(), 
					"相机不存在或者正在使用");
		}
		return c;
	}
	
	public int getCameraId(){
		return mCurCamId ;
	}
	
	public CameraInfo getCameraInfo(int cameraId){
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, cameraInfo);
		if(cameraInfo != null){
			return cameraInfo ;
		}
		return null ;
	}
	
	public Camera.Size getPreviewSize(Camera camera){
		if(camera == null){
			return null;
		}
		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
		if(previewSizes.size() == 1){
			return previewSizes.get(0);
		}
		
		int result = 0 ;
		int delta = 0 ;
		for(int i= 0 ;i< previewSizes.size();i++){
			if(i == 0){
				delta = Math.abs(previewSizes.get(i).width - max_width);
			}
			if(Math.abs(previewSizes.get(i).width - max_width) < delta){
				delta = Math.abs(previewSizes.get(i).width - max_width) ;
				result = i ;
			}
		}
		return previewSizes.get(result);
	}
	
	public List<int[]> getMaxFrameRate(Camera camera){
		if(camera == null){
			return null;
		}
		Camera.Parameters params = camera.getParameters();
		List<int[]> list = params.getSupportedPreviewFpsRange();
		return list ;
	}
	
	public Camera.Size getPreviewSizeFull(Camera camera){
		if(camera == null){
			return null ;
		}
		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
		for(Camera.Size size : previewSizes){
			if(size.height == EngagementApp.getAppInstance().getApplicationContext().getResources().getDisplayMetrics().widthPixels){
				return size ;
			}
		}
		return null ;
	}
	
	public Camera getNextCamera() {
		Camera c = null;
		try{
			c = Camera.open((mCurCamId + 1) % Camera.getNumberOfCameras());
			mCurCamId = (mCurCamId + 1)% Camera.getNumberOfCameras();
		}catch(Exception e){
			ToastUtil.showToast(EngagementApp.getAppInstance().getApplicationContext(), 
					"切换摄像头异常");
		}
		return c;
	}
}
