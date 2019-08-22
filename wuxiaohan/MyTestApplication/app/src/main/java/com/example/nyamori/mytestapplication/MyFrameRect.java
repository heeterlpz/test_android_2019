package com.example.nyamori.mytestapplication;

import com.example.nyamori.gles.GlUtil;

import java.nio.FloatBuffer;

/**
 * 这个类的远期目标是成为矩形绘图的数据储存，提供矩形固定的矩阵
 */
public class MyFrameRect {
    private static int cameraType=0;
    private static final int SIZEOF_FLOAT = 4;

    private static final float[] FULL_RECTANGLE_COORDS = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f,  1.0f,   // 2 top left
            1.0f,  1.0f,   // 3 top right
    };

    private static final float[] FULL_RECTANGLE_TEX_COORDS = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f,      // 3 top right

    };

    private static final float[] FULL_RECTANGLE_TEX_COORDS_ROTATE_90={
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };

    private static final float[] FULL_RECTANGLE_TEX_COORDS_ROTATE_90_FRONT={
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };


    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_ROTATE_90_BUF_BACK =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS_ROTATE_90);
    private static final FloatBuffer FULL_RECTANGLE_TEX_ROTATE_90_BUF_FRONT =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS_ROTATE_90_FRONT);

    private static int mCoordsPerVertex=2;
    private static int mVertexCount=FULL_RECTANGLE_COORDS.length / mCoordsPerVertex;
    private static int mVertexStride=mCoordsPerVertex*SIZEOF_FLOAT;
    private static int mTexCoordStride=2 * SIZEOF_FLOAT;


    public static FloatBuffer getFullRectangleBuf() {
        return FULL_RECTANGLE_BUF;
    }

    public static FloatBuffer getFullRectangleTexBuf() {
         return FULL_RECTANGLE_TEX_BUF;
    }

    public static FloatBuffer getFullRectangleTexRotate90Buf() {
        if(cameraType==Config.CAMERA_TYPE.FRONT_TYPE)return FULL_RECTANGLE_TEX_ROTATE_90_BUF_FRONT;
        else return FULL_RECTANGLE_TEX_ROTATE_90_BUF_BACK;
    }

    public static int getCoordsPerVertex() {
        return mCoordsPerVertex;
    }

    public static int getTexCoordStride() {
        return mTexCoordStride;
    }

    public static int getVertexCount() {
        return mVertexCount;
    }

    public static int getVertexStride() {
        return mVertexStride;
    }


    public static void setCameraType(int cameraType) {
        MyFrameRect.cameraType = cameraType;
    }
}
