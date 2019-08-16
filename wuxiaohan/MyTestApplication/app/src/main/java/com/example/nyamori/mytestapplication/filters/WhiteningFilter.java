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

    @Override
    public void setLevel(int newLevel) {
        if(newLevel==0){
            isLevelZero=true;
            beta=0;
        }
        else {
            isLevelZero=false;
            if(newLevel<=2)newLevel=2;
            this.beta=newLevel;
        }
    }

    @Override
    public int getLevelMax() {
        return 7;
    }

    @Override
    public int getLevel() {
        return (int)beta;
    }
}
