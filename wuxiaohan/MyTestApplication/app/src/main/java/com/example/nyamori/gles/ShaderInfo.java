package com.example.nyamori.gles;

public class ShaderInfo {
    // Simple vertex shader, used for all programs.
    public static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    // Simple fragment shader for use with "normal" 2D textures.
    //相机的texture必须为samplerExternalOES 而sampler2D是二维纹理
    //最后一步直出
    public static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    // Simple fragment shader for use with external 2D textures (e.g. what we get from
    // SurfaceTexture).
    public static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";


    public static final String FRAGMENT_SHADER_EXT_HP =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision highp float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    public static final String FRAGMENT_DIV_UD =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    vec2 uv = vTextureCoord;\n" +
                    "    if (uv.y < 0.5) {\n" +
                    "        uv.y = 1.0 - uv.y;\n" +
                    "    }\n" +
                    "    if (uv.x > 0.5) {\n" +
                    "       uv.x = 1.0 - uv.x;\n" +
                    "    }\n" +
                    "    gl_FragColor = texture2D(sTexture, fract(uv));\n" +
                    "}\n";

    public static final String FRAGMENT_SPLIT =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    vec2 uv = vTextureCoord;\n" +
                    "    if (uv.x < 1.0 / 3.0) {\n" +
                    "        uv.x = uv.x * 3.0;\n" +
                    "    } else if (uv.x < 2.0 / 3.0) {\n" +
                    "        uv.x = (uv.x - 1.0 / 3.0) * 3.0;\n" +
                    "    } else {\n" +
                    "        uv.x = (uv.x - 2.0 / 3.0) * 3.0;\n" +
                    "    }\n" +
                    "    if (uv.y <= 1.0 / 3.0) {\n" +
                    "        uv.y = uv.y * 3.0;\n" +
                    "    } else if (uv.y < 2.0 / 3.0) {\n" +
                    "        uv.y = (uv.y - 1.0 / 3.0) * 3.0;\n" +
                    "    } else {\n" +
                    "        uv.y = (uv.y - 2.0 / 3.0) * 3.0;\n" +
                    "    }\n" +
                    "    gl_FragColor = texture2D(sTexture, uv);\n" +
                    "}\n";

    public static final String FRAGMENT_MOSAIC =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    vec2 uv  = vTextureCoord.xy;\n" +
                    "    float dx = 0.02;\n" +
                    "    float dy = 0.02;\n" +
                    "    vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));\n" +
                    "    vec3 tc = texture2D(sTexture, coord).xyz;\n" +
                    "    gl_FragColor = vec4(tc, 1.0);\n" +
                    "}\n";

    // Fragment shader that converts color to black & white with a simple transformation.
    public static final String FRAGMENT_SHADER_EXT_BW =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "//uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
                    "    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;\n" +
                    "    gl_FragColor = vec4(color, color, color, 1.0);\n" +
                    "}\n";

    // Fragment shader with a convolution filter.  The upper-left half will be drawn normally,
    // the lower-right half will have the filter applied, and a thin red line will be drawn
    // at the border.
    //
    // This is not optimized for performance.  Some things that might make this faster:
    // - Remove the conditionals.  They're used to present a half & half view with a red
    //   stripe across the middle, but that's only useful for a demo.
    // - Unroll the loop.  Ideally the compiler does this for you when it's beneficial.
    // - Bake the filter kernel into the shader, instead of passing it through a uniform
    //   array.  That, combined with loop unrolling, should reduce memory accesses.
    public static final int KERNEL_SIZE = 9;
    public static final String FRAGMENT_SHADER_EXT_FILT =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "#define KERNEL_SIZE " + KERNEL_SIZE + "\n" +
                    "precision highp float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "uniform float uKernel[KERNEL_SIZE];\n" +
                    "uniform vec2 uTexOffset[KERNEL_SIZE];\n" +
                    "uniform float uColorAdjust;\n" +
                    "void main() {\n" +
                    "    int i = 0;\n" +
                    "    vec4 sum = vec4(0.0);\n" +
                    "    if (vTextureCoord.x < vTextureCoord.y-0.001) {\n" +
                    "        for (i = 0; i < KERNEL_SIZE; i++) {\n" +
                    "            vec4 texc = texture2D(sTexture, vTextureCoord + uTexOffset[i]);\n" +
                    "            sum += texc * uKernel[i];\n" +
                    "        }\n" +
                    "    sum += uColorAdjust;\n" +
                    "    } else if (vTextureCoord.x > vTextureCoord.y+0.001) {\n" +
                    "        sum = texture2D(sTexture, vTextureCoord);\n" +
                    "    } else {\n" +
                    "        sum.r = 1.0;\n" +
                    "    }\n" +
                    "    gl_FragColor = sum;\n" +
                    "}\n";

