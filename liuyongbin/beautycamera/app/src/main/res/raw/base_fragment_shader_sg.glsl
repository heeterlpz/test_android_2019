#extension GL_OES_EGL_image_external : require
    precision mediump float;
    uniform float alphaLevel;
    uniform samplerExternalOES uTextureSampler;
    varying vec2 vTextureCoord;
    void main()
    {
        vec2 blurCoordinates[20];
        vec2 singleStepOffset = vec2(1.0/500.0, 1.0/500.0);
        //取当前数据周边的20个点的颜色通道的值，进行相加后得出平均值。
        blurCoordinates[0] = vTextureCoord.xy + singleStepOffset*vec2(0.0, -10.0);
        blurCoordinates[1] = vTextureCoord.xy + singleStepOffset*vec2(0.0, 10.0);
        blurCoordinates[2] = vTextureCoord.xy + singleStepOffset*vec2(-10.0, 0.0);
        blurCoordinates[3] = vTextureCoord.xy + singleStepOffset*vec2(10.0, 0.0);
        blurCoordinates[4] = vTextureCoord.xy + singleStepOffset*vec2(5.0, -8.0);
        blurCoordinates[5] = vTextureCoord.xy + singleStepOffset*vec2(5.0, 8.0);
        blurCoordinates[6] = vTextureCoord.xy + singleStepOffset*vec2(-5.0, 8.0);
        blurCoordinates[7] = vTextureCoord.xy + singleStepOffset*vec2(-5.0, -8.0);
        blurCoordinates[8] = vTextureCoord.xy + singleStepOffset*vec2(8.0, -5.0);
        blurCoordinates[9] = vTextureCoord.xy + singleStepOffset*vec2(8.0, 5.0);
        blurCoordinates[10] = vTextureCoord.xy + singleStepOffset*vec2(-8.0, 5.0);
        blurCoordinates[11] = vTextureCoord.xy + singleStepOffset*vec2(-8.0, -5.0);
        blurCoordinates[12] = vTextureCoord.xy + singleStepOffset*vec2(0.0, -6.0);
        blurCoordinates[13] = vTextureCoord.xy + singleStepOffset*vec2(0.0, 6.0);
        blurCoordinates[14] = vTextureCoord.xy + singleStepOffset*vec2(6.0, 0.0);
        blurCoordinates[15] = vTextureCoord.xy + singleStepOffset*vec2(-6.0, 0.0);
        blurCoordinates[16] = vTextureCoord.xy + singleStepOffset*vec2(-4.0, -4.0);
        blurCoordinates[17] = vTextureCoord.xy + singleStepOffset*vec2(-4.0, 4.0);
        blurCoordinates[18] = vTextureCoord.xy + singleStepOffset*vec2(4.0, -4.0);
        blurCoordinates[19] = vTextureCoord.xy + singleStepOffset*vec2(4.0, 4.0);

        vec4 currentColor = texture2D(uTextureSampler, vTextureCoord);
        float green = currentColor.g;
        for (int i = 0; i < 20; i++) {
            //采集20个点的像素值，相加得到总和。
            green += texture2D(uTextureSampler, blurCoordinates[i].xy).g;
        }
        float highpass = currentColor.g - green/21.0 + 0.5;


        for (int i = 0; i < 3; i++) {
            if(highpass <= 0.5) {
                highpass = highpass * highpass * 2.0;
            }else{
                highpass = 1.0 - ((1.0 - highpass)*(1.0 - highpass)*2.0);
            }
        }

        float lumance = 0.299*currentColor.r + 0.587*currentColor.g + 0.114*currentColor.b;

//        if(alphalevel == 0.0) {
//            float alpha = alphalevel;
//        }else{
//            float alpha = pow(lumance, alphalevel);
//        }
        float alpha = pow(lumance, alphaLevel);
        //gl_FragColor = vec4(currentColor.rgb - vec3(highpass, highpass, highpass)*alpha*0.1,1.0);
        vec3 smoothColor = currentColor.rgb + (currentColor.rgb - vec3(highpass, highpass, highpass))*alpha*0.1;

        gl_FragColor = vec4(smoothColor, 1.0);
    }

