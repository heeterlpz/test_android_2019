package com.example.nyamori.mytestapplication;

public class Config {
    public static class OPenGLMsg{
        public static final int MSG_UPDATE_IMG = 0;
        public static final int MSG_INIT_OUT = 1;
        public static final int MSG_CHANGE_TYPE=2;
        public static final int MSG_ADD_FILTER=3;
        public static final int MSG_CHANGE_CAMERA=4;
    }

    public static class MsgType {
        public static final int NO_TYPE =0;
        public static final int OBSCURE_TYPE=1;
        public static final int SHARPENING_TYPE=2;
        public static final int EDGE_TYPE=3;
        public static final int EMBOSS_TYPE=4;
        public static final int BW_TYPE=5;
        public static final int MOSAIC_TYPE=6;
        public static final int SMOOTH_TYPE=7;
        public static final int BEAUTY_TYPE=8;
        public static final int WHITENING_TYPE=9;
        public static final int TEST_TYPE=100;
    }

    public static class FilterName{
        public static final String OBSCURE_TYPE="高斯模糊";
        public static final String SHARPENING_TYPE="锐化";
        public static final String EDGE_TYPE="边缘检测";
        public static final String EMBOSS_TYPE="浮雕";
        public static final String BW_TYPE="黑白";
        public static final String MOSAIC_TYPE="马赛克";
        public static final String SMOOTH_TYPE="平滑模糊";
        public static final String BEAUTY_TYPE="磨皮美颜";
        public static final String WHITENING_TYPE="美白";
        public static final String TEST_TYPE="测试";
    }

    public static class UIMsg{
        public static final int UI_UPDATE_FPS = 0;
        public static final int UI_UPDATE_LIST=1;
    }

    public static class CAMERA_TYPE{
        public final static int BACK_TYPE=0;
        public final static int FRONT_TYPE=1;
    }
}
