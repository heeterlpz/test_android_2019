package com.example.nyamori.mytestapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

public class DrawSurface {
    private final static String TAG = "DrawSurface";
    private NV21ToBitmap changer;
    private Runnable drawSurface;
    private Surface mSurface;
    private List<YuvImage> yuvImageList;
    private int width;
    private int height;
    private boolean isThreadRunning=false;
    private boolean isPause=false;

    private Handler mUIHandler;

    private int fpsCount;
    private long fpsTime;

    public DrawSurface(int width, int height, Surface surface,Handler mUIHandler, Context context){
        this.width=width;
        this.height=height;
        this.mUIHandler=mUIHandler;
        fpsCount=0;
        fpsTime=System.currentTimeMillis();
        yuvImageList = new ArrayList<>();
        changer=new NV21ToBitmap(context);
        mSurface=surface;
        initThread();
    }

    public void startThread() {
        if(isPause==true){
            Log.d(TAG, "startThread: start");
            isPause=false;
            initThread();
        }
    }

    public void pauseThread(){
        if(isPause==false){
            Log.d(TAG, "pauseThread: pause");
            isPause=true;
            isThreadRunning=false;
            yuvImageList.clear();
        }
    }

    private void initThread() {
        isThreadRunning=true;
        drawSurface=new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                while (isThreadRunning){
                    if(yuvImageList.isEmpty()){
                        Log.v(TAG, "run: List is empty");
                    }else {
                        YuvImage yuv=yuvImageList.get(0);
                        if(yuv!=null){
                            Log.d(TAG, "run: success get 1 image");
                            Canvas canvas=mSurface.lockHardwareCanvas();
                            Bitmap bitmap=changer.nv21ToBitmap(
                                    rotateYUV420Degree90(yuv.getYuvData(),yuv.getWidth(),yuv.getHeight())
                                    ,yuv.getHeight(),yuv.getWidth());
                            canvas.drawBitmap(bitmap
                                    ,null,new Rect(0,0,width,height),null);
                            mSurface.unlockCanvasAndPost(canvas);
                            bitmap.recycle();
                        }
                        Log.d(TAG, "run: list size="+yuvImageList.size());
                        //可以观察到内存泄露，暂时先在size大于一定值时clear
                        // TODO: 19-8-2 找到内存泄露的原因
                        if(yuvImageList.size()>5){
                            yuvImageList.clear();
                            Log.d(TAG, "run: remove all happened");
                        }
                        if(!yuvImageList.isEmpty()){
                            yuvImageList.remove(0);
                            Log.d(TAG, "run: success remove 1 image");
                            fpsCount++;
                            long nowTime=System.currentTimeMillis();
                            if((nowTime-fpsTime)>999){
                                mUIHandler.obtainMessage(11,fpsCount,0).sendToTarget();
                                fpsTime=nowTime;
                                fpsCount=0;
                            }
                        }
                    }
                }
            }
        };
        new Thread(drawSurface).start();
    }

    public void putNV21Data(YuvImage nv21){
        if(isPause==false){
            yuvImageList.add(nv21);
        }
    }

    public void closeThread(){
        isThreadRunning=false;
        yuvImageList.clear();
    }

    //come from stack overflow
    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++)
        {
            for(int y = imageHeight-1;y >= 0;y--)
            {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth*imageHeight*3/2-1;
        for(int x = imageWidth-1;x > 0;x=x-2)
        {
            for(int y = 0;y < imageHeight/2;y++)
            {
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i--;
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i--;
            }
        }
        return yuv;
    }

    public boolean isPause() {
        return isPause;
    }
}
