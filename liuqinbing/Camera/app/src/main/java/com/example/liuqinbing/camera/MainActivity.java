package com.example.liuqinbing.camera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liuqinbing.camera.camera.OpenglCameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.path;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,SeekBar.OnSeekBarChangeListener {

    OpenglCameraView mCameraView;
    LinearLayout mCamsView;
    int mCamIndex = 0;                            //摄像头索引
    private Button btn_takePhoto;                 //拍照按钮
    private SeekBar smooth_skin_seekbar;          //磨皮程度选择器
    private TextView smooth_skin_intensity;       //磨皮程度显示
    private SeekBar white_seekbar;                //美白程度选择器
    private TextView white_intensity;             //美白程度显示
    private SeekBar ruddy_seekbar;                //红润程度选择器
    private TextView ruddy_intensity;             //红润程度显示
    private int textureType = 0;                  //滤镜类型索引

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mCamsView = (LinearLayout) findViewById(R.id.layout_cams);
        btn_takePhoto = (Button)findViewById(R.id.btn_takePhoto);
        smooth_skin_seekbar = (SeekBar) findViewById(R.id.smooth_skin_seekbar);
        smooth_skin_intensity = (TextView) findViewById(R.id.smooth_skin_intensity);
        white_seekbar = (SeekBar) findViewById(R.id.white_seekbar);
        white_intensity = (TextView) findViewById(R.id.white_intensity);
        ruddy_seekbar = (SeekBar) findViewById(R.id.ruddy_seekbar);
        ruddy_intensity = (TextView) findViewById(R.id.ruddy_intensity);

        //隐藏滚动条
        hideSeekBar();

        smooth_skin_seekbar.setOnSeekBarChangeListener(this);
        white_seekbar.setOnSeekBarChangeListener(this);
        ruddy_seekbar.setOnSeekBarChangeListener(this);
        btn_takePhoto.setOnClickListener(new BtnTakePhotoListener());
        mCameraView = new OpenglCameraView(MainActivity.this, mCamIndex, textureType);
        mCamsView.addView(mCameraView, LinearLayout.LayoutParams.MATCH_PARENT, 1800);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView = null;
        mCamsView.removeAllViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamsView.getChildCount() == 0) {
            mCameraView = new OpenglCameraView(MainActivity.this, mCamIndex, textureType);
            mCamsView.addView(mCameraView, LinearLayout.LayoutParams.MATCH_PARENT, 1800);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView = null;
        mCamsView.removeAllViews();
    }

    /**
     * 拍照按钮事件回调
     */
    public class BtnTakePhotoListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Log.e("TAG","拍照按钮事件回调");
            takePhoto();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float intensity;

        switch (seekBar.getId()) {
            case R.id.smooth_skin_seekbar:
                smooth_skin_intensity.setText("磨皮程度：" + progress + "%");
                intensity = (float) progress / 100;
                mCameraView.setSmoothIntensity(intensity);
                break;
            case R.id.white_seekbar:
                white_intensity.setText("美白程度：" + progress + "%");
                intensity = (float) progress / 100;
                mCameraView.setWhiteIntensity(intensity);
                break;
            case R.id.ruddy_seekbar:
                ruddy_intensity.setText("红润程度：" + progress + "%");
                intensity = (float) progress / 100;
                mCameraView.setRuddyIntensity(intensity);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_original) {
            // Handle the camera action
            mCameraView.changeTextureType(0);
            textureType = 0;
            //隐藏滚动条
            hideSeekBar();
        } else if (id == R.id.nav_mosaic) {
            mCameraView.changeTextureType(1);
            textureType = 1;
            //隐藏滚动条
            hideSeekBar();
        } else if (id == R.id.nav_div_ud) {
            mCameraView.changeTextureType(2);
            textureType = 2;
            //隐藏滚动条
            hideSeekBar();
        } else if (id == R.id.nav_split) {
            mCameraView.changeTextureType(3);
            textureType = 3;
            //隐藏滚动条
            hideSeekBar();
        } else if (id == R.id.nav_smooth) {
            mCameraView.changeTextureType(4);
            textureType = 4;
            //隐藏滚动条
            hideSeekBar();
        } else if (id == R.id.nav_ext_bw) {
            mCameraView.changeTextureType(5);
            textureType = 5;
            //隐藏滚动条
            hideSeekBar();
        } else if (id == R.id.nav_white) {
            mCameraView.changeTextureType(8);
            textureType = 6;
            //隐藏滚动条
            hideSeekBar();
        } else if (id == R.id.nav_smooth_skin) {
            mCameraView.changeTextureType(7);
            textureType = 7;
            //显示滚动条
            showSeekBar();
        } else if (id == R.id.nav_switch_camera) {
            mCameraView = null;
            mCamsView.removeAllViews();

            if(mCamIndex == 0) {
                mCamIndex = 1;
            }else {
                mCamIndex = 0;
            }
            mCameraView = new OpenglCameraView(MainActivity.this, mCamIndex, textureType);
            mCamsView.addView(mCameraView, LinearLayout.LayoutParams.MATCH_PARENT, 1800);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 隐藏滚动条
     */
    public void hideSeekBar() {
        smooth_skin_seekbar.setVisibility(View.GONE);
        smooth_skin_intensity.setVisibility(View.GONE);
        white_seekbar.setVisibility(View.GONE);
        white_intensity.setVisibility(View.GONE);
        ruddy_seekbar.setVisibility(View.GONE);
        ruddy_intensity.setVisibility(View.GONE);
    }

    /**
     * 显示滚动条
     */
    public void showSeekBar() {
        smooth_skin_seekbar.setVisibility(View.VISIBLE);
        smooth_skin_intensity.setVisibility(View.VISIBLE);
        white_seekbar.setVisibility(View.VISIBLE);
        white_intensity.setVisibility(View.VISIBLE);
        ruddy_seekbar.setVisibility(View.VISIBLE);
        ruddy_intensity.setVisibility(View.VISIBLE);
    }

    /**
     * 拍照功能
     */
    public void takePhoto() {
        Bitmap bitmap= mCameraView.getBitmap();
        savePhoto(bitmap);
    }

    /**
     * 保存照片
     * @param bmp
     */
    public void savePhoto(Bitmap bmp) {
        //生成路径
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }

        //文件名为时间
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String fileName = timeStamp + ".jpg";

        //获取文件
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    Toast.makeText(this,"拍照成功!", Toast.LENGTH_SHORT).show();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(this.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));

    }
}
