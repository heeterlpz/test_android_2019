precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
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
	gl_FragColor=color;
}
