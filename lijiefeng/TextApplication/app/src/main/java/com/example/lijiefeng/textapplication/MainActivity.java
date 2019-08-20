package com.example.lijiefeng.textapplication;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioGroup radioGroup;
    private RadioGroup radioGroup1;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private TranslateAnimation translateAnimation;
    private EditText editText;
    private Button sendbutton;
    private Button playbutton;
    private RelativeLayout root;
    private VideoView videoView;
    private Timer timer;
    double value = 1.0;

    StringBuilder mFormatBuilder = new StringBuilder();
    Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        sendbutton.setOnClickListener(this);
        playbutton.setOnClickListener(this);
        run();
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId){
                    case R.id.RB1:
                        value = 0.25;
                        break;
                    case R.id.RB2:
                        value = 0.5;
                        break;
                    case R.id.RB3:
                        value = 0.75;
                        break;
                    case R.id.RB4:
                        value = 1.0;
                        break;
                }
            }
        });
    }

    //控件初始化
    public void init() {
        videoView = (VideoView) findViewById(R.id.videoView);
        root = (RelativeLayout) findViewById(R.id.root);
        editText = (EditText) findViewById(R.id.editText);
        playbutton = (Button) findViewById(R.id.playbutton);
        sendbutton = (Button) findViewById(R.id.sendbutton);
        radioGroup1 = (RadioGroup) findViewById(R.id.RG0);
        radioGroup = (RadioGroup) findViewById(R.id.rg);
        radioButton1 = (RadioButton) findViewById(R.id.rb1);
        radioButton2 = (RadioButton) findViewById(R.id.rb2);

    }

    //点击按钮触发事件
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendbutton:
                String inputtext = editText.getText().toString();//获取EditView的内容
                if (!inputtext.equals("")) {
                    sendBarrage();
                    editText.setText("");
                }
                videoView.setFocusable(true);
                videoView.requestFocus();
                break;
            case R.id.playbutton:
                if(radioGroup.getCheckedRadioButtonId() == R.id.rb1){
                    localVideo(radioButton1.getText().toString());
                }else
                    localVideo(radioButton2.getText().toString());
                break;
        }
    }

    //播放本地视频
    private void localVideo(String filename) {
        videoView.setMediaController(new MediaController(this));
        String uri;
        if(filename.equals("解忧大队")){
            uri = ("android.resource://" + getPackageName() + "/" + R.raw.video0);
        }else{
            uri = ("android.resource://" + getPackageName() + "/" + R.raw.video1);
        }
        videoView.setVideoURI(Uri.parse(uri));
        videoView.start();
    }

    //添加.发送弹幕
    private void sendBarrage() {
        //使用TextView作为弹幕的载体
        TextView tv = new TextView(this);
        tv.setTextSize(20);
        tv.setText(editText.getText().toString());
        tv.setTextColor(getResources().getColor(R.color.red));
        String playTime = stringForTime(videoView.getCurrentPosition()-1000);
        saveData(playTime, tv.getText().toString());
        root.addView(tv);
        designBarrage(tv, root);
    }

    //发送库存弹幕
    private void sendstoragebarrage(String barrages) {
        TextView tv0 = new TextView(this);
        tv0.setTextSize(20);
        tv0.setText(barrages);
        tv0.setTextColor(getResources().getColor(R.color.blue));
        root.addView(tv0);
        designBarrage(tv0, root);
    }

    //存放数据
    private void saveData(String time, String barragecontent) {

        SharedPreferences barrage = this.getSharedPreferences("barrage", MODE_APPEND);
        SharedPreferences.Editor editor = barrage.edit();
        Map<String, String> barragemap = (Map<String, String>) barrage.getAll();
        Set<Map.Entry<String, String>> entryset = barragemap.entrySet();
        for (Map.Entry<String, String> entry : entryset) {
            if (entry.getKey().equals(time)) {
                barragecontent = entry.getValue() + "," + barragecontent;
            }
        }
        editor.putString(time, barragecontent);
        editor.apply();
    }

    //获取键值,发送库存弹幕
    private void kv(String time) {
        SharedPreferences barrage = this.getSharedPreferences("barrage", MODE_APPEND);
        Map<String, String> barragemap = (Map<String, String>) barrage.getAll();
        Set<Map.Entry<String, String>> entryset = barragemap.entrySet();
        for (Map.Entry<String, String> entry : entryset) {
            if (entry.getKey().equals(time)) {
                for(String barrage0 : entry.getValue().split(",")){
                    sendstoragebarrage(barrage0);
                }
            }
        }
    }

    //补间动画实现弹幕滚动效果
    private void designBarrage(TextView textView, RelativeLayout relativeLayout) {
        textView.measure(0, 0);
        int measuredWidth = textView.getMeasuredWidth();  //获取TextView的宽度
        int measuredHeight = textView.getMeasuredHeight();//获取TextView的高度
        int layoutHeight = relativeLayout.getBottom() - relativeLayout.getTop();  //获取布局的宽度
        int y = (int) (Math.random() * layoutHeight * value );  //设置弹幕随机产生的Y坐标
        if (y > layoutHeight * value  - measuredHeight) {  // 出现在布局底部时坐标要扣除TextView的高度
            y -= measuredHeight;
        }

        int fromx = relativeLayout.getRight() - relativeLayout.getLeft();
        int tox = 0 - measuredWidth;
        int fromy = y;
        int toy = y;
        translateAnimation = new TranslateAnimation(fromx, tox, fromy, toy); //动画的位置显示
        translateAnimation.setDuration(3000);  //动画持续的时间 单位ms
        translateAnimation.setFillAfter(true);
        translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator()); //在动画开始与结束的地方速率改变比较慢，在中间的时候加速
        textView.setAnimation(translateAnimation); //为TextView设置动画效果
        translateAnimation.start();
    }

    //获取播放时间,将毫秒的时间转换为时分秒时间
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    //使用计时器+handle产生弹幕
    public void run() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message message = handler.obtainMessage();
                handler.sendMessage(message);
                handler.removeMessages(1);
            }
        };
        timer.schedule(task, videoView.getCurrentPosition(), 1000);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            kv(stringForTime(videoView.getCurrentPosition()));
            super.handleMessage(msg);
        }
    };
}