package com.example.nyamori.mytestapplication.filters;

import android.opengl.GLES20;

import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class WhiteningFilter extends BaseFilter {
    private int betaLoc;
    private float beta;
    public WhiteningFilter(int width, int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_whitening);
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }

    @Override
    public void getLocation() {
        super.getLocation();
        betaLoc=GLES20.glGetUniformLocation(mProgramHandle, "beta");
        beta=5.0f;
    }

    @Override
    public void setUniform() {
        super.setUniform();
        GLES20.glUniform1f(betaLoc,beta);
    }


    public void setBeta(float beta) {
        this.beta = beta;
    }
}