    public static final String FRAGMENT_SMOOTH =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    //给出卷积内核中各个元素对应像素相对于待处理像素的纹理坐标偏移量 3*3内核\n" +
                    "    vec2 offset0=vec2(-1.0,-1.0); vec2 offset1=vec2(0.0,-1.0); vec2 offset2=vec2(1.0,-1.0);\n" +
                    "    vec2 offset3=vec2(-1.0,0.0); vec2 offset4=vec2(0.0,0.0); vec2 offset5=vec2(1.0,0.0);\n" +
                    "    vec2 offset6=vec2(-1.0,1.0); vec2 offset7=vec2(0.0,1.0); vec2 offset8=vec2(1.0,1.0);\n" +
                    "    const float scaleFactor=1.0/9.0;//给出最终求和时的加权因子(调整亮度)\n" +
                    "    //卷积内核中各个位置的值\n" +
                    "    float kernelValue0 = 1.0; float kernelValue1 = 1.0; float kernelValue2 = 1.0;\n" +
                    "    float kernelValue3 = 1.0; float kernelValue4 = 1.0; float kernelValue5 = 1.0;\n" +
                    "    float kernelValue6 = 1.0; float kernelValue7 = 1.0; float kernelValue8 = 1.0;\n" +
                    "    vec4 sum;//最终的颜色和\n" +
                    "    //获取卷积内核中各个元素对应像素的颜色值\n" +
                    "    vec4 cTemp0,cTemp1,cTemp2,cTemp3,cTemp4,cTemp5,cTemp6,cTemp7,cTemp8;\n" +
                    "    cTemp0=texture2D(sTexture, vTextureCoord.st + offset0.xy/512.0);\n" +
                    "    cTemp1=texture2D(sTexture, vTextureCoord.st + offset1.xy/512.0);\n" +
                    "    cTemp2=texture2D(sTexture, vTextureCoord.st + offset2.xy/512.0);\n" +
                    "    cTemp3=texture2D(sTexture, vTextureCoord.st + offset3.xy/512.0);\n" +
                    "    cTemp4=texture2D(sTexture, vTextureCoord.st + offset4.xy/512.0);\n" +
                    "    cTemp5=texture2D(sTexture, vTextureCoord.st + offset5.xy/512.0);\n" +
                    "    cTemp6=texture2D(sTexture, vTextureCoord.st + offset6.xy/512.0);\n" +
                    "    cTemp7=texture2D(sTexture, vTextureCoord.st + offset7.xy/512.0);\n" +
                    "    cTemp8=texture2D(sTexture, vTextureCoord.st + offset8.xy/512.0);\n" +
                    "    //颜色求和\n" +
                    "    sum =kernelValue0*cTemp0+kernelValue1*cTemp1+kernelValue2*cTemp2+\n" +
                    "    kernelValue3*cTemp3+kernelValue4*cTemp4+kernelValue5*cTemp5+\n" +
                    "    kernelValue6*cTemp6+kernelValue7*cTemp7+kernelValue8*cTemp8;\n" +
                    "    gl_FragColor=sum*scaleFactor;//进行亮度加权后将最终颜色传递给管线\n" +
                    "}\n";


    public static final String FRAGMENT_SHADER_BW =
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
                    "    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;\n" +
                    "    gl_FragColor = vec4(color, color, color, 1.0);\n" +
                    "}\n";

}
