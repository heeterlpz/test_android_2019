package com.example.nyamori.mytestapplication;

import com.example.nyamori.gles.Drawable2d;
import com.example.nyamori.gles.GlUtil;

public class MyFrameRect {

    private final Drawable2d mRectDrawable = new Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE);
    private My2DFilterManager mProgram;

    /**
     * Prepares the object.
     *
     * @param program The program to use.  FullFrameRect takes ownership, and will release
     *     the program when no longer needed.
     */
    public MyFrameRect(My2DFilterManager program) {
        mProgram = program;
    }

    /**
     * Releases resources.
     * <p>
     * This must be called with the appropriate EGL context current (i.e. the one that was
     * current when the constructor was called).  If we're about to destroy the EGL context,
     * there's no value in having the caller make it current just to do this cleanup, so you
     * can pass a flag that will tell this function to skip any EGL-context-specific cleanup.
     */
    public void release(boolean doEglCleanup) {
        if (mProgram != null) {
            if (doEglCleanup) {
                mProgram.release();
            }
            mProgram = null;
        }
    }

    /**
     * Returns the program currently in use.
     */
    public My2DFilterManager getProgram() {
        return mProgram;
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
        mProgram.draw(GlUtil.IDENTITY_MATRIX, mRectDrawable.getVertexArray(), 0,
                mRectDrawable.getVertexCount(), mRectDrawable.getCoordsPerVertex(),
                mRectDrawable.getVertexStride(),
                texMatrix, mRectDrawable.getTexCoordArray(), textureId,
                mRectDrawable.getTexCoordStride());
    }
}
