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

// TODO: 19-8-9 解决整体的时延问题 
public class MyCamera {
    private final static String TAG = "MyCamera";

    private Size mPreviewSize;

    private CameraManager mCameraManager;
    private String cameraID;
    private CameraDevice mCamera;
    private Handler mCameraHandler;
    private CaptureRequest.Builder mPreviewBuilder;

    private EglCore mEglCore;
    private SurfaceTexture mSurfaceTexture;
    private WindowSurface mWindowSurface;
    private float[] mMatrix=new float[16];
    private Handler mOpenGLHandler;

    private int fpsCount;
    private long fpsTime;

    private int mTextureID=-1;
    private int xStart;
    private int yStart;

    private Handler mUIHandler;
    private Context mContext;

    private My2DFilterManager my2DFilterManager;

    public MyCamera(Handler mUIHandler,Surface mOutSurface,Context context){
        this.mUIHandler=mUIHandler;
        this.mContext=context;
        mEglCore=new EglCore(null,EglCore.FLAG_RECORDABLE);
        mWindowSurface=new WindowSurface(mEglCore,mOutSurface,false);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        initHandler();
    }

    public void initCamera(int width, int height){
        try{
            for (String cameraID:mCameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics=mCameraManager.getCameraCharacteristics(cameraID);
                Integer facing=cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if(facing!=null&&facing==CameraCharacteristics.LENS_FACING_FRONT)continue;
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
                xStart = (width - mPreviewSize.getWidth()) / 2;
                yStart = (height - mPreviewSize.getHeight());
                mOpenGLHandler.obtainMessage(MsgConfig.OPenGLMsg.MSG_INIT_OUT).sendToTarget();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void destroyCamera() {
        if(mCamera!=null){
            mCamera.close();
            mCamera=null;
        }
        if(mWindowSurface!=null){
            mWindowSurface.release();
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
        if(mEglCore!=null){
            mEglCore.release();
        }
    }

    public void changeCameraType(){

    }

    public void addFilter(int programType){
        mOpenGLHandler.obtainMessage(MsgConfig.OPenGLMsg.MSG_ADD_FILTER,programType,0).sendToTarget();
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
                    case MsgConfig.OPenGLMsg.MSG_INIT_OUT:
                        initOpenGL();
                        break;
                    case MsgConfig.OPenGLMsg.MSG_UPDATE_IMG:
                        updateImg();
                        break;
                    case MsgConfig.OPenGLMsg.MSG_CHANGE_TYPE:
                        changeType(msg.arg1);
                        break;
                    case MsgConfig.OPenGLMsg.MSG_ADD_FILTER:
                        Log.d(TAG, "handleMessage: add filter");
                        my2DFilterManager.addFilter(msg.arg1);
                        break;
                }
            }
        };
    }

    private void changeType(int code) {
        switch (code){
            case MsgConfig.MsgType.NO_TYPE:
                break;
            case MsgConfig.MsgType.OBSCURE_TYPE:
                break;
            case MsgConfig.MsgType.SHARPENING_TYPE:
                break;
            case MsgConfig.MsgType.EDGE_TYPE:
                break;
            case MsgConfig.MsgType.EMBOSS_TYPE:
                break;
            default:
                break;
        }
    }


    private void updateImg() {
        mSurfaceTexture.updateTexImage();//更新了信息
        mSurfaceTexture.getTransformMatrix(mMatrix);
        //设置了view的大小和起始坐标
        my2DFilterManager.draw(mMatrix,mTextureID);
        mWindowSurface.swapBuffers();

        fpsCount++;
        long nowTime=System.currentTimeMillis();
        if((nowTime-fpsTime)>999){
            mUIHandler.obtainMessage(MsgConfig.UIMsg.UI_UPDATE_FPS,fpsCount,0).sendToTarget();
            fpsTime=nowTime;
            fpsCount=0;
        }
    }

    private void initOpenGL() {
        mWindowSurface.makeCurrent();
        mSurfaceTexture=new SurfaceTexture(-1);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mOpenGLHandler.obtainMessage(MsgConfig.OPenGLMsg.MSG_UPDATE_IMG).sendToTarget();
            }
        });
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
        my2DFilterManager=new My2DFilterManager(mPreviewSize.getWidth(),mPreviewSize.getHeight(),xStart,yStart);
        mTextureID=my2DFilterManager.createInputTextureObject();
        mSurfaceTexture.detachFromGLContext();
        mSurfaceTexture.attachToGLContext(mTextureID);
        fpsCount=0;
        fpsTime=System.currentTimeMillis();
        openCamera();
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
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if(mCamera==null)return;
            CameraCaptureSession mSession = session;
            //配置完毕开始预览
            try {
                //自动对焦
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //无限次的重复获取图像
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mCameraHandler);
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
