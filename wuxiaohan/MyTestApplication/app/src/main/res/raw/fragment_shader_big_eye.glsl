precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform float level;
uniform vec2 rightEyePoint[5];
uniform vec2 leftEyePoint[5];
vec2 newPointBigEye(vec2 textureCoord, vec2 center,float radius,float intensity,float curve){
	vec2 offset = textureCoord-center;
	vec2 result = textureCoord;
	float dis=distance(textureCoord,center);
	if(dis<radius){
		float weight=dis/radius;
		weight=1.0-intensity*(1.0-pow(weight,curve));
		weight=clamp(weight,0.0,1.0);
		result=center+offset*weight;
	}
	return result;
}
void main() {
	float r=1.0/500.0;
	vec2 pointToUse=vTextureCoord;
	float leftradius1=distance(leftEyePoint[1],leftEyePoint[2]);
	float leftradius2=distance(leftEyePoint[3],leftEyePoint[4]);
	float rightradius1=distance(rightEyePoint[1],rightEyePoint[2]);
	float rightradius2=distance(rightEyePoint[3],rightEyePoint[4]);
	pointToUse=newPointBigEye(pointToUse,leftEyePoint[0],leftradius1,0.2,level);
	pointToUse=newPointBigEye(pointToUse,leftEyePoint[0],leftradius2,0.3,level);
	pointToUse=newPointBigEye(pointToUse,rightEyePoint[0],rightradius1,0.2,level);
	pointToUse=newPointBigEye(pointToUse,rightEyePoint[0],rightradius2,0.3,level);
	vec4 color=texture2D(sTexture, pointToUse);
	gl_FragColor=color;
}
