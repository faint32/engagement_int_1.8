package com.netease.engagement.image.zoom;

import java.util.Observable;

/**
 * 记录图片缩放和移动等状态，能够对外通知发生状态变化。
 */
public class ZoomState extends Observable {
	/** 图片缩放的倍数，值越大图像越大 */
    private float mZoom;
    /** 控制图片水平方向移动的变量，值越大图片可视区域的左边界距离图片左边界越远，图像越靠左，值为0.5f时居中 */
    private float mPanX;
    /** 控制图片竖直方向移动的变量，值越大图片可视区域的上边界距离图片上边界越远，图像越靠上，值为0.5f时居中 */
    private float mPanY;

    public ZoomState() {
        super();
        reset();
    }

    public float getPanX() {
        return mPanX;
    }

    public float getPanY() {
        return mPanY;
    }

    public float getZoom() {
        return mZoom;
    }

    public void setPanX(float panX) {
        if (panX != mPanX) {
            mPanX = panX;
            setChanged();
        }
    }

    public void setPanY(float panY) {
        if (panY != mPanY) {
            mPanY = panY;
            setChanged();
        }
    }

    public void setZoom(float zoom) {
        if (zoom != mZoom) {
            mZoom = zoom;
            setChanged();
        }
        doCallback();
    }

    public float getZoomX(float aspectQuotient) {
        return Math.min(mZoom, mZoom * aspectQuotient);
    }

    public float getZoomY(float aspectQuotient) {
        return Math.min(mZoom, mZoom / aspectQuotient);
    }
    
    public ZoomState reset() {
        setPanX(0.5f);
        setPanY(0.5f);
        setZoom(1f);
        notifyObservers();
        return this;
    }

    public boolean isZoomed() {
        if(getPanX() == 0.5f && getPanY() == 0.5f && getZoom() == 1f) {
            return false;
        } else {
            return true;
        }
    }
    
    private IZoomStateCallBack callback;
    public interface IZoomStateCallBack{
    	public void doCallback();
    }
    public void setActivity(IZoomStateCallBack v){
    	callback = v;
    }
    public void doCallback(){
    	if(callback != null){
    		callback.doCallback();
    	}
    }
}
