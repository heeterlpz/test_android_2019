package com.example.beautycamera;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private CameraglSurfaceview mCameraglSurfaceview;
    private Camera mCamera;
    private SeekBar mSeekBar;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        relativeLayout = findViewById(R.id.camera);
        LinearLayout linearLayout = findViewById(R.id.textureView);
        TextView textView = findViewById(R.id.InfoView);
        mCameraglSurfaceview = new CameraglSurfaceview(this);//实例化一个GLSurfaceView
        mCamera = new Camera(this);
        mCamera.setupCamera(1280, 720);
        if (!mCamera.openCamera()) {
            return;
        }
        mCameraglSurfaceview.init(mCamera, false, MainActivity.this,textView,0);
        //setContentView(mCameraV2GLSurfaceView); //在屏幕上显示GLSurfaceView
        linearLayout.addView(mCameraglSurfaceview);
    }

    /***
     * 选择片段着色器类型
     * @param shaderType
     * @return
     */
    public SeekBar addSeekBar(int shaderType){
        final SeekBar mSeekBar = new SeekBar(this);
        final int mshaderType = shaderType;
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (mshaderType) {
                    case 1:
                        mSeekBar.setMax(10);
                        mCameraglSurfaceview.getmCameraRenderer().setBetaLevel(mSeekBar.getProgress() + 2.0f);
                        break;
                    case 2:
                        mSeekBar.setMax(100);
                        mCameraglSurfaceview.getmCameraRenderer().setAlphaLevel(mSeekBar.getProgress()/100.0f);
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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_ext:
                if (mCameraglSurfaceview != null) {
                    relativeLayout.removeView(mSeekBar);
                    mCameraglSurfaceview.getmCameraRenderer().setType(0);
                    Toast.makeText(this, "原像图", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_bw:
                if (mCameraglSurfaceview != null) {
                    mCameraglSurfaceview.getmCameraRenderer().setType(1);
                    Toast.makeText(this, "黑白滤镜", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_relief:
                if (mCameraglSurfaceview != null) {
                    mCameraglSurfaceview.getmCameraRenderer().setType(2);
                    Toast.makeText(this, "浮雕", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_split:
                if (mCameraglSurfaceview != null) {
                    mCameraglSurfaceview.getmCameraRenderer().setType(3);
                    Toast.makeText(this, "分屏", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_mosaic:
                if (mCameraglSurfaceview != null) {
                    mCameraglSurfaceview.getmCameraRenderer().setType(4);
                    Toast.makeText(this, "马赛克", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_sw:
                if (mCameraglSurfaceview != null) {
                    relativeLayout.removeView(mSeekBar);
                    mCameraglSurfaceview.getmCameraRenderer().setType(5);
                    mSeekBar = addSeekBar(1);
                    relativeLayout.addView(mSeekBar, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    Toast.makeText(this, "美白", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_sg:
                if (mCameraglSurfaceview != null) {
                    relativeLayout.removeView(mSeekBar);
                    mCameraglSurfaceview.getmCameraRenderer().setType(6);
                    mSeekBar = addSeekBar(2);
                    relativeLayout.addView(mSeekBar, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    Toast.makeText(this, "磨皮", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
