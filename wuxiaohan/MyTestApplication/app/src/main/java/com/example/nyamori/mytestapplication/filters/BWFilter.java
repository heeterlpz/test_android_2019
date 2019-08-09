package com.example.nyamori.mytestapplication.filters;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.gles.ShaderInfo;

public class BWFilter extends baseFilter{
    public BWFilter(){
        super();
        mProgramHandle= GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SHADER_BW);
        createFrame();
    }
}
