package com.example.nyamori.mytestapplication;

import com.example.nyamori.gles.GlUtil;

import java.nio.FloatBuffer;

/**
 * 这个类的远期目标是成为矩形绘图的数据储存，提供矩形固定的矩阵
 */
public class MyFrameRect {

    private My2DFilterManager mProgram;
    private static final int SIZEOF_FLOAT = 4;

    private static final float[] FULL_RECTANGLE_COORDS = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f, 1.0f,   // 2 top left
            1.0f, 1.0f,   // 3 top right
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

    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_ROTATE_90_BUF=
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS_ROTATE_90);

    private FloatBuffer mVertexArray;
    private FloatBuffer mTexCoordArray;
    private int mVertexCount;
    private int mCoordsPerVertex;
    private int mVertexStride;
    private int mTexCoordStride;


    public MyFrameRect(My2DFilterManager program) {
        mProgram = program;
        mVertexArray = FULL_RECTANGLE_BUF;
        mTexCoordArray = FULL_RECTANGLE_TEX_BUF;
        mCoordsPerVertex = 2;
        mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
        mVertexCount = FULL_RECTANGLE_COORDS.length / mCoordsPerVertex;
        mTexCoordStride = 2 * SIZEOF_FLOAT;
    }

    /**
     * Changes the program.  The previous program will be released.
     * <p>
     * The appropriate EGL context must be current.
     */
    public void changeProgram(My2DFilterManager program) {
        mProgram.release();
        mProgram = program;
    }

    /**
     *接收外部纹理的输入的texture
     */
    public int createTextureObject() {
        return mProgram.createInputTextureObject();
    }

    /**
     * Draws a viewport-filling rect, texturing it with the specified texture object.
     */
    public void drawFrame(int textureId, float[] texMatrix) {
        // Use the identity matrix for MVP so our 2x2 FULL_RECTANGLE covers the viewport.
        mProgram.draw(mVertexArray, 0, mVertexCount,
                mCoordsPerVertex, mVertexStride, texMatrix,
                mTexCoordArray, textureId, mTexCoordStride);
    }

    public static FloatBuffer getFullRectangleBuf() {
        return FULL_RECTANGLE_BUF;
    }

    public static FloatBuffer getFullRectangleTexBuf() {
        return FULL_RECTANGLE_TEX_BUF;
    }

    public static FloatBuffer getFullRectangleTexRotate90Buf() {
        return FULL_RECTANGLE_TEX_ROTATE_90_BUF;
    }

    public int getmCoordsPerVertex() {
        return mCoordsPerVertex;
    }
}
