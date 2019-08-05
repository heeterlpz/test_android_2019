package com.starnet.heeter.openglcamerasample.camera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.starnet.heeter.openglcamerasample.opengl.EglCore;
import com.starnet.heeter.openglcamerasample.opengl.FullFrameRect;
import com.starnet.heeter.openglcamerasample.opengl.OffscreenSurface;
import com.starnet.heeter.openglcamerasample.opengl.Texture2dProgram;
import com.starnet.heeter.openglcamerasample.opengl.WindowSurface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 18-12-29.
 */

public class OpenglCameraView extends RelativeLayout implements TextureView.SurfaceTextureListener {
    public static final int CAM_WIDTH = 1280;
    public static final int CAM_HEIGHT= 720;
    private static final int DFT_TEXT_ID = -1;

    protected int mCamIndex;
    protected TextureView mTextureView;
    protected AndroidCamera mCamera;
    private SurfaceTexture mSurfaceTexture;
    private Surface mOutSourface;
    EglCore mEglCore;
    private TextView mInfoView;

    private Handler mUIHandle;
    private Handler mHandle;
    private static final int MSG_UPDATE_IMG = 0;
    private static final int MSG_INIT_OUT = 1;
    private static final int MSG_CHANGE_TYPE = 3;

    private static final int UI_UPDATE_FPS = 0;

    private int mTextureTypeIndex = 0;
    private List<Texture2dProgram.ProgramType> mTextureTypeList = new ArrayList<>();

    public OpenglCameraView(Context context, int camIndex) {
        super(context);
        mCamIndex = camIndex;
        mCamera = new AndroidCamera();
        mTextureView = new TextureView(context);
        mTextureView.setSurfaceTextureListener(this);
        mInfoView= new TextView(context);
        mInfoView.setTextColor(Color.RED);
        mInfoView.setText("Camera " + camIndex);
        addView(mTextureView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mInfoView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_EXT);
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_MOSAIC);
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_DIV_UD);
        mTextureTypeList.add(Texture2dProgram.ProgramType.TEXTURE_SPLIT);

        //输入画布(与摄像头绑定)
        mSurfaceTexture = new SurfaceTexture(DFT_TEXT_ID);
        mSurfaceTexture.setOnFrameAvailableListener(mInSourceAvilable);
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);

        HandlerThread handlerThread = new HandlerThread("OpenglCamera");
        handlerThread.start();
        mHandle = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_IMG:
                        try {
                            mScreenSurface.makeCurrent();
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
                            long curTime = System.currentTimeMillis();
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
                        mTextureTypeIndex++;
                        if(mTextureTypeIndex >= mTextureTypeList.size()) {
                            mTextureTypeIndex = 0;
                        }
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
        mFullFrameBlit = new FullFrameRect(new Texture2dProgram(mTextureTypeList.get(typeIndex)));
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

    public void changeTextureType() {
        mHandle.sendEmptyMessage(MSG_CHANGE_TYPE);
    }
}
