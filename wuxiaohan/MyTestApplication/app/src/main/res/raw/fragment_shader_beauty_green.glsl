precision mediump float;
uniform sampler2D sTexture;
varying vec2 vTextureCoord;
uniform float level;
const highp vec3 W = vec3(0.299,0.587,0.114);
float hardLight(float color)
{
	if(color <= 0.5)
	color = color * color * 2.0;
	else
	color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);
	return color;
}

void main()
{
	vec2 blurCoordinates[20];
	vec2 singleStepOffset =vec2(1.0/1536.0,1.0/1536.0);
	//取当前数据周边的20个点的颜色通道的值，进行相加然后得出平均值
	blurCoordinates[0] = vTextureCoord.xy + singleStepOffset *vec2(0.0, -10.0);
	blurCoordinates[1] = vTextureCoord.xy + singleStepOffset *vec2(0.0, 10.0);
	blurCoordinates[2] = vTextureCoord.xy + singleStepOffset *vec2(-10.0, 0.0);
	blurCoordinates[3] = vTextureCoord.xy + singleStepOffset *vec2(10.0, 0.0);
	
	blurCoordinates[4] = vTextureCoord.xy + singleStepOffset *vec2(5.0, -8.0);
	blurCoordinates[5] = vTextureCoord.xy + singleStepOffset *vec2(5.0, 8.0);
	blurCoordinates[6] = vTextureCoord.xy + singleStepOffset *vec2(-5.0, 8.0);
	blurCoordinates[7] = vTextureCoord.xy + singleStepOffset *vec2(-5.0, -8.0);
	
	blurCoordinates[8] = vTextureCoord.xy + singleStepOffset *vec2(8.0, -5.0);
	blurCoordinates[9] = vTextureCoord.xy + singleStepOffset *vec2(8.0, 5.0);
	blurCoordinates[10] = vTextureCoord.xy + singleStepOffset *vec2(-8.0, 5.0);
	blurCoordinates[11] = vTextureCoord.xy + singleStepOffset *vec2(-8.0, -5.0);
	
	blurCoordinates[12] = vTextureCoord.xy + singleStepOffset *vec2(0.0, -6.0);
	blurCoordinates[13] = vTextureCoord.xy + singleStepOffset *vec2(0.0, 6.0);
	blurCoordinates[14] = vTextureCoord.xy + singleStepOffset *vec2(6.0, 0.0);
	blurCoordinates[15] = vTextureCoord.xy + singleStepOffset *vec2(-6.0, 0.0);
	
	blurCoordinates[16] = vTextureCoord.xy + singleStepOffset *vec2(-4.0, -4.0);
	blurCoordinates[17] = vTextureCoord.xy + singleStepOffset *vec2(-4.0, 4.0);
	blurCoordinates[18] = vTextureCoord.xy + singleStepOffset *vec2(4.0, -4.0);
	blurCoordinates[19] = vTextureCoord.xy + singleStepOffset *vec2(4.0, 4.0);
	
	vec4 currentColor = texture2D(sTexture, vTextureCoord);
	float greenColor = currentColor.g*20.0;

	for (int i =0; i <20; i++) {
		//采集20个点 的像素值 相加 得到总和
		if(i<12)greenColor += texture2D(sTexture, blurCoordinates[i].xy).g;
		else greenColor+=texture2D(sTexture, blurCoordinates[i].xy).g*2.0;
	}
	greenColor=greenColor/48.0;
	float highPassColor = currentColor.g - greenColor+0.5;
	for(int i=0;i<5;i++)highPassColor=hardLight(highPassColor);
	float lumance=dot(currentColor.rgb,W);
	float alpha = pow(lumance, level);
	vec3 smoothColor = currentColor.rgb + (currentColor.rgb-vec3(highPassColor))*alpha*0.1;
	gl_FragColor = vec4(mix(smoothColor.rgb,max(smoothColor.rgb,currentColor.rgb), alpha), 1.0);

}
