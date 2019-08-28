#extension GL_OES_EGL_image_external : require
precision highp float;
uniform samplerExternalOES uTextureSampler;
varying  lowp vec2 vTextureCoord;
const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);
const vec2 TexSize = vec2(100.0, 100.0);
const vec4 bkColor = vec4(0.5, 0.5, 0.5, 1.0);
void main()
    {
        vec2 upLeftUV = vec2(vTextureCoord.x-1.0/TexSize.x, vTextureCoord.y-1.0/TexSize.y);
        vec4 curColor = texture2D(uTextureSampler, vTextureCoord);
        vec4 upLeftColor = texture2D(uTextureSampler, upLeftUV);
        vec4 delColor = curColor - upLeftColor;
        float luminance = dot(delColor.rgb, W);
        gl_FragColor = vec4(vec3(luminance), 0.0) + bkColor;
    }