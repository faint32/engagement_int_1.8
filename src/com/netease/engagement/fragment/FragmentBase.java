
package com.netease.engagement.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.netease.framework.activity.IConfigChangeInstance;

/***
 * 父的Fragment, 公共方法入口
 * 
 * @author echo
 * @since 2013-04-09
 */

public abstract class FragmentBase extends Fragment implements IConfigChangeInstance {

    private boolean mIsInited = true;
    private boolean mNeedLoad = true;
    private ProgressDialog mWaitingProgress;

    /**
     * 保存数据
     */
    public Object onRetainCustomNonConfigurationInstance() {
        return null;
    }

    @Override
    public void onSave() {
//        Object value = onRetainCustomNonConfigurationInstance();
//        if (null != value) {
//            ConfigChangeDataMgr.getInstance().put(getTag(), value);
//        }
    }

    /**
     * 恢复数据
     * 
     * @param value
     */
    public void onRestoreCustomNonConfigurationInstance(Object value) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        Object value = ConfigChangeDataMgr.getInstance().get(getTag());
//        if (null != value) {
//            onRestoreCustomNonConfigurationInstance(value);
//        }
//        NTLog.i("FragmentBase", "onActivityCreated");
    }
    
    public boolean onBackPressed() {
    	return false;
    }

    public void setInited(boolean isInited) {
        this.mIsInited = isInited;
    }

    public boolean isInited() {
        return mIsInited;
    }

    public void setNeedload(boolean needLoad) {
        this.mNeedLoad = needLoad;
        if (!needLoad) {
            mIsInited = false;
        }
    }

    public boolean isNeedLoad() {
        return mNeedLoad;
    }

    /**
     * 用于首次进入未加载数据的fragment,在切过去需要加载数据的情况
     */
    public void loadData() {
        if (!mIsInited) {
            if (onLoadData()) {
                mIsInited = true;
            }
        }
    }

    /**
     * 需子类重载的方法，已处理请返回 true
     */
    public boolean onLoadData() {
        return false;
    }

    /**
     * 用于中间刷新数据，子类需响应请重载
     */
    public void refreshData() {

    }

    /**
     * 设置是否显示菜单，默认显示，不显示的页面重载该方法返回false
     */
    public boolean needShowMenu() {
        return true;
    }

    /**
     * Toast 提示
     * 
     * @param resid
     */
    public void showToast(int resid) {
        showToast(resid, Toast.LENGTH_SHORT);
    }

    /**
     * Toast 提示
     * 
     * @param resid
     * @param duration
     */
    public void showToast(int resid, int duration) {
        Context context = getActivity();
        if (context != null) {
            Toast.makeText(context, resid, duration).show();
        }
    }

    /**
     * Toast 提示
     * 
     * @param message
     */
    public void showToast(String message) {
        Context context = getActivity();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Toast 提示
     * 
     * @param message
     * @param duration
     */
    public void showToast(String message, int duration) {
        Context context = getActivity();
        if (context != null) {
            Toast.makeText(context, message, duration).show();
        }
    }

    /**
     * Toast 提示
     * 
     * @param message
     */
    public void showToast(CharSequence message) {
        Context context = getActivity();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Toast 提示
     * 
     * @param message
     * @param duration
     */
    public void showToast(CharSequence message, int duration) {
        Context context = getActivity();
        if (context != null) {
            Toast.makeText(context, message, duration).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onLeftTopBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onLeftTopBack() {
        // getFragmentManager().popBackStack();
        if(false) {
            
        } else {
            InputMethodManager im = (InputMethodManager)getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (im.isActive() && getActivity().getCurrentFocus() != null) {
                im.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
            getActivity().finish();
        }
    }
    
    protected void clickBack() {
        InputMethodManager im = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im.isActive() && getActivity().getCurrentFocus() != null) {
            im.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
        
        FragmentManager manager = getFragmentManager();
        if(manager.getBackStackEntryCount() > 0){
            manager.popBackStack();
        }
        else{
            getActivity().finish();
        }
    }

    public void showWatting(String message) {
        showWatting("", message);
    }

    public void showWatting(String title, String message) {
        if (mWaitingProgress != null)
            stopWaiting();
        if(getActivity() == null)
        		return;
        if(title == null)
        		title = "";

        mWaitingProgress = ProgressDialog.show(getActivity(), title, message, true, true);
    }
    
    public void showWatting(String title, String message, boolean cancle) {
        if (mWaitingProgress != null)
            stopWaiting();
        if(getActivity() == null)
    			return;
	    if(title == null)
	    		title = "";

        mWaitingProgress = ProgressDialog.show(getActivity(), title, message, true, true);
        mWaitingProgress.setCancelable(cancle);
    }

    public void stopWaiting() {
        if (mWaitingProgress != null) {
            mWaitingProgress.dismiss();
            mWaitingProgress = null;
        }
    }

    public boolean isWaiting() {
        return (mWaitingProgress != null && mWaitingProgress.isShowing());
    }
}
