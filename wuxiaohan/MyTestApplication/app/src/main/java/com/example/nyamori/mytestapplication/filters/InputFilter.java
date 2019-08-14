package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.example.nyamori.mytestapplication.MyFrameRect;

public class InputFilter extends BaseFilter {

    public InputFilter(int width,int height){
        super(width,height);
    }

    @Override
    public void initRect() {
        super.initRect();
        setTexBuffer(true);
    }

    @Override
    public void setTexture(int preTexture) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, preTexture);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, myTexture[0]);
    }

    public void setTexBuffer(boolean flag){
        if(flag){
            texBuffer= MyFrameRect.getFullRectangleTexRotate90Buf();
        }else {
            texBuffer=MyFrameRect.getFullRectangleTexBuf();
        }
    }
}
