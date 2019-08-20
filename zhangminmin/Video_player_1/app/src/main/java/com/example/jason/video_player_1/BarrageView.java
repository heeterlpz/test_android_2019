package com.example.jason.video_player_1;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BarrageView extends RelativeLayout {

    public ObjectAnimator objAnim;
    private Context mContext;
    private Random random = new Random(System.currentTimeMillis());   //获取自1970年1月1日0时起到现在的毫秒数

    private static final long BARRAGE_GAP_MIN_DURATION = 1000;//两个弹幕的最小间隔时间
    private static final long BARRAGE_GAP_MAX_DURATION = 2000;//两个弹幕的最大间隔时间
    private int maxSpeed = 5000;   // 最小速度，ms，越大越慢
    private int minSpeed = 3000;    //  最快速度，ms，越大越慢
    private int maxSize = 27;       //最大字体文字，dp
    private int minSize = 17;       //最小文字大小，dp
    private int totalHeight = 0;    //总高度
    private List<String> itemText = new ArrayList<>();  //内容list
    private Handler hdr;
    private int situation;
    private View tempTextView;

    //    三重初始化
    public BarrageView(Context context) {
        this(context, null);
    }

    public BarrageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarrageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    //弹幕生成方法
    public void generateItem(String content) {
        this.generateItem(content,Color.rgb(255,255,255),21,random.nextInt(1) * this.getHeight(),-1,0,true);
    }

    @SuppressLint("HandlerLeak")
    public void generateItem(String content, int color, float size, float height, int level, int situations,boolean visiable) {
        situation = situations;
        final BarrageItem item = new BarrageItem();
        item.text = content;
        item.textView = new TextView(mContext);//创建textView 控件做单个弹幕条
        item.level = level;
        //设置文本属性
        item.textView.setText(content);
        item.textView.setTextSize(size);
        item.textView.setTextColor(color);
        item.textView.setMinimumHeight(item.level);
        if(!visiable){
            item.textView.setVisibility(INVISIBLE);//根据当前弹幕优先度
        }
        //获取滚动TextView的宽度
        item.textMeasuredWidth = (int) getTextWidth(item, content, size);//用paint内置的方法将一个框框住文本，用框内置函数计算起其长度
        item.textMeasuredHeight = (int) getTextHeight(item, content, size);//用paint内置的方法将一个框框住文本，用框内置函数计算起其长度
        //设置随机移动速度
        item.moveSpeed = (int) (minSpeed + (maxSpeed - minSpeed) * Math.random());
        totalHeight = (int)((height)*(this.getHeight()-item.textMeasuredHeight));//获取实际高度
        item.verticalPos = totalHeight;
        hdr = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x02:
                        BarrageItem item = (BarrageItem)msg.obj;
                        System.out.println("2");
                        showBarrageItem(item);
                        break;
                    case 0x03:
                        BarrageItem items = (BarrageItem)msg.obj;
                        showBarrageItem(items);
                        break;
                    case 0x01:
                        break;
                }
            }
        };
        //判断场景是否允许遮挡
        Thread temptr = new Thread(new Runnable() {
            @Override
            public void run() {
                if(situation == 1) {
                    while(true){
                        try{
                            Thread.currentThread().sleep((long)(Math.random()*1000));
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        if(testDanmuBef(item))
                            break;
                    }
                    System.out.println("快捷进图1:"+situation);
                    item.moveSpeed = maxSpeed;
                    Message ms = Message.obtain();
                    ms.obj = item;
                    ms.what = 0x03;
                    hdr.sendMessage(ms);
                }else {
                    System.out.println("进图2:"+situation);
                    Message ms = Message.obtain();
                    ms.what = 0x02;
                    ms.obj = item;
                    hdr.sendMessage(ms);
                }
            }
        });
        temptr.start();
    }

    //在弹幕添加进动画之前判断他会不会和之前的弹幕view碰撞，false表示会碰撞
    public boolean testDanmuBef(BarrageItem item) {
        ViewGroup vg = this;
        int num = vg.getChildCount();
        int selfHeight = item.textMeasuredHeight;
        int selfMargin = item.verticalPos;
        for(int i = 0;i < num;i++) {
            View tempTextView = vg.getChildAt(i);
            int tempHeight = tempTextView.getHeight();
            int tempWidth = tempTextView.getWidth();
            int tempLeft = (int)(tempTextView.getTranslationX()+1);
            LayoutParams tempparams = (LayoutParams) tempTextView.getLayoutParams();
            int tempMargin = tempparams.topMargin;
            if(((selfMargin>tempMargin)&&(selfMargin<(tempHeight+tempMargin))||((selfMargin<tempMargin)&&((selfHeight+selfMargin)>tempMargin)))&&(tempLeft>(vg.getWidth()-tempWidth))){
                System.out.println(tempWidth+" : "+tempHeight+" : "+tempMargin+" | "+tempLeft+"  "+selfHeight+" | "+selfMargin);
                return false;
            }
        }
        return true;
    }

    /**
     * 显示TextView 的动画效果
     * @param item
     */
     synchronized private void showBarrageItem(final BarrageItem item) {
         System.out.println("pp "+item.text);
         //屏幕宽度 像素
        int leftMargin = this.getRight() - this.getLeft() - this.getPaddingLeft();//getrigth - getleft 就是width
        //显示的TextView 的位置，
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.topMargin = item.verticalPos;
        params.addRule(RelativeLayout.OVER_SCROLL_IF_CONTENT_SCROLLS);
        this.addView(item.textView, params);
        transAnimRun(item, leftMargin);
    }

    synchronized private void transAnimRun(final BarrageItem item, int leftMargin) {
        objAnim =ObjectAnimator
                //滑动位置是x方向滑动，从屏幕宽度+View的长度到左边0-View的长度
                .ofFloat(item.textView,"translationX" , leftMargin+5, -item.textMeasuredWidth-5)//前者控制出现（）右边，后者控制消失（）左边
                .setDuration(item.moveSpeed);
        //设置移动的过程速度，开始快之后满  //Interpolator是一个插值器
        objAnim.setInterpolator(new LinearInterpolator());
        //开始动画
        objAnim.start();
        //动画监听
        objAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }
            @Override
            public void onAnimationEnd(Animator animation) {
                //动画执行完毕，清除动画，删除view，
                item.textView.clearAnimation();
                BarrageView.this.removeView(item.textView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    //清理已完成动画的弹幕视图,其实上面已经有onend清理，之前忘了，但可以添加防止动画卡死
    public void cleanall(){
         ViewGroup vg = this;
         int num = vg.getChildCount();
         for(int i = 0;i < num;i++){
             vg.removeView(vg.getChildAt(i));

         }
    }

    /**
     * 计算TextView中字符串的长度
     *
     * @param text 要计算的字符串
     * @param Size 字体大小
     * @return TextView中字符串的长度
     */
    public float getTextWidth(BarrageItem item, String text, float Size) {
        //Rect表示一个矩形，由四条边的坐标组成
        Rect bounds = new Rect();
        TextPaint paint;
        paint = item.textView.getPaint();
        paint.getTextBounds(text, 0, text.length(), bounds);
        //System.out.println(item.textView.getText()+(bounds.width()+"")+"宽度");
        return bounds.width();
    }

    /**
     * 计算TextView中字符串的宽度
     *
     * @param text 要计算的字符串
     * @param Size 字体大小
     * @return TextView中字符串的长度
     */
    public float getTextHeight(BarrageItem item, String text, float Size) {
        //Rect表示一个矩形，由四条边的坐标组成
        Rect bounds = new Rect();
        TextPaint paint;
        paint = item.textView.getPaint();
        paint.getTextBounds(text, 0, text.length(), bounds);
        //System.out.println(item.textView.getText()+(bounds.width()+"")+"宽度");
        return bounds.height();
    }
}
