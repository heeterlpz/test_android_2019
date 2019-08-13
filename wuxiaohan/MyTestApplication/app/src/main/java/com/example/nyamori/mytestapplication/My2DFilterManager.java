package com.example.nyamori.mytestapplication;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.mytestapplication.filters.*;

import java.util.ArrayList;
import java.util.List;


// TODO: 19-8-7 拆解出filter类,将类改造成滤镜管理
public class My2DFilterManager {
    private static final String TAG = "My2DFilterManager";

    private int width;
    private int height;

    private InputFilter inputFilter;
    private OutputFilter outputFilter;
    private List<BaseFilter> filterList;

    public My2DFilterManager(int width,int height,int xStart,int yStart) {
        this.width=width;
        this.height=height;
        filterList=new ArrayList<>();
        inputFilter=new InputFilter(width,height);
        outputFilter =new OutputFilter(width,height,xStart,yStart);
    }

    public void release() {
        releaseList();
        inputFilter.release();
        outputFilter.release();
    }

    private void releaseList(){
        for(BaseFilter filter:filterList){
            filter.release();
        }
        filterList.clear();
    }

    public void addFilter(int typeCode){
        switch (typeCode) {
            case MsgConfig.MsgType.NO_TYPE:
                releaseList();
                break;
            case MsgConfig.MsgType.OBSCURE_TYPE:
                GaussianFilter gaussianFilter =new GaussianFilter(width,height);
                filterList.add(gaussianFilter);
                break;
            case MsgConfig.MsgType.SHARPENING_TYPE:
                SharpeningFilter sharpeningFilter=new SharpeningFilter(width,height);
                filterList.add(sharpeningFilter);
                break;
            case MsgConfig.MsgType.EDGE_TYPE:
                EdgeFilter edgeFilter=new EdgeFilter(width,height);
                filterList.add(edgeFilter);
                break;
            case MsgConfig.MsgType.EMBOSS_TYPE:
                EmbossFilter embossFilter=new EmbossFilter(width,height);
                filterList.add(embossFilter);
                break;
            case MsgConfig.MsgType.BW_TYPE:
                BWFilter bwFilter=new BWFilter(width,height);
                filterList.add(bwFilter);
                break;
            case MsgConfig.MsgType.MOSAIC_TYPE:
                MosaicFilter mosaicFilter=new MosaicFilter(width,height);
                filterList.add(mosaicFilter);
                break;
            case MsgConfig.MsgType.SMOOTH_TYPE:
                SmoothFilter smoothFilter=new SmoothFilter(width,height);
                filterList.add(smoothFilter);
                break;
            default:
                throw new RuntimeException("Unhandled type");
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

    public void draw(float[] texMatrix, int textureId) {
        int preTexture=textureId;
        //偶数次滤镜需要手动转90°，奇数次不用，这个设置写到addFilter和changeFilter
        inputFilter.draw(preTexture,texMatrix);
        preTexture=inputFilter.getTexture();
        for(BaseFilter filter:filterList){
            filter.draw(preTexture,texMatrix);
            preTexture=filter.getTexture();
        }
        outputFilter.draw(preTexture,texMatrix);
    }
}
