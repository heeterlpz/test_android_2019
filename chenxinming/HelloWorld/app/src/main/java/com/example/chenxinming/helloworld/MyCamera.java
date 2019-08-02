package com.example.chenxinming.helloworld;

import android.hardware.Camera;
import android.util.Log;

/**
 * Created by chenxinming on 19-8-2.
 */

public class MyCamera implements Camera.PreviewCallback {
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Camera.Parameters ps = camera.getParameters();
        int[] imgs = new int[ps.getPreviewSize().width * ps.getPreviewSize().height];

        try
        {
            Log.i("tyty", ps.getPreviewSize().width + "=====" + ps.getPreviewSize().height);
            //doyouself
        }
        catch (Exception e)
        {
            Log.i("tyty", "Exception new bmp" + e.toString());
            return;
        }

        camera.addCallbackBuffer(bytes);

        Log.i("tyty", "回调");
    }
}
