package com.example.nyamori.mytestapplication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyCamera {
    private final static String TAG = "MyCamera";

    private CameraManager mCameraManager;
    private String cameraID;
    private CameraDevice mCamera;
    private Handler mCameraHandler;
    private CaptureRequest.Builder mPreviewBuilder;
    private Surface targetSurface;

    private Context mContext;
    private int cameraType;

    public MyCamera(Context context){
        this.mContext=context;
        cameraType=Config.CAMERA_TYPE.FRONT_TYPE;
        MyFrameRect.setCameraType(cameraType);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        HandlerThread handlerThreadCamera = new HandlerThread("Camera");
        handlerThreadCamera.start();
        mCameraHandler = new Handler(handlerThreadCamera.getLooper());
    }

    public ViewPort initCamera(int width,int height){
        Size mPreviewSize=new Size(width,height);
        try{
            for (String cameraID:mCameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics=mCameraManager.getCameraCharacteristics(cameraID);
                Integer facing=cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if(cameraType==Config.CAMERA_TYPE.BACK_TYPE){
                    if(facing!=null&&facing==CameraCharacteristics.LENS_FACING_FRONT)continue;
                }else {
                    if(facing!=null&&facing==CameraCharacteristics.LENS_FACING_BACK)continue;
                }
                this.cameraID=cameraID;
                // TODO: 19-8-5 获取相机角度
                //获取预览尺寸
                if(width>height){
                    mPreviewSize=getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class),width,height);
                }else {
                    mPreviewSize=getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class),height,width);
                    mPreviewSize=new Size(mPreviewSize.getHeight(),mPreviewSize.getWidth());
                    Log.d(TAG, "initCamera: new size="+mPreviewSize.toString());
                }
            }
            int xStart = (width - mPreviewSize.getWidth()) / 2;
            int yStart = (height - mPreviewSize.getHeight());
            return new ViewPort(xStart,yStart,mPreviewSize.getWidth(),mPreviewSize.getHeight());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return new ViewPort(0,0,mPreviewSize.getWidth(),mPreviewSize.getHeight());
    }

    public ViewPort changeCamera(int width,int height){
        mCamera.close();
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

    public void destroyCamera() {
        if(mCamera!=null){
            mCamera.close();
            mCamera=null;
        }
    }

    public void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }else {
                mCameraManager.openCamera(cameraID, deviceStateCallback, mCameraHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private CameraDevice.StateCallback deviceStateCallback=new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCamera=camera;
            try{
                takePreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if(mCamera!=null){
                mCamera.close();
                mCamera=null;
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "onError: 相机未开启 error code="+error);
            Toast.makeText(mContext,"相机开启失败",Toast.LENGTH_SHORT).show();
        }
    };

    private void takePreview() throws CameraAccessException {
        mPreviewBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(targetSurface);
        mCamera.createCaptureSession(Arrays.asList(targetSurface),mSessionPreviewStateCallback, mCameraHandler);
    }
    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {//配置完毕开始预览
            if(mCamera==null)return;

            try {
                //自动对焦
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //无限次的重复获取图像
                session.setRepeatingRequest(mPreviewBuilder.build(), null, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Toast.makeText(mContext, "配置失败", Toast.LENGTH_SHORT).show();
        }
    };

    private Size getPreferredPreviewSize(@NonNull Size[] sizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for (Size option : sizes) {
            Log.d(TAG, "getPreferredPreviewSize:size="+option.toString());
            if (option.getWidth() < width || option.getHeight() < height) {
                collectorSizes.add(option);
            }
        }
        if (collectorSizes.size() > 0) {
            return Collections.max(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size s1, Size s2) {
                    return Long.signum(s1.getWidth() * s1.getHeight() - s2.getWidth() * s2.getHeight());
                }
            });
        }
        return sizes[0];
    }

    public void setTargetSurface(Surface surface) {
        targetSurface=surface;
    }
}
