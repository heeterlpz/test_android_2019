package com.example.jason.video_player_1;

import android.widget.TextView;

/**
 * Created by tpnet
 * 2016\4\16
 *
 */
public class BarrageItem {
    public TextView textView;
    public int textColor;
    public String text;
    public int textSize;
    public int moveSpeed;//移动速度
    public int verticalPos;//垂直方向显示的位置
    public int textMeasuredWidth;//字体显示占据的宽度
    public int textMeasuredHeight;//字体显示占据的高度
    public int level;//弹幕的优先等级
    public int situation;//弹幕所处的状态（遮挡：无遮）

}