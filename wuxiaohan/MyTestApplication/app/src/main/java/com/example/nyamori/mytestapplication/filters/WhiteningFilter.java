package com.example.nyamori.mytestapplication.filters;

import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class WhiteningFilter extends BaseFilter {
    public WhiteningFilter(int width, int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_whitening);
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }
}
