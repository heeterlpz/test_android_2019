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
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private TextureView mTextureView;
    private TextureView textureViewAfterEdit;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener;
    private Size mPreviewSize;
    private CameraManager mCameraManager;
    private String cameraID;
    private CameraDevice mCamera;
    private CameraCaptureSession mSession;
    private Handler mHandler;
    private CaptureRequest.Builder mPreviewBuilder;
    private ImageReader imageReader;
    private Surface mSurface;
    private DrawSurface drawSurface;

    // TODO: 19-8-2 整理mainActivity结构
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
        Log.d(TAG, "onResume: start");
        if(drawSurface!=null
                &&drawSurface.isPause()==true)drawSurface.startThread();
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        textureViewAfterEdit.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable: use available");
                SurfaceTexture surfaceTexture=textureViewAfterEdit.getSurfaceTexture();
                surfaceTexture.setDefaultBufferSize(width,height);
                mSurface=new Surface(surfaceTexture);
                drawSurface=new DrawSurface(width,height,mSurface,getApplicationContext());
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable: use size changed");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTextureAvailable: use destroyed");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                Log.d(TAG, "onSurfaceTextureAvailable: use update");
            }
        });
    }

    @Override
    protected void onPause() {
        if(drawSurface.isPause()==false)drawSurface.pauseThread();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        destroyCamera();
        super.onDestroy();
    }

    private void initSurfaceView() {
        textureViewAfterEdit=(TextureView)findViewById(R.id.texture_view_after_edit);
        mTextureView = (TextureView) findViewById(R.id.m_texture_view);
        mSurfaceTextureListener=new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                initCamera(width,height);
                Log.d(TAG, "onSurfaceTextureAvailable: 初始化成功");
                openCamera();
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
    }

    private void openCamera() {
        HandlerThread handlerThread = new HandlerThread("My First Camera2");
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
        if(imageReader!=null){
            imageReader.close();
            imageReader=null;
        }
        if(drawSurface!=null){
            drawSurface.closeThread();
            drawSurface=null;
        }
    }

    private void initCamera(int width, int height) {
        // TODO: 19-7-31 专门写个size相关的初始化函数
        mPreviewSize=new Size(width,height);
        mCameraManager = (CameraManager) getApplicationContext().getSystemService(CAMERA_SERVICE);
        try{
            for (String cameraID:mCameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics=mCameraManager.getCameraCharacteristics(cameraID);
                Integer facing=cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                Log.d(TAG, "initCamera: facing="+facing);
                Log.d(TAG, "initCamera: cameraID="+cameraID);
                if(facing!=null&&facing==CameraCharacteristics.LENS_FACING_FRONT)continue;
                this.cameraID=cameraID;
                //获取相机角度

                initImageReader();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initImageReader() {
        imageReader=ImageReader.newInstance(mPreviewSize.getWidth(),mPreviewSize.getHeight(),
                ImageFormat.YUV_420_888,2); //设置的格式为yuv420，其实可以直接设置为nv21
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onImageAvailable(ImageReader reader) {
                //获得相机数据YUV420
                Image mImage = reader.acquireLatestImage();
                if(mImage == null) {
                    Log.e(TAG, "onImageAvailable .............mImage == null");
                    return;
                }
                if(drawSurface!=null) {
                    YuvImage yuv=new YuvImage(getDataFromImage(mImage),ImageFormat.NV21,mImage.getWidth(),mImage.getHeight(),null);
                    drawSurface.putNV21Data(yuv);
                }
                mImage.close();
            }
        },mHandler);
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
        SurfaceTexture surfaceTexture=mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
        Surface previewSurface=new Surface(surfaceTexture);
        mPreviewBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(previewSurface);
        mPreviewBuilder.addTarget(imageReader.getSurface());
        mCamera.createCaptureSession(Arrays.asList(previewSurface,imageReader.getSurface()), mSessionPreviewStateCallback, mHandler);
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
                //打开闪光灯
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
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

    //解析yuv420数据为nv21
    private static byte[] getDataFromImage(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }
}

