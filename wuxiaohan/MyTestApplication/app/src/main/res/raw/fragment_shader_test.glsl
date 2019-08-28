precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform float left;
uniform float top;
uniform float right;
uniform float bottom;
uniform float level;
uniform vec2 edgePoint[19];
vec2 newPointWarp(vec2 textureCoord, vec2 originPosition, vec2 targetPosition, float radius)
{
	vec2 offset = vec2(0.0);
	vec2 result = textureCoord;
	float dis=distance(textureCoord,originPosition);
	if(dis<radius){
		vec2 direction =targetPosition - originPosition;
		
		float length=distance(targetPosition,originPosition);
		float dradius=radius*radius;
		float ddmc = length*length;
		float ddis = dis*dis;
		
		float infect = (dradius-ddis)/(dradius-ddis+ddmc);
		infect=infect*infect;
		
		offset =infect*direction;
		result = textureCoord - offset;
	}
	return result;
}
void main() {
	float step= 1.0/1000.0;
	float r=1.0/250.0;
	vec2 pointToUse=vTextureCoord;
	float radius;
	for(int i=3;i<19;i++){
		if(i<11){
			radius=distance(edgePoint[i],edgePoint[i+8])*level*(0.97+0.03*float(i));
			pointToUse=newPointWarp(pointToUse,edgePoint[i],edgePoint[i+8],radius);
		}else{
			radius=distance(edgePoint[i],edgePoint[i-8])*level*(0.97+0.03*float(i-8));
			pointToUse=newPointWarp(pointToUse,edgePoint[i],edgePoint[i-8],radius);
		}
	}
	vec4 color=texture2D(sTexture, pointToUse);
	if(vTextureCoord.x>left-step&&vTextureCoord.x<left+step
	&&vTextureCoord.y>top&&vTextureCoord.y<bottom)color=vec4(1.0);
	if(vTextureCoord.x>right-step&&vTextureCoord.x<right+step
	&&vTextureCoord.y>top&&vTextureCoord.y<bottom)color=vec4(1.0);
	if(vTextureCoord.y>top-step&&vTextureCoord.y<top+step
	&&vTextureCoord.x>left&&vTextureCoord.x<right)color=vec4(1.0);
	if(vTextureCoord.y>bottom-step&&vTextureCoord.y<bottom+step
	&&vTextureCoord.x>left&&vTextureCoord.x<right)color=vec4(1.0);
	for(int i=0;i<19;i++){
		if(distance(vTextureCoord,edgePoint[i])<r){
			color=vec4(1.0,0.0,0.0,1.0);
		}
	}
	gl_FragColor=color;
}
