package com.example.nyamori.mytestapplication;

import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nyamori.gles.Texture2dProgram;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener;
    private TextView withOpenGL;
    private Surface mOutSurface;
    private Handler mUIHandler;
    private MyCamera myCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView navigationView =(NavigationView)findViewById(R.id.nav_view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.CAMERA"}, 1);
        }else {
            initDrawer();

            withOpenGL=(TextView)findViewById(R.id.with_OpenGL);
            initSurfaceView();
        }
    }

    private void initDrawer() {
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView navigationView =(NavigationView)findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_ext);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if(myCamera==null){
                            Toast.makeText(MainActivity.this,"尚未初始化",Toast.LENGTH_SHORT).show();
                        }else {
                            switch (item.getItemId()){
                                case R.id.nav_ext:
                                    myCamera.changeCameraType(Texture2dProgram.ProgramType.TEXTURE_EXT);
                                    break;
                                case R.id.nav_ext_hd:
                                    myCamera.changeCameraType(Texture2dProgram.ProgramType.TEXTURE_EXT_HP);
                                    break;
                                case R.id.nav_ext_bw:
                                    myCamera.changeCameraType(Texture2dProgram.ProgramType.TEXTURE_EXT_BW);
                                    break;
                                case R.id.nav_mosaic:
                                    myCamera.changeCameraType(Texture2dProgram.ProgramType.TEXTURE_MOSAIC);
                                    break;
                                case R.id.nav_smooth:
                                    myCamera.changeCameraType(Texture2dProgram.ProgramType.TEXTURE_SMOOTH);
                                    break;
                                default:
                                    break;
                            }
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            for(int i=0;i<permissions.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    withOpenGL=(TextView)findViewById(R.id.with_OpenGL);
                    initSurfaceView();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(myCamera!=null){
            myCamera.destroyCamera();
        }
        super.onDestroy();
    }

    private void initSurfaceView() {
        mUIHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MsgConfig.UIMsg.UI_UPDATE_FPS:
                        withOpenGL.setText("withOpenGL-FPS:"+msg.arg1);
                        break;
                }
            }
        };
        mTextureView = (TextureView) findViewById(R.id.m_texture_view);
        mSurfaceTextureListener=new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.v(TAG, "onSurfaceTextureAvailable: size="+width+"x"+height);
                mOutSurface=new Surface(surface);
                myCamera=new MyCamera(mUIHandler,mOutSurface,getApplicationContext());
                myCamera.initCamera(width,height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };
    }

}

