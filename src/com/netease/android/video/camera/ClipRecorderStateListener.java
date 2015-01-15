package com.netease.android.video.camera;
public abstract interface ClipRecorderStateListener
{
  public abstract void onClipRecorderStateChange(ClipRecorderStateListener.ClipRecorderState paramClipRecorderState);
  public enum ClipRecorderState{
	  PREPARING(0),RECORDING(1),STOPPING(2),STOPPED(3);
	  private int state;
	  private ClipRecorderState(int state){
		  this.state = state;
	  }
  }
}