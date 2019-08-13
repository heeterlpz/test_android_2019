package com.example.nyamori.mytestapplication;

import android.content.Context;

import com.example.nyamori.gles.GlUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderLoader {
    private static Context mContext;
    private static volatile ShaderLoader instance = null;

    private ShaderLoader(Context context){
        mContext=context.getApplicationContext();
    }

    public static ShaderLoader getInstance(Context context) {
        if(instance==null){
            synchronized (ShaderLoader.class){
                if(instance==null){
                    instance = new ShaderLoader(context);
                }
            }
        }
        return instance;
    }

    public static ShaderLoader getInstance(){
        if(instance!=null)return instance;
        return null;
    }

    public int loadShader(int resourceID){
        String vertexSource=readShaderFromResource(mContext,R.raw.vertex_shader);
        String fragmentSource=readShaderFromResource(mContext,resourceID);
        int program=GlUtil.createProgram(vertexSource,fragmentSource);
        if (program == 0) {
            throw new RuntimeException("Unable to create program");
        }
        return program;
    }

    public static String readShaderFromResource(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            is = context.getResources().openRawResource(resourceId);
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}
