package com.example.jason.video_player_1;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jason.video_player_1.BarrageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class BarrageView extends RelativeLayout {

    private OnClickActionListener mClick = null;
    // 为接口设置监听器
    public void setOnClickActionListener(OnClickActionListener down) {

//        Toast.makeText(getContext(),"bar click",Toast.LENGTH_SHORT).show();
        mClick = down;
    }
    //定义接口
    public interface OnClickActionListener {
        void onClick(String str);
    }


    public ObjectAnimator objAnim;
    private Context mContext;
    private BarrageHandler mHandler = new BarrageHandler();
    private Random random = new Random(System.currentTimeMillis());   //获取自1970年1月1日0时起到现在的毫秒数

    private static final long BARRAGE_GAP_MIN_DURATION = 1000;//两个弹幕的最小间隔时间
    private static final long BARRAGE_GAP_MAX_DURATION = 2000;//两个弹幕的最大间隔时间
    private int maxSpeed = 5000;   // 最小速度，ms，越大越慢
    private int minSpeed = 3000;    //  最快速度，ms，越大越慢
    private int maxSize = 27;       //最大字体文字，dp
    private int minSize = 17;       //最小文字大小，dp

    public int totalHeight = 0;    //总高度
    private int lineHeight = 0;     //每一行弹幕的高度
    private int totalLine = 0;      //弹幕的行数
    private List<String> itemText = new ArrayList<>();  //内容list
    private int textCount;   //条目的数量
    private  long anitime;
    private Handler hdr;
    private int situation;


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
        initData();
        init();
    }

    private void init() {
        //初始化条目
        textCount = itemText.size();
    }

    public void generateItem(String content) {
        this.generateItem(content,Color.rgb(255,255,255),21,random.nextInt(1) * this.getHeight(),-1,0);
    }

    @SuppressLint("HandlerLeak")
    public void generateItem(String content, int color, float size, float height, int level, int situations) {
        situation = situations;
        final BarrageItem item = new BarrageItem();
        item.text = content;
        //String tx = itemText[(int) (Math.random() * textCount)];
        //随机获取条目

        //范围随机获取大小
//        int sz = (int) (minSize + (maxSize - minSize) * Math.random());

        //创建textView 控件
        item.textView = new TextView(mContext);
        item.level = level;

        //设置文本属性
        item.textView.setText(content);
        item.textView.setTextSize(size);
        item.textView.setTextColor(color);
        item.textView.setMinimumHeight(item.level);
        //item.textView.setBackgroundColor(R.color.black);

//        item.textView.setTextColor(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));

        //获取滚动TextView的宽度
        item.textMeasuredWidth = (int) getTextWidth(item, content, size);//用paint内置的方法将一个框框住文本，用框内置函数计算起其长度
        item.textMeasuredHeight = (int) getTextHeight(item, content, size);//用paint内置的方法将一个框框住文本，用框内置函数计算起其长度
        //设置随机移动速度
        item.moveSpeed = (int) (minSpeed + (maxSpeed - minSpeed) * Math.random());

        //获取当前View的实际高度
//            VideoView vv2 = (VideoView)findViewById(R.id.videoView);a

        totalHeight = (int)((height)*(this.getHeight()-item.textMeasuredHeight));
//        System.out.println("最后结果："+item.textView.getText()+" "+item.textMeasuredHeight+" "+totalHeight);
        //vihei =  ((VideoView)findViewById(R.id.videoView)).getHeight();
        //获取行高
        lineHeight = getLineHeight();//用paint内置的方法将一个框框住文本，用框内置函数计算起其高度,文字大小取最大
        //获取总行数
        totalLine = totalHeight / lineHeight;
        //垂直方向显示位置,行数的随机一行，nextInt(n) 返回一个大于等于0小于n的随机数
//        item.verticalPos = random.nextInt(totalLine) * lineHeight;
        item.verticalPos = totalHeight;
//        System.out.println("高度"+item.verticalPos+" "+this.getHeight());

        final ViewGroup vg = (ViewGroup) this;
        hdr = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                BarrageItem item = (BarrageItem)msg.obj;
                ViewGroup vgs = (ViewGroup)vg;
                switch (msg.what) {
                    case 0x02:
                        System.out.println("2");
                        showBarrageItem(item);
                        break;
                    case 0x03:
                        System.out.println("1");
                        showBarrageItem(item);
                        break;
                    case 0x01:
                        System.out.println("母鸡");
                        break;
                }
            }
        };

            final int totalwidth = this.getWidth();
            Thread temptr = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(situation == 1) {
//                      int num = vg.getChildCount();
                        for (int i = 0; i < vg.getChildCount(); i++) {
                            View here = vg.getChildAt(i);
                            LayoutParams params = (LayoutParams) here.getLayoutParams();
                            int heibef = params.topMargin;
                            int heibe = here.getHeight();
                            int heiba = here.getWidth();
                            if (((heibef > item.verticalPos) && (heibef < (item.verticalPos + item.textMeasuredHeight)))
                                    || (((heibe + heibef) > item.verticalPos) && ((heibe + heibef) < (item.verticalPos + item.textMeasuredHeight)))) {
                                int[] tempintarr = new int[2];
                                here.getLocationInWindow(tempintarr);
                                System.out.println("前弹幕" + here.getLeft()+" "+heibe+" "+heiba+" "+heibef+" "+item.textMeasuredHeight+" "+item.verticalPos+" "+here.getPaddingLeft()+" "+tempintarr[0]+" "+tempintarr[1]);
                                item.moveSpeed = maxSpeed;
                                while (tempintarr[0] > (totalwidth - heiba)) {
                                    here.getLocationInWindow(tempintarr);
                                    System.out.println("前弹幕sss" + tempintarr[0]+"  "+(totalwidth - heiba));
                                }
                            }
                        }
                        System.out.println("快捷进图1:"+situation);
                        Message ms = Message.obtain();
                        ms.obj = item;
                        ms.arg1 = totalwidth;
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



    /**
     * 显示TextView 的动画效果
     * @param item
     */
     synchronized private void showBarrageItem(final BarrageItem item) {
         System.out.println("pp "+item.text);
         //屏幕宽度 像素
        int leftMargin = this.getRight() - this.getLeft() - this.getPaddingLeft();//getrigth - get left 就是width
        //System.out.println(item.textView.getText()+(leftMargin+""));
        //显示的TextView 的位置，
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.topMargin = item.verticalPos;
        params.addRule(RelativeLayout.OVER_SCROLL_IF_CONTENT_SCROLLS);
        this.addView(item.textView, params);


//        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        WindowManager.LayoutParams wl = new WindowManager.LayoutParams();
//        wl.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//        wm.addView(item.textView,wl);




        //设置回调回调点击
        final String temp = item.textView.getText().toString();
//        item.textView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getContext(),temp+" selected",Toast.LENGTH_SHORT).show();
//            }
//        });

        //使用属性动画，确保控件位置不固定
        transAnimRun(item, leftMargin);
    }

    synchronized private void transAnimRun(final BarrageItem item, int leftMargin) {
//        System.out.println("leftmargin"+leftMargin+" :"+(-item.textMeasuredWidth+150));
        objAnim =ObjectAnimator
                //滑动位置是x方向滑动，从屏幕宽度+View的长度到左边0-View的长度
                .ofFloat(item.textView,"translationX" , leftMargin+5, -item.textMeasuredWidth-5)//前者控制出现（）右边，后者控制消失（）左边
                .setDuration(item.moveSpeed);
        //设置移动的过程速度，开始快之后满  //Interpolator是一个插值器
        objAnim.setInterpolator(new LinearInterpolator());

//        objAnim.setRepeatCount(1);//无限循环
//        objAnim.setRepeatMode(ValueAnimator.RESTART);//

        //开始动画
        objAnim.start();

        objAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Toast.makeText(getContext(),"anima start",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //动画执行完毕，清除动画，删除view，
                item.textView.clearAnimation();
                BarrageView.this.removeView(item.textView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //Toast.makeText(getContext(),"anima cancel",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //Toast.makeText(getContext(),"anima repee",Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void anipa() {
//        this.anitime = this.objAnim.getCurrentPlayTime();
//        this.objAnim.pause();
    }

    public void anirun() {
//        this.objAnim.start();
//        this.objAnim.setCurrentPlayTime(this.anitime);
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

    public float getTextHeight(BarrageItem item, String text, float Size) {
        //Rect表示一个矩形，由四条边的坐标组成
        Rect bounds = new Rect();
        TextPaint paint;
        paint = item.textView.getPaint();
        paint.getTextBounds(text, 0, text.length(), bounds);
        //System.out.println(item.textView.getText()+(bounds.width()+"")+"宽度");
        return bounds.height();
    }

    /**
     * 获得每一行弹幕的最大高度
     *
     * @return
     */
    private int getLineHeight() {
        BarrageItem item = new BarrageItem();
        String tx;
        tx = itemText.get(0);
        item.textView = new TextView(mContext);
        item.textView.setText(tx);
        item.textView.setTextSize(maxSize);
        Rect bounds = new Rect();
        TextPaint paint;
        paint = item.textView.getPaint();
        paint.getTextBounds(tx, 0, tx.length(), bounds);
        return bounds.height();
    }

    class BarrageHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //每个弹幕产生的间隔时间随机
            int duration = (int) ((BARRAGE_GAP_MAX_DURATION - BARRAGE_GAP_MIN_DURATION) * Math.random()+7000);
            generateItem("233");
            this.sendEmptyMessageDelayed(0, duration);//过一段时间duration之后在发送一次meg
        }
    }

    /**
     * 当view显示在窗口的时候，回调的visibility等于View.VISIBLE。。当view不显示在窗口时，回调的visibility等于View.GONE
     *
     * 窗口隐藏了，把内容全部清空，防止onPause时候内容停滞
     *
     * **/
    @Override
    protected void onWindowVisibilityChanged(int visibility) {

//        Toast.makeText(getContext(),"win vis chang",Toast.LENGTH_SHORT).show();

        super.onWindowVisibilityChanged(visibility);
//        if(visibility == View.GONE){
//            mHandler.removeMessages(0);
//        }else if(visibility == View.VISIBLE){
//            mHandler.sendEmptyMessage(0);//调用handle
//        }
    }

    /**
     *
     * 初始化数据
     *
     * **/
    private void initData(){
        itemText.add("疯狂动物城");
        itemText.add("师父");
        itemText.add("my特工doc");
        itemText.add("风之谷");
        itemText.add("美人鱼");
        itemText.add("唐人街探案");
        itemText.add("西游记");
        itemText.add("解救吾先生");
    }

}
