package com.netease.engagement.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.netease.common.cache.CacheManager;
import com.netease.common.http.filedownload.FileDownloadListener;
import com.netease.common.http.filedownload.FileDownloadManager;
import com.netease.common.log.NTLog;
import com.netease.common.task.TransTypeCode;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants.BUNDLE_KEY;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.EgmServiceCode;


public class FragmentDownload extends FragmentBase {
    private TextView mProgressText;
    private ProgressBar mProgressBar;
    private String mUrl;
    private AlertDialog mDialog;
    private boolean mCancelAble;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    		super.onCreate(savedInstanceState);
    		Intent intent = getActivity().getIntent();
    		if(intent != null){
    			mCancelAble = intent.getBooleanExtra(BUNDLE_KEY.DOWNLOAD_CANCELABLE, true);
    		}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_download,container,false);
        initViews(root);
        return root;
    }
    
    private void initViews(View root) {
        mProgressText = (TextView)root.findViewById(R.id.download_progress_text);
        mProgressBar = (ProgressBar)root.findViewById(R.id.download_progress);
        Intent i = getActivity().getIntent();
        if (i != null) {
            mUrl = i.getStringExtra(BUNDLE_KEY.UPDATE_URL);
        }
        String path = CacheManager.getRoot();
        FileDownloadManager.getInstance().downloadFile(mUrl, path, "",listener);
    }
    
    private void updateProgress(int percent){
        if(percent < 0){
            percent = 0;
        }
        if(percent > 100){
            percent = 100;
        }
        mProgressText.setText(percent + "%");
        mProgressBar.setProgress(percent);
        
    }
    
    FileDownloadListener listener = new FileDownloadListener(){

        @Override
        public void onSuccess(String path) {
            NTLog.i("FragmentDownload", "onSuccess path = " + path);
            if (mDialog !=null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
            showToast(R.string.download_success_install);
            try {
				Uri uri = Uri.parse("file://" + path);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setDataAndType(uri, "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); 
				startActivity(intent);
			} catch (Exception e) {
			}
            getActivity().finish();
        }

        @Override
        public void onFailed(String err, int errCode) {
            if(errCode == EgmServiceCode.ERR_CODE_NO_NETWORK 
            		|| errCode == EgmServiceCode.ERR_CODE_NETWORK_IOEXCEPTION 
            		|| errCode == TransTypeCode.ERR_CODE_NETWORK_EXCEPTION){
                errCode = EgmServiceCode.NETWORK_ERR_COMMON;
                err = ErrorToString.getString(errCode);
            } else {
                err = getResources().getString(R.string.download_error);
            }
            showToast(err);
            getActivity().finish();
        }

        @Override
        public void onProgress(long current, long total, int percent, int speed) {
            updateProgress(percent);
        }
        
    };
    
    public void onBack(){
    	    if(!mCancelAble)
    	    		return;
        mDialog = EgmUtil.createEgmBtnDialog(getActivity(), getResources().getString(R.string.tip), getResources().getString(R.string.download_cancel_confirm),
                getResources().getString(R.string.cancel),  getResources().getString(R.string.confirm),
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        int which = (Integer)view.getTag();
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                FileDownloadManager.getInstance().cancelDownload(mUrl);
                                getActivity().finish();
                                break;
                            default:
                                break;
                        }

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                            mDialog = null;
                        }
                    }
                });
        mDialog.show(); 
       
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDialog !=null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    };
  
}
