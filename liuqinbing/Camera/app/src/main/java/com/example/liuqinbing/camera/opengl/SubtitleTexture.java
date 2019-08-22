package com.example.liuqinbing.camera.opengl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;

/**
 * Created on 2017/11/27.
 */
public class SubtitleTexture {
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
    //字幕宽高
    private int mSubWidth;
    private int mSubHeight;
    //字幕位置
    private int mPosition = POSITION_CENTER_TOP;
    private int mOffx = 0;
    private int mOffy = 0;

    public SubtitleTexture(int width, int height) {
        mRect = new Sprite2d(mRectDrawable);
//        //画布分辨率固定，保证分辨率切换字幕大小无变化
        mWidth = 1280;
        mHeight = 720;
        Matrix.orthoM(mSubMatrix, 0, 0, mWidth, 0, mHeight, -1, 1);
        Matrix.orthoM(mSubMirrorMatrix, 0, mWidth, 0, 0, mHeight, -1, 1);
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

    public static final int POSITION_CENTER_TOP = 0;
    public static final int POSITION_LEFT_BOTTOM = 1;
    public static final int POSITION_RIGHT_BOTTOM = 2;

    public void setPosition(int position, int offx, int offy) {
        int mPositionX;
        int mPositionY;

        mPosition = position;
        mOffx = offx;
        mOffy = offy;
        switch (position) {
            case POSITION_LEFT_BOTTOM:
                mPositionX = mSubWidth / 2 + offx;
                mPositionY = mSubHeight / 2 + offy;
                break;
            case POSITION_RIGHT_BOTTOM:
                mPositionX = mWidth - (mSubWidth / 2 + offx);
                mPositionY = mSubHeight / 2 + offy;
                break;
            case POSITION_CENTER_TOP:
            default:
                mPositionX = mWidth / 2;
                mPositionY = mHeight - (mSubHeight / 2 + offy);
                break;
        }
        mRect.setPosition(mPositionX, mPositionY);
    }

    public void setSubtitle(String content, int size, int color, int alpha) {
        Bitmap map = initBitmap(content, size, color, alpha);
        mTextrueId = createBitTexture(map);
        setPosition(mPosition, mOffx, mOffy);
        mRect.setTexture(mTextrueId);
        mRect.setScale(mSubWidth, mSubHeight);
    }

    private int createBitTexture(Bitmap map) {
        if(mTextrueId >= 0) {
            GlUtil.releaseTexture(mTextrueId);
        }
        if (map == null) {
            return -1;
        }
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

    private Bitmap initBitmap(String content, int size, int color, int alpha) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        content = content.trim();
        int height = size;
        Paint paint = new Paint();
        String familyName = "宋体";
        Typeface font = Typeface.create(familyName, Typeface.BOLD);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTypeface(font);
        paint.setTextSize(size);
        //计算文字绘制坐标，居中显示
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int width = (int) paint.measureText(content);
        int baseline = (height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvasTemp = new Canvas(bmp);
        canvasTemp.drawColor(Color.TRANSPARENT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(2, 1, 1, Color2Contrary2(color));
        canvasTemp.drawText(content, width / 2, baseline, paint);
        return bmp;
    }

    private static int conColor(int num) {
        int reNum = 255 - num;
        if (reNum > 64 && reNum < 128) {
            reNum -= 64;
        } else if (reNum >= 128 && reNum < 192) {
            reNum += 64;
        }
        return reNum;
    }

    public static int Color2Contrary2(int color) {
        return Color.rgb(conColor(Color.red(color)), conColor(Color.green(color)), conColor(Color.blue(color)));
    }
}
