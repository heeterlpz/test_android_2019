package com.example.nyamori.mytestapplication.filters;

import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class BWFilter extends BaseFilter {

    public BWFilter(int width,int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_bw);
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }
}
