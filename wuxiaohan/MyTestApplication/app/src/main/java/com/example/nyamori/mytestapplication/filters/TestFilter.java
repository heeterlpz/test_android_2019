package com.example.nyamori.mytestapplication.filters;

import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

import com.example.nyamori.mytestapplication.Faces;
import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;


public class TestFilter extends BaseFilter{
    private int leftLoc;
    private int topLoc;
    private int rightLoc;
    private int bottomLoc;
    private int edgePointLoc;
    private int levelLoc;
    private int leftEyeLoc;
    private int rightEyeLoc;

    private float level;
    private RectF faceRect;
    private PointF[] edgePoint;
    private PointF[] rightEyePoint;
    private PointF[] leftEyePoint;
    public TestFilter(int width, int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_test);
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }

    //每次绘制都会调用这个方法所以用这个方法更新数据
    @Override
    public boolean isLevelZero() {
        int faceNumber = Faces.getFaceNumber();
        faceRect=Faces.getFaceInfoList();
        edgePoint=Faces.getEdgePoints();
        rightEyePoint=Faces.getRightEyePoints();
        leftEyePoint=Faces.getLeftEyePoints();
        if(faceNumber ==0)return true;
        else return isLevelZero;
    }

    @Override
    public void getLocation() {
        super.getLocation();
        leftLoc = GLES20.glGetUniformLocation(mProgramHandle, "left");
        topLoc = GLES20.glGetUniformLocation(mProgramHandle, "top");
        rightLoc = GLES20.glGetUniformLocation(mProgramHandle, "right");
        bottomLoc = GLES20.glGetUniformLocation(mProgramHandle, "bottom");
        edgePointLoc = GLES20.glGetUniformLocation(mProgramHandle, "edgePoint");
        levelLoc = GLES20.glGetUniformLocation(mProgramHandle, "level");
        leftEyeLoc = GLES20.glGetUniformLocation(mProgramHandle, "leftEyePoint");
        rightEyeLoc = GLES20.glGetUniformLocation(mProgramHandle, "rightEyePoint");
        level=0.3f;
    }

    @Override
    public void setUniform() {
        super.setUniform();
        RectF rectF;
        float[] edgeArray=new float[38];
        float[] leftArray=new float[10];
        float[] rightArray=new float[10];
        if(faceRect!=null){
            rectF=faceRect;
            for(int i=0;i<19;i++){
                if(i<5){
                    leftArray[i*2]=leftEyePoint[i].x;
                    leftArray[i*2+1]=leftEyePoint[i].y;
                    rightArray[i*2]=rightEyePoint[i].x;
                    rightArray[i*2+1]=rightEyePoint[i].y;
                }
                edgeArray[i*2]=edgePoint[i].x;
                edgeArray[i*2+1]=edgePoint[i].y;
            }
        }else {
            rectF=new RectF(0,0,0,0);
            edgeArray=new float[38];
            leftArray=new float[10];
            rightArray=new float[10];
        }
        Log.i(TAG, "setUniform: rect="+rectF);
        GLES20.glUniform1f(leftLoc,rectF.left);
        GLES20.glUniform1f(topLoc,rectF.top);
        GLES20.glUniform1f(rightLoc,rectF.right);
        GLES20.glUniform1f(bottomLoc,rectF.bottom);
        GLES20.glUniform1f(levelLoc,level);
        GLES20.glUniform2fv(edgePointLoc,19,edgeArray,0);
        GLES20.glUniform2fv(leftEyeLoc,5,leftArray,0);
        GLES20.glUniform2fv(rightEyeLoc,5,rightArray,0);
    }
}
