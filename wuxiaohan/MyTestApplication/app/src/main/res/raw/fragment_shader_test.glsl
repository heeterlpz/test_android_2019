precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform float left;
uniform float top;
uniform float right;
uniform float bottom;
uniform float level;
uniform vec2 edgePoint[19];
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
	pointToUse=newPointBigEye(pointToUse,leftEyePoint[0],leftradius1,0.2,3.0);
	pointToUse=newPointBigEye(pointToUse,leftEyePoint[0],leftradius2,0.3,3.0);
	pointToUse=newPointBigEye(pointToUse,rightEyePoint[0],rightradius1,0.2,3.0);
	pointToUse=newPointBigEye(pointToUse,rightEyePoint[0],rightradius2,0.3,3.0);
	vec4 color=texture2D(sTexture, pointToUse);
	if(vTextureCoord.x>left-r&&vTextureCoord.x<left+r
	&&vTextureCoord.y>top&&vTextureCoord.y<bottom)color=vec4(1.0);
	if(vTextureCoord.x>right-r&&vTextureCoord.x<right+r
	&&vTextureCoord.y>top&&vTextureCoord.y<bottom)color=vec4(1.0);
	if(vTextureCoord.y>top-r&&vTextureCoord.y<top+r
	&&vTextureCoord.x>left&&vTextureCoord.x<right)color=vec4(1.0);
	if(vTextureCoord.y>bottom-r&&vTextureCoord.y<bottom+r
	&&vTextureCoord.x>left&&vTextureCoord.x<right)color=vec4(1.0);
	for(int i=0;i<19;i++){
		if(i<5){
			if(distance(vTextureCoord,rightEyePoint[i])<r)color=vec4(1.0,1.0,0.0,1.0);
			if(distance(vTextureCoord,leftEyePoint[i])<r)color=vec4(1.0,1.0,0.0,1.0);
		}
		if(distance(vTextureCoord,edgePoint[i])<r){
			color=vec4(1.0,0.0,0.0,1.0);
		}
	}
	gl_FragColor=color;
}
