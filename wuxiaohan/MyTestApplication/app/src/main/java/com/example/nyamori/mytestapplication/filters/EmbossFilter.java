package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.gles.ShaderInfo;

public class EmbossFilter extends BaseFilter {
    private float[] mKernel = new float[ShaderInfo.KERNEL_SIZE];
    private float[] mTexOffset;
    private int muKernelLoc;
    private int muTexOffsetLoc;

    public EmbossFilter(int width, int height){
        super();
        mProgramHandle= GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SHADER_FILT);
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }

    @Override
    public void getLocation() {
        super.getLocation();
        muKernelLoc = GLES20.glGetUniformLocation(mProgramHandle, "uKernel");
        muTexOffsetLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexOffset");
        // initialize default values
        setKernel(new float[] {-2f, -1f, 0f,  -1f, 1f, 1f,  0f, 1f, 2f});
        setTexSize(256, 256);
    }

    @Override
    public void setUniform() {
        super.setUniform();
        GLES20.glUniform1fv(muKernelLoc, ShaderInfo.KERNEL_SIZE, mKernel, 0);
        GLES20.glUniform2fv(muTexOffsetLoc, ShaderInfo.KERNEL_SIZE, mTexOffset, 0);

    }

    public void setKernel(float[] values) {
        if (values.length != ShaderInfo.KERNEL_SIZE) {
            throw new IllegalArgumentException("Kernel size is " + values.length +
                    " vs. " + ShaderInfo.KERNEL_SIZE);
        }
        System.arraycopy(values, 0, mKernel, 0, ShaderInfo.KERNEL_SIZE);
    }

    /**
     * Sets the size of the texture.  This is used to find adjacent texels when filtering.
     */
    public void setTexSize(int width, int height) {
        float rw = 1.0f / width;
        float rh = 1.0f / height;

        // Don't need to create a new array here, but it's syntactically convenient.
        mTexOffset = new float[] {
                -rw, -rh,   0f, -rh,    rw, -rh,
                -rw, 0f,    0f, 0f,     rw, 0f,
                -rw, rh,    0f, rh,     rw, rh
        };
    }
}
