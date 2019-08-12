#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTextureSampler;
varying vec2 vTextureCoord;
void main() {
    //进行纹理采样,拿到当前颜色
    vec4 nColor = texture2D(uTextureSampler, vTextureCoord);

    float dis = 0.01; //距离越大越模糊

    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x-dis, vTextureCoord.y-dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x-dis, vTextureCoord.y+dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x+dis, vTextureCoord.y-dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x+dis, vTextureCoord.y+dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x-dis, vTextureCoord.y-dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x-dis, vTextureCoord.y+dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x+dis, vTextureCoord.y-dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x+dis, vTextureCoord.y+dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x-dis, vTextureCoord.y-dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x-dis, vTextureCoord.y+dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x+dis, vTextureCoord.y-dis));
    nColor+=texture2D(uTextureSampler, vec2(vTextureCoord.x+dis, vTextureCoord.y+dis));
    nColor/=13.0; //周边13个颜色相加，然后取平均，作为这个点的颜色
    gl_FragColor=nColor;

//    vec2 blurCoordinates[25];
//    vec2 singleStepOffset =vec2(1.0/500.0,1.0/500.0);
//
//    blurCoordinates[0] = vTextureCoord.xy + singleStepOffset *vec2(-2, -2);
//    blurCoordinates[1] = vTextureCoord.xy + singleStepOffset *vec2(-2, -1);
//    blurCoordinates[2] = vTextureCoord.xy + singleStepOffset *vec2(-2, 0);
//    blurCoordinates[3] = vTextureCoord.xy + singleStepOffset *vec2(-2, 1);
//    blurCoordinates[4] = vTextureCoord.xy + singleStepOffset *vec2(-2, 2);
//    blurCoordinates[5] = vTextureCoord.xy + singleStepOffset *vec2(-1, -2);
//    blurCoordinates[6] = vTextureCoord.xy + singleStepOffset *vec2(-1, -1);
//    blurCoordinates[7] = vTextureCoord.xy + singleStepOffset *vec2(-1, 0);
//    blurCoordinates[8] = vTextureCoord.xy + singleStepOffset *vec2(-1, 1);
//    blurCoordinates[9] = vTextureCoord.xy + singleStepOffset *vec2(-1, 2);
//    blurCoordinates[10] = vTextureCoord.xy + singleStepOffset *vec2(0, -2);
//    blurCoordinates[11] = vTextureCoord.xy + singleStepOffset *vec2(0, -1);
//    blurCoordinates[12] = vTextureCoord.xy + singleStepOffset *vec2(0, 0);
//    blurCoordinates[13] = vTextureCoord.xy + singleStepOffset *vec2(0, 1);
//    blurCoordinates[14] = vTextureCoord.xy + singleStepOffset *vec2(0, 2);
//    blurCoordinates[15] = vTextureCoord.xy + singleStepOffset *vec2(1, -2);
//    blurCoordinates[16] = vTextureCoord.xy + singleStepOffset *vec2(1, -1);
//    blurCoordinates[17] = vTextureCoord.xy + singleStepOffset *vec2(1, 0);
//    blurCoordinates[18] = vTextureCoord.xy + singleStepOffset *vec2(1, 1);
//    blurCoordinates[19] = vTextureCoord.xy + singleStepOffset *vec2(1, 2);
//    blurCoordinates[20] = vTextureCoord.xy + singleStepOffset *vec2(2, -2);
//    blurCoordinates[21] = vTextureCoord.xy + singleStepOffset *vec2(2, -1);
//    blurCoordinates[22] = vTextureCoord.xy + singleStepOffset *vec2(2, 0);
//    blurCoordinates[23] = vTextureCoord.xy + singleStepOffset *vec2(2, 1);
//    blurCoordinates[24] = vTextureCoord.xy + singleStepOffset *vec2(2, 2);
//
//    //采集25个点 的像素值 相加 得到总和 
//    vec3 rgb = texture2D(uTextureSampler, blurCoordinates[0].xy).rgb+texture2D(uTextureSampler, blurCoordinates[1].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[2].xy).rgb+texture2D(uTextureSampler, blurCoordinates[3].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[4].xy).rgb+texture2D(uTextureSampler, blurCoordinates[5].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[6].xy).rgb+texture2D(uTextureSampler, blurCoordinates[7].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[8].xy).rgb+texture2D(uTextureSampler, blurCoordinates[9].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[10].xy).rgb+texture2D(uTextureSampler, blurCoordinates[11].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[12].xy).rgb+texture2D(uTextureSampler, blurCoordinates[13].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[14].xy).rgb+texture2D(uTextureSampler, blurCoordinates[15].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[16].xy).rgb+texture2D(uTextureSampler, blurCoordinates[17].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[18].xy).rgb+texture2D(uTextureSampler, blurCoordinates[19].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[20].xy).rgb+texture2D(uTextureSampler, blurCoordinates[21].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[22].xy).rgb+texture2D(uTextureSampler, blurCoordinates[23].xy).rgb+
//    texture2D(uTextureSampler, blurCoordinates[24].xy).rgb;
//    //平均值 模糊效果   
//    // rgba   
//    vec4 blur =vec4(rgb *1.0 /25.0, texture2D(uTextureSampler, blurCoordinates[12].xy).a);
//    gl_FragColor = blur;
//    vec4 sample0,sample1,sample2,sample3,sample4,sample5,sample6,sample7,sample8;
//    float fstep=0.0015;
//    sample0=texture2D(uTextureSampler,vec2(vTextureCoord.x-fstep,vTextureCoord.y-fstep));
//    sample1=texture2D(uTextureSampler,vec2(vTextureCoord.x+fstep,vTextureCoord.y-fstep));
//    sample2=texture2D(uTextureSampler,vec2(vTextureCoord.x-fstep,vTextureCoord.y+fstep));
//    sample3=texture2D(uTextureSampler,vec2(vTextureCoord.x+fstep,vTextureCoord.y+fstep));
//
//    sample4=texture2D(uTextureSampler,vec2(vTextureCoord.x+fstep,vTextureCoord.y));
//    sample5=texture2D(uTextureSampler,vec2(vTextureCoord.x-fstep,vTextureCoord.y));
//    sample6=texture2D(uTextureSampler,vec2(vTextureCoord.x,vTextureCoord.y+fstep));
//    sample7=texture2D(uTextureSampler,vec2(vTextureCoord.x,vTextureCoord.y-fstep));
//
//    sample8=texture2D(uTextureSampler,vec2(vTextureCoord.x,vTextureCoord.y));
//
//    //高斯模糊
//    vec3 color=sample0.rgb+sample1.rgb+sample2.rgb+sample3.rgb+sample4.rgb+sample5.rgb+sample6.rgb+sample7.rgb+sample8.rgb;
//    gl_FragColor = vec4(color/9.0,sample8.a);

//    vec2 tex_offset =vec2(1.0/300.0,1.0/300.0);
//    vec4 orColor=texture(uTextureSampler,vTextureCoord);
//    float orAlpha=orColor.a;
//    float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
//    vec3 color=orColor.rgb*weight[0];
//    for(int i=1;i<5;i++)
//    {
//        color+=texture(sampler,textureCoord+vec2(tex_offset.x * float(i), 0.0)).rgb*weight[i];
//        color+=texture(sampler,textureCoord-vec2(tex_offset.x * float(i), 0.0)).rgb*weight[i];
//
//    }
//    gl_FragColor=vec4(color,orAlpha);
}
