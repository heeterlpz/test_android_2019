package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.example.nyamori.mytestapplication.MyFrameRect;
import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class InputFilter extends BaseFilter {

    public InputFilter(int width,int height){
        super();
        mProgramHandle = ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_ext);
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }

    @Override
    public void initRect() {
        super.initRect();
        flashTexBuffer();
    }

    @Override
    public void setTexture(int preTexture) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, preTexture);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, myTexture[0]);
    }

    public void flashTexBuffer(){
        texBuffer=MyFrameRect.getFullRectangleTexRotate90Buf();
    }
}
