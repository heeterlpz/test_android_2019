package com.example.nyamori.mytestapplication.filters;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.gles.ShaderInfo;

public class BWFilter extends BaseFilter {

    public BWFilter(int width,int height){
        super();
        mProgramHandle= GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SHADER_BW);
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }
}
