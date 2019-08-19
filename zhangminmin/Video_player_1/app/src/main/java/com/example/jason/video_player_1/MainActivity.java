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
import android.util.Log;
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

import com.example.jason.video_player_1.BarrageView;
import com.example.jason.video_player_1.MySqlite;

import java.io.File;
import java.util.Random;
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
    int colornum;
    int sizenum;
    int getintime;
    TextView tv;
    AudioManager am;
    int x = 0,y = 0;
    int thistime = 0;
    int thismusic = 0;
    String path;
    Handler hdr;
    Switch sw;
    int situation;




    @SuppressLint({"ClickableViewAccessibility", "HandlerLeak"})
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
        situation = 0;
        pb = findViewById(R.id.progressBar);
        pbb = findViewById(R.id.progressBar2);
        tv = findViewById(R.id.textView);
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        hdr = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 0x01:
                        if(vv.isPlaying()){
                            int progress = (int)(((float)vv.getCurrentPosition()/vv.getDuration())*pb.getMax());
                            pb.setProgress(progress);
                        }
                        break;
                }
            }
        };

        mysqliteOH = new MySqlite(this,"danmu.db",null,1);
        mysqliteOH.getReadableDatabase().close();
        colornum = Color.rgb(0,0,0);
        sizenum = 20;
        getintime = 0;

        path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/testi.mp4";
//        Toast.makeText(MainActivity.this, "path: " + path, Toast.LENGTH_SHORT).show();




        mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) bv.getLayoutParams();

        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
//                System.out.println("turndown");
            if(getSupportActionBar().isShowing()){
                getSupportActionBar().hide();
            }
            getSupportActionBar().hide();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
            tv.setVisibility(View.GONE);
            sb.setVisibility(View.GONE);
            ssb.setVisibility(View.GONE);
            pb.setVisibility(View.GONE);
            sssb.setVisibility(View.GONE);
            pbb.setVisibility(View.GONE);
            btn3.setVisibility(View.GONE);
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


        if(savedInstanceState != null) {
            getintime = savedInstanceState.getInt("reback");
            colornum = savedInstanceState.getInt("color");
            sizenum = savedInstanceState.getInt("size");
            path = savedInstanceState.getString("path");
            tv.setTextSize(sizenum);
            tv.setTextColor(colornum);
//            vv.start();
        }
        vv.setVideoPath(path);
        vv.seekTo(getintime);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vv.isPlaying()){
                    vv.pause();
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                //显示文件管理器列表
                try {
                  startActivityForResult(intent, 1000);
                } catch (android.content.ActivityNotFoundException ex) {

                  Toast.makeText(getApplicationContext(), "请安装文件管理器",Toast.LENGTH_SHORT).show();

                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int heightv = vv.getHeight();
//                bv.totalHeight = heightv;
                String danmuContent = et.getText().toString();
//                int color = Color.rgb(sb.getProgress()*25,255 - sb.getProgress()*25,250-Math.abs(50*sb.getProgress()-250));
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
//                    System.out.println("记录"+danmuContent);
                }
//                bv.generateItem(danmuContent,color,size,height);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(),"咋哇鲁多",Toast.LENGTH_SHORT).show();
                if(vv.isPlaying()) {

                    vv.pause();
                    bv.anipa();

                    btn.setText("播放");
                } else {
                    vv.start();
                    bv.anirun();
                    btn.setText("暂停");
                }


            }
        });

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                System.out.println("seekat: "+seekBar.getProgress());
                colornum = Color.rgb(seekBar.getProgress()*25,255 - seekBar.getProgress()*25,250-Math.abs(50*seekBar.getProgress()-250));
                tv.setTextColor(colornum);
            }
        });

        ssb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                System.out.println("seekat: "+seekBar.getProgress());
                sizenum = 17 + seekBar.getProgress();
                tv.setTextSize(sizenum);
            }
        });

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
//                    System.out.println(vg.getChildAt(i).getMinimumHeight()+" main "+sssb.getProgress());
                    if(vg.getChildAt(i).getMinimumHeight() < sssb.getProgress()){
                        vg.getChildAt(i).setVisibility(View.INVISIBLE);
                    } else {
                        vg.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

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

        vv.setOnTouchListener(new View.OnTouchListener(){

                @Override
                public boolean onTouch(View V, MotionEvent e) {
                    int xc = 0,yc =0;
                    float xe = 0,ye =0;
                    final int maxmusic = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    final float totallength = vv.getWidth() ;
                    final float totalheight = vv.getHeight();
                    final int totaltime = vv.getDuration();
                    int stepmusic = maxmusic/17;
                    //横屏
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x=(int)e.getX();
                            y=(int)e.getY();
                            thistime = vv.getCurrentPosition();
                            thismusic = am.getStreamVolume(AudioManager.STREAM_MUSIC);
//                            System.out.println("初始 ： x="+x+" y="+y);
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
//                                    pb.setProgress((temptime/totaltime)*pb.getMax());
                                    pb.setProgress(temppos);
                                    vv.seekTo(temptime);
                                } else {
                                    pb.setProgress(0);
                                    vv.seekTo(0);
                                }
//                                System.out.println("横向移动："+xd+" : "+totallength+" temptime : "+temptime+"maxpd: "+pb.getMax()+"temppos : "+temppos);

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
//                                    System.out.println("纵向移动："+yd+" : "+totalheight+" tempmusic: "+tempmusic+"maxpd: "+pbb.getMax()+"temppos : "+temppos);
                                } else {
//                                    System.out.println("yc : "+yc);
                                    //获取设置的配置信息
                                    RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) bv.getLayoutParams();
                                    rl.bottomMargin = (int)(totalheight-yc);
                                    bv.setLayoutParams(rl);
                                }


                            }
