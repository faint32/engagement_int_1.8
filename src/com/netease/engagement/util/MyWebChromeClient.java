
package com.netease.engagement.util;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

public class MyWebChromeClient extends WebChromeClient {

    public static interface OnOpenFileChooserListener {

        void onOpen();
    }

    private OnOpenFileChooserListener mListener;
    private ValueCallback<Uri> mCallback;

    public void setOnOpenFileChooserListener(OnOpenFileChooserListener listener) {
        mListener = listener;
    }

    public OnOpenFileChooserListener getOnOpenFileChooserListener(OnOpenFileChooserListener listener) {
        return mListener;
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        mCallback = uploadMsg;
        if (mListener != null) {
            mListener.onOpen();
        }
    }

    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        mCallback = uploadMsg;
        if (mListener != null) {
            mListener.onOpen();
        }
    }

    // For Android > 4.1.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mCallback = uploadMsg;
        if (mListener != null) {
            mListener.onOpen();
        }
    }

    public ValueCallback<Uri> getValueCallback() {
        return mCallback;
    }
}
