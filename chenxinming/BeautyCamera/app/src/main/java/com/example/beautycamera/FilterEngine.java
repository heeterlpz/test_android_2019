package com.example.beautycamera;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

public class FilterEngine {
    private static FilterEngine filterEngine = null;

    private Context mContext;
    private FloatBuffer mBuffer;
    private int mOESTextureId = -1;
    private int vertexShader = -1;
    private int fragmentShader = -1;

    private int mShaderProgram = -1;

    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;

    public FilterEngine(int OESTextureId, Context context, int type) {
        mContext = context;
        mOESTextureId = OESTextureId;
        //将顶点和纹理坐标数据使用FloatBuffer来存储，防止内存回收
        mBuffer = createBuffer(vertexData);
        //加载着色器，GL_VERTEX_SHADER代表生成顶点着色器，GL_FRAGMENT_SHADER代表生成片段着色器
        vertexShader = loadShader(GL_VERTEX_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_vertex_shader));
        switch (type){
            case 0:
                fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader_original));
                break;
            case 1:
                fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader_blackandwhite));
                break;
            case 2:
                fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader_sharpen));
                break;
            case 3:
                fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader_vague));
                break;
            case 4:
                fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader_africa));
                break;
            case 5:
                fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader_beauty));
                break;
            case 6:
                fragmentShader = loadShader(GL_FRAGMENT_SHADER, Utils.readShaderFromResource(mContext, R.raw.base_fragment_shader_relief));
                break;
            default:
                break;
        }
        //将两个Shader链接至program中
        mShaderProgram = linkProgram(vertexShader, fragmentShader);
    }

    /*public static FilterEngine getInstance() {
        if (filterEngine == null) {
            synchronized (FilterEngine.class) {
                if (filterEngine == null)
                    filterEngine = new FilterEngine();
            }
        }
        return filterEngine;
    }*/

    //每行前两个值为顶点坐标，后两个为纹理坐标
    private static final float[] vertexData = {
            1f, 1f, 1f, 1f,
            -1f, 1f, 0f, 1f,
            -1f, -1f, 0f, 0f,
            1f, 1f, 1f, 1f,
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f
    };

    public static final String POSITION_ATTRIBUTE = "aPosition";
    public static final String TEXTURE_COORD_ATTRIBUTE = "aTextureCoordinate";
    public static final String TEXTURE_MATRIX_UNIFORM = "uTextureMatrix";
    public static final String TEXTURE_SAMPLER_UNIFORM = "uTextureSampler";

    //将顶点和纹理坐标数据使用FloatBuffer来存储，防止内存回收
    public FloatBuffer createBuffer(float[] vertexData) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(vertexData, 0, vertexData.length).position(0);
        return buffer;
    }


    //加载着色器，GL_VERTEX_SHADER代表生成顶点着色器，GL_FRAGMENT_SHADER代表生成片段着色器
    public int loadShader(int type, String shaderSource) {
        //创建Shader
        int shader = glCreateShader(type);
        if (shader == 0) {
            throw new RuntimeException("Create Shader Failed!" + glGetError());
        }
        //加载Shader代码
        glShaderSource(shader, shaderSource);
        //编译Shader
        glCompileShader(shader);
        return shader;
    }

    //将两个Shader链接至program中
    public int linkProgram(int verShader, int fragShader) {
        //创建program
        int program = glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Create Program Failed!" + glGetError());
        }
        //附着顶点和片段着色器
        glAttachShader(program, verShader);
        glAttachShader(program, fragShader);
        //链接program
        glLinkProgram(program);

        //告诉OpenGL ES使用此program
        glUseProgram(program);
        return program;
    }

    public void drawTexture(float[] transformMatrix) {
        //获取Shader中定义的变量在program中的位置
        aPositionLocation = glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM);

        //激活纹理单元0
        glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定外部纹理到纹理单元0
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        //将此纹理单元床位片段着色器的uTextureSampler外部纹理采样器
        glUniform1i(uTextureSamplerLocation, 0);
        //将纹理矩阵传给片段着色器
        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        //将顶点和纹理坐标传给顶点着色器
        if (mBuffer != null) {
            //顶点坐标从位置0开始读取
            mBuffer.position(0);
            //使能顶点属性
            glEnableVertexAttribArray(aPositionLocation);
            //顶点坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mBuffer);

            //纹理坐标从位置2开始读取
            mBuffer.position(2);
            glEnableVertexAttribArray(aTextureCoordLocation);
            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mBuffer);

            //绘制两个三角形（6个顶点）
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }
    }

    public int getShaderProgram() {
        return mShaderProgram;
    }

    public FloatBuffer getBuffer() {
        return mBuffer;
    }

    public int getOESTextureId() {
        return mOESTextureId;
    }

    public void setOESTextureId(int OESTextureId) {
        mOESTextureId = OESTextureId;
    }
}