package com.example.nyamori.mytestapplication;

public  class MsgConfig {
    public static class OPenGLMsg{
        public static final int MSG_UPDATE_IMG = 0;
        public static final int MSG_INIT_OUT = 1;
        public static final int MSG_CHANGE_TYPE=2;
        public static final int MSG_ADD_FILTER=3;
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
    }

    public static class UIMsg{
        public static final int UI_UPDATE_FPS = 0;
    }
}