//                            System.out.println("xc="+xc+" yc="+yc);
                            break;
                        case MotionEvent.ACTION_UP:
                            xe=(int)e.getX();
                            ye=(int)e.getY();
//                            System.out.println("xe="+xe+" ye="+ye);
                            if (mConfiguration.orientation == mConfiguration.ORIENTATION_LANDSCAPE) {
                                pb.setVisibility(View.GONE);
                                pbb.setVisibility(View.GONE);
                            }
                            break;

                        //System.out.println("x="+x+" y="+y);
                    }
                    // TODO 自动生成的方法存根
                    return true;
                }

        });
//
//        Thread tr = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if(situation == 1){
//                    Message ms = Message.obtain();
//                    ms.what = 0x02;
//                    hdr.sendMessage(ms);
//                }
//            }
//        });

         Thread threads = new Thread(new Runnable() {
            @Override
            public void run() {
                    if (vv.isPlaying()) {
//                        System.out.println("一秒内");
                        SQLiteDatabase db = mysqliteOH.getWritableDatabase();
                        //参数以此表示 表名 列名 where约束条件 占位符填充值 groudby having orderBy
                        int timehere = vv.getCurrentPosition();
                        String[] tempstringary = {path};
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
                            System.out.println(content + "--" + height + "--" + time + " : " + timehere + " level:"+level+":"+sssb.getProgress() + " vido : "+path+":"+temppath+" ?"+level+" "+situation);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if(level >= sssb.getProgress())
                                    bv.generateItem(content,color,size,height,level,situation);
//                                    else System.out.println("out");
                                }
                            });
                        }

                    }
            }
        });

        Thread threadss = new Thread(new Runnable() {
            @Override
            public void run() {
                Message ms = Message.obtain();
                ms.what = 0x01;
                hdr.sendMessage(ms);
            }
        });



        ScheduledExecutorService ses = Executors.newScheduledThreadPool(5);
//        final ScheduledFuture<?> future = ses.scheduleAtFixedRate(thread, 1, 1, TimeUnit.SECONDS);
        final ScheduledFuture<?> futures = ses.scheduleAtFixedRate(threads, 1, 1, TimeUnit.SECONDS);
        final ScheduledFuture<?> futuress = ses.scheduleAtFixedRate(threadss, 1, 1, TimeUnit.SECONDS);
//        final ScheduledFuture<?> futuresss = ses.scheduleAtFixedRate(tr, 1, 1, TimeUnit.SECONDS);



    }

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        // 记录当前播放进度
        outState.putInt("reback", vv.getCurrentPosition());
        outState.putInt("color" , colornum);
        outState.putInt("size" , sizenum);
        outState.putString("path" , path);

    }

}
