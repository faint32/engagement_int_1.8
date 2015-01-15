package com.netease.android.camera;

/***
 * This class represents the condition that we cannot open the camera hardware
 * successfully. For example, another process is using the camera.
 */
public class CameraHardwareException extends Exception {

    public CameraHardwareException(Throwable t) {
        super(t);
    }
}
