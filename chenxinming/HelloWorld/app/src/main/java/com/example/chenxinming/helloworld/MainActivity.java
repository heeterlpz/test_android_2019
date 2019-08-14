package com.example.chenxinming.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback{

    private static Context context = null;
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceholder;

    private SurfaceView surfaceview1;
    private SurfaceHolder surfaceholder1;
    private Camera camera = null;
    private int fpx=0;
    private long currentTimeMillis=0;
    private long lastCurrentTimeMillis=0;
    private String laststr="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        surfaceview = (SurfaceView)findViewById(R.id.surfaceview);
        surfaceholder = surfaceview.getHolder();
        surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceholder.addCallback(MainActivity.this);

        surfaceview1 = (SurfaceView)findViewById(R.id.surfaceview1);
        surfaceholder1 = surfaceview1.getHolder();
        surfaceholder1.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        System.out.println("surfacecreated");
        //获取camera对象
        camera = Camera.open();
        try {
            //设置预览监听
            camera.setPreviewDisplay(surfaceHolder);
            Camera.Parameters parameters = camera.getParameters();

            if (this.getResources().getConfiguration().orientation
                    != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait");
                camera.setDisplayOrientation(90);
                parameters.setRotation(90);
            } else {
                parameters.set("orientation", "landscape");
                camera.setDisplayOrientation(0);
                parameters.setRotation(0);
            }
            camera.setParameters(parameters);
            //启动摄像头预览
            camera.addCallbackBuffer(new byte[460800]);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
            System.out.println("camera.startpreview");

        } catch (IOException e) {
            e.printStackTrace();
            camera.release();
            System.out.println("camera.release");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        System.out.println("surfacechanged");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            System.out.println("surfaceDestroyed");
            if (camera != null) {
                camera.stopPreview();
                camera.release();
            }
    }

    //拍照
    public void takePhoto(View view){
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //将字节数组
                Bitmap bitmap= BitmapFactory.decodeByteArray(data,0,data.length);
                //输出流保存数据
                try {
                    FileOutputStream fileOutputStream=new FileOutputStream("/sdcard/output.png");
                    bitmap.compress(Bitmap.CompressFormat.PNG,85,fileOutputStream);

                    camera.stopPreview();
                    camera.startPreview();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

        Camera.Size size = camera.getParameters().getPreviewSize();
        if (surfaceview1 == null) {
            return;
        }
        // 将NV21类型的数据转为Image
        YuvImage yuvImage = new YuvImage(bytes, ImageFormat.NV21, size.width, size.height, null);
        if (yuvImage != null) {
               Paint paint = new Paint();//画笔
               paint.setAntiAlias(true);//设置是否抗锯齿
               paint.setStyle(Paint.Style.STROKE);//设置画笔风格
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, out);
            // 得到原始图片
            Bitmap bitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
            SurfaceHolder customSurfaceHolder = surfaceview1.getHolder();
            // 获取到画布
            Canvas canvas = customSurfaceHolder.lockCanvas();
            // 将加了水印的图片绘制到预览窗口
            //String strfpx="fpx:"+Integer.toString(fpx);
            String strfpx="null";
            if(currentTimeMillis == 0){
                currentTimeMillis=System.currentTimeMillis();
            }else{
                lastCurrentTimeMillis=System.currentTimeMillis();
                if(lastCurrentTimeMillis-currentTimeMillis >= 1000){
                    strfpx="fpx:"+Integer.toString(fpx);
                    laststr=strfpx;
                    fpx=0;
                    currentTimeMillis=lastCurrentTimeMillis;
                }else{
                    strfpx=laststr;
                }
            }
            System.currentTimeMillis();
            canvas.drawBitmap(rotateBitmapAndWaterMark(bitmap, 90, strfpx, size.width, size.height, paint), 0, 0, null);
            customSurfaceHolder.unlockCanvasAndPost(canvas);
            fpx++;
        }

        camera.addCallbackBuffer(bytes);
    }
    /**
     * 给图片添加水印和时间戳
     *
     * @param originBitmap 原始图片
     * @param degree 旋转角度
     * @param watermark 水印文字
     * @param paint 绘制水印的画笔对象
     * @return 最终处理的结果
     */
    public static Bitmap rotateBitmapAndWaterMark(Bitmap originBitmap, int degree, String watermark, int width, int height, Paint paint) {
//        int width = originBitmap.getWidth();
//        int height = originBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.save();
        canvas.rotate(degree, width / 2, height / 2);
        canvas.drawBitmap(originBitmap, 0, 0, null);
        canvas.restore();
        int textWidht = (int) paint.measureText(watermark);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int textHeight = (int) (fontMetrics.ascent - fontMetrics.descent);
        int x = (width - textWidht) / 2;
        int y = (height - textHeight) / 2;
        y = (int) (y - fontMetrics.descent);
        canvas.drawText(watermark, x, y, paint);
        canvas.drawText(String.valueOf(System.currentTimeMillis()), x, y + textHeight, paint);
        // 立即回收无用内存
        originBitmap.recycle();
        originBitmap = null;
        return resultBitmap;
    }
}
