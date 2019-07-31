package com.example.chenxinming.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements SurfaceHolder.Callback{

//    private final int CAMERA_REQUEST = 1;
//    private ImageView picture;
//    private Uri imageUri;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        final Button takephoto=(Button)findViewById(R.id.takephoto);
//        takephoto.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v){
////                File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
////                if(outputImage.exists()){
////                    outputImage.delete();
////                }
////                try {
////                    outputImage.createNewFile();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////                if(Build.VERSION.SDK_INT>=24){
////                    imageUri= FileProvider.getUriForFile(MainActivity.this,"com.example.chenxinming.helloworld.fileprovider",outputImage);
////                }else{
////                    imageUri=Uri.fromFile(outputImage);
////                }
////                Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
////                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
////                startActivityForResult(intent,CAMERA_REQUEST);
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
//            }
//        });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode,int resultCode,Intent data){
////        switch (requestCode){
////            case CAMERA_REQUEST:
////                if(resultCode == RESULT_OK){
////                    try {
////                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
////                        picture.setImageBitmap(bitmap);
////                    } catch (FileNotFoundException e) {
////                        e.printStackTrace();
////                    }
////                }
////                break;
////            default:
////                break;
////        }
//        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            picture=(ImageView)findViewById(R.id.image);
//            picture.setImageBitmap(photo);
//        }
//    }

    private static Context context = null;
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceholder;
    private Camera camera = null;
    private ImageView picture;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        surfaceview = (SurfaceView)findViewById(R.id.surfaceview);
        surfaceholder = surfaceview.getHolder();
        surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceholder.addCallback(MainActivity.this);
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

}
