package com.starnet.heeter.openglcamerasample.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;

/**
 * Created on 2017/11/27.
 */
public class ForgroundTexture {
    private static final int FORMAT = GLES20.GL_RGBA;
    private final Drawable2d mRectDrawable = new Drawable2d(Drawable2d.Prefab.RECTANGLE);
    private Texture2dProgram mTexProgram;
    private final float[] mSubMatrix = new float[16];
    private final float[] mSubMirrorMatrix = new float[16];
    private Sprite2d mRect;
    private int mTextrueId = -1;
    //画布宽高
    private int mWidth;
    private int mHeight;
    //内容宽高
    private int mSubWidth;
    private int mSubHeight;

    public ForgroundTexture(int width, int height) {
        mWidth = width;
        mHeight = height;
        mRect = new Sprite2d(mRectDrawable);
        mTexProgram = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D);
    }

    public void release() {
        if (mTexProgram != null) {
            mTexProgram.release();
            mTexProgram = null;
        }
    }

    public void draw(boolean isMirror) {
        if (mTextrueId == -1)
            return;
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        if (isMirror) {
            mRect.draw(mTexProgram, mSubMirrorMatrix);
        } else {
            mRect.draw(mTexProgram, mSubMatrix);
        }
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public void setImg(Bitmap map) {
        if(mTextrueId >= 0) {
            GlUtil.releaseTexture(mTextrueId);
        }
        if(map == null) {
            mTextrueId = -1;
            return;
        }
        mTextrueId = createBitTexture(map);
//        mWidth = mSubWidth;
//        mHeight = mSubHeight;
        Matrix.orthoM(mSubMatrix, 0, 0, mWidth, 0, mHeight, -1, 1);
        Matrix.orthoM(mSubMirrorMatrix, 0, mWidth, 0, 0, mHeight, -1, 1);
        mRect.setPosition(mWidth/2, mHeight/2);
        mRect.setTexture(mTextrueId);
        mRect.setScale(mSubWidth, mSubHeight);
        map.recycle();
    }

    private int createBitTexture(Bitmap map) {
        if (map == null)
            return -1;
        mSubWidth = map.getWidth();
        mSubHeight = map.getHeight();
        int lenth = map.getByteCount();
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(lenth);
        map.copyPixelsToBuffer(byteBuf);
        byteBuf.position(0);
        map.recycle();
        int textureId = GlUtil.createImageTexture(byteBuf, mSubWidth, mSubHeight, FORMAT);
        return textureId;
    }
}
