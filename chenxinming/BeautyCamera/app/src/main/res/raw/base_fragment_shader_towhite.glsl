#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTextureSampler;
uniform float betaLevel;
varying vec2 vTextureCoord;
void main()
{
    vec4 color = texture2D(uTextureSampler, vTextureCoord);
    float red = color.r;
    float green = color.g;
    float blue = color.b;
    color.r=log(red*(betaLevel-1.0)+1.0)/log(betaLevel);
    color.g=log(green*(betaLevel-1.0)+1.0)/log(betaLevel);
    color.b=log(blue*(betaLevel-1.0)+1.0)/log(betaLevel);
    gl_FragColor = vec4(color.rgb,1.0);


}
