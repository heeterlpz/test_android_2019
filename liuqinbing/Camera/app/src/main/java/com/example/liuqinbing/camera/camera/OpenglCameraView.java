package com.example.liuqinbing.camera.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.liuqinbing.camera.opengl.EglCore;
import com.example.liuqinbing.camera.opengl.FullFrameRect;
import com.example.liuqinbing.camera.opengl.OffscreenSurface;
import com.example.liuqinbing.camera.opengl.Texture2dProgram;
import com.example.liuqinbing.camera.opengl.WindowSurface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuqinbing on 19-8-9.
 */

public class OpenglCameraView extends RelativeLayout implements TextureView.SurfaceTextureListener {
    public static final int CAM_WIDTH = 1920;
    public static final int CAM_HEIGHT= 1080;
    private static final int DFT_TEXT_ID = -1;

    protected int mCamIndex;                  //摄像头索引
    protected TextureView mTextureView;       //摄像头预览内容显示
    protected AndroidCamera mCamera;          //摄像头对象
    private SurfaceTexture mSurfaceTexture;
    private Surface mOutSourface;
    EglCore mEglCore;
    private TextView mInfoView;               //显示fps信息
    private Texture2dProgram mTexture2dProgram;

    private Handler mUIHandle;
    private Handler mHandle;
    private static final int MSG_UPDATE_IMG = 0;
    private static final int MSG_INIT_OUT = 1;
    private static final int MSG_CHANGE_TYPE = 3;

    private static final int UI_UPDATE_FPS = 0;

    private int mTextureTypeIndex = 0;
    private List<Texture2dProgram.ProgramType> mTextureTypeList = new ArrayList<>();

