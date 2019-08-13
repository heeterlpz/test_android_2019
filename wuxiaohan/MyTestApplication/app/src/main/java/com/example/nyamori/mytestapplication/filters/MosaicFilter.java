package com.example.nyamori.mytestapplication.filters;

import com.example.nyamori.mytestapplication.R;
import com.example.nyamori.mytestapplication.ShaderLoader;

public class MosaicFilter extends BaseFilter {
    public MosaicFilter(int width,int height){
        super();
        mProgramHandle= ShaderLoader.getInstance().loadShader(R.raw.fragment_shader_mosaic);
        getLocation();
        chooseSize(width,height);
        createFrame();
        initRect();
    }
}
