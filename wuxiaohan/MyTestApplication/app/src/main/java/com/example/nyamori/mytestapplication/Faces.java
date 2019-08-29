package com.example.nyamori.mytestapplication;


import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;


public class Faces {
    public static long lastUpdatetime=0;
    private static RectF faceInfo;
    private static int faceNumber;
    private static int camera= Config.CAMERA_TYPE.FRONT_TYPE;
    private static int width;
    private static int height;

    private static PointF[] edgePoints=new PointF[19];
    private static PointF[] rightEyePoints=new PointF[5];
    private static PointF[] leftEyePoints=new PointF[5];

    public static void setSize(int width,int height){
        Faces.width=width;
        Faces.height=height;
    }

    public static RectF getFaceInfoList() {
        return faceInfo;
    }

    public static void setPoints(PointF[] points){
        lastUpdatetime=System.currentTimeMillis();
        if(points.length!=81){
            throw new RuntimeException("wrong points number");
        }else {
            for(int i=0;i<81;i++){
                if(i<5){
                    rightEyePoints[i]=adjustPoint(points[i]);
                }else if(i>8&&i<14){
                    leftEyePoints[i-9]=adjustPoint(points[i]);
                }else if(i>61){
                    edgePoints[i-62]=adjustPoint(points[i]);
                }
            }
        }
    }

    private static PointF adjustPoint(PointF point){
        float x,y;
        if(camera== Config.CAMERA_TYPE.FRONT_TYPE){
            x=1-point.y/height;
            y=point.x/width;
        }else {
            //暂时没有验证的转换 后置
            x=point.y/height;
            y=1-point.x/width;
        }
        return new PointF(x,y);
    }

    public static PointF[] getEdgePoints() {
        return edgePoints;
    }

    public static PointF[] getLeftEyePoints() {
        return leftEyePoints;
    }

    public static PointF[] getRightEyePoints() {
        return rightEyePoints;
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
        faceInfo= new RectF(left,top,right,bottom);

    }

    public static int getFaceNumber() {
        return faceNumber;
    }

    public static void setFaceNumber(int faceNumber) {
        Faces.faceNumber = faceNumber;
    }

    public static void clearList(){
        faceInfo=null;
        faceNumber=0;
    }

    public static void setCamera(int camera) {
        Faces.camera = camera;
    }
}