    public OpenglCameraView(Context context, int camIndex, int textureType) {
        super(context);
        Log.d("debug", "OpenglCameraView: start CameraView");
        mCamIndex = camIndex;    //给摄像头索引赋值
        mTextureTypeIndex = textureType; //滤镜效果索引
        mCamera = new AndroidCamera();   //生成摄像头对象
        mTextureView = new TextureView(context);
        mTextureView.setSurfaceTextureListener(this);  //设置监听函数
        mInfoView = new TextView(context);
        mInfoView.setTextColor(Color.RED);             //设置文本颜色
        mInfoView.setText("Camera " + camIndex);       //设置内容，显示摄像头索引
        addView(mTextureView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);  //摄像头预览添加到屏幕上
        addView(mInfoView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);     //fps添加到屏幕上

        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_EXT);    //正常的片段着色器
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_MOSAIC); //马赛克
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_DIV_UD); //对称
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_SPLIT);  //9宫格
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_SMOOTH); //模糊
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_EXT_BW); //黑白
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_WIHITE); //美白
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_SMOOTH_SKIN); //磨皮
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_EXT_FILT);


        //输入画布(与摄像头绑定)
        mSurfaceTexture = new SurfaceTexture(DFT_TEXT_ID);   //用于接收camera图像流，但不显示
        mSurfaceTexture.setOnFrameAvailableListener(mInSourceAvilable); //设置监听函数
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);  //创建好EGL渲染环境

        HandlerThread handlerThread = new HandlerThread("OpenglCamera");
        handlerThread.start();
        mHandle = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_IMG:
                        try {
                            mScreenSurface.makeCurrent(); //使用提供的表面进行“绘制”和“读取”，使我们的EGL上下文变为当前。
                            mSurfaceTexture.updateTexImage();
                            mSurfaceTexture.getTransformMatrix(mTmpMatrix);

                            mWindowSurface.makeCurrent();
                            //位置：在第一象限内，以（0，0）为原点；宽高
                            GLES20.glViewport(0, 0, mOutWidth, mOutHeight);
                            mFullFrameBlit.drawFrame(mTextureId, mTmpMatrix);
//                            mSubtitle.draw(false);
                            mWindowSurface.swapBuffers();

                            //帧率统计
                            mFpsCount ++;
                            long curTime = System.currentTimeMillis(); //获取系统时间
                            if(curTime - mFpsTime > 999) {
                                mUIHandle.obtainMessage(UI_UPDATE_FPS, mFpsCount, 0).sendToTarget();
                                mFpsTime = curTime;
                                mFpsCount = 0;
                            }
                        } catch (Exception ex) {
                            //ex.printStackTrace();
                        }
                        break;
                    case MSG_INIT_OUT:
                        Log.d("debug", "handleMessage: start init");
                        mScreenSurface = new OffscreenSurface(mEglCore, CAM_WIDTH, CAM_HEIGHT);
                        mScreenSurface.makeCurrent();
                        setFrameBlit(mTextureTypeIndex);
                        //surface(输出画布)
                        mOutSourface = (Surface) msg.obj;
                        mWindowSurface = new WindowSurface(mEglCore, mOutSourface, false);
                        //开启摄像头
                        mCamera.start(mCamIndex, mSurfaceTexture, CAM_WIDTH, CAM_HEIGHT);
                        break;
                    case MSG_CHANGE_TYPE:
                        Bundle bundle = msg.getData();
                        mTextureTypeIndex = bundle.getInt("type");
                        setFrameBlit(mTextureTypeIndex);
                        break;
                }
            }
        };

        mUIHandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UI_UPDATE_FPS:
                        mInfoView.setText("fps:" + msg.arg1);
                        break;
                }
            }
        };
    }

    private void setFrameBlit(int typeIndex) {
        if(mFullFrameBlit!=null) {
            mFullFrameBlit.release(true);
        }
        //纹理绘制对象
        mTexture2dProgram = new Texture2dProgram(mTextureTypeList.get(typeIndex));
        mFullFrameBlit = new FullFrameRect(mTexture2dProgram);
        mTextureId = mFullFrameBlit.createTextureObject();
        //输入源绑定绘制纹理
        mSurfaceTexture.detachFromGLContext();
        mSurfaceTexture.attachToGLContext(mTextureId);
    }

    private int mOutWidth = 1920;
    private int mOutHeight = 1080;

    private int mTextureId = -1;
    private OffscreenSurface mScreenSurface;
    private WindowSurface mWindowSurface;
    private FullFrameRect mFullFrameBlit;
    private final float[] mTmpMatrix = new float[16];

    private long mFpsTime = System.currentTimeMillis();
    private int mFpsCount = 0;

    private SurfaceTexture.OnFrameAvailableListener mInSourceAvilable = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mHandle.sendEmptyMessage(MSG_UPDATE_IMG);
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        System.out.println("onSurfaceTextureAvailable");
        mOutWidth = width;
        mOutHeight = height;

        //输出画布
        Surface surface = new Surface(surfaceTexture);
        mHandle.obtainMessage(MSG_INIT_OUT, surface).sendToTarget();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mOutWidth = width;
        mOutHeight = height;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        System.out.println("onSurfaceTextureDestroyed");
        try {
            mCamera.relese();
            mCamera = null;
        } catch (Exception ex){}
        try {
            mSurfaceTexture.detachFromGLContext();
            mSurfaceTexture.release();
        } catch (Exception ex) {}
        try {
            mFullFrameBlit.release(true);
        } catch (Exception ex) {}
        try {
            mWindowSurface.release();
        } catch (Exception ex) {}
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * 改变渲染效果
     * @param type
     */
    public void changeTextureType(int type) {
        Bundle bundle = new Bundle();
        Message msg = new Message();
        msg.what = MSG_CHANGE_TYPE;
        bundle.putInt("type", type);
        msg.setData(bundle);
        mHandle.sendMessage(msg);
    }

    /**
     * 获取图片
     */
    public Bitmap getBitmap() {
        Bitmap bitmap_get= mTextureView.getBitmap();
        return bitmap_get;
    }

    /**
     * 设置磨皮程度的参数
     * @param intensity
     */
    public void setSmoothIntensity(float intensity) {
        mTexture2dProgram.setSmoothIntensity(intensity);
    }

    /**
     * 设置美白程度的参数
     * @param intensity
     */
    public void setWhiteIntensity(float intensity) {
        mTexture2dProgram.setWhiteIntensity(intensity);
    }

    /**
     * 设置红润程度的参数
     * @param intensity
     */
    public void setRuddyIntensity(float intensity) {
        mTexture2dProgram.setRuddyIntensity(intensity);
    }
}
