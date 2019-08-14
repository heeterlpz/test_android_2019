#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTextureSampler;
varying vec2 vTextureCoord;
void main() {
    vec4 sample0,sample1,sample2,sample3,sample4,sample5,sample6,sample7,sample8;
    float fstep=0.0015;
    sample0=texture2D(uTextureSampler,vec2(vTextureCoord.x-fstep,vTextureCoord.y-fstep));
    sample1=texture2D(uTextureSampler,vec2(vTextureCoord.x+fstep,vTextureCoord.y-fstep));
    sample2=texture2D(uTextureSampler,vec2(vTextureCoord.x-fstep,vTextureCoord.y+fstep));
    sample3=texture2D(uTextureSampler,vec2(vTextureCoord.x+fstep,vTextureCoord.y+fstep));

    sample4=texture2D(uTextureSampler,vec2(vTextureCoord.x+fstep,vTextureCoord.y));
    sample5=texture2D(uTextureSampler,vec2(vTextureCoord.x-fstep,vTextureCoord.y));
    sample6=texture2D(uTextureSampler,vec2(vTextureCoord.x,vTextureCoord.y+fstep));
    sample7=texture2D(uTextureSampler,vec2(vTextureCoord.x,vTextureCoord.y-fstep));

    sample8=texture2D(uTextureSampler,vec2(vTextureCoord.x,vTextureCoord.y));

    //锐化
    vec3 color=sample8.rgb*(5.0)+sample4.rgb*(-1.0)+sample5.rgb*(-1.0)+sample6.rgb*(-1.0)+sample7.rgb*(-1.0);
    gl_FragColor = vec4(color.rgb,sample8.a);
}
