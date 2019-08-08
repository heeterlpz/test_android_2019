package com.example.cameravtwo.CameraV2GLSurfaceView;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cameravtwo.CameraV2;
import com.example.cameravtwo.R;

public class CameraV2GLSurfaceViewActivity extends Activity {
    private CameraV2GLSurfaceView mCameraV2GLSurfaceView;
    private CameraV2 mCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout linearLayout = findViewById(R.id.textureView);

        TextView textView=findViewById(R.id.InfoView);

        mCameraV2GLSurfaceView = new CameraV2GLSurfaceView(this);//实例化一个GLSurfaceView
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mCamera = new CameraV2(this);
        mCamera.setupCamera(dm.widthPixels, dm.heightPixels);
        if (!mCamera.openCamera()) {
            return;
        }
        mCameraV2GLSurfaceView.init(mCamera, false, CameraV2GLSurfaceViewActivity.this,textView);
//        setContentView(mCameraV2GLSurfaceView); //在屏幕上显示GLSurfaceView
        linearLayout.addView(mCameraV2GLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
