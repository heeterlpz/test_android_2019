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

    public static class UIMsg{
        public static final int UI_UPDATE_FPS = 0;
        public static final int UI_UPDATE_LIST=1;
    }

    public static class CAMERA_TYPE{
        public final static int BACK_TYPE=0;
        public final static int FRONT_TYPE=1;
    }
}
