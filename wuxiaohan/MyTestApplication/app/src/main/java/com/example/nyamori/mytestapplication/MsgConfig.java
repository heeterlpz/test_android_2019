package com.example.nyamori.mytestapplication;

public  class MsgConfig {
    public static class OPenGLMsg{
        public static final int MSG_UPDATE_IMG = 0;
        public static final int MSG_INIT_OUT = 1;
        public static final int MSG_CHANGE_TYPE=2;
    }

    public static class MsgArg{
        public static final int NO_ARG=0;
        public static final int OBSCURE_TYPE=1;
        public static final int SHARPENING_TYPE=2;
        public static final int EDGE_TYPE=3;
        public static final int EMBOSS_TYPE=4;
    }

    public static class UIMsg{
        public static final int UI_UPDATE_FPS = 0;
    }
}
