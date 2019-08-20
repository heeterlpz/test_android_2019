package com.example.cameravtwo.CameraV2GLSurfaceView;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.widget.TextView;

import com.example.cameravtwo.CameraV2;

public class CameraV2GLSurfaceView extends GLSurfaceView {
    public static final String TAG = "Filter_CameraV2GLSurfaceView";
    private CameraV2Renderer mCameraV2Renderer;



    public void init(CameraV2 camera, boolean isPreviewStarted, Context context, TextView textView) {
        setEGLContextClientVersion(2);

        mCameraV2Renderer = new CameraV2Renderer(); //给GLSurfaceView设置Renderer渲染器
        mCameraV2Renderer.init(this, camera, isPreviewStarted, context,textView);


        setRenderer(mCameraV2Renderer);
    }

    public CameraV2GLSurfaceView(Context context) {
        super(context);
    }
}