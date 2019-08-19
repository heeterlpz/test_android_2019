package com.example.nyamori.mytestapplication;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyCamera {
    private final static String TAG = "MyCamera";

    private Camera mCamera;
    private SurfaceTexture targetSurface;

    private int cameraType;

    public MyCamera(){
        cameraType=Config.CAMERA_TYPE.FRONT_TYPE;
        MyFrameRect.setCameraType(cameraType);
    }

    public ViewPort initCamera(int width,int height){
        Log.d(TAG, "initCamera: size="+width+"*"+height);
        Camera.Size mPreviewSize;
        if(cameraType==Config.CAMERA_TYPE.BACK_TYPE){
            mCamera=Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }else {
            mCamera=Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        Camera.Parameters parameters=mCamera.getParameters();
        List<Camera.Size> sizes=parameters.getSupportedPreviewSizes();
        //获取预览尺寸
        if(width>height){
            mPreviewSize=getPreferredPreviewSize(sizes,width,height);
            parameters.setPreviewSize(mPreviewSize.width,mPreviewSize.height);
        }else {
            mPreviewSize=getPreferredPreviewSize(sizes,height,width);
            parameters.setPreviewSize(mPreviewSize.width,mPreviewSize.height);
            int temp=mPreviewSize.width;
            mPreviewSize.width=mPreviewSize.height;
            mPreviewSize.height=temp;
            Log.d(TAG, "initCamera: new size="+mPreviewSize.width+"*"+mPreviewSize.height);
        }
        if(mPreviewSize.width<width&&mPreviewSize.height<height){
            int newHeight=(int)(mPreviewSize.height*((float)width/mPreviewSize.width));
            Log.d(TAG, "initCamera: new heigth="+newHeight);
            int yStart = (height - newHeight);
            Log.d(TAG, "initCamera: xStart=0 yStart="+yStart);
            return new ViewPort(0,yStart,width,newHeight);
        }else {
            int xStart = (width - mPreviewSize.width) / 2;
            int yStart = (height - mPreviewSize.height);
            Log.d(TAG, "initCamera: xStart="+xStart+"yStart="+yStart);
            return new ViewPort(xStart,yStart,mPreviewSize.width,mPreviewSize.height);
        }
    }

    public void setTargetSurface(SurfaceTexture surface){
        targetSurface=surface;
    }

    public ViewPort changeCamera(int width,int height){
        mCamera.release();
        if(cameraType== Config.CAMERA_TYPE.FRONT_TYPE){
            cameraType= Config.CAMERA_TYPE.BACK_TYPE;
        }else {
            cameraType= Config.CAMERA_TYPE.FRONT_TYPE;
        }
        MyFrameRect.setCameraType(cameraType);
        ViewPort viewPort=initCamera(width,height);
        openCamera();
        return viewPort;
    }

    public void openCamera(){
        try {
            mCamera.setPreviewTexture(targetSurface);
            mCamera.startPreview();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destoryCamera(){
        if(mCamera!=null){
            mCamera.release();
            mCamera=null;
        }
    }

    private Camera.Size getPreferredPreviewSize(@NonNull List<Camera.Size> sizes, int width, int height) {
        List<Camera.Size> collectorSizes = new ArrayList<>();
        for (Camera.Size option : sizes) {
            Log.d(TAG, "getPreferredPreviewSize:size="+option.width+"*"+option.height);
            if (option.width >=width && option.height >=height) {
                collectorSizes.add(option);
            }
        }
        if (collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size s1, Camera.Size s2) {
                    return Long.signum(s1.width * s1.height - s2.width * s2.height);
                }
            });
        }
        return sizes.get(0);
    }
}
