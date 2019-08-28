package com.example.beautycamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
/***
 * Created by liuyongbin on 2019/8/13
 */
public class CameraRenderer implements GLSurfaceView.Renderer { //当Surface创建后会调用此方法
    public static final String TAG = "Filter_CameraV2Renderer";
    Camera mCamera;
    CameraglSurfaceview mCameraglSurfaceview;
    boolean bIsPreviewStarted;
    private Context mContext;
    private TextView mInfoView;
    private int type;
    private int TYPE;
    private long mFpsTime;
    private int mFpsCount;
    private SurfaceTexture mSurfaceTexture;
    private FilterEngine mFilterEngine;
    private FloatBuffer mDataBuffer;
    private int mOESTextureId = -1;
    private int mShaderProgram = -1;
    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;
    private int betaLocation = -1;
    private int alphalevelLocation = -1;
    private int[] mFBOIds = new int[1];
    private float[] transformMatrix = new float[16];
    private static final int UI_UPDATE_FPS = 0;
    private float betalevel;
    private float alphaLevel;
    private Handler mUIHandle = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UI_UPDATE_FPS:
                    mInfoView.setText("fps:" + msg.arg1);
                    break;
            }
        }
    };

    /***
     * 初始化类的属性，传入参数的值
     * @param surfaceView
     * @param camera
     * @param isPreviewStarted
     * @param context
     * @param textView
     * @param type
     */
    public void init(CameraglSurfaceview surfaceView, Camera camera, boolean isPreviewStarted, Context context, TextView textView, int type) {
        mContext = context;
        mCameraglSurfaceview = surfaceView;
        mCamera = camera;
        bIsPreviewStarted = isPreviewStarted;
        mInfoView = textView;
        this.type = type;
        this.TYPE = type;
    }

    /***
     * 选择片段着色器，用于更改滤镜
     * @param type
     */
    public void changeFilter(int type) {
        mFilterEngine = new FilterEngine(mOESTextureId, mContext, type);
        mDataBuffer = mFilterEngine.getBuffer();
        mShaderProgram = mFilterEngine.getShaderProgram();
    }

    /***
     *创建Surface
     * @param gl10
     * @param eglConfig
     */
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mOESTextureId = Utils.createOESTextureObject();
        changeFilter(type);
        glGenFramebuffers(1, mFBOIds, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, mFBOIds[0]);
        Log.i(TAG, "onSurfaceCreated: mFBOId: " + mFBOIds[0]);
    }
    /***
     * 当Surface创建成功或尺寸改变时都调用此方法
     * @param gl10
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        Log.i(TAG, "onSurfaceChanged: " + width + ", " + height);
    }

    /***
     * 每绘制一帧都会调用此方法
     * @param gl10
     */
    @Override
    public void onDrawFrame(GL10 gl10) {
        if (TYPE != type){
            TYPE = type;
            changeFilter(type);
        }
        Long t1 = System.currentTimeMillis();
        if (mSurfaceTexture != null) { //更新纹理图像
            mSurfaceTexture.updateTexImage(); //获取外部纹理的矩阵，用来确定纹理的采样位置
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        if (!bIsPreviewStarted) {
            bIsPreviewStarted = initSurfaceTexture(); //在onDrawFrame方法中调用此方法
            bIsPreviewStarted = true;
            return;
        }

        //glClear(GL_COLOR_BUFFER_BIT); 清空颜色缓冲区，然后使用glClearColor()方法设置填充屏幕的颜色
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        //获取Shader中定义的变量在program中的位置
        aPositionLocation = glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM);
        betaLocation = glGetUniformLocation(mShaderProgram, FilterEngine.BETALEVEL);
        alphalevelLocation = glGetUniformLocation(mShaderProgram, FilterEngine.AlphaLevel);

        glActiveTexture(GL_TEXTURE_EXTERNAL_OES); //激活纹理单元
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId); //绑定外部纹理到纹理单元
        glUniform1i(uTextureSamplerLocation, 0); //将此纹理单元床位片段着色器的uTextureSampler外部纹理采样器
        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0); //将纹理矩阵传给片段着色器
        glUniform1f(betaLocation, betalevel);
        glUniform1f(alphalevelLocation, alphaLevel);

        if (mDataBuffer != null) { //将顶点和纹理坐标传给顶点着色器
            mDataBuffer.position(0); //顶点坐标从位置0开始读取
            glEnableVertexAttribArray(aPositionLocation); //使能顶点属性
            //顶点坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mDataBuffer);

            mDataBuffer.position(2); //纹理坐标从位置2开始读取
            glEnableVertexAttribArray(aTextureCoordLocation);
            //纹理坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mDataBuffer);
        }

        //glDrawElements(GL_TRIANGLE_FAN, 6,GL_UNSIGNED_INT, 0);
        //glDrawArrays(GL_TRIANGLE_FAN, 0 , 6);
        //绘制两个三角形（6个顶点）
        glDrawArrays(GL_TRIANGLES, 0, 6);
        //glDrawArrays(GL_TRIANGLES, 3, 3);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        long t2 = System.currentTimeMillis();
        long t = t2 - t1;
        Log.i(TAG, "onDrawFrame: time: " + t);

    }

    /***
     * 初始化surfaceTexture
     * @return 为true时开启相机预览
     */
    public boolean initSurfaceTexture() {
        if (mCamera == null || mCameraglSurfaceview == null) {
            Log.i(TAG, "mCamera or mGLSurfaceView is null!");
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId); //根据外部纹理ID创建SurfaceTexture
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mCameraglSurfaceview.requestRender();  //每获取到一帧数据时请求OpenGL ES进行渲染

                mFpsCount ++;
                long curTime = System.currentTimeMillis();
                if(curTime - mFpsTime > 999) {
                    mUIHandle.obtainMessage(UI_UPDATE_FPS, mFpsCount, 0).sendToTarget();
                    mFpsTime = curTime;
                    mFpsCount = 0;
                }
            }
        });
        mCamera.setPreviewTexture(mSurfaceTexture); //讲此SurfaceTexture作为相机预览输出
        mCamera.startPreview(); //开启预览
        return true;
    }

    public void setType(int type) { this.type = type; }


    public void setBetaLevel(float BetaLevel) {
        this.betalevel = BetaLevel;
    }

    public void setAlphaLevel(float AlphaLevel) {
        this.alphaLevel = AlphaLevel;
        Log.e("liu", "onProgressChanged: mopizhi="+ AlphaLevel);
    }
}

