package com.example.beautycamera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;

public class CameraGLSurfaceView extends GLSurfaceView {
    private CameraRenderer mRenderer;

    public CameraGLSurfaceView(Context context) {
        super(context);
    }

    public void init( Camera camera, boolean isPreviewStarted, Context context, int type, Handler mHandle) {
        //配置OpenGL ES，主要是版本设置和设置Renderer，Renderer用于执行OpenGL的绘制
        setEGLContextClientVersion(2);
        mRenderer = new CameraRenderer();
        mRenderer.init(this, camera, isPreviewStarted, context, type, mHandle);
        setRenderer(mRenderer);
    }

    public void deinit() {
        if (mRenderer != null) {
            mRenderer.deinit();
            mRenderer = null;
        }
    }

    public CameraRenderer getmRenderer(){
        return mRenderer;
    }
}
