package com.starnet.heeter.openglcamerasample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.starnet.heeter.openglcamerasample.camera.AndroidCamera;
import com.starnet.heeter.openglcamerasample.camera.OpenglCameraView;

public class MainActivity extends AppCompatActivity {
    LinearLayout mBtnView;
    LinearLayout mCamsView;
    OpenglCameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnView = (LinearLayout) findViewById(R.id.layout_btns);
        mCamsView = (LinearLayout) findViewById(R.id.layout_cams);

        Button btn_cam = new Button(this);
        btn_cam.setText("cam");
        btn_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCamsView.getChildCount() > 0) {
                    mCameraView = null;
                    mCamsView.removeAllViews();
                } else {
                    mCameraView = new OpenglCameraView(MainActivity.this, 0);
                    mCamsView.addView(mCameraView, LinearLayout.LayoutParams.MATCH_PARENT, 1600);
                }
            }
        });

        Button btn_type = new Button(this);
        btn_type.setText("type");
        btn_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraView == null) {
                    return;
                }
                mCameraView.changeTextureType();
            }
        });

        mBtnView.addView(btn_cam);
        mBtnView.addView(btn_type);
    }
}
