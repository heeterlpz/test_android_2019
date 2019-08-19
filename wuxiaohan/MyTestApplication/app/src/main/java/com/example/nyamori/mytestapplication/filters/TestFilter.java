package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;

import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class TestFilter extends BaseFilter{
    private int beautyLevelLoc;
    private float beautyLevel=1.0f;
    public TestFilter(int width, int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_beauty_green);
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
        beautyLevelLoc = GLES20.glGetUniformLocation(mProgramHandle, "level");
        // initialize default values
        beautyLevel=0.05f;
    }

    @Override
    public void setUniform() {
        super.setUniform();
        GLES20.glUniform1f(beautyLevelLoc,beautyLevel);
    }
}
