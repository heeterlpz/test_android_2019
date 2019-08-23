package com.example.beautycamera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.widget.TextView;
/***
 * Created by liuyongbin on 2019/8/13
 */
public class CameraglSurfaceview extends GLSurfaceView {
    public static final String TAG = "Filter_CameraglSurfaceView";
    private CameraRenderer mCameraRenderer;
    public void init(Camera camera, boolean isPreviewStarted, Context context, TextView textView, int type) {
        setEGLContextClientVersion(2);
        mCameraRenderer = new CameraRenderer(); //给GLSurfaceView设置Renderer渲染器
        mCameraRenderer.init(this, camera, isPreviewStarted, context,textView,type);
        setRenderer(mCameraRenderer);
    }
    public CameraglSurfaceview(Context context) { super(context); }
    public CameraRenderer getmCameraRenderer() {
        return mCameraRenderer;
    }
}
