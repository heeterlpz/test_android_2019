package com.example.nyamori.mytestapplication;

public class ViewPort {
    private int xStart;
    private int yStart;
    private int width;
    private int height;

    public ViewPort(int xStart,int yStart,int width,int height){
        this.xStart=xStart;
        this.yStart=yStart;
        this.width=width;
        this.height=height;
    }

    public int getxStart() {
        return xStart;
    }

    public int getyStart() {
        return yStart;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
