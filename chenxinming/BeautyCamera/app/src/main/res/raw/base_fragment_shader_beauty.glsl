#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTextureSampler;
varying vec2 vTextureCoord;
void main()
{
    vec2 blurCoordinates[20];
    vec2 singleStepOffset =vec2(1.0/2000.0,1.0/2000.0);
    //取当前数据周边的20个点的颜色通道的值，进行相加然后得出平均值
    blurCoordinates[0] = vTextureCoord.xy + singleStepOffset *vec2(0.0, -10.0);
    blurCoordinates[1] = vTextureCoord.xy + singleStepOffset *vec2(0.0, 10.0);
    blurCoordinates[2] = vTextureCoord.xy + singleStepOffset *vec2(-10.0, 0.0);
    blurCoordinates[3] = vTextureCoord.xy + singleStepOffset *vec2(10.0, 0.0);
    blurCoordinates[4] = vTextureCoord.xy + singleStepOffset *vec2(5.0, -8.0);
    blurCoordinates[5] = vTextureCoord.xy + singleStepOffset *vec2(5.0, 8.0);
    blurCoordinates[6] = vTextureCoord.xy + singleStepOffset *vec2(-5.0, 8.0);
    blurCoordinates[7] = vTextureCoord.xy + singleStepOffset *vec2(-5.0, -8.0);
    blurCoordinates[8] = vTextureCoord.xy + singleStepOffset *vec2(8.0, -5.0);
    blurCoordinates[9] = vTextureCoord.xy + singleStepOffset *vec2(8.0, 5.0);
    blurCoordinates[10] = vTextureCoord.xy + singleStepOffset *vec2(-8.0, 5.0);
    blurCoordinates[11] = vTextureCoord.xy + singleStepOffset *vec2(-8.0, -5.0);
    blurCoordinates[12] = vTextureCoord.xy + singleStepOffset *vec2(0.0, -6.0);
    blurCoordinates[13] = vTextureCoord.xy + singleStepOffset *vec2(0.0, 6.0);
    blurCoordinates[14] = vTextureCoord.xy + singleStepOffset *vec2(6.0, 0.0);
    blurCoordinates[15] = vTextureCoord.xy + singleStepOffset *vec2(-6.0, 0.0);
    blurCoordinates[16] = vTextureCoord.xy + singleStepOffset *vec2(-4.0, -4.0);
    blurCoordinates[17] = vTextureCoord.xy + singleStepOffset *vec2(-4.0, 4.0);
    blurCoordinates[18] = vTextureCoord.xy + singleStepOffset *vec2(4.0, -4.0);
    blurCoordinates[19] = vTextureCoord.xy + singleStepOffset *vec2(4.0, 4.0);
    //计算平均值   
    //本身的点的像素值   
    vec4 currentColor = texture2D(uTextureSampler, vTextureCoord);
    vec3 rgb = currentColor.rgb;
    // 计算偏移坐标的颜色值总和       
    for (int i =0; i <20; i++) {
        //采集20个点 的像素值 相加 得到总和           
        rgb += texture2D(uTextureSampler, blurCoordinates[i].xy).rgb;
    }
    // rgb：21个点的像素和   
    //平均值 模糊效果   
    // rgba   
    vec4 blur =vec4(rgb *1.0 /21.0, currentColor.a);
    vec4 highPassColor = currentColor - blur;
    highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r *24.0, 0.0, 1.0);
    highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g *24.0, 0.0, 1.0);
    highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b *24.0, 0.0, 1.0);
    // 过滤疤痕       
    vec4 highPassBlur =vec4(highPassColor.rgb, 1.0);
    //3、融合 -> 磨皮           
    //蓝色通道值       
    float b = min(currentColor.b, blur.b);
    float value = clamp((b -0.2) *5.0, 0.0, 1.0);
    // RGB的最大值       
    float maxChannelColor = max(max(highPassBlur.r, highPassBlur.g), highPassBlur.b);
    // 磨皮程度       
    float intensity =0.8; // 0.0 - 1.0f 再大会很模糊       
    float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor +0.2)) * value * intensity;
    // 一个滤镜       
    //opengl 内置函数 线性融合       
    //混合 x*(1−a)+y⋅a       
    // 第三个值越大，在这里融合的图像 越模糊       
    vec3 r = mix(currentColor.rgb,blur.rgb,currentIntensity);
    gl_FragColor =vec4(r,1.0);

}
