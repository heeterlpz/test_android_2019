package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;

import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class EdgeFilter extends BaseFilter {
    private int muTexOffsetLoc;

    public EdgeFilter(int width,int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_small_edge);
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
        // initialize default values
        setTexSize(720, 720);
    }

    @Override
    public void setUniform() {
        super.setUniform();
        GLES20.glUniform2fv(muTexOffsetLoc, KERNEL_SIZE_SMALL, mTexOffset, 0);

    }




}
