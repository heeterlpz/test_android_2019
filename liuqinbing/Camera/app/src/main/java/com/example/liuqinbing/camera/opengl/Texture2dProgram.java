/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.liuqinbing.camera.opengl;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;

/**
 * GL program and supporting functions for textured 2D shapes.
 */
public class Texture2dProgram {
    private static final String TAG = GlUtil.TAG;

    public enum ProgramType {
        TEXTURE_2D, TEXTURE_EXT, TEXTURE_EXT_BW, TEXTURE_EXT_FILT, TEXTURE_DIV_UD, TEXTURE_SPLIT, TEXTURE_MOSAIC, TEXTURE_SMOOTH, TEXTURE_WIHITE, TEXTURE_SMOOTH_SKIN
    }

    // Simple vertex shader, used for all programs.
    private static final String VERTEX_SHADER =
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
    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    // 用于外部2D纹理的简单片段着色器（例如我们从SurfaceTexture得到的内容）
    // Simple fragment shader for use with external 2D textures (e.g. what we get from
    // SurfaceTexture).
    private static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    private static final String FRAGMENT_SHADER_EXT_HP =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    private static final String FRAGMENT_DIV_UD =
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

    private static final String FRAGMENT_SPLIT =
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

    private static final String FRAGMENT_MOSAIC =
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

    private static final String FRAGMENT_SMOOTH =
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

    // Fragment shader that converts color to black & white with a simple transformation.
    private static final String FRAGMENT_SHADER_EXT_BW =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
            "    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;\n" +
            "    gl_FragColor = vec4(color, color, color, 1.0);\n" +
            "}\n";

    private static final String FRAGMENT_SHADER_WIHITE =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    vec4 nColor = texture2D(sTexture, vTextureCoord);\n" +
            "    vec4 deltaColor = nColor+vec4(vec3(1.0 * 0.25),0.0);\n" +
            "    gl_FragColor = deltaColor;\n" +
            "}";

