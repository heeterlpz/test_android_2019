package com.example.nyamori.mytestapplication;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.gles.ShaderInfo;

import java.nio.FloatBuffer;



// TODO: 19-8-7 拆解出filter类,将类改造成滤镜管理

public class My2DFilterManager {
    private static final String TAG = "My2DFilterManager";
    private int[] myFrame=new int[1];
    private int[] textures=new int[1];
    private int[] fRender=new int[1];

    public enum ProgramType {
        TEXTURE_EXT,//原图片
        TEXTURE_EXT_HP,//高清版
        TEXTURE_EXT_BW,//黑白滤镜
        TEXTURE_DIV_UD,//切割-镜像处理
        TEXTURE_SPLIT,//切割-九宫图
        TEXTURE_MOSAIC,//马赛克
        TEXTURE_EXT_FILT,//这是一个卷积滤镜
        TEXTURE_SMOOTH //平滑
    }

    // Handles to the GL program and various components of it.
    private int mProgramHandle;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;
    private int muKernelLoc;
    private int muTexOffsetLoc;
    private int muColorAdjustLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;

    private int mTextureTarget;



    private float[] mKernel = new float[ShaderInfo.KERNEL_SIZE];
    private float[] mTexOffset;
    private float mColorAdjust;

    /**
     * Prepares the program in the current EGL context.
     */
    public My2DFilterManager(ProgramType programType) {

        switch (programType) {
            case TEXTURE_EXT:
                mProgramHandle = GlUtil.createProgram(ShaderInfo.VERTEX_SHADER, ShaderInfo.FRAGMENT_SHADER_EXT);
                break;
            case TEXTURE_DIV_UD:
                mProgramHandle = GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_DIV_UD);
                break;
            case TEXTURE_SPLIT:
                mProgramHandle = GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SPLIT);
                break;
            case TEXTURE_MOSAIC:
                mProgramHandle = GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_MOSAIC);
                break;
            case TEXTURE_SMOOTH:
                mProgramHandle = GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SMOOTH);
                break;
            case TEXTURE_EXT_BW:
                mProgramHandle = GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SHADER_EXT_BW);
                break;
            case TEXTURE_EXT_FILT:
                mProgramHandle = GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SHADER_EXT_FILT);
                break;
            case TEXTURE_EXT_HP:
                mProgramHandle = GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SHADER_EXT_HP);
                break;
            default:
                throw new RuntimeException("Unhandled type " + programType);
        }
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }

        mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        // get locations of attributes and uniforms

        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");
        muKernelLoc = GLES20.glGetUniformLocation(mProgramHandle, "uKernel");
        if (muKernelLoc < 0) {
            // no kernel in this one
            muKernelLoc = -1;
            muTexOffsetLoc = -1;
            muColorAdjustLoc = -1;
        } else {
            // has kernel, must also have tex offset and color adj
            muTexOffsetLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexOffset");
            GlUtil.checkLocation(muTexOffsetLoc, "uTexOffset");
            muColorAdjustLoc = GLES20.glGetUniformLocation(mProgramHandle, "uColorAdjust");
            GlUtil.checkLocation(muColorAdjustLoc, "uColorAdjust");
            // initialize default values
            setKernel(new float[] {0f, 0f, 0f,  0f, 1f, 0f,  0f, 0f, 0f}, 0f);
            setTexSize(256, 256);
        }
    }

    /**
     * Releases the program.
     * <p>
     * The appropriate EGL context must be current (i.e. the one that was used to create
     * the program).
     */
    public void release() {
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }

    /**
     * Creates a texture object suitable for use with this program.
     * <p>
     * On exit, the texture will be bound.
     */
    public int createInputTextureObject() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GlUtil.checkGlError("glGenTextures");

        int texId = textures[0];
        GLES20.glBindTexture(mTextureTarget, texId);
        GlUtil.checkGlError("glBindTexture " + texId);

        setTexParameterOfTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);

        innerTexture();
        return texId;
    }

    /**
     * 内部的2d纹理的textures
     */
    public void innerTexture(){
        GLES20.glGenFramebuffers(1, myFrame, 0);
        GLES20.glGenTextures(1, textures,0);
        GlUtil.checkGlError("glGenTextures");

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,myFrame[0]);
        GlUtil.checkGlError("glBindFramebuffer "+myFrame[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GlUtil.checkGlError("glBindTexture " + textures[0]);

        setTexParameterOfTexture(GLES20.GL_TEXTURE_2D);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 2048, 2048, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GlUtil.checkGlError("glTexImage2D");

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,textures[0],0);
        GlUtil.checkGlError("glFramebufferTexture2D");

        GLES20.glGenRenderbuffers(1,fRender,0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,fRender[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,GLES20.GL_DEPTH_COMPONENT16,
                2048, 2048);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, fRender[0]);
        GlUtil.checkGlError("glFramebufferRenderbuffer");

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "innerTexture: error init frame buffer="+GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER));
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
    }

    /**
     * Configures the convolution filter values.
     *
     * @param values Normalized filter values; must be KERNEL_SIZE elements.
     */
    public void setKernel(float[] values, float colorAdj) {
        if (values.length != ShaderInfo.KERNEL_SIZE) {
            throw new IllegalArgumentException("Kernel size is " + values.length +
                    " vs. " + ShaderInfo.KERNEL_SIZE);
        }
        System.arraycopy(values, 0, mKernel, 0, ShaderInfo.KERNEL_SIZE);
        mColorAdjust = colorAdj;
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

    public void setTexParameterOfTexture(int target){
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");
    }

    public void clearScreen(){
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GlUtil.checkGlError("glClearColor");
    }

    // TODO: 19-8-9 继续拆解

    /**
     *偶发 报错如下
     * E/Adreno-GSL: <gsl_memory_alloc_pure:2236>: GSL MEM ERROR: kgsl_sharedmem_alloc ioctl failed.
     * W/Adreno-GSL: <sharedmem_gpuobj_alloc:2436>: sharedmem_gpumem_alloc: mmap failed errno 12 Out of memory
     */
    public void onDraw(FloatBuffer vertexBuffer, int firstVertex,
                       int vertexCount, int coordsPerVertex, int vertexStride,
                       float[] texMatrix, FloatBuffer texBuffer,int texStride){
        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, GlUtil.IDENTITY_MATRIX, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");
        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");
        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");
        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");
        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");
        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");
        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
    }

    /**
     * Issues the draw call.  Does the full setup on every call.
     * @param vertexBuffer Buffer with vertex position data.
     * @param firstVertex Index of first vertex to use in vertexBuffer.
     * @param vertexCount Number of vertices in vertexBuffer.
     * @param coordsPerVertex The number of coordinates per vertex (e.g. x,y is 2).
     * @param vertexStride Width, in bytes, of the position data for each vertex (often
     *        vertexCount * sizeof(float)).
     * @param texMatrix A 4x4 transformation matrix for texture coords.  (Primarily intended
     *        for use with SurfaceTexture.)
     * @param texBuffer Buffer with vertex texture data.
     * @param texStride Width, in bytes, of the texture data for each vertex.
     */
    public void draw(FloatBuffer vertexBuffer, int firstVertex,
                     int vertexCount, int coordsPerVertex, int vertexStride,
                     float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        GLES20.glViewport(0,0,2048,2048);
        GlUtil.checkGlError("draw start");
        //绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,myFrame[0]);
        GlUtil.checkGlError("glBindFramebuffer");
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        clearScreen();

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mTextureTarget, textureId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        // Populate the convolution kernel, if present.
        if (muKernelLoc >= 0) {
            GLES20.glUniform1fv(muKernelLoc, ShaderInfo.KERNEL_SIZE, mKernel, 0);
            GLES20.glUniform2fv(muTexOffsetLoc, ShaderInfo.KERNEL_SIZE, mTexOffset, 0);
            GLES20.glUniform1f(muColorAdjustLoc, mColorAdjust);
        }
        onDraw(vertexBuffer,firstVertex,vertexCount,
                coordsPerVertex,vertexStride,
                texMatrix,MyFrameRect.getFullRectangleTexRotate90Buf(),texStride);

        // Done -- disable frame, texture, and program.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glBindTexture(mTextureTarget, 0);
        GLES20.glUseProgram(0);

        //接下来把frame的数据渲染到屏幕上
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);

        GLES20.glViewport(0,0,1536,2048);
        clearScreen();

        int program=GlUtil.createProgram(ShaderInfo.VERTEX_SHADER,ShaderInfo.FRAGMENT_SHADER_2D);
        GLES20.glUseProgram(program);
        GlUtil.checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        onDraw(vertexBuffer,firstVertex,vertexCount,
                coordsPerVertex,vertexStride,
                texMatrix,texBuffer,texStride);

        // Done -- disable frame, texture, and program.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glBindTexture(mTextureTarget, 0);
        GLES20.glUseProgram(0);
    }
}
