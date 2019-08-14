precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
void main() {
    vec2 uv  = vTextureCoord.xy;
    float dx = 0.02;
    float dy = 0.02;
    vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));
    vec3 tc = texture2D(sTexture, coord).xyz;
    gl_FragColor = vec4(tc, 1.0);
}