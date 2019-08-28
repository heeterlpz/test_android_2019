package com.example.nyamori.mytestapplication.filters;

import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

import com.example.nyamori.mytestapplication.Faces;
import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

import java.util.ArrayList;
import java.util.List;

public class TestFilter extends BaseFilter{
    private int leftLoc;
    private int topLoc;
    private int rightLoc;
    private int bottomLoc;
    private int edgePointLoc;
    private int levelLoc;

    private float level;
    private int faceNumber=0;
    private List<RectF> faceRect;
    private PointF[] edgePoint;
    public TestFilter(int width, int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_test);
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        faceRect=new ArrayList<>();
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }

    //每次绘制都会调用这个方法所以用这个方法更新数据
    @Override
    public boolean isLevelZero() {
        faceRect.clear();
        faceNumber=Faces.getFaceNumber();
        faceRect.addAll(Faces.getFaceInfoList());
        edgePoint=Faces.getEdgePoints();
        if(faceNumber==0)return true;
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
        level=0.3f;
    }

    @Override
    public void setUniform() {
        super.setUniform();
        RectF rectF;
        float[] edgeArray=new float[edgePoint.length*2];
        if(faceRect.size()>0){
            rectF=faceRect.get(0);
            for(int i=0;i<edgePoint.length;i++){
                edgeArray[i*2]=edgePoint[i].x;
                edgeArray[i*2+1]=edgePoint[i].y;
            }
        }else {
            rectF=new RectF(0,0,0,0);
            edgeArray=new float[]{0};
        }
        Log.i(TAG, "setUniform: rect="+rectF);
        GLES20.glUniform1f(leftLoc,rectF.left);
        GLES20.glUniform1f(topLoc,rectF.top);
        GLES20.glUniform1f(rightLoc,rectF.right);
        GLES20.glUniform1f(bottomLoc,rectF.bottom);
        GLES20.glUniform1f(levelLoc,level);
        GLES20.glUniform2fv(edgePointLoc,edgePoint.length,edgeArray,0);
    }
}
