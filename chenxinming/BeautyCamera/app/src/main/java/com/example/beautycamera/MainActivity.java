package com.example.beautycamera;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private CameraGLSurfaceView mGLSurfaceView;
    private RelativeLayout relativeLayout;
    private TextView mInfoView;
    private Handler mHandle;
    private int mCameraId;
    private Camera mCamera;
    private static final int MSG_UPDATE_IMG = 0;
    private int mFpsCount=0;
    private long mFpsTime=0;
    private SeekBar mSeekBar;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.camera);

        //实例化一个GLSurfaceView
        mGLSurfaceView = new CameraGLSurfaceView(this);
        mInfoView= new TextView(this);
        mInfoView.setTextColor(Color.RED);
        mInfoView.setText("fps:");

        //设置后置摄像头
        mCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
        DisplayMetrics dm = new DisplayMetrics();
        mCamera = new Camera(this);
        if (!mCamera.openCamera(dm.widthPixels, dm.heightPixels, mCameraId)) {
            return;
        }
        mHandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_IMG:
                        //帧率统计
                        mFpsCount ++;
                        long curTime = System.currentTimeMillis();
                        if(curTime - mFpsTime > 999) {
                            mFpsTime = curTime;
                            mInfoView.setText("fps:" + mFpsCount);
                            mFpsCount = 0;
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        //初始化GLSurfaceView
        mGLSurfaceView.init(mCamera, false, MainActivity.this, 0, mHandle);
        //在屏幕上显示GLSurfaceView
        relativeLayout.addView(mGLSurfaceView);
        relativeLayout.addView(mInfoView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    public SeekBar addSeekBar(int shaderType){
        final SeekBar mSeekBar = new SeekBar(this);
        final int mShaderType = shaderType;
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                switch (mShaderType){
                    case 1:
                        mSeekBar.setMax(100);
                        mGLSurfaceView.getmRenderer().setBrightLevel(mSeekBar.getProgress());
                        break;
                    case 5:
                        mSeekBar.setMax(100);
                        mGLSurfaceView.getmRenderer().setAlphaLevel(mSeekBar.getProgress()/100.0f);
                        break;
                    case 7:
                        mSeekBar.setMax(100);
                        mGLSurfaceView.getmRenderer().setBetaLevel(mSeekBar.getProgress()+2.0f);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });
        return mSeekBar;
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
                    relativeLayout.removeView(mSeekBar);
                    mGLSurfaceView.getmRenderer().setType(0);
                    Toast.makeText(this,"原图像", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_black_and_white:
                if(mGLSurfaceView != null){
                    relativeLayout.removeView(mSeekBar);
                    mGLSurfaceView.getmRenderer().setType(1);
                    mSeekBar = addSeekBar(1);
                    relativeLayout.addView(mSeekBar,RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                    Toast.makeText(this,"黑白滤镜", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_sharpen:
                if(mGLSurfaceView != null){
                    relativeLayout.removeView(mSeekBar);
                    mGLSurfaceView.getmRenderer().setType(2);
                    Toast.makeText(this,"锐化", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_slur:
                if(mGLSurfaceView != null){
                    relativeLayout.removeView(mSeekBar);
                    mGLSurfaceView.getmRenderer().setType(3);
                    Toast.makeText(this,"模糊", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_africa:
                if(mGLSurfaceView != null){
                    relativeLayout.removeView(mSeekBar);
                    mGLSurfaceView.getmRenderer().setType(4);
                    Toast.makeText(this,"非洲滤镜", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_beauty:
                if(mGLSurfaceView != null){
                    relativeLayout.removeView(mSeekBar);
                    mGLSurfaceView.getmRenderer().setType(5);
                    mSeekBar = addSeekBar(5);
                    relativeLayout.addView(mSeekBar,RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                    Toast.makeText(this,"美颜", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_quarter:
                if(mGLSurfaceView != null){
                    relativeLayout.removeView(mSeekBar);
                    mGLSurfaceView.getmRenderer().setType(6);
                    Toast.makeText(this,"四分", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_towhite:
                if(mGLSurfaceView != null){
                    relativeLayout.removeView(mSeekBar);
                    mGLSurfaceView.getmRenderer().setType(7);
                    mSeekBar = addSeekBar(7);
                    relativeLayout.addView(mSeekBar,RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                    Toast.makeText(this,"美白", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
