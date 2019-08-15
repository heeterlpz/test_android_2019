precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform float beta;
void main() {
	vec4 currentColor = texture2D(sTexture, vTextureCoord);
	float redCurveValue = currentColor.r;
	float greenCurveValue = currentColor.g;
	float blueCurveValue = currentColor.b;
	
	redCurveValue=log(redCurveValue*(beta-1.0)+1.0)/log(beta);
	greenCurveValue=log(greenCurveValue*(beta-1.0)+1.0)/log(beta);
	blueCurveValue=log(blueCurveValue*(beta-1.0)+1.0)/log(beta);
	
	vec4 textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
	gl_FragColor = textureColor;
	
}
