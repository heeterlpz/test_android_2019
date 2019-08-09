package com.starnet.heeter.openglcamerasample.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.util.List;

/**
 * Created on 18-12-29.
 */

public class AndroidCamera implements Camera.ErrorCallback, Camera.PreviewCallback {
    public static int getCameraNum() {
        return Camera.getNumberOfCameras();
    }
    private Camera mCamera;

    public void start(int index ,SurfaceTexture surface,
                            int width, int height) {
        relese();//
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
