package com.example.nyamori.mytestapplication.filters;

import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class BeautyFilter extends BaseFilter {
    public BeautyFilter(int width, int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_beauty_big);
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }
}
