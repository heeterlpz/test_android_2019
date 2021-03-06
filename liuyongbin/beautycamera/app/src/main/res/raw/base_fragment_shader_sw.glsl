#extension GL_OES_EGL_image_external : require
    precision mediump float;
    uniform float betalevel;
    uniform samplerExternalOES uTextureSampler;
    varying vec2 vTextureCoord;
    void main()
    {

//        vec4 sample0, sample1, sample2, sample3, sample4, sample5, sample6, sample7, sample8,color;
//        float fstep = 1.0/256.0;
//        sample0 = texture2D(uTextureSampler, vec2(vTextureCoord.x-fstep, vTextureCoord.y+fstep));
//        sample1 = texture2D(uTextureSampler, vec2(vTextureCoord.x, vTextureCoord.y+fstep));
//        sample2 = texture2D(uTextureSampler, vec2(vTextureCoord.x+fstep, vTextureCoord.y+fstep));
//
//        sample3 = texture2D(uTextureSampler, vec2(vTextureCoord.x-fstep, vTextureCoord.y));
//        sample4 = texture2D(uTextureSampler, vec2(vTextureCoord.x, vTextureCoord.y));
//        sample5 = texture2D(uTextureSampler, vec2(vTextureCoord.x+fstep, vTextureCoord.y));
//
//        sample6 = texture2D(uTextureSampler, vec2(vTextureCoord.x-fstep, vTextureCoord.y-fstep));
//        sample7 = texture2D(uTextureSampler, vec2(vTextureCoord.x, vTextureCoord.y-fstep));
//        sample8 = texture2D(uTextureSampler, vec2(vTextureCoord.x+fstep, vTextureCoord.y-fstep));
//
//        color = sample0*0.095 + sample1*0.118 + sample2*0.095 + sample3*0.118 + sample4*0.148 + sample5*0.118 +
//        sample6*0.095 + sample7*0.118 + sample8*0.095;
            vec4 color = texture2D(uTextureSampler, vTextureCoord);

            float redCurveValue = color.r;
            float greenCurveValue = color.g;
            float blueCurveValue = color.b;

            redCurveValue = log(redCurveValue*(betalevel - 1.0) + 1.0)/log(betalevel);
            greenCurveValue = log(greenCurveValue*(betalevel - 1.0) + 1.0)/log(betalevel);
            blueCurveValue = log(blueCurveValue*(betalevel - 1.0) + 1.0)/log(betalevel);

            gl_FragColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);

    }

