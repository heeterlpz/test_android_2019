package com.example.nyamori.mytestapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nyamori.mytestapplication.filters.BaseFilter;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener;
    private TextView withOpenGL;
    private ArrayAdapter<String> filterListAdapter;
    private List<String> filters;
    private Handler mUIHandler;
    private MyCamera myCamera;
    private MyCamera2 myCamera2;
    private MyOpenGL myOpenGL;
    private int surfaceWidth;
    private int surfaceHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.CAMERA"}, 1);
        }else {
            filters=new ArrayList<>();
            initDrawer();
            initSurfaceView();
            ShaderLoader.getInstance(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            for(int i=0;i<permissions.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    filters=new ArrayList<>();
                    initDrawer();
                    initSurfaceView();
                    ShaderLoader.getInstance(this);
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
        if(myCamera2 !=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                myCamera2.destroyCamera();
            }
        }
        if(myCamera!=null){
            myCamera.destoryCamera();
        }
        if(myOpenGL!=null){
            myOpenGL.destroyOpenGL();
        }
        mUIHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void initDrawer() {
        withOpenGL= findViewById(R.id.with_OpenGL);
        mDrawerLayout= findViewById(R.id.drawer_layout);
        findViewById(R.id.change_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if(myCamera2 !=null){
                        myOpenGL.changeCamera(
                                myCamera2.changeCamera(surfaceWidth,surfaceHeight));

                    }
                }else {
                    if(myCamera != null){
                        myOpenGL.changeCamera(
                                myCamera.changeCamera(surfaceWidth,surfaceHeight));
                    }
                }
            }
        });
        initEndDrawerList();
        initStartDrawer();
    }

    private void initStartDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_ext);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if(myOpenGL==null){
                            Toast.makeText(MainActivity.this,"尚未初始化",Toast.LENGTH_SHORT).show();
                        }else {
                            switch (item.getItemId()){
                                case R.id.nav_ext:
                                    myOpenGL.addFilter(Config.MsgType.NO_TYPE);
                                    break;
                                case R.id.nav_beauty:
                                    myOpenGL.changeFilterType(Config.MsgType.BEAUTY_TYPE);
                                    break;
                                case R.id.nav_whitening:
                                    myOpenGL.changeFilterType(Config.MsgType.WHITENING_TYPE);
                                    break;
                                case R.id.nav_bw:
                                    myOpenGL.addFilter(Config.MsgType.BW_TYPE);
                                    break;
                                case R.id.nav_mosaic:
                                    myOpenGL.addFilter(Config.MsgType.MOSAIC_TYPE);
                                    break;
                                case R.id.nav_smooth:
                                    myOpenGL.addFilter(Config.MsgType.SMOOTH_TYPE);
                                    break;
                                case R.id.nav_obscure:
                                    myOpenGL.addFilter(Config.MsgType.OBSCURE_TYPE);
                                    break;
                                case R.id.nav_sharpening:
                                    myOpenGL.addFilter(Config.MsgType.SHARPENING_TYPE);
                                    break;
                                case R.id.nav_edge:
                                    myOpenGL.addFilter(Config.MsgType.EDGE_TYPE);
                                    break;
                                case R.id.nav_emboss:
                                    myOpenGL.addFilter(Config.MsgType.EMBOSS_TYPE);
                                    break;
                                case R.id.nav_add_whitening:
                                    myOpenGL.addFilter(Config.MsgType.WHITENING_TYPE);
                                    break;
                                case R.id.nav_add_beauty:
                                    myOpenGL.addFilter(Config.MsgType.BEAUTY_TYPE);
                                    break;
                                case R.id.nav_test:
                                    myOpenGL.changeFilterType(Config.MsgType.TEST_TYPE);
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

    private void initEndDrawerList() {
        ListView filterList = findViewById(R.id.filter_list);
        filters.add("没有添加");
        filterListAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,filters);
        filterList.setAdapter(filterListAdapter);
        filterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filterName=filters.get(position);
                if(filterName.equals(Config.FilterName.BEAUTY_TYPE)){
                    settingDialogs("磨皮强度",myOpenGL.getFilter(position));
                }else if(filterName.equals(Config.FilterName.SHARPENING_TYPE)){
                    settingDialogs("锐化强度",myOpenGL.getFilter(position));
                }else if(filterName.equals(Config.FilterName.WHITENING_TYPE)){
                    settingDialogs("美白强度",myOpenGL.getFilter(position));
                }else {
                    Toast.makeText(MainActivity.this,"暂时不支持该滤镜的设置",Toast.LENGTH_SHORT).show();
                }
                mDrawerLayout.closeDrawers();
            }
        });
        filterList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String delete="删除了"+filters.get(position)+"滤镜";
                Toast.makeText(MainActivity.this,delete,Toast.LENGTH_SHORT).show();
                myOpenGL.deleteFilter(position);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    public void settingDialogs(String title, final BaseFilter filter){
        final AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_level_setting,null);
        TextView maxProgressText=dialogView.findViewById(R.id.max_progress);
        final TextView nowProgressText=dialogView.findViewById(R.id.now_progress);
        final SeekBar levelBar=dialogView.findViewById(R.id.level_bar);
        maxProgressText.setText(String.valueOf(filter.getLevelMax()));
        nowProgressText.setText(String.valueOf(filter.getLevel()));
        levelBar.setProgress(filter.getLevel());
        levelBar.setMax(filter.getLevelMax());
        levelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nowProgressText.setText(String.valueOf(progress));
                filter.setLevel(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        customizeDialog.setTitle(title);
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        customizeDialog.show();
    }

    @SuppressLint("HandlerLeak")
    private void initSurfaceView() {
        mUIHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Config.UIMsg.UI_UPDATE_FPS:
                        String fps="withOpenGL-FPS:"+msg.arg1;
                        withOpenGL.setText(fps);
                        break;
                    case Config.UIMsg.UI_UPDATE_LIST:
                        filters.clear();
                        filters.addAll(0,myOpenGL.getFilterList());
                        filterListAdapter.notifyDataSetChanged();
                        break;
                    case Config.UIMsg.GL_SURFACE_PREPARE:
                        SurfaceTexture surfaceTexture=(SurfaceTexture)msg.obj;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            myCamera2.setTargetSurface(new Surface(surfaceTexture));
                            myCamera2.openCamera();
                        }else {
                            myCamera.setTargetSurface(surfaceTexture);
                            myCamera.openCamera();
                        }
                }
            }
        };
        mTextureView = findViewById(R.id.m_texture_view);
        mSurfaceTextureListener=new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                surfaceWidth=width;
                surfaceHeight=height;
                Log.v(TAG, "onSurfaceTextureAvailable: size="+width+"x"+height);
                myOpenGL=new MyOpenGL(mUIHandler,new Surface(surface));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    myCamera2 =new MyCamera2(getApplicationContext());
                    myOpenGL.init(myCamera2.initCamera(width,height));
                }else {
                    myCamera=new MyCamera();
                    myOpenGL.init(myCamera.initCamera(width,height));
                }
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

