package com.netease.engagement.adapter;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.netease.common.image.util.ImageUtil;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityPicUploadEntrance;
import com.netease.engagement.activity.ActivityPicUploadEntrance.PicType;
import com.netease.engagement.activity.ActivityUtil;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EgmConstants.IsCameraPhotoFlag;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.image.explorer.FileExplorerActivity;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.EgmDBProviderExport;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.PictureInfo;


/**
 * 上传图片辅助类
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class UploadPictureHelper {
	private static final String KEY_CAPTURE_ID = "key_capture_id";
	
    /** 调用该类的使用者，activity或者fragment */
    private Object mMaster;
    private Context mContext;
    private IUploadPicture mFinishListener;
    
    private int mPictureType;
    private int mTid;
    
    public UploadPictureHelper(Object context, IUploadPicture finishListener){
        mMaster = context;
        mFinishListener = finishListener;
        
        if(mMaster instanceof Activity){
            mContext = (Activity)context;
        }
        else if(mMaster instanceof FragmentBase){
            mContext = ((FragmentBase)context).getActivity();
        }
        else{
            return;
        }
    }
    
    public void registerCallback(){
        EgmService.getInstance().addListener(mEgmCallback);
    }
    
    public void removeCallback(){
        EgmService.getInstance().removeListener(mEgmCallback);
    }
    
    private AlertDialog mUploadPicDialog;
    /** 上传照片入口：公众和私照 */
    public void showUploadUpPicEntrance(){
        if(mUploadPicDialog == null){
            mUploadPicDialog = EgmUtil.createEgmMenuDialog(mContext, 
                    mContext.getString(R.string.upload_picture), 
                    mContext.getResources().getStringArray(R.array.upload_picture), 
                    new View.OnClickListener() {
                            @Override
                        public void onClick(View view) {
                            int which = (Integer)view.getTag();

                            switch (which) {
                                case 0: // 上传私照
                                    mPictureType = EgmConstants.Photo_Type.TYPE_PRIVATE;
                                    ActivityPicUploadEntrance.startActivity(mContext,
                                            PicType.PRIVATE_PIC);
                                    mUploadPicDialog.dismiss();

                                    break;
                                case 1: // 上传公照
                                    mPictureType = EgmConstants.Photo_Type.TYPE_PUBLIC;
                                    ActivityPicUploadEntrance.startActivity(mContext,
                                            PicType.PUBLIC_PIC);
                                    mUploadPicDialog.dismiss();
                                    break;
                            }

                            if (mUploadPicDialog.isShowing()) {
                                mUploadPicDialog.dismiss();
                            }
                        }
                    });

        }
        
        mUploadPicDialog.setCanceledOnTouchOutside(false);
        mUploadPicDialog.show();
    }
    
    private AlertDialog mUploadPicSourceDialog;
    /** 上传照片来源：相机和相册 */
    public void showUploadUpPicSource(final int type){
        String title;
        if(type == EgmConstants.Photo_Type.TYPE_PRIVATE){
            title = mContext.getString(R.string.rec_upload_picture_private);
        }
        else{
            title = mContext.getString(R.string.rec_upload_picture_public);
        }
        
        mUploadPicSourceDialog = EgmUtil.createEgmMenuDialog(mContext, 
                title, 
                mContext.getResources().getStringArray(R.array.register_change_avatar), 
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int which = (Integer)view.getTag();
                        
                        switch (which) {
                        case 0: // 相机
                            mPictureType = type;
                            fromCamera();
                            break;
                        case 1: // 相册
                            FileExplorerActivity.startForUploadPicture(mContext, type, 
                                    EgmConstants.COUNT_MAX_UPLOAD_PICTURE, 
                                    EgmConstants.SIZE_MAX_PICTURE, EgmConstants.SIZE_MIN_PICTURE);
                            break;
                        }
                        
                        if(mUploadPicSourceDialog.isShowing()){
                            mUploadPicSourceDialog.dismiss();
                        }
                    }
                });
        
        mUploadPicSourceDialog.setCanceledOnTouchOutside(false);
        mUploadPicSourceDialog.show();
    }
    
    public void onSaveInstanceState(Bundle outState) {
        if(!TextUtils.isEmpty(mCameraImgId)){
            outState.putString(KEY_CAPTURE_ID, mCameraImgId);
        }
    }
    
    public void onRestoreId(Bundle savedInstanceState) {
        if(savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString(KEY_CAPTURE_ID))){
        	mCameraImgId = savedInstanceState.getString(KEY_CAPTURE_ID);
        }
    }
    
    private String mCameraImgId = null;
    private void fromCamera(){
        mCameraImgId = String.valueOf(java.lang.System.currentTimeMillis());  //用来生成图片名称
        
        if(mMaster instanceof Activity){
            ActivityUtil.capturePhotoForResult((Activity)mMaster, 
                    EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mCameraImgId), 
                    EgmConstants.REQUEST_CAPTURE_PHOTO);
        }
        else if(mMaster instanceof FragmentBase){
            ActivityUtil.capturePhotoForResult((FragmentBase)mMaster, 
                    EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mCameraImgId), 
                    EgmConstants.REQUEST_CAPTURE_PHOTO);
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
//            Toast.makeText(mContext, R.string.reg_tip_avatar_get_error, Toast.LENGTH_SHORT).show();
            return;
        }
        
        switch(requestCode){
            // 从相册里选取的已经在相册类里做了上传处理，这里只做从相机拍的照片的压缩和上传处理
            case EgmConstants.REQUEST_CAPTURE_PHOTO:    // 从相机拍照
                Uri uri;
                
                if(requestCode == EgmConstants.REQUEST_SELECT_PICTURE){
                    if(data == null){
                        Toast.makeText(mContext, R.string.reg_tip_avatar_get_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uri = data.getData();        //获得图片的uri 
                }
                else{
                	if (TextUtils.isEmpty(mCameraImgId)) {
            			return;
            		}
                	
                    uri = Uri.fromFile(new File(EgmUtil.getFilePathByType(EgmUtil.TYPE_CAMERA, mCameraImgId)));
                }
                
                // 判断图片是否符合尺寸要求，过大的图片进行缩小
                Bitmap result = ImageUtil.legitimateImageSize(mContext, uri, 
                        EgmConstants.SIZE_MAX_PICTURE, 
                        EgmConstants.SIZE_MIN_PICTURE);
                
                if(result == null){
                    Toast.makeText(mContext, R.string.reg_tip_avatar_get_error, Toast.LENGTH_SHORT).show();
                }
                else{
                    String path = ImageUtil.getBitmapFilePath(result, EgmConstants.TEMP_PROFILE_NAME);// 要先存到本地，否则图片过大会失败
                    result.recycle();
                    
                    if(mFinishListener != null){
                        mFinishListener.onStartUpload();
                    }
                    mTid = EgmService.getInstance().upLoadPicture(path, mPictureType, IsCameraPhotoFlag.CameraPhoto);
                }
                
                break;
        }
    }
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
        /**上传公开照和私照*/
        @Override
        public void onUploadPicSucess(int transactionId, PictureInfo obj){
            if(mTid != transactionId)
                return;
            
            EgmPrefHelper.putUpdatePicTime(mContext, java.lang.System.currentTimeMillis());
            Toast.makeText(mContext, R.string.rec_upload_picture_result2, Toast.LENGTH_SHORT).show();
            if(mFinishListener != null){
                mFinishListener.onFinishUpload();
            }
        }
        @Override
        public void onUploadPicError(int transactionId, int errCode, String err){
            if(mTid != transactionId)
                return;
            
            if(mFinishListener != null){
                mFinishListener.onFinishUpload();
            }
            Toast.makeText(mContext, err, Toast.LENGTH_SHORT).show();
        }
    };
    
    public interface IUploadPicture{
        public void onStartUpload();
        public void onFinishUpload();
    }
}
