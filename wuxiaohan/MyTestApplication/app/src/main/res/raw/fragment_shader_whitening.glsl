precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform float beta;
const vec3 CbC = vec3(0.5,-0.4187,-0.0813);
const vec3 CrC = vec3(-0.1687,-0.3313,0.5);
void main() {
	vec4 currentColor = texture2D(sTexture, vTextureCoord);
	float redCurveValue = currentColor.r;
	float greenCurveValue = currentColor.g;
	float blueCurveValue = currentColor.b;
	//用颜色空间判断效果非常生硬，可能还是查找表自然，或者加一个高斯模糊
//	float Cb=dot(currentColor.rgb*255.0,CbC)+128.0;
//	float Cr=dot(currentColor.rgb*255.0,CrC)+128.0;
//
//	if((Cb>=75.0&&Cb<=130.0)&&(Cr>=130.0&&Cr<=175.0)){
//		gl_FragColor = currentColor;
//	}else{
//		redCurveValue=log(redCurveValue*(beta-1.0)+1.0)/log(beta);
//		greenCurveValue=log(greenCurveValue*(beta-1.0)+1.0)/log(beta);
//		blueCurveValue=log(blueCurveValue*(beta-1.0)+1.0)/log(beta);
//
//		vec4 textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
//		gl_FragColor = textureColor;
//	}
	
	redCurveValue=log(redCurveValue*(beta-1.0)+1.0)/log(beta);
	greenCurveValue=log(greenCurveValue*(beta-1.0)+1.0)/log(beta);
	blueCurveValue=log(blueCurveValue*(beta-1.0)+1.0)/log(beta);
	
	vec4 textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
	gl_FragColor = textureColor;
}
