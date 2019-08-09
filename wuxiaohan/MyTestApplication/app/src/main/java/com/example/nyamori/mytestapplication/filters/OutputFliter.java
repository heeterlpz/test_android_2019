package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.gles.ShaderInfo;

public class OutputFliter extends baseFilter{

    public OutputFliter(int width,int height){
        super();
        this.width=width;
        this.height=height;
        mProgramHandle = GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SHADER_2D);
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        getLocation();
        initRect();
    }

    @Override
    public void onBindFrame() {
        GLES20.glViewport(0,0,width,height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        clearScreen();
    }

    @Override
    public void setTexture(int preTexture) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, preTexture);
    }

    @Override
    public void unBindFrame() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
        GlUtil.checkGlError("draw done");
    }
}
