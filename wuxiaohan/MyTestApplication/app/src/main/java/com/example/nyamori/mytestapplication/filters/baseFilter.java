package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;

import com.example.nyamori.gles.GlUtil;

// TODO: 19-8-8 完成基础fliter类
public class baseFilter {
    public void clearScreen(){
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GlUtil.checkGlError("glClearColor");
    }
}
