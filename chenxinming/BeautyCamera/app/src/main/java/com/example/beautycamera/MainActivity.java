package com.example.beautycamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private CameraGLSurfaceView mGLSurfaceView;
    private int mCameraId;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout linearLayout = findViewById(R.id.camera);

        //实例化一个GLSurfaceView
        mGLSurfaceView = new CameraGLSurfaceView(this);
        //设置后置摄像头
        mCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
        DisplayMetrics dm = new DisplayMetrics();
        mCamera = new Camera(this);
        if (!mCamera.openCamera(dm.widthPixels, dm.heightPixels, mCameraId)) {
            return;
        }
        //初始化GLSurfaceView
        mGLSurfaceView.init(mCamera, false, MainActivity.this, 0);
        //在屏幕上显示GLSurfaceView
        linearLayout.addView(mGLSurfaceView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_original:
                if(mGLSurfaceView != null) {
                    mGLSurfaceView.getmRenderer().setType(0);
                    Toast.makeText(this,"原图像", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_black_and_white:
                if(mGLSurfaceView != null){
                    mGLSurfaceView.getmRenderer().setType(1);
                    Toast.makeText(this,"黑白滤镜", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_sharpen:
                if(mGLSurfaceView != null){
                    mGLSurfaceView.getmRenderer().setType(2);
                    Toast.makeText(this,"锐化", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_slur:
                if(mGLSurfaceView != null){
                    mGLSurfaceView.getmRenderer().setType(3);
                    Toast.makeText(this,"模糊", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_africa:
                if(mGLSurfaceView != null){
                    mGLSurfaceView.getmRenderer().setType(4);
                    Toast.makeText(this,"非洲滤镜", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_beauty:
                if(mGLSurfaceView != null){
                    mGLSurfaceView.getmRenderer().setType(5);
                    Toast.makeText(this,"美颜", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_relief:
                if(mGLSurfaceView != null){
                    mGLSurfaceView.getmRenderer().setType(6);
                    Toast.makeText(this,"浮雕", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
