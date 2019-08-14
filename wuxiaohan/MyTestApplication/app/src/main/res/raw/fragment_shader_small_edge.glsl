#define KERNEL_SIZE 9
precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform vec2 uTexOffset[KERNEL_SIZE];
void main() {
	//卷积内核中各个位置的值
	float kernelValue0 = -1.0; float kernelValue1 = 0.0; float kernelValue2 = 1.0;
	float kernelValue3 = -2.0; float kernelValue4 = 0.0; float kernelValue5 = 2.0;
	float kernelValue6 = -1.0; float kernelValue7 = 0.0; float kernelValue8 = 1.0;
	vec4 sum;//最终的颜色和
	//获取卷积内核中各个元素对应像素的颜色值
	vec4 cTemp0,cTemp1,cTemp2,cTemp3,cTemp4,cTemp5,cTemp6,cTemp7,cTemp8;
	cTemp0=texture2D(sTexture, vTextureCoord.st + uTexOffset[0].xy);
	cTemp1=texture2D(sTexture, vTextureCoord.st + uTexOffset[1].xy);
	cTemp2=texture2D(sTexture, vTextureCoord.st + uTexOffset[2].xy);
	cTemp3=texture2D(sTexture, vTextureCoord.st + uTexOffset[3].xy);
	cTemp4=texture2D(sTexture, vTextureCoord.st + uTexOffset[4].xy);
	cTemp5=texture2D(sTexture, vTextureCoord.st + uTexOffset[5].xy);
	cTemp6=texture2D(sTexture, vTextureCoord.st + uTexOffset[6].xy);
	cTemp7=texture2D(sTexture, vTextureCoord.st + uTexOffset[7].xy);
	cTemp8=texture2D(sTexture, vTextureCoord.st + uTexOffset[8].xy);
	//颜色求和
	sum =kernelValue0*cTemp0+kernelValue1*cTemp1+kernelValue2*cTemp2
	+kernelValue3*cTemp3+kernelValue4*cTemp4+kernelValue5*cTemp5
	+kernelValue6*cTemp6+kernelValue7*cTemp7+kernelValue8*cTemp8;
	
	gl_FragColor=vec4(sum.rgb,1.0);
}
