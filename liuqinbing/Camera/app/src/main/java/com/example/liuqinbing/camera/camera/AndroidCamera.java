package com.example.liuqinbing.camera.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.util.List;

/**
 * Created by liuqinbing on 19-8-9.
 */

public class AndroidCamera implements Camera.ErrorCallback, Camera.PreviewCallback {
    public static int getCameraNum() {
        return Camera.getNumberOfCameras();
    }
    private Camera mCamera;

    public void start(int index ,SurfaceTexture surface,
                      int width, int height) {
        relese();
        try {
            System.out.println("openCamera : " + index);
            mCamera = Camera.open(index);
            mCamera.setErrorCallback(this);
            Camera.Parameters mParameters = mCamera.getParameters();
            List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes();
            for(Camera.Size size : sizes) {
                System.out.println("size : " + size);
            }
            //set param
            mParameters.setPreviewSize(width, height);
            //如果开启后置摄像头
            if(index == 0) {
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//设置自动对焦
            }
            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(mParameters);
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
            System.out.println("openCamera : " + index + " over");
        }catch (Exception ex) {
            ex.printStackTrace();
            relese();
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        camera.addCallbackBuffer(bytes);
    }

    public void relese() {
        if(mCamera != null) {
            System.out.println("release Camera : " + mCamera);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onError(int error, Camera camera) {
        System.out.println("onCamera err : " + camera + " ," + error);
    }
}