    private static final String FRAGMENT_SHADER_SMOOTH_SKIN =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "vec2 blurCoordinates[20];\n" +
                    "// 磨皮程度\n" +
                    "uniform float uSmoothIntensity;\n" +
                    "// 美白程度\n" +
                    "uniform float uWhiteIntensity;\n" +
                    "// 红润程度\n" +
                    "uniform float uRuddyIntensity;\n" +
                    "float hardLight(float color)\n" +
                    "{\n" +
                    "    if(color <= 0.5)\n" +
                    "        color = color * color * 2.0;\n" +
                    "    else\n" +
                    "        color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);\n" +
                    "    return color;\n" +
                    "}\n" +
                    "void main(){\n" +
                    "\n" +
                    "    vec2 singleStepOffset = vec2(1.0/1920.0,1.0/1080.0);\n" +
                    "    blurCoordinates[0] = vTextureCoord.xy + singleStepOffset * vec2(0.0, -10.0);\n" +
                    "    blurCoordinates[1] = vTextureCoord.xy + singleStepOffset * vec2(0.0, 10.0);\n" +
                    "    blurCoordinates[2] = vTextureCoord.xy + singleStepOffset * vec2(-10.0, 0.0);\n" +
                    "    blurCoordinates[3] = vTextureCoord.xy + singleStepOffset * vec2(10.0, 0.0);\n" +
                    "    blurCoordinates[4] = vTextureCoord.xy + singleStepOffset * vec2(5.0, -8.0);\n" +
                    "    blurCoordinates[5] = vTextureCoord.xy + singleStepOffset * vec2(5.0, 8.0);\n" +
                    "    blurCoordinates[6] = vTextureCoord.xy + singleStepOffset * vec2(-5.0, 8.0);\n" +
                    "    blurCoordinates[7] = vTextureCoord.xy + singleStepOffset * vec2(-5.0, -8.0);\n" +
                    "    blurCoordinates[8] = vTextureCoord.xy + singleStepOffset * vec2(8.0, -5.0);\n" +
                    "    blurCoordinates[9] = vTextureCoord.xy + singleStepOffset * vec2(8.0, 5.0);\n" +
                    "    blurCoordinates[10] = vTextureCoord.xy + singleStepOffset * vec2(-8.0, 5.0);\n" +
                    "    blurCoordinates[11] = vTextureCoord.xy + singleStepOffset * vec2(-8.0, -5.0);\n" +
                    "    blurCoordinates[12] = vTextureCoord.xy + singleStepOffset * vec2(0.0, -6.0);\n" +
                    "    blurCoordinates[13] = vTextureCoord.xy + singleStepOffset * vec2(0.0, 6.0);\n" +
                    "    blurCoordinates[14] = vTextureCoord.xy + singleStepOffset * vec2(6.0, 0.0);\n" +
                    "    blurCoordinates[15] = vTextureCoord.xy + singleStepOffset * vec2(-6.0, 0.0);\n" +
                    "    blurCoordinates[16] = vTextureCoord.xy + singleStepOffset * vec2(-4.0, -4.0);\n" +
                    "    blurCoordinates[17] = vTextureCoord.xy + singleStepOffset * vec2(-4.0, 4.0);\n" +
                    "    blurCoordinates[18] = vTextureCoord.xy + singleStepOffset * vec2(4.0, -4.0);\n" +
                    "    blurCoordinates[19] = vTextureCoord.xy + singleStepOffset * vec2(4.0, 4.0);\n" +
                    "\n" +
                    "    // 计算平均值\n" +
                    "    vec4 currentColor = texture2D(sTexture, vTextureCoord);\n" +
                    "    vec3 rgb = currentColor.rgb;\n" +
                    "    // 计算坐标的颜色值总和 \n" +
                    "    for(int i=0 ; i < 20 ; i++){ \n" +
                    "    rgb += texture2D(sTexture, blurCoordinates[i].xy).rgb;\n" +
                    "    }\n" +
                    "    // 平均值\n" +
                    "    vec4 blur = vec4(rgb * 1.0 /21.0, currentColor.a);\n" +
                    "    // 高反差保留算法\n" +
                    "    // 原图 - 高斯模糊图\n" +
                    "    vec4 highPassColor = currentColor - blur;\n" +
                    "    // 强光处理 color = 2 * color1 * color2\n" +
                    "    //  24.0 强光程度\n" +
                    "    //clamp  获取三个参数中处于中间的那个\n" +
                    "    highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r * 24.0,0.0,1.0);\n" +
                    "    highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g * 24.0,0.0,1.0);\n" +
                    "    highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b * 24.0,0.0,1.0);\n" +
                    "    //过滤疤痕\n" +
                    "    vec4 highPassBlur = vec4(highPassColor.rgb,1.0);\n" +
                    "    // 调节蓝色通道值\n" +
                    "    float b = min(currentColor.b,blur.b);\n" +
                    "    float value = clamp((b - 0.2) * 5.0,0.0,1.0);\n" +
                    "    // 找到模糊之后RGB通道的最大值\n" +
                    "    float maxChannelColor = max(max(highPassBlur.r,highPassColor.g),highPassBlur.b);\n" +
                    "    // 磨皮实际强度\n" +
                    "    float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value *uSmoothIntensity;\n" +
                    "    // 混合 -> 磨皮\n" +
                    "    // OpenGL 内置函数 线性融合\n" +
                    "    // 公式 x * (1-a) + y.a \n" +
                    "    vec3 smoothColor = mix(currentColor.rgb,blur.rgb,currentIntensity);\n" +
                    "\n" +
                    "    // 红润\n" +
                    "    vec3 rColor = 2.0*currentColor.rgb*smoothColor + currentColor.rgb*currentColor.rgb - 2.0*currentColor.rgb*currentColor.rgb*smoothColor;\n" +
                    "    vec3 ruddyColor = mix(smoothColor, rColor, uRuddyIntensity* 0.6);\n" +
                    "\n" +
                    "    // 美白\n" +
                    "    vec3 deltaColor = ruddyColor+vec3(uWhiteIntensity * 0.15);\n" +
                    "\n" +
                    "    gl_FragColor = vec4(deltaColor,1.0);\n" +
                    "}";


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
    private static final String FRAGMENT_SHADER_EXT_FILT =
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
            "    if (vTextureCoord.x < vTextureCoord.y - 0.005) {\n" +
            "        for (i = 0; i < KERNEL_SIZE; i++) {\n" +
            "            vec4 texc = texture2D(sTexture, vTextureCoord + uTexOffset[i]);\n" +
            "            sum += texc * uKernel[i];\n" +
            "        }\n" +
            "    sum += uColorAdjust;\n" +
            "    } else if (vTextureCoord.x > vTextureCoord.y + 0.005) {\n" +
            "        sum = texture2D(sTexture, vTextureCoord);\n" +
            "    } else {\n" +
            "        sum.r = 1.0;\n" +
            "    }\n" +
            "    gl_FragColor = sum;\n" +
            "}\n";

