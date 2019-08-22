package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;

import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class SharpeningFilter extends BaseFilter{
    private int muTexOffsetLoc;
    private int sharpeningLevelLoc;
    private float sharpeningLevel=1.0f;

    public SharpeningFilter(int width,int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_small_sharpening);
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }

    @Override
    public void getLocation() {
        super.getLocation();
        muTexOffsetLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexOffset");
        sharpeningLevelLoc=GLES20.glGetUniformLocation(mProgramHandle, "level");
        // initialize default values
        setTexSize(512, 512);
        setLevel(1);
    }

    @Override
    public void setUniform() {
        super.setUniform();
        GLES20.glUniform2fv(muTexOffsetLoc, KERNEL_SIZE_SMALL, mTexOffset, 0);
        GLES20.glUniform1f(sharpeningLevelLoc,sharpeningLevel);
    }

    @Override
    public void setLevel(int newLevel) {
        if(newLevel==0){
            isLevelZero=true;
            sharpeningLevel=4.5f;
        }
        else {
            isLevelZero=false;
            if(newLevel<8)sharpeningLevel=4.5f-0.5f*newLevel;
            else sharpeningLevel=0.5f;
        }
    }

    @Override
    public int getLevelMax() {
        return 8;
    }

    @Override
    public int getLevel() {
        if(sharpeningLevel==0.5f)return 8;
        else return (int)((4.5f-sharpeningLevel)/0.5f);
    }
}
