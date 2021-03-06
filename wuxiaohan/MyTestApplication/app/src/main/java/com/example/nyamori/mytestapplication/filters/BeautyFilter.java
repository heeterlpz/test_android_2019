package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;

import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class BeautyFilter extends BaseFilter {
    private int beautyLevelLoc;
    private float beautyLevel=1.0f;
    public BeautyFilter(int width, int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_beauty_green);
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }

    @Override
    public void getLocation() {
        super.getLocation();
        beautyLevelLoc = GLES20.glGetUniformLocation(mProgramHandle, "level");
        // initialize default values
        beautyLevel=0.3f;
    }

    @Override
    public void setUniform() {
        super.setUniform();
        GLES20.glUniform1f(beautyLevelLoc,beautyLevel);
    }

    @Override
    public void setLevel(int newLevel) {
        if(newLevel==0){
            isLevelZero=true;
            beautyLevel=1.05f;
        }
        else {
            isLevelZero=false;
            if(newLevel<20)beautyLevel=1.05f-newLevel*0.05f;
            else beautyLevel=0.05f;
        }
    }

    @Override
    public int getLevelMax() {
        return 20;
    }

    @Override
    public int getLevel() {
        if(beautyLevel==0.05f)return 19;
        else return (int)((1-beautyLevel)/0.05f);
    }
}
