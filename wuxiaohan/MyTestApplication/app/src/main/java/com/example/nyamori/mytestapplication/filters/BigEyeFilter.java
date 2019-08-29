package com.example.nyamori.mytestapplication.filters;

import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

import com.example.nyamori.mytestapplication.Faces;
import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class BigEyeFilter extends BaseFilter {
    private int levelLoc;
    private int leftEyeLoc;
    private int rightEyeLoc;

    private float level;
    private PointF[] rightEyePoint;
    private PointF[] leftEyePoint;
    public BigEyeFilter(int width, int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_big_eye);
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
        rightEyePoint=Faces.getRightEyePoints();
        leftEyePoint=Faces.getLeftEyePoints();
        if(faceNumber ==0)return true;
        else return isLevelZero;
    }

    @Override
    public void getLocation() {
        super.getLocation();
        levelLoc = GLES20.glGetUniformLocation(mProgramHandle, "level");
        leftEyeLoc = GLES20.glGetUniformLocation(mProgramHandle, "leftEyePoint");
        rightEyeLoc = GLES20.glGetUniformLocation(mProgramHandle, "rightEyePoint");
        level=2.0f;
    }

    @Override
    public void setUniform() {
        super.setUniform();
        float[] leftArray=new float[10];
        float[] rightArray=new float[10];
        if(Faces.getFaceNumber()>0){
            for(int i=0;i<5;i++){
                leftArray[i*2]=leftEyePoint[i].x;
                leftArray[i*2+1]=leftEyePoint[i].y;
                rightArray[i*2]=rightEyePoint[i].x;
                rightArray[i*2+1]=rightEyePoint[i].y;
            }
        }else {
            leftArray=new float[10];
            rightArray=new float[10];
        }
        GLES20.glUniform1f(levelLoc,level);
        GLES20.glUniform2fv(leftEyeLoc,5,leftArray,0);
        GLES20.glUniform2fv(rightEyeLoc,5,rightArray,0);
    }

    @Override
    public void setLevel(int newLevel) {
        if(newLevel==0){
            isLevelZero=true;
            level=1.0f;
        }
        else {
            isLevelZero=false;
            level=1.0f+newLevel*0.1f;
        }
    }

    @Override
    public int getLevelMax() {
        return 30;
    }

    @Override
    public int getLevel() {
        if(level==0.1f)return 0;
        else return (int)((level-1.0f)/0.1f);
    }
}
