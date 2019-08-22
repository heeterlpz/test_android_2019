package com.example.jason.video_player_1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    VideoView vv ;
    BarrageView bv;
    Button btn,btn2,btn3;
    EditText et;
    Configuration mConfiguration;
    MySqlite mysqliteOH;
    SeekBar sb,ssb,sssb;
    ProgressBar pb,pbb;
    int colornum;//弹幕颜色
    int sizenum;//弹幕字体大小
    int getintime;//当前视频播放进度，用于横竖屏切换缓存
    TextView tv;
    AudioManager am;
    int x = 0,y = 0;
    int thistime = 0;//当前视频播放进度，用于滑动跳转
    int thismusic = 0;//当前音量，用于滑动增减
    String path;//视频播放路径
    Handler hdr;
    Switch sw;
    int situation;//状态：遮挡、无遮

    @SuppressLint({"ClickableViewAccessibility", "HandlerLeak"})
//    原因：Handler在Android中用于消息的发送与异步处理，常常在Activity中作为一个匿名内部类来定义，此时Handler会隐式地持有一个外部类对象（通常是一个Activity）的引用。当Activity已经被用户关闭时，由于Handler持有Activity的引用造成Activity无法被GC回收，这样容易造成内存泄露。 
//    解决办法：将其定义成一个静态内部类（此时不会持有外部类对象的引用），在构造方法中传入Activity并对Activity对象增加一个弱引用，这样Activity被用户关闭之后，即便异步消息还未处理完毕，Activity也能够被GC回收，从而避免了内存泄露。
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.jason.video_player_1.R.layout.activity_main);
        vv = findViewById(com.example.jason.video_player_1.R.id.videoView);
        bv = findViewById(com.example.jason.video_player_1.R.id.danmu);
        btn  =findViewById(com.example.jason.video_player_1.R.id.button);
        btn2 = findViewById(com.example.jason.video_player_1.R.id.button2);
        btn3  =findViewById(R.id.button3);
        et = findViewById(com.example.jason.video_player_1.R.id.edit_text);
        sb = findViewById(R.id.seekBar);
        ssb = findViewById(R.id.seekBar2);
        sssb = findViewById(R.id.seekBar3);
        sw = findViewById(R.id.switch1);
        pb = findViewById(R.id.progressBar);
        pbb = findViewById(R.id.progressBar2);
        tv = findViewById(R.id.textView);
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        hdr = new Handler(new Handler.Callback(){//用于子线程修改ui线程中的视图
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 0x01://update progressbar
                        if(vv.isPlaying()){
                            int progressVideo = (int)(((float)vv.getCurrentPosition()/vv.getDuration())*pb.getMax());
                            pb.setProgress(progressVideo);
                            thismusic = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                            int progressMusic = (int)(((float)thismusic/am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))*pbb.getMax());
                            pbb.setProgress(progressMusic);
                        }
                        break;
                    case 0x02://cleandanmu检测弹幕是否完成动画并去除view。
                        bv.removeView((View)msg.obj);
                        break;
                }
                return false;
            }
        });
        mysqliteOH = new MySqlite(this,"danmu.db",null,1);
        mysqliteOH.getReadableDatabase().close();
        colornum = Color.rgb(0,0,0);
        sizenum = 17;
        getintime = 0;
        situation = 0;
        path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/testi.mp4";
        henshuping();//判断横竖屏设置
        if(savedInstanceState != null) {//状态切换回档
            getintime = savedInstanceState.getInt("reback");
            colornum = savedInstanceState.getInt("color");
            sizenum = savedInstanceState.getInt("size");
            path = savedInstanceState.getString("path");
            tv.setTextSize(sizenum);
            tv.setTextColor(colornum);
        }
        vv.setVideoPath(path);
        vv.seekTo(getintime);

        //视频文件选择
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vv.isPlaying()){
                    vv.pause();
                    bv.pause = true;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                //显示文件管理器列表
                try {
                  startActivityForResult(intent, 1000);
                } catch (android.content.ActivityNotFoundException ex) {

                  Toast.makeText(getApplicationContext(), "找不到文件管理器",Toast.LENGTH_SHORT).show();

                }
            }
        });

        //发送弹幕
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String danmuContent = et.getText().toString();
                int color = colornum;
                float size = sizenum;
                float height = (float)Math.random();
                if(!danmuContent.equals("")) {
                    SQLiteDatabase db = mysqliteOH.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("content",danmuContent);
                    values.put("color",color);
                    values.put("size",size);
                    values.put("height",height);
                    values.put("time",vv.getCurrentPosition());
                    values.put("level",sssb.getProgress());
                    values.put("video",path);
                    db.insert("danmu",null,values);
                }
                vv.start();
                bv.pause = false;
            }
        });

        //播放暂停
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vv.isPlaying()) {
                    vv.pause();
                    bv.pause = true;
                    btn.setText("播放");
                } else {
                    vv.start();
                    bv.pause = false;
                    btn.setText("暂停");
                }


            }
        });

        //当编辑弹幕时
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vv.pause();
                bv.pause = true;
            }
        });


        //弹幕字体颜色选择
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                colornum = Color.rgb(seekBar.getProgress()*25,255 - seekBar.getProgress()*25,250-Math.abs(50*seekBar.getProgress()-250));
                tv.setTextColor(colornum);
            }
        });

        //但木有字体大小选择
        ssb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sizenum = 10 + seekBar.getProgress();
                tv.setTextSize(sizenum);
            }
        });

        //弹幕优先度选择及当前显示优先度选择
        sssb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ViewGroup vg = (ViewGroup) bv;
                int num = vg.getChildCount();
                for(int i = 0 ;i < num ;i++){
                    if(vg.getChildAt(i).getMinimumHeight() < sssb.getProgress()){
                        vg.getChildAt(i).setVisibility(View.INVISIBLE);
                    } else {
                        vg.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //场景选择：遮挡，无遮
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if(isChecked) {
                    situation = 1;
                } else {
                    situation = 0;
                }
            }
        });

        //触屏控制音量，视频进度，弹幕视图高度
        vv.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View V, MotionEvent e) {
                    int xc = 0,yc =0;
                    float xe = 0,ye =0;
                    final int maxmusic = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    final float totallength = vv.getWidth() ;
                    final float totalheight = vv.getHeight();
                    final int totaltime = vv.getDuration();
                    //横屏
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x=(int)e.getX();
                            y=(int)e.getY();
                            thistime = vv.getCurrentPosition();
                            thismusic = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                            int ori = mConfiguration.orientation; //获取屏幕方向
                            if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
                                if (btn.getVisibility() != View.INVISIBLE) {
                                    btn.setVisibility(View.INVISIBLE);
                                    btn2.setVisibility(View.INVISIBLE);
                                } else {
                                    btn.setVisibility(View.VISIBLE);
                                    btn2.setVisibility(View.VISIBLE);
                                }
                                pb.setVisibility(View.VISIBLE);
                                pbb.setVisibility(View.VISIBLE);
                            }
                        case MotionEvent.ACTION_MOVE:
                            xc=(int)e.getX();
                            yc=(int)e.getY();
                            float xd = xc - x ;
                            float yd = y - yc ;
                            if(Math.abs(xd) > Math.abs(yd)){
                                float percent = ((float)xd/totallength);
                                int temptime = (int)(thistime+percent*totaltime);
                                int temppos = (int)(((float)temptime/totaltime)*pb.getMax());
                                if((temptime > 0) &&(temptime < totaltime)){
                                    pb.setProgress(temppos);
                                    vv.seekTo(temptime);
                                } else {
                                    pb.setProgress(0);
                                    vv.seekTo(0);
                                }
                                bv.cleanall();
                            } else {
                                if(x > totallength/2){
                                    float percent = ((float)yd/totalheight);
                                    int tempmusic = (int)(thismusic+percent*maxmusic);
                                    int temppos = (int)(((float)tempmusic/maxmusic)*pbb.getMax());
                                    if((tempmusic > 0)&&(tempmusic < maxmusic)) {
                                        pbb.setProgress(temppos);
                                        am.setStreamVolume(AudioManager.STREAM_MUSIC,tempmusic,AudioManager.FLAG_PLAY_SOUND);
                                    } else if(tempmusic > maxmusic){
                                        pbb.setProgress(pbb.getMax());
                                        am.setStreamVolume(AudioManager.STREAM_MUSIC,maxmusic,AudioManager.FLAG_PLAY_SOUND);
                                    }else {
                                        pbb.setProgress(0);
                                        am.setStreamVolume(AudioManager.STREAM_MUSIC,0,AudioManager.FLAG_PLAY_SOUND);
                                    }
                                } else {
                                    //获取设置的配置信息
                                    RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) bv.getLayoutParams();
                                    rl.bottomMargin = (int)(totalheight-yc);
                                    bv.setLayoutParams(rl);
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            xe=(int)e.getX();
                            ye=(int)e.getY();
                            if (mConfiguration.orientation == mConfiguration.ORIENTATION_LANDSCAPE) {
                                pb.setVisibility(View.GONE);
                                pbb.setVisibility(View.GONE);
                            }
                            break;
                    }
                    return true;
                }
        });

        //清理已完成的弹幕视图
        Thread threadCleanDanmu = new Thread(new Runnable() {
            @Override
            public void run() {
                ViewGroup vg = bv;
                int num = vg.getChildCount();
                for(int i = 0;i < num;i++){
                    View temptTextView = vg.getChildAt(i);
                    if(temptTextView.getTranslationX() < -temptTextView.getWidth()){
                        Message ms = Message.obtain();
                        ms.obj = temptTextView;
                        ms.what = 0x02;
                        hdr.sendMessage(ms);
                    }
                }
            }
        });

        //读取数据库并发送弹幕
         Thread threads = new Thread(new Runnable() {
            @Override
            public void run() {
                    if (vv.isPlaying()) {
                        SQLiteDatabase db = mysqliteOH.getWritableDatabase();
                        int timehere = vv.getCurrentPosition();
                        String[] tempstringary = {path};
                        //参数以此表示 表名 列名 where约束条件 占位符填充值 groudby having orderBy
                        Cursor cursor = db.query("danmu", null, "time >= " + (timehere-1000) + " and time <= " + (timehere + 500) + " and video = ?",tempstringary, null, null, null);
                        while (cursor.moveToNext()) {
                            final String content = cursor.getString(cursor.getColumnIndex("content"));
                            final int color = cursor.getInt(cursor.getColumnIndex("color"));
                            final float size = cursor.getFloat(cursor.getColumnIndex("size"));
                            final float height = cursor.getFloat(cursor.getColumnIndex("height"));
                            final int level = cursor.getInt(cursor.getColumnIndex("level"));
                            final int time = cursor.getInt(cursor.getColumnIndex("time"));
                            final String temppath = cursor.getString(cursor.getColumnIndex("video"));
                            //也可以使用sql语句,达到同样效果
                            //db.rawQuery("select * from person",null);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bv.generateItem(content,color,size,height,level,situation,(level >= sssb.getProgress()));
                                }
                            });
                        }
                    }
            }
        });

         //更新进度条位置
        Thread threadss = new Thread(new Runnable() {
            @Override
            public void run() {
                Message ms = Message.obtain();
                ms.what = 0x01;
                hdr.sendMessage(ms);
            }
        });

        //定义线程池
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(5);
        final ScheduledFuture<?> futures = ses.scheduleAtFixedRate(threads, 1, 1, TimeUnit.SECONDS);
        final ScheduledFuture<?> futuress = ses.scheduleAtFixedRate(threadss, 1, 1, TimeUnit.SECONDS);
        final ScheduledFuture<?> futuresss = ses.scheduleAtFixedRate(threadCleanDanmu, 1, 1, TimeUnit.SECONDS);
    }

    //视频文件选择结果处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String temppath = uri.getPath();
            System.out.println("path : "+temppath);
            File file = new File(temppath);
            // 文件存在并可读
            if (file.exists() && file.canRead()) {
                path = temppath;
                vv.setVideoPath(path);
                Toast.makeText(getApplicationContext(),"切换成功",Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getApplicationContext(),"文件读取错误",Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(),"文件读取失败",Toast.LENGTH_LONG).show();

        }
    }

    //状态转换数据缓存
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("reback", vv.getCurrentPosition());// 记录当前播放进度
        outState.putInt("color" , colornum);
        outState.putInt("size" , sizenum);
        outState.putString("path" , path);
    }

    //横竖屏转换调整
    public void henshuping() {
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) bv.getLayoutParams();//获取弹幕视图层的LayoutParams
        mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {//判断是否横屏
            getSupportActionBar().hide();//消除标题栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
            //隐藏控制按钮
            tv.setVisibility(View.GONE);
            sb.setVisibility(View.GONE);
            ssb.setVisibility(View.GONE);
            pb.setVisibility(View.GONE);
            sssb.setVisibility(View.GONE);
            pbb.setVisibility(View.GONE);
            btn3.setVisibility(View.GONE);
            //调整弹幕视图位置
            rl.bottomMargin = 90;
            bv.setLayoutParams(rl);
        } else {
            tv.setVisibility(View.VISIBLE);
            sb.setVisibility(View.VISIBLE);
            ssb.setVisibility(View.VISIBLE);
            sssb.setVisibility(View.VISIBLE);
            pb.setVisibility(View.VISIBLE);
            pbb.setVisibility(View.VISIBLE);
            btn3.setVisibility(View.VISIBLE);
            rl.bottomMargin = 0;
            bv.setLayoutParams(rl);
        }
    }
}
