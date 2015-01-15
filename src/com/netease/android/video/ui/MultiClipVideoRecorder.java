package com.netease.android.video.ui;
public abstract interface MultiClipVideoRecorder
{
  public abstract void cancelClip();

  public abstract void endClip(boolean isFull);

  public abstract void startClip();
}