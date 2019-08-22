package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class OutputFilter extends BaseFilter {
    private int xStart;
    private int yStart;

    public OutputFilter(int width, int height,int xStart,int yStart){
        super();
        setSize(width,height,xStart,yStart);
        mProgramHandle = ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_2d);
        getLocation();
        initRect();
    }

    public void setSize(int width, int height,int xStart,int yStart) {
        this.width=width;
        this.height=height;
        this.xStart=xStart;
        this.yStart=yStart;
    }

    @Override
    public void onBindFrame() {
        GLES20.glViewport(xStart,yStart,width,height);
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
