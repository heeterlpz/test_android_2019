package com.example.nyamori.mytestapplication;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.gles.ShaderInfo;
import com.example.nyamori.mytestapplication.filters.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;


// TODO: 19-8-7 拆解出filter类,将类改造成滤镜管理

public class My2DFilterManager {
    private static final String TAG = "My2DFilterManager";

    public enum ProgramType {
        TEXTURE_EXT_HP,//高清版
        TEXTURE_EXT_BW,//黑白滤镜
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

    private int width;
    private int height;

    private InputFilter inputFilter;
    private OutputFilter outputFliter;
    private List<BaseFilter> filterList;

    private float[] mKernel = new float[ShaderInfo.KERNEL_SIZE];
    private float[] mTexOffset;
    private float mColorAdjust;

    public My2DFilterManager(int width,int height) {
        this.width=width;
        this.height=height;
        filterList=new ArrayList<>();

        inputFilter=new InputFilter(2048,2048);
        outputFliter=new OutputFilter(1536,2048);
    }

    public void release() {
        releaseList();
        inputFilter.release();
        outputFliter.release();
    }

    private void releaseList(){
        for(BaseFilter filter:filterList){
            filter.release();
        }
        filterList.clear();
    }

    public void addFilter(int typeCode){

        switch (typeCode) {
            case MsgConfig.MsgArg.NO_ARG:
                releaseList();
                break;
            case MsgConfig.MsgArg.OBSCURE_TYPE:
                ObscureFilter obscureFilter=new ObscureFilter(width,height);
                filterList.add(obscureFilter);
                break;
            case MsgConfig.MsgArg.SHARPENING_TYPE:
                SharpeningFilter sharpeningFilter=new SharpeningFilter(width,height);
                filterList.add(sharpeningFilter);
                break;
            case MsgConfig.MsgArg.EDGE_TYPE:
                EdgeFilter edgeFilter=new EdgeFilter(width,height);
                filterList.add(edgeFilter);
                break;
            case MsgConfig.MsgArg.EMBOSS_TYPE:
                EmbossFilter embossFilter=new EmbossFilter(width,height);
                filterList.add(embossFilter);
                break;
            case MsgConfig.MsgArg.BW_TYPE:
                BWFilter bwFilter=new BWFilter(width,height);
                filterList.add(bwFilter);
                break;
            case MsgConfig.MsgArg.MOSAIC_TYPE:
                MosaicFilter mosaicFilter=new MosaicFilter(width,height);
                filterList.add(mosaicFilter);
                break;
            case MsgConfig.MsgArg.SMOOTH_TYPE:
                SmoothFilter smoothFilter=new SmoothFilter(width,height);
                filterList.add(smoothFilter);
                break;
            default:
                throw new RuntimeException("Unhandled type ");
        }

        if(filterList.size()%2==0){
            inputFilter.setTexBuffer(true);
        }else {
            inputFilter.setTexBuffer(false);
        }
    }

    public int createInputTextureObject() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GlUtil.checkGlError("glGenTextures");

        int texId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId);
        GlUtil.checkGlError("glBindTexture " + texId);

        setTexParameterOfTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        return texId;
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


    // TODO: 19-8-9 继续拆解

    public void draw(float[] texMatrix, int textureId) {
        int preTexture=textureId;
        //偶数次滤镜需要手动转90°，奇数次不用，这个设置写到addFilter和changeFilter
        inputFilter.draw(preTexture,texMatrix);
        preTexture=inputFilter.getTexture();
        for(BaseFilter filter:filterList){
            filter.draw(preTexture,texMatrix);
            preTexture=filter.getTexture();
        }
        outputFliter.draw(preTexture,texMatrix);
    }
}
