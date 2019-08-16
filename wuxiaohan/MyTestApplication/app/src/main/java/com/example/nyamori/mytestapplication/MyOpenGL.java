package com.example.nyamori.mytestapplication;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;

import com.example.nyamori.gles.EglCore;
import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.gles.WindowSurface;
import com.example.nyamori.mytestapplication.filters.BaseFilter;

import java.util.List;

public class MyOpenGL {
    private final static String TAG = "MyOpenGL";

    private EglCore mEglCore;
    private SurfaceTexture mSurfaceTexture;
    private WindowSurface mWindowSurface;
    private Handler mOpenGLHandler;

    private int fpsCount;
    private long fpsTime;

    private int mTextureID=-1;

    private Handler mUIHandler;
    private ViewPort mViewPort;
    private My2DFilterManager my2DFilterManager;

    public MyOpenGL(Handler mUIHandler, Surface mOutSurface){
        this.mUIHandler=mUIHandler;
        mEglCore=new EglCore(null,EglCore.FLAG_RECORDABLE);
        mWindowSurface=new WindowSurface(mEglCore,mOutSurface,false);
        initHandler();
    }

    public void init(ViewPort viewPort){
        mViewPort=viewPort;
        mOpenGLHandler.obtainMessage(Config.OPenGLMsg.MSG_INIT_OUT).sendToTarget();
    }


    public void changeCamera(ViewPort viewPort){
        mViewPort=viewPort;
        mOpenGLHandler.obtainMessage(Config.OPenGLMsg.MSG_CHANGE_CAMERA).sendToTarget();
    }

    public void destroyOpenGL() {
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

    public void deleteFilter(int position){
        my2DFilterManager.deleteFilter(position);
        mUIHandler.obtainMessage(Config.UIMsg.UI_UPDATE_LIST).sendToTarget();
    }

    public void addFilter(int programType){
        mOpenGLHandler.obtainMessage(Config.OPenGLMsg.MSG_ADD_FILTER,programType,0).sendToTarget();
    }

    public List<String> getFilterList(){
        return my2DFilterManager.getFilterTypeList();
    }

    public BaseFilter getFilter(int position){return my2DFilterManager.getFilter(position);}

    private void initHandler() {
        HandlerThread handlerThreadOpenGL = new HandlerThread("OpenGL");
        handlerThreadOpenGL.start();
        mOpenGLHandler = new Handler(handlerThreadOpenGL.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case Config.OPenGLMsg.MSG_INIT_OUT:
                        initOpenGL();
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
                        break;
                }
            }
        };
    }

    private void changeCameraInOpenGL() {
        my2DFilterManager.changeSize(mViewPort.getWidth(),mViewPort.getHeight(),mViewPort.getxStart(),mViewPort.getyStart());
        mSurfaceTexture.setDefaultBufferSize(mViewPort.getWidth(),mViewPort.getHeight());
    }


    private void updateImg() {
        mSurfaceTexture.updateTexImage();//更新了信息
        my2DFilterManager.draw(mTextureID);//绘制的细节过程是manager管理的
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
        mWindowSurface.makeCurrent();
        my2DFilterManager=new My2DFilterManager(mViewPort.getWidth(),mViewPort.getHeight(),mViewPort.getxStart(),mViewPort.getyStart());
        mTextureID=my2DFilterManager.createInputTextureObject();
        mSurfaceTexture=new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mOpenGLHandler.obtainMessage(Config.OPenGLMsg.MSG_UPDATE_IMG).sendToTarget();
            }
        });
        mSurfaceTexture.setDefaultBufferSize(mViewPort.getWidth(),mViewPort.getHeight());
        fpsCount=0;
        fpsTime=System.currentTimeMillis();
        mUIHandler.obtainMessage(Config.UIMsg.GL_SURFACE_PREPARE,mSurfaceTexture).sendToTarget();
    }
}
