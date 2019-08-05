package com.example.nyamori.mytestapplication;

import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nyamori.gles.EglCore;
import com.example.nyamori.gles.FullFrameRect;
import com.example.nyamori.gles.OffscreenSurface;
import com.example.nyamori.gles.Texture2dProgram;
import com.example.nyamori.gles.WindowSurface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private static final int MSG_UPDATE_IMG = 0;
    private static final int MSG_INIT_OUT = 1;

    private static final int UI_UPDATE_FPS_OPENGL = 10;

    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener;
    private Size mPreviewSize;
    private Size mSurfaceSize;
    private CameraManager mCameraManager;
    private String cameraID;
    private CameraDevice mCamera;
    private CameraCaptureSession mSession;
    private Handler mHandler;
    private CaptureRequest.Builder mPreviewBuilder;
    private TextView withOpenGL;

    private EglCore mEglCore;
    private SurfaceTexture mSurfaceTexture;
    private OffscreenSurface mOffscreenSurface;
    private WindowSurface mWindowSurface;
    private FullFrameRect mFullFrameRect;
    private float[] mTempMatrix=new float[16];
    private Surface mOutSurface;
    private int mTextureID=-1;
    private Handler mOpenGLHandler;
    private Handler mUIHandler;

    private int fpsCount;
    private long fpsTime;

    // TODO: 19-8-5 把camera操作全部移交到别的类 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.CAMERA"}, 1);
        }else {
            mEglCore=new EglCore(null,EglCore.FLAG_RECORDABLE);
            
            withOpenGL=(TextView)findViewById(R.id.with_OpenGL);

            initSurfaceView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            for(int i=0;i<permissions.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    initSurfaceView();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        destroyCamera();
        super.onDestroy();
    }

    private void initSurfaceView() {
        HandlerThread handlerThread = new HandlerThread("OpenGL Camera");
        handlerThread.start();
        mOpenGLHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_IMG:
                        mOffscreenSurface.makeCurrent();
                        mSurfaceTexture.updateTexImage();
                        mSurfaceTexture.getTransformMatrix(mTempMatrix);

                        mWindowSurface.makeCurrent();
                        int xStart=(mSurfaceSize.getWidth()-mPreviewSize.getHeight())/2;
                        int yStart=mSurfaceSize.getHeight()-mPreviewSize.getWidth();
                        GLES20.glViewport(xStart,yStart,mPreviewSize.getHeight(),mPreviewSize.getWidth());
                        mFullFrameRect.drawFrame(mTextureID,mTempMatrix);
                        mWindowSurface.swapBuffers();
                        fpsCount++;
                        long nowTime=System.currentTimeMillis();
                        if((nowTime-fpsTime)>999){
                            mUIHandler.obtainMessage(UI_UPDATE_FPS_OPENGL,fpsCount,0).sendToTarget();
                            fpsTime=nowTime;
                            fpsCount=0;
                        }
                        break;
                    case MSG_INIT_OUT:
                        initCamera(mSurfaceSize.getWidth(),mSurfaceSize.getHeight());
                        mOffscreenSurface=new OffscreenSurface(mEglCore,mPreviewSize.getWidth(),mPreviewSize.getHeight());
                        mOffscreenSurface.makeCurrent();
                        mFullFrameRect=new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT_BW));
                        mTextureID=mFullFrameRect.createTextureObject();
                        mSurfaceTexture.detachFromGLContext();
                        mSurfaceTexture.attachToGLContext(mTextureID);
                        mWindowSurface=new WindowSurface(mEglCore,mOutSurface,false);
                        fpsCount=0;
                        fpsTime=System.currentTimeMillis();
                        openCamera();
                        break;
                }
            }
        };

        mUIHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UI_UPDATE_FPS_OPENGL:
                        withOpenGL.setText("withOpenGL-FPS:"+msg.arg1);
                        break;
                }
            }
        };
        mTextureView = (TextureView) findViewById(R.id.m_texture_view);
        mSurfaceTextureListener=new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable: 开始初始化");
                mOutSurface=new Surface(surface);
                mSurfaceSize=new Size(width,height);
                Log.d(TAG, "onSurfaceTextureAvailable: size="+width+"x"+height);
                mOpenGLHandler.obtainMessage(MSG_INIT_OUT).sendToTarget();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };
        mSurfaceTexture=new SurfaceTexture(-1);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mOpenGLHandler.obtainMessage(MSG_UPDATE_IMG).sendToTarget();
            }
        });
    }

    private void openCamera() {
        HandlerThread handlerThread = new HandlerThread("My Camera2");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }else {
                mCameraManager.openCamera(cameraID, deviceStateCallback, mHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void destroyCamera() {
        if(mCamera!=null){
            mCamera.close();
            mCamera=null;
        }
    }

    private void initCamera(int width, int height) {
        mCameraManager = (CameraManager) getApplicationContext().getSystemService(CAMERA_SERVICE);
        try{
            for (String cameraID:mCameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics=mCameraManager.getCameraCharacteristics(cameraID);
                Integer facing=cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if(facing!=null&&facing==CameraCharacteristics.LENS_FACING_FRONT)continue;
                this.cameraID=cameraID;
                //获取相机角度
                Log.d(TAG, "initCamera: get size");
                mPreviewSize=getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class),width,height);
                mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getHeight(),mPreviewSize.getWidth());
                Log.d(TAG, "initCamera: new size="+mPreviewSize.toString());
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
            Toast.makeText(MainActivity.this,"相机开启失败",Toast.LENGTH_SHORT).show();
        }
    };

    public void takePreview() throws CameraAccessException {
        mPreviewBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(new Surface(mSurfaceTexture));
        mCamera.createCaptureSession(Arrays.asList(new Surface(mSurfaceTexture)),mSessionPreviewStateCallback, mHandler);
    }

    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if(mCamera==null)return;
            mSession = session;
            //配置完毕开始预览
            try {
                //自动对焦
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //无限次的重复获取图像
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Toast.makeText(MainActivity.this, "配置失败", Toast.LENGTH_SHORT).show();
        }
    };

    private Size getPreferredPreviewSize(Size[] sizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for (Size option : sizes) {
            if (width > height) {
                if (option.getWidth() < width || option.getHeight() < height) {
                    collectorSizes.add(option);
                    Log.d(TAG, "getPreferredPreviewSize:add size="+option.toString());
                }
            } else {
                if (option.getHeight() < width || option.getWidth() < height) {
                    collectorSizes.add(option);
                    Log.d(TAG, "getPreferredPreviewSize:add size="+option.toString());

                }
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

