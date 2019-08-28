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

public class FaceLiftFilter extends BaseFilter {
    private int edgePointLoc;
    private int levelLoc;

    private float level;
    private PointF[] edgePoint;
    public FaceLiftFilter(int width, int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_face_lift);
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
        edgePoint=Faces.getEdgePoints();
        if(faceNumber ==0)return true;
        else return isLevelZero;
    }

    @Override
    public void getLocation() {
        super.getLocation();
        edgePointLoc = GLES20.glGetUniformLocation(mProgramHandle, "edgePoint");
        levelLoc = GLES20.glGetUniformLocation(mProgramHandle, "level");
        level=0.3f;
    }

    @Override
    public void setUniform() {
        super.setUniform();
        float[] edgeArray=new float[edgePoint.length*2];
        if(edgePoint.length>0){
            for(int i=0;i<edgePoint.length;i++){
                edgeArray[i*2]=edgePoint[i].x;
                edgeArray[i*2+1]=edgePoint[i].y;
            }
        }else {
            edgeArray=new float[]{0};
        }
        GLES20.glUniform1f(levelLoc,level);
        GLES20.glUniform2fv(edgePointLoc,edgePoint.length,edgeArray,0);
    }

    @Override
    public void setLevel(int newLevel) {
        if(newLevel==0){
            isLevelZero=true;
            level=0.1f;
        }
        else {
            isLevelZero=false;
            level=0.1f+newLevel*0.025f;
        }
    }

    @Override
    public int getLevelMax() {
        return 12;
    }

    @Override
    public int getLevel() {
        if(level==0.1f)return 0;
        else return (int)((level-0.1f)/0.025f);
    }
}