    private ProgramType mProgramType;

    // Handles to the GL program and various components of it.
    private int mProgramHandle;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;
    private int muKernelLoc;
    private int muTexOffsetLoc;
    private int muColorAdjustLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;

    private int mTextureTarget;

    private float[] mKernel = new float[KERNEL_SIZE];
    private float[] mTexOffset;
    private float mColorAdjust;

    private int muSmoothIntensityLoc;
    private float mSmoothIntensity;
    private int muWhiteIntensityLoc;
    private float mWhiteIntensity;
    private int muRuddyIntensityLoc;
    private float mRuddyIntensity;

    /**
     * Prepares the program in the current EGL context.
     */
    public Texture2dProgram(ProgramType programType) {
        mProgramType = programType;

        switch (programType) {
            case TEXTURE_2D:
                mTextureTarget = GLES20.GL_TEXTURE_2D;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D);
                break;
            case TEXTURE_EXT:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT);
                break;
            case TEXTURE_DIV_UD:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_DIV_UD);
                break;
            case TEXTURE_SPLIT:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SPLIT);
                break;
            case TEXTURE_MOSAIC:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_MOSAIC);
                break;
            case TEXTURE_SMOOTH:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SMOOTH);
                break;
            case TEXTURE_EXT_BW:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_BW);
                break;
            case TEXTURE_EXT_FILT:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT_FILT);
                break;
            case TEXTURE_WIHITE:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_WIHITE);
                break;
            case TEXTURE_SMOOTH_SKIN:
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_SMOOTH_SKIN);
                break;
            default:
                throw new RuntimeException("Unhandled type " + programType);
        }
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }

        // get locations of attributes and uniforms

        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(maPositionLoc, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");
        muKernelLoc = GLES20.glGetUniformLocation(mProgramHandle, "uKernel");
        if (muKernelLoc < 0) {
            // no kernel in this one
            muKernelLoc = -1;
            muTexOffsetLoc = -1;
            muColorAdjustLoc = -1;
        } else {
            // has kernel, must also have tex offset and color adj
            muTexOffsetLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexOffset");
            GlUtil.checkLocation(muTexOffsetLoc, "uTexOffset");
            muColorAdjustLoc = GLES20.glGetUniformLocation(mProgramHandle, "uColorAdjust");
            GlUtil.checkLocation(muColorAdjustLoc, "uColorAdjust");

            // initialize default values
            setKernel(new float[] {0f, 0f, 0f,  0f, 1f, 0f,  0f, 0f, 0f}, 0f);
            setTexSize(256, 256);
        }

        muSmoothIntensityLoc = GLES20.glGetUniformLocation(mProgramHandle, "uSmoothIntensity");
        if (muSmoothIntensityLoc >= 0) {
            setSmoothIntensity(0.0f);
        }

        muWhiteIntensityLoc = GLES20.glGetUniformLocation(mProgramHandle, "uWhiteIntensity");
        if (muWhiteIntensityLoc >= 0) {
            setWhiteIntensity(0.0f);
        }

        muRuddyIntensityLoc = GLES20.glGetUniformLocation(mProgramHandle, "uRuddyIntensity");
        if (muRuddyIntensityLoc >= 0) {
            setRuddyIntensity(0.0f);
        }
    }

    /**
     * Releases the program.
     * <p>
     * The appropriate EGL context must be current (i.e. the one that was used to create
     * the program).
     */
    public void release() {
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }

    /**
     * Returns the program type.
     */
    public ProgramType getProgramType() {
        return mProgramType;
    }

    /**
     * Creates a texture object suitable for use with this program.
     * <p>
     * On exit, the texture will be bound.
     */
    public int createTextureObject() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GlUtil.checkGlError("glGenTextures");

        int texId = textures[0];
        GLES20.glBindTexture(mTextureTarget, texId);
        GlUtil.checkGlError("glBindTexture " + texId);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");

        return texId;
    }

    /**
     * 设置磨皮程度的参数
     * @param intensity
     */
    public void setSmoothIntensity(float intensity) {
        mSmoothIntensity = intensity;
    }

    /**
     * 设置美白程度的参数
     * @param intensity
     */
    public void setWhiteIntensity(float intensity) {
        mWhiteIntensity = intensity;
    }

    /**
     * 设置红润程度的参数
     * @param intensity
     */
    public void setRuddyIntensity(float intensity) {
        mRuddyIntensity = intensity;
    }

    /**
     * Configures the convolution filter values.
     *
     * @param values Normalized filter values; must be KERNEL_SIZE elements.
     */
    public void setKernel(float[] values, float colorAdj) {
        if (values.length != KERNEL_SIZE) {
            throw new IllegalArgumentException("Kernel size is " + values.length +
                    " vs. " + KERNEL_SIZE);
        }
        System.arraycopy(values, 0, mKernel, 0, KERNEL_SIZE);
        mColorAdjust = colorAdj;
        //Log.d(TAG, "filt kernel: " + Arrays.toString(mKernel) + ", adj=" + colorAdj);
    }

    /**
     * Sets the size of the texture.  This is used to find adjacent texels when filtering.
     */
    public void setTexSize(int width, int height) {
        float rw = 1.0f / width;
        float rh = 1.0f / height;

        // Don't need to create a new array here, but it's syntactically convenient.
        mTexOffset = new float[] {
            -rw, -rh,   0f, -rh,    rw, -rh,
            -rw, 0f,    0f, 0f,     rw, 0f,
            -rw, rh,    0f, rh,     rw, rh
        };
        //Log.d(TAG, "filt size: " + width + "x" + height + ": " + Arrays.toString(mTexOffset));
    }

    /**
     * Issues the draw call.  Does the full setup on every call.
     *
     * @param mvpMatrix The 4x4 projection matrix.
     * @param vertexBuffer Buffer with vertex position data.
     * @param firstVertex Index of first vertex to use in vertexBuffer.
     * @param vertexCount Number of vertices in vertexBuffer.
     * @param coordsPerVertex The number of coordinates per vertex (e.g. x,y is 2).
     * @param vertexStride Width, in bytes, of the position data for each vertex (often
     *        vertexCount * sizeof(float)).
     * @param texMatrix A 4x4 transformation matrix for texture coords.  (Primarily intended
     *        for use with SurfaceTexture.)
     * @param texBuffer Buffer with vertex texture data.
     * @param texStride Width, in bytes, of the texture data for each vertex.
     */
    public void draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                     int vertexCount, int coordsPerVertex, int vertexStride,
                     float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        GlUtil.checkGlError("draw start");

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mTextureTarget, textureId);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
            GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer);
            GlUtil.checkGlError("glVertexAttribPointer");

        // Populate the convolution kernel, if present.
        if (muKernelLoc >= 0) {
            GLES20.glUniform1fv(muKernelLoc, KERNEL_SIZE, mKernel, 0);
            GLES20.glUniform2fv(muTexOffsetLoc, KERNEL_SIZE, mTexOffset, 0);
            GLES20.glUniform1f(muColorAdjustLoc, mColorAdjust);
        }

        if (muSmoothIntensityLoc >= 0) {
            GLES20.glUniform1f(muSmoothIntensityLoc, mSmoothIntensity);
        }

        if (muWhiteIntensityLoc >= 0) {
            GLES20.glUniform1f(muWhiteIntensityLoc, mWhiteIntensity);
        }

        if (muRuddyIntensityLoc >= 0) {
            GLES20.glUniform1f(muRuddyIntensityLoc, mRuddyIntensity);
        }

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(mTextureTarget, 0);
        GLES20.glUseProgram(0);
    }
}
