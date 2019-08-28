package com.example.nyamori.mytestapplication;


import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Faces {
    private static List<RectF> faceInfoList;
    private static int faceNumber;
    private static int camera= Config.CAMERA_TYPE.FRONT_TYPE;

    private static PointF[] edgePoints=new PointF[19];

    static {
        faceInfoList=new ArrayList<>();
    }

    public static List<RectF> getFaceInfoList() {
        return faceInfoList;
    }

    public static void setPoints(PointF[] points,int width,int height){
        if(points.length!=81){
            throw new RuntimeException("wrong points number");
        }else {
            int eNum=0;
            for(int i=62;i<81;i++){
                float x,y;
                if(camera== Config.CAMERA_TYPE.FRONT_TYPE){
                    x=1-points[i].y/height;
                    y=points[i].x/width;
                }else {
                    x=points[i].y/height;
                    y=1-points[i].x/width;
                }
                edgePoints[eNum]=new PointF(x,y);
                Log.v("test", "setPoints: a point="+edgePoints[eNum]);
                eNum++;
            }
            eNum=0;
        }
    }

    public static PointF[] getEdgePoints() {
        return edgePoints;
    }

    public static void setFaceInfoList(Rect faceRect,int width,int height) {
        float left=0.0f;
        float top=0.0f;
        float right=0.0f;
        float bottom=0.0f;
        if(camera== Config.CAMERA_TYPE.FRONT_TYPE){
            left=1.0f-faceRect.bottom/(float)height;
            right=1.0f-faceRect.top/(float)height;
            top = faceRect.left/(float)width;
            bottom =faceRect.right/(float)width;
        }else {
            //暂时没有验证的转换
            left=(height-faceRect.top)/(float)height;
            right=(height-faceRect.bottom)/(float)height;
            top = faceRect.left/(float)width;
            bottom =faceRect.right/(float)width;
        }
        RectF fitRect=new RectF(left,top,right,bottom);
        faceInfoList.add(fitRect);
    }

    public static int getFaceNumber() {
        return faceNumber;
    }

    public static void setFaceNumber(int faceNumber) {
        Faces.faceNumber = faceNumber;
    }

    public static void clearList(){
        faceInfoList.clear();
        faceNumber=0;
    }

    public static void setCamera(int camera) {
        Faces.camera = camera;
    }
}
