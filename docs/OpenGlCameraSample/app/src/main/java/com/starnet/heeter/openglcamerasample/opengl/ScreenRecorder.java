package com.starnet.heeter.openglcamerasample.opengl;

import android.graphics.SurfaceTexture;
import android.view.Surface;

/**
 * Created by joans on 17-9-27.
 */

public class ScreenRecorder implements SurfaceTexture.OnFrameAvailableListener {
    SurfaceTexture mSurfaceTexture;
    Surface decodeSurface;
    int mWidth;
    int mHeight;

    private void setup() {

        mSurfaceTexture = new SurfaceTexture(0);
        mSurfaceTexture.setDefaultBufferSize(mWidth, mHeight);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        decodeSurface = new Surface(mSurfaceTexture);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }
}
