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
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import com.example.nyamori.gles.EglCore;
import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.gles.WindowSurface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyCamera {
    private final static String TAG = "MyCamera";

    private Size mSurfaceSize;
    private Size mPreviewSize;

    private CameraManager mCameraManager;
    private String cameraID;
    private CameraDevice mCamera;
    private Handler mCameraHandler;
    private CaptureRequest.Builder mPreviewBuilder;

    private EglCore mEglCore;
    private SurfaceTexture mSurfaceTexture;
    private WindowSurface mWindowSurface;
    private Handler mOpenGLHandler;

    private int fpsCount;
    private long fpsTime;

    private int mTextureID=-1;

    private Handler mUIHandler;
    private Context mContext;
    private int cameraType;

    private My2DFilterManager my2DFilterManager;

    private long nowTime;

    public MyCamera(Handler mUIHandler,Surface mOutSurface,Context context){
        this.mUIHandler=mUIHandler;
        this.mContext=context;
        cameraType=Config.CAMERA_TYPE.FRONT_TYPE;
        MyFrameRect.setCameraType(cameraType);
        mEglCore=new EglCore(null,EglCore.FLAG_RECORDABLE);
        mWindowSurface=new WindowSurface(mEglCore,mOutSurface,false);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        initHandler();
    }

    public void init(int width,int height){
        mSurfaceSize=new Size(width,height);
        initCamera();
        mOpenGLHandler.obtainMessage(Config.OPenGLMsg.MSG_INIT_OUT).sendToTarget();
    }

    public void initCamera(){
        nowTime=System.currentTimeMillis();
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
                if(mSurfaceSize.getWidth()>mSurfaceSize.getHeight()){
                    mPreviewSize=getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class),mSurfaceSize.getWidth(),mSurfaceSize.getHeight());
                }else {
                    mPreviewSize=getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class),mSurfaceSize.getHeight(),mSurfaceSize.getWidth());
                    mPreviewSize=new Size(mPreviewSize.getHeight(),mPreviewSize.getWidth());
                    Log.d(TAG, "initCamera: new size="+mPreviewSize.toString());
                }
                long time=System.currentTimeMillis();
                Log.i(TAG, "initCamera: time="+(time-nowTime));
                nowTime=time;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void changeCamera(){
        mCamera.close();
        if(cameraType== Config.CAMERA_TYPE.FRONT_TYPE){
            cameraType= Config.CAMERA_TYPE.BACK_TYPE;
        }else {
            cameraType= Config.CAMERA_TYPE.FRONT_TYPE;
        }
        MyFrameRect.setCameraType(cameraType);
        initCamera();
        mOpenGLHandler.obtainMessage(Config.OPenGLMsg.MSG_CHANGE_CAMERA).sendToTarget();
    }

    public void destroyCamera() {
        if(mCamera!=null){
            mCamera.close();
            mCamera=null;
        }
        if(mSurfaceTexture!=null){
            mSurfaceTexture.release();
        }
        if(mTextureID!=-1){
            GlUtil.releaseTexture(mTextureID);
            mTextureID=-1;
        }
        if(my2DFilterManager!=null){
            my2DFilterManager.release();
        }
        if(mWindowSurface!=null){
            mWindowSurface.release();
        }
        if(mEglCore!=null){
            mEglCore.release();
        }
    }

    public void changeFilterType(int comboType){
        mOpenGLHandler.obtainMessage(Config.OPenGLMsg.MSG_CHANGE_TYPE,comboType,0).sendToTarget();
    }

    public void addFilter(int programType){
        mOpenGLHandler.obtainMessage(Config.OPenGLMsg.MSG_ADD_FILTER,programType,0).sendToTarget();
    }

    public List<String> getFilterList(){
        return my2DFilterManager.getFilterTypeList();
    }

    private void initHandler() {
        HandlerThread handlerThreadCamera = new HandlerThread("Camera");
        handlerThreadCamera.start();
        mCameraHandler = new Handler(handlerThreadCamera.getLooper());
        HandlerThread handlerThreadOpenGL = new HandlerThread("OpenGL");
        handlerThreadOpenGL.start();
        mOpenGLHandler = new Handler(handlerThreadOpenGL.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case Config.OPenGLMsg.MSG_INIT_OUT:
                        initOpenGL();
                        openCamera();
                        break;
                    case Config.OPenGLMsg.MSG_UPDATE_IMG:
                        updateImg();
                        break;
                    case Config.OPenGLMsg.MSG_CHANGE_TYPE:
                        my2DFilterManager.changeFilter(msg.arg1);
                        mUIHandler.obtainMessage(Config.UIMsg.UI_UPDATE_LIST).sendToTarget();
                        break;
                    case Config.OPenGLMsg.MSG_ADD_FILTER:
                        my2DFilterManager.addFilter(msg.arg1);
                        mUIHandler.obtainMessage(Config.UIMsg.UI_UPDATE_LIST).sendToTarget();
                        break;
                    case Config.OPenGLMsg.MSG_CHANGE_CAMERA:
                        changeCameraInOpenGL();
                        openCamera();
                        break;
                }
            }
        };
    }

    private void changeCameraInOpenGL() {
        int width=mPreviewSize.getWidth();
        int height=mPreviewSize.getHeight();
        int xStart = (mSurfaceSize.getWidth() - mPreviewSize.getWidth()) / 2;
        int yStart = (mSurfaceSize.getHeight() - mPreviewSize.getHeight());
        my2DFilterManager.changeCamera(width,height,xStart,yStart);
        mSurfaceTexture.setDefaultBufferSize(width,height);
    }


    private void updateImg() {
        mSurfaceTexture.updateTexImage();//更新了信息
        //设置了view的大小和起始坐标
        my2DFilterManager.draw(mTextureID);
        mWindowSurface.swapBuffers();

        fpsCount++;
        long nowTime=System.currentTimeMillis();
        if((nowTime-fpsTime)>999){
            mUIHandler.obtainMessage(Config.UIMsg.UI_UPDATE_FPS,fpsCount,0).sendToTarget();
            fpsTime=nowTime;
            fpsCount=0;
        }
    }

    private void initOpenGL() {
        int width=mPreviewSize.getWidth();
        int height=mPreviewSize.getHeight();
        int xStart = (mSurfaceSize.getWidth() - mPreviewSize.getWidth()) / 2;
        int yStart = (mSurfaceSize.getHeight() - mPreviewSize.getHeight());

        mWindowSurface.makeCurrent();
        my2DFilterManager=new My2DFilterManager(width,height,xStart,yStart);
        mTextureID=my2DFilterManager.createInputTextureObject();
        mSurfaceTexture=new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mOpenGLHandler.obtainMessage(Config.OPenGLMsg.MSG_UPDATE_IMG).sendToTarget();
            }
        });
        mSurfaceTexture.setDefaultBufferSize(width,height);

        long time=System.currentTimeMillis();
        Log.i(TAG, "initOpenGL: time="+(time-nowTime));
        nowTime=time;
    }

    private void openCamera() {
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

            long time=System.currentTimeMillis();
            Log.i(TAG, "Camera open: time="+(time-nowTime));
            nowTime=time;

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
        mPreviewBuilder.addTarget(new Surface(mSurfaceTexture));
        mCamera.createCaptureSession(Arrays.asList(new Surface(mSurfaceTexture)),mSessionPreviewStateCallback, mCameraHandler);
    }
    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {//配置完毕开始预览
            if(mCamera==null)return;
            fpsCount=0;
            fpsTime=System.currentTimeMillis();

            long time=System.currentTimeMillis();
            Log.i(TAG, "initSession: time="+(time-nowTime));
            nowTime=time;
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
}
