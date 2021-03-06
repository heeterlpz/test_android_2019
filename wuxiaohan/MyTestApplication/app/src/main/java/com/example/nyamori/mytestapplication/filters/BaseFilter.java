package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;
import android.util.Log;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.mytestapplication.MyFrameRect;
import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

import java.nio.FloatBuffer;

public class BaseFilter {
    protected final static String TAG="filter";

    protected static final int KERNEL_SIZE_SMALL = 9;
    protected static final int KERNEL_SIZE_MEDIUM = 13;
    protected static final int KERNEL_SIZE_BIG = 25;

    protected int mProgramHandle;

    protected int[] myFrame=new int[1];
    protected int[] myTexture=new int[1];
    protected int[] myRender=new int[1];

    protected int width;
    protected int height;

    protected int maPositionLoc;
    protected int maTextureCoordLoc;

    protected FloatBuffer vertexBuffer;
    protected FloatBuffer texBuffer;
    protected int vertexCount;
    protected int coordsPerVertex;
    protected int vertexStride;
    protected int texStride;

    protected float[] mTexOffset;

    public boolean isLevelZero() {
        return isLevelZero;
    }

    protected boolean isLevelZero=false;

    public BaseFilter(){}

    public BaseFilter(int width, int height){
        mProgramHandle = ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_2d);
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }

    public void release() {
        GLES20.glDeleteFramebuffers(1,myFrame,0);
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
        myFrame[0]=-1;
    }

    public int getTexture(){return myTexture[0];}

    public void setSize(int width,int height){
        chooseSize(width,height);
    }

    public void setLevel(int newLevel){
    }

    public int getLevelMax(){
        return 0;
    }

    public int getLevel(){
        return 0;
    }

    public void chooseSize(int width,int height){
        this.width=judgeSize(width);
        this.height=judgeSize(height);
        Log.i(TAG, "chooseSize: width="+this.width+" height="+this.height);
    }

    public int judgeSize(int length) {
        int i=256;
        while (length>i)i=i*2;
        return i;
    }

    public void createFrame() {
        GLES20.glGenFramebuffers(1, myFrame, 0);
        GLES20.glGenTextures(1, myTexture,0);
        GlUtil.checkGlError("glGenTextures");

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,myFrame[0]);
        GlUtil.checkGlError("glBindFramebuffer "+myFrame[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, myTexture[0]);
        GlUtil.checkGlError("glBindTexture " + myTexture[0]);

        setTexParameterOfTexture(GLES20.GL_TEXTURE_2D);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GlUtil.checkGlError("glTexImage2D");

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,myTexture[0],0);
        GlUtil.checkGlError("glFramebufferTexture2D");

        GLES20.glGenRenderbuffers(1,myRender,0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,myRender[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,GLES20.GL_DEPTH_COMPONENT16,
                width, height);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, myRender[0]);
        GlUtil.checkGlError("glFramebufferRenderbuffer");

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "innerTexture: error init frame buffer="+GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER));
            throw new RuntimeException("Unable to create frame");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
    }

    public void getLocation(){
        // get locations of attributes and uniforms
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
    }

    public void initRect(){
        vertexBuffer=MyFrameRect.getFullRectangleBuf();
        texBuffer=MyFrameRect.getFullRectangleTexBuf();
        vertexCount=MyFrameRect.getVertexCount();
        coordsPerVertex=MyFrameRect.getCoordsPerVertex();
        vertexStride=MyFrameRect.getVertexStride();
        texStride=MyFrameRect.getTexCoordStride();
    }

    public void draw(int preTexture){
        GlUtil.checkGlError("draw start");
        onBindFrame();
        setProgram();
        setTexture(preTexture);
        setUniform();
        onDraw();
        unBindFrame();
    }

    public void onBindFrame(){
        //绑定FBO
        GLES20.glViewport(0,0,width,height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,myFrame[0]);
        GlUtil.checkGlError("glBindFramebuffer");
        clearScreen();
    }

    public void setProgram(){
        GLES20.glUseProgram(mProgramHandle);
        GlUtil.checkGlError("glUseProgram");
    }

    public void setTexture(int preTexture){
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, preTexture);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, myTexture[0]);
    }

    public void setUniform(){

    }

    public void onDraw(){
        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");
        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");
        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");
        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");
        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0,vertexCount);
        GlUtil.checkGlError("glDrawArrays");
        // Done -- disable vertex array
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
    }

    public void unBindFrame(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
        GlUtil.checkGlError("draw done");
    }

    public void setTexParameterOfTexture(int target){
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");
    }

    public void clearScreen(){
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GlUtil.checkGlError("glClearColor");
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
