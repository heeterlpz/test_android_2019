package com.example.nyamori.mytestapplication;

import android.content.Context;
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
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import com.megvii.facepp.sdk.Facepp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyCamera2 {
    private final static String TAG = "MyCamera2";

    private CameraManager mCameraManager;
    private String cameraID;
    private CameraDevice mCamera;
    private Handler mCameraHandler;
    private CaptureRequest.Builder mPreviewBuilder;
    private Surface targetSurface;

    private ImageReader imageReader;

    private Context mContext;
    private int cameraType;
    private Facepp facepp;
    private List<Image> imageList;
    private Thread faceThread;
    private boolean isThradRunning=false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyCamera2(Context context){
        this.mContext=context.getApplicationContext();
        cameraType=Config.CAMERA_TYPE.FRONT_TYPE;
        MyFrameRect.setCameraType(cameraType);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        HandlerThread handlerThreadCamera = new HandlerThread("Camera");
        handlerThreadCamera.start();
        mCameraHandler = new Handler(handlerThreadCamera.getLooper());
        initThread();
    }

    private void initThread() {
        faceThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThradRunning){
                    if(imageList.size()>0){
                        Image image=imageList.get(0);
                        imageList.remove(0);

                        int width=image.getWidth();
                        int height=image.getHeight();

                        byte[] data=getDataFromImage(image);//在这个函数里面close了

                        Facepp.Face[] faces = facepp.detect(data, width, height, Facepp.IMAGEMODE_NV21);
                        Faces.clearList();
                        if(faces.length>0){
                            for(Facepp.Face face:faces){
                                Faces.setFaceInfoList(face.rect,width,height);
                                facepp.getLandmarkRaw(face,Facepp.FPP_GET_LANDMARK81);
                                Faces.setPoints(face.points,width,height);
                            }
                            Faces.setFaceNumber(faces.length);
                        }else {
                            Log.v(TAG, "there is no face");
                        }
                        if(imageList.size()>5){//防止可能的溢出，目前没有观测到。
                            imageList.clear();
                        }
                    }else {
                        Log.v(TAG, "run: no picture");
                        try {
                            Thread.sleep(33);//沉睡一帧
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void initFaceEngine(){
        if(imageList==null)imageList=new ArrayList<>();
        else imageList.clear();
        Faces.setCamera(cameraType);
        
        facepp = new Facepp();
        Log.d("megvii", "initFaceEngine: time="+Facepp.getApiExpirationMillis(mContext,MainActivity.getFileContent(mContext, R.raw.megviifacepp_0_5_2_model)));
        String errorCode = facepp.init(mContext, MainActivity.getFileContent(mContext, R.raw.megviifacepp_0_5_2_model), 1);
        if(errorCode!=null)Log.e("megvii", "MyCamera2: error code="+errorCode);
        else Log.i("megvii", "initFaceEngine: success");
        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        faceppConfig.detectionMode= Facepp.FaceppConfig.DETECTION_MODE_TRACKING_FAST;
        faceppConfig.interval=25;
        if(cameraType==Config.CAMERA_TYPE.FRONT_TYPE){
            faceppConfig.rotation=270;
        }else {
            faceppConfig.rotation=90;
        }
        facepp.setFaceppConfig(faceppConfig);
    }

    public ViewPort initCamera(int width,int height){
        initFaceEngine();
        Size previewSize = new Size(width, height);
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
                //获取预览尺寸
                if(width>height){
                    previewSize =getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class),width,height);
                }else {
                    previewSize =getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class),height,width);
                    previewSize =new Size(previewSize.getHeight(), previewSize.getWidth());
                    Log.d(TAG, "initCamera: new size="+ previewSize.toString());
                }
            }
            imageReader=ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(),
                    ImageFormat.YUV_420_888,5);
            if(previewSize.getWidth()<width&& previewSize.getHeight()<height){
                int newHeight=(int)(previewSize.getHeight()*((float)width/ previewSize.getWidth()));
                Log.d(TAG, "initCamera: new heigth="+newHeight);
                int yStart = (height - newHeight);
                Log.d(TAG, "initCamera: xStart=0 yStart="+yStart);
                return new ViewPort(0,yStart,width,newHeight);
            }else {
                int xStart = (width - previewSize.getWidth()) / 2;
                int yStart = (height - previewSize.getHeight());
                Log.d(TAG, "initCamera: xStart="+xStart+"yStart="+yStart);
                return new ViewPort(xStart,yStart, previewSize.getWidth(), previewSize.getHeight());
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return new ViewPort(0,0, previewSize.getWidth(), previewSize.getHeight());
    }

    public void setTargetSurface(final SurfaceTexture surface) {
        if(surface!=null) targetSurface=new Surface(surface);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image=reader.acquireLatestImage();
                if(image!=null){
                    imageList.add(image);//在线程里面要记得关闭close image
                }
            }
        },mCameraHandler);
    }

    public ViewPort changeCamera(int width,int height){
        mCamera.close();
        imageReader.close();
        facepp.release();
        isThradRunning=false;
        if(cameraType== Config.CAMERA_TYPE.FRONT_TYPE){
            cameraType= Config.CAMERA_TYPE.BACK_TYPE;
        }else {
            cameraType= Config.CAMERA_TYPE.FRONT_TYPE;
        }
        MyFrameRect.setCameraType(cameraType);
        ViewPort viewPort=initCamera(width,height);
        setTargetSurface(null);
        openCamera();
        return viewPort;
    }

    public void destroyCamera() {
        isThradRunning=false;
        if(mCamera!=null){
            mCamera.close();
            mCamera=null;
        }
        if(imageReader!=null){
            imageReader.close();
        }
        if(facepp!=null){
            facepp.release();
        }
        if(imageList!=null){
            imageList.clear();
        }
    }

    public void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("no camera permission");
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
        mPreviewBuilder.addTarget(imageReader.getSurface());
        mCamera.createCaptureSession(Arrays.asList(targetSurface,imageReader.getSurface()),mSessionPreviewStateCallback, mCameraHandler);
    }
    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {//配置完毕开始预览
            if(mCamera==null)return;

            isThradRunning=true;
            faceThread.start();
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
            if (option.getWidth() >= width || option.getHeight() >= height) {
                collectorSizes.add(option);
            }
        }
        if (collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size s1, Size s2) {
                    return Long.signum(s1.getWidth() * s1.getHeight() - s2.getWidth() * s2.getHeight());
                }
            });
        }
        return sizes[0];
    }

    // TODO: 19-8-26 引入libyuv
    private static byte[] getDataFromImage(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        Log.v(TAG, "get data from " + planes.length + " planes");
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

            Log.v(TAG, "pixelStride " + pixelStride);
            Log.v(TAG, "rowStride " + rowStride);
            Log.v(TAG, "width " + width);
            Log.v(TAG, "height " + height);
            Log.v(TAG, "buffer size " + buffer.remaining());
            
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
        image.close();//这里close了图片
        return data;
    }
}
