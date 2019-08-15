#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform float brightLevel;
uniform samplerExternalOES uTextureSampler;
varying vec2 vTextureCoord;
void main()
{
    vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);
    float fGrayColor = (0.3*vCameraColor.r + 0.59*vCameraColor.g + 0.11*vCameraColor.b);
    gl_FragColor = vec4(brightLevel/100.0*fGrayColor,brightLevel/100.0*fGrayColor,brightLevel/100.0*fGrayColor,1.0);
}