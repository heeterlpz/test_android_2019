#extension GL_OES_EGL_image_external : require
    precision mediump float;
    uniform samplerExternalOES uTextureSampler;
    varying vec2 vTextureCoord;
    void main()
    {
        vec2 uv = vTextureCoord;
        if (uv.x <= 1.0/3.0) {
            uv.x = uv.x * 3.0;
        } else {
            uv.x = (uv.x - 1.0/3.0) * 3.0;
        }
        if (uv.y <= 1.0/3.0) {
            uv.y = uv.y * 3.0;
        } else {
            uv.y = (uv.y - 1.0/3.0) * 3.0;
        }
        gl_FragColor = texture2D(uTextureSampler, fract(uv));
    }