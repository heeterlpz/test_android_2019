package com.example.beautycamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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

public class CameraRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "Filter_MyRenderer";
    private Context mContext;
    private int mOESTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private float[] transformMatrix = new float[16];
    private CameraGLSurfaceView mGLSurfaceView;
    private Camera mCamera;
    private boolean bIsPreviewStarted;
    private FilterEngine mFilterEngine;
    private FloatBuffer mDataBuffer;
    private int mShaderProgram = -1;
    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;
    private int[] mFBOIds = new int[1];
    private int type;
    private int type1;
    private Handler mHandle;
    private int alphaLevelLocation = -1;
    private float alphaLevel;
    private int betaLevelLocation = -1;
    private float betaLevel;
    private int redLevelLocation = -1;
    private float redLevel;

    public void init(CameraGLSurfaceView glSurfaceView, Camera camera, boolean isPreviewStarted, Context context, int type, Handler mHandle) {
        mContext = context;
        mGLSurfaceView = glSurfaceView;
        mCamera = camera;
        bIsPreviewStarted = isPreviewStarted;
        this.type=type;
        this.type1=type;
        this.mHandle=mHandle;
    }

    public void changeFilter(int type){
        mFilterEngine = new FilterEngine(mOESTextureId, mContext, type);
        mDataBuffer = mFilterEngine.getBuffer();
        mShaderProgram = mFilterEngine.getShaderProgram();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //创建一个外部纹理用于接收预览数据
        mOESTextureId = Utils.createOESTextureObject();
        changeFilter(type);
//        mFilterEngine = new FilterEngine(mOESTextureId, mContext, type);
//        mDataBuffer = mFilterEngine.getBuffer();
//        mShaderProgram = mFilterEngine.getShaderProgram();
        glGenFramebuffers(1, mFBOIds, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, mFBOIds[0]);
        Log.i("lb6905", "onSurfaceCreated: mFBOId: " + mFBOIds[0]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(type1 != type){
            type1=type;
            changeFilter(type);
        }
        Long t1 = System.currentTimeMillis();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        if (!bIsPreviewStarted) {
            bIsPreviewStarted = initSurfaceTexture();
            bIsPreviewStarted = true;
            return;
        }

        //glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        aPositionLocation = glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM);
        alphaLevelLocation = glGetUniformLocation(mShaderProgram,FilterEngine.ALPHA_LEVEL);
        betaLevelLocation = glGetUniformLocation(mShaderProgram,FilterEngine.BETAL_LEVEL);
        redLevelLocation = glGetUniformLocation(mShaderProgram,FilterEngine.RED_LEVEL);


        glActiveTexture(GLES20.GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        glUniform1i(uTextureSamplerLocation, 0);
        glUniform1f(alphaLevelLocation,alphaLevel);
        glUniform1f(betaLevelLocation,betaLevel);
        glUniform1f(redLevelLocation,redLevel);
        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        if (mDataBuffer != null) {
            mDataBuffer.position(0);
            glEnableVertexAttribArray(aPositionLocation);
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mDataBuffer);

            mDataBuffer.position(2);
            glEnableVertexAttribArray(aTextureCoordLocation);
            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mDataBuffer);
        }

        //glDrawElements(GL_TRIANGLE_FAN, 6,GL_UNSIGNED_INT, 0);
        //glDrawArrays(GL_TRIANGLE_FAN, 0 , 6);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        //glDrawArrays(GL_TRIANGLES, 3, 3);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        long t2 = System.currentTimeMillis();
        long t = t2 - t1;
        Log.i("lb6905", "onDrawFrame: time: " + t);
    }

    public boolean initSurfaceTexture() {
        if (mCamera == null || mGLSurfaceView == null) {
            Log.i(TAG, "mCamera or mGLSurfaceView is null!");
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mGLSurfaceView.requestRender();
                mHandle.sendEmptyMessage(0);
            }
        });

        mCamera.setPreviewTexture(mSurfaceTexture);
        mCamera.startPreview();
        return true;
    }

    public void deinit() {
        if (mFilterEngine != null) {
            mFilterEngine = null;
        }
        mDataBuffer = null;
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mCamera = null;
        mOESTextureId = -1;
        bIsPreviewStarted = false;
    }

    public void setType(int type){
        this.type=type;
    }

    public void setAlphaLevel(float alphaLevel){
        this.alphaLevel=alphaLevel;
    }

    public void setBetaLevel(float betaLevel){
        this.betaLevel = betaLevel;
    }

    public void setRedLevel(float redLevel){
        this.redLevel = redLevel;
    }
}
