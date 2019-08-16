package com.example.nyamori.mytestapplication;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.example.nyamori.gles.GlUtil;
import com.example.nyamori.mytestapplication.filters.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class My2DFilterManager {
    private static final String TAG = "My2DFilterManager";

    private int width;
    private int height;

    private InputFilter inputFilter;
    private OutputFilter outputFilter;
    private List<BaseFilter> filterList;
    private List<String> filterTypeList;

    public My2DFilterManager(int width,int height,int xStart,int yStart) {
        this.width=width;
        this.height=height;
        filterList=new ArrayList<>();
        filterTypeList=new ArrayList<>();
        inputFilter=new InputFilter(width,height);
        outputFilter =new OutputFilter(width,height,xStart,yStart);
    }

    public void changeSize(int width, int height, int xStart, int yStart){
        this.width=width;
        this.height=height;
        inputFilter.setSize(width,height);
        inputFilter.flashTexBuffer();
        outputFilter.setSize(width,height,xStart,yStart);
        for(BaseFilter filter:filterList){
            filter.setSize(width,height);
        }
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
        filterTypeList.clear();
    }

    public void changeFilter(int typeCode){
        releaseList();
        if(typeCode==Config.MsgType.BEAUTY_TYPE){
            addFilter(typeCode);
            SharpeningFilter sharpeningFilter=new SharpeningFilter(width,height);
            sharpeningFilter.setLevel(1);
            filterList.add(sharpeningFilter);
            filterTypeList.add(Config.FilterName.SHARPENING_TYPE);
            WhiteningFilter whiteningFilter=new WhiteningFilter(width,height);
            whiteningFilter.setLevel(4);
            filterList.add(whiteningFilter);
            filterTypeList.add(Config.FilterName.WHITENING_TYPE);
        }else {
            addFilter(typeCode);
        }
    }

    public void deleteFilter(int position){
        filterList.remove(position);
        filterTypeList.remove(position);
        Log.d(TAG, "deleteFilter: now list"+ filterTypeList);
    }

    public void addFilter(int typeCode){
        switch (typeCode) {
            case Config.MsgType.NO_TYPE:
                releaseList();
                break;
            case Config.MsgType.OBSCURE_TYPE:
                GaussianFilter gaussianFilter =new GaussianFilter(width,height);
                filterList.add(gaussianFilter);
                filterTypeList.add(Config.FilterName.OBSCURE_TYPE);
                break;
            case Config.MsgType.SHARPENING_TYPE:
                SharpeningFilter sharpeningFilter=new SharpeningFilter(width,height);
                sharpeningFilter.setLevel(4);
                filterList.add(sharpeningFilter);
                filterTypeList.add(Config.FilterName.SHARPENING_TYPE);
                break;
            case Config.MsgType.EDGE_TYPE:
                EdgeFilter edgeFilter=new EdgeFilter(width,height);
                filterList.add(edgeFilter);
                filterTypeList.add(Config.FilterName.EDGE_TYPE);
                break;
            case Config.MsgType.EMBOSS_TYPE:
                EmbossFilter embossFilter=new EmbossFilter(width,height);
                filterList.add(embossFilter);
                filterTypeList.add(Config.FilterName.EMBOSS_TYPE);
                break;
            case Config.MsgType.BW_TYPE:
                BWFilter bwFilter=new BWFilter(width,height);
                filterList.add(bwFilter);
                filterTypeList.add(Config.FilterName.BW_TYPE);
                break;
            case Config.MsgType.MOSAIC_TYPE:
                MosaicFilter mosaicFilter=new MosaicFilter(width,height);
                filterList.add(mosaicFilter);
                filterTypeList.add(Config.FilterName.MOSAIC_TYPE);
                break;
            case Config.MsgType.SMOOTH_TYPE:
                SmoothFilter smoothFilter=new SmoothFilter(width,height);
                filterList.add(smoothFilter);
                filterTypeList.add(Config.FilterName.SMOOTH_TYPE);
                break;
            case Config.MsgType.BEAUTY_TYPE:
                BeautyFilter beautyFilter=new BeautyFilter(width,height);
                filterList.add(beautyFilter);
                filterTypeList.add(Config.FilterName.BEAUTY_TYPE);
                break;
            case Config.MsgType.WHITENING_TYPE:
                WhiteningFilter whiteningFilter=new WhiteningFilter(width,height);
                whiteningFilter.setLevel(5);
                filterList.add(whiteningFilter);
                filterTypeList.add(Config.FilterName.WHITENING_TYPE);
                break;
            case Config.MsgType.TEST_TYPE:
                TestFilter testFilter=new TestFilter(width,height);
                filterList.add(testFilter);
                filterTypeList.add(Config.FilterName.TEST_TYPE);
                break;
            default:
                throw new RuntimeException("Unhandled type");
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

    public void draw(int textureId) {
        int preTexture=textureId;
        inputFilter.draw(preTexture);
        preTexture=inputFilter.getTexture();
        for(BaseFilter filter:filterList){
            if(!filter.isLevelZero()){
                filter.draw(preTexture);
                preTexture=filter.getTexture();
            }
        }
        outputFilter.draw(preTexture);
    }

    public List<String> getFilterTypeList() {
        return filterTypeList;
    }

    public BaseFilter getFilter(int position) {
        return filterList.get(position);
    }
}
