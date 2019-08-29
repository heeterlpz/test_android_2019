# 相机实时美颜功能
---
## 需求：
**编写apk，使用OpenGL ES方式，修改摄像头采集的视频数据，达到美颜磨皮效果，要求性能达到720P@30FPS**
## 功能描述：
* 启动摄像头预览图像
* 自由的添加滤镜：已经实现的滤镜有磨皮，美白，锐化，黑白，马赛克，模糊，浮雕，边缘检测（可以在左侧抽屉来添加滤镜）
* 滤镜管理：右侧抽屉提供滤镜管理的功能，所有滤镜按照添加先后顺序排列，长按滤镜可以删除对应滤镜。磨皮，美白，锐化滤镜支持单击设置强度。
* 实时美颜功能：实时美颜功能由磨皮，美白，锐化三个滤镜组成，可以在右侧抽屉设置强度来调节美颜效果
* 星期四, 29. 八月 2019 现在起支持Android 5.0以上启动人脸识别，使用瘦脸和眼睛放大功能
## 项目描述：
1. 没有上传face++的sdk相关文件，需要请在face++官网下载。
2. 没有上传使用的api key和secret。
3. 使用的sdk版本在开发环境中有说明
---
### 开发环境

Android studio 3.4.2
Gradle 5.1.1

MGFaceppSDK-0.5.2
MGLicenseManagerSDK-0.3.1

### 程序类图
星期四, 29. 八月 2019 
新增了Faces类存储面部信息，FaceLiftFilter和BigEyeFilter

![类图](https://b2.bmp.ovh/imgs/2019/08/3ad29139435c7c3b.png)
### /gles/
---
OpenGL ES相关的类，来自Google的grafika项目的/gles/ 文件夹
#### EglCore
---
存放egl context的类，里面存放着egl的环境建立，surface建立等操作
#### EglSurfaceBase
---
egl surface的基类，存放着surface操作
##### WindowSurface
---
继承自surface base，连接实际surface的surface，最后一步绘制output即绘制到window surface绑定的表面上
##### OffscreenSurface
---
继承自surface base，没有实际表面的surface
#### GlUtil
---
egl 工具类，编译shader并载入
### /mytestapplication/
---
#### MainActivity
---
提供界面和交互，左侧抽屉用于添加滤镜，右侧用于管理滤镜。按钮用于切换前后摄像头。
#### MyCamera
---
使用Android Camera API，适配Android5.0以下版本，用于启动相机开启预览，需要OpenGL提供的surfaceTexture作为目标。

提供的函数：

- initCamera(int width,int height)：初始化camera，主要是进行了屏幕尺寸和输出尺寸的适配，返回一个ViewPort
- setTargetSurface(SurfaceTexture surface) ：设定相机预览图像的输出目标
- changeCamera(int width,int height)：切换相机，返回一个ViewPort
- openCamera()：启动预览
- destroyCamera()：释放相机资源
#### MyCamera2
---
使用Android Camera2 API，适配Android5.0以上版本，构造器需要context作为参数，其他同Camera
#### MyOpenGL
---
运行于OpenGL线程

需要传入：

* Surface表面，用于显示渲染的图像
* UIhandler，用于和主线程交互更新fps和开启摄像头预览

存放了OpenGL ES 2.0相关的流程，例如初始化，建立OpenGL环境，更新渲染画面，滤镜操作
#### My2DFilterManager
---
滤镜管理类，需要OpenGL环境建立后才能使用

管理滤镜：

- 输入滤镜（inputfilter）:用于接受外部纹理的滤镜，纹理采样材质是samplerExternalOES，将图像输入到FBO保存
- 输出滤镜（outputfilter）：用于绘制图像到屏幕的滤镜，将FBO的图像绘制到屏幕上
- 滤镜列表（filterList）：管理用于添加效果的滤镜，读取上一个FBO的texture渲染到自己的FBO。
- 滤镜名列表（filterTypeList）：对应位置存储了filterList对应位置滤镜的类型名，会和MainActivity的滤镜列表同步用于管理。

提供滤镜操作功能：

- addFilter(int type)：添加一个对应类型的滤镜到List尾部
- deleteFilter(int position)：删除对应位置的滤镜
- changeFilter(int typeCode)：清空滤镜并按照预设定添加对应类型的滤镜
- getFilter(int position)：获取指定位置的filter
- getFilterTypeList()：获取当前滤镜列表

其他功能：

- createInputTextureObject()：创建一个扩展纹理供外部SurfaceTexture绑定，获取预览输入
- draw(int textureId)：读取外部纹理后进行绘制
- changeSize(int width, int height, int xStart, int yStart)：重新设定输出尺寸
- release() ：释放全部滤镜资源
#### MyFeameRect & Config &ShaderLoader & ViewPort
---
- MyFrameRect: 提供了矩形相关的矩阵和参数用于绘制
- Config：存放消息代码，类型代码
- SharderLoader：读取存放在/res/raw/目录下的滤镜并编译和link，需要OpenGL 环境建立后才可以使用
- ViewPort ：规定显示图像Size的类

#### /filter/
---
##### BaseFilter
filter的基类,所有filter都继承自这个基类。
绘制过程采用离屏渲染
##### 其他filter
全部继承自BaseFilter；
除去input和output，所有中间滤镜采样texture类别为sampler2D，需要上一个滤镜的texture纹理；
input filter使用samplerExternalOES，其上一个纹理由外部输入；
output filter使用sampler2D，没有自己的纹理
### 程序流程图![流程](https://b2.bmp.ovh/imgs/2019/08/db571b72d6d5c12b.png)
### 滤镜效果实现
暂时没有实现空间变化的滤镜，因此顶点着色器只是传入坐标并没有进行变换。所有的图像处理都在片着色器中。
全部shader被放置在/res/raw/文件夹下，由LoadShader类读取。
#### 1.BWFilter 黑白滤镜
黑白滤镜主要是进行了一个灰度处理，目前的参数为：R：0.3；G：0.59；B：0.11
#### 2.WhiteningFilter 美白滤镜
美白滤镜基于一个公式对r，g，b值进行单独调整。
```
redCurveValue=log(redCurveValue*(beta-1.0)+1.0)/log(beta);
greenCurveValue=log(greenCurveValue*(beta-1.0)+1.0)/log(beta);
blueCurveValue=log(blueCurveValue*(beta-1.0)+1.0)/log(beta);
```
其中beta值是控制颜色调整的参数，公式的曲线如下：
![函数图像](https://images0.cnblogs.com/i/349293/201407/142149030537494.png)
可以看到整个函数的曲线实现的效果，beta越高颜色调整越大，整个画面越白。
==需要调整的点：==
**首先颜色调整覆盖了整个图像，整个滤镜后续需要对肤色进行检测**
**其次使用公式计算不如直接将外部的查找表变换为纹理传入着色器，色调配置可以更加细化，美白效果更好**
#### 3.MosaicFilter 马赛克滤镜
马赛克滤镜就是基于马赛克的原理，对图像进行分割，然后用每个区块的中心点颜色来取代整个区域的颜色，达到马赛克的效果。
#### 4.卷积滤镜
卷积（邻域滤波）就是通过给定像素值和其周围的指定像素值来决定该像素值的颜色，每个像素值的权重形成的矩阵就是卷积核，通过不同的卷积核，可以实现比较丰富的效果。
基于这种滤波方案，使用3*3的卷积核，本项目实现了模糊（平滑模糊，高斯模糊），锐化，边缘检测，浮雕四种简单的效果。这四种效果都可以继续进行优化。当卷积核越大，对不同的处理会具有更加强烈的效果，同时会延长算法运行的时间。同时，矩阵中的各个权重值的和要为1才能保持亮度信息的不变。
#####4.1 模糊效果
模糊效果就是使用周边的像素值和本像素值混合，实现一个低通滤波的效果，去除了图片中的高频信息，同时图片也会变得模糊。本次主要实现了均值模糊和高斯模糊。
1. 均值模糊：均值模糊就是取像素的均值，其卷积核比较简单：
![](https://b2.bmp.ovh/imgs/2019/08/c5f698f84285f8bc.png)
矩阵的和为9，所以最后要将颜色值除以9，即可保持亮度恒定。这时候输出的图片的像素值就变成了其周边的均值。
2. 高斯模糊：由于各个点的权重一样，均值模糊产生的模糊效果是很不平滑的。为了比较好的效果，要对像素点的权重做出区分。根据正态分布分配权重的卷积核就是高斯模糊。卷积核如下：
![](https://b2.bmp.ovh/imgs/2019/08/1eed8fcd0095ae6e.png)
这样就能产生更加平滑的模糊矩阵。
#####4.2锐化效果
在锐化之前先说边缘，最简单的边缘检测效果，可以直接通过用自身的像素值减去周边像素值的和实现，矩阵总体和为0，当像素信息发生变换的时候，像素差值的和就不是0，边缘信息就被保留了下来。
那么在边缘信息上加入本身的信息，就实现了锐化效果。锐化效果的强度取决于边缘信息和本身信息的比值，本项目通过这个比值控制锐化强度。卷积核如下：
![](https://b2.bmp.ovh/imgs/2019/08/1cb39fee350f0095.png)
其中的a值是本身信息的强度，亮度加权为1/a。
##### 4.3 边缘检测
在锐化效果中，就已经声明了最简单的边缘检测，但是为了实现比较好的边缘效果，同时又做到比较快速。本项目使用sobel算子进行边缘检测。sobel算子分为横向和纵向，分别对图像进行卷积后相加再亮度加权（除以2），即可获得比较大略的边缘信息。
*x方向卷积核*
![](https://b2.bmp.ovh/imgs/2019/08/cb0405ec64f425ac.png)
*y方向卷积核*
![](https://b2.bmp.ovh/imgs/2019/08/121a13e657af65d0.png)
##### 4.4 浮雕效果
主要是突出相邻像素之差，形成浮雕效果
卷积核如下：
![](https://b2.bmp.ovh/imgs/2019/08/4371d58f80273357.png)
####5.磨皮效果
首先，列出磨皮算法的基本思路：
**新图像=模糊+锐化*系数**
其中系数控制控制边缘勾勒的强度，模糊强度控制了磨皮效果。
根据这个思路，采样的点决定了效果的强烈。根据网络，选取了需要采样的20个周边点，如下：
```
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
```
加上本身21个点进行模糊处理，再锐化，就可以实现基本磨皮。
但是效果并不够优异，为了优化效果，引入高反差保留。
高反差保留就是保留图像中的高反差部分，即高通滤波。因此与模糊的低通滤波可以对应。因此可以得到高反差保留的获取公式：
**高反差保留=原图-模糊+128**
使用高反差保留代替锐化，可以得到新的公式：
**新图像=模糊+高反差保留*系数**
再进行处理之后，会发现边缘信息还是比较强烈。
考虑到系数太低效果比较差，考虑改变思路。
既然高反差保留基本保存了边缘信息，那么实际上就是说，想被磨皮抹去的细节也保留在里面了。
那么，用原图减去高反差保留，就是我们想要的皮肤部分，并且根据高反差保留的获取公式，这部分值是模糊后的值。推得新公式：
**新图像=原图+（原图-高反差保留）*系数**
到这里为止，基本的磨皮效果就实现了，由于新图像是原图+模糊*系数，将系数控制的适当低，就可以实现磨皮+美白两种效果了。
接下来进行优化，首先采样颜色部分，由于皮肤偏红，导致r通道亮度高，细节保留就少。
所以考虑使用g和b两个可能保留了更多细节的通道代替每个通道单独滤波，同时减小了计算量。
这里单独选取绿色通道滤波，先对绿色进行模糊再得到高反差保留。同时为了模糊效果比较好，对各个点赋予了权重，见如下代码：
```
	vec4 currentColor = texture2D(sTexture, vTextureCoord);
	float greenColor = currentColor.g*20.0;
	for (int i =0; i <20; i++) {
		//采集20个点 的像素值 相加 得到总和
		if(i<12)greenColor += texture2D(sTexture, blurCoordinates[i].xy).g;
		else greenColor+=texture2D(sTexture, blurCoordinates[i].xy).g*2.0;
	}
	greenColor=greenColor/48.0;
	float highPassColor = currentColor.g - greenColor+0.5;
```
这样就获取了高反差保留，这里可以输出细节观察，发现细节比较不明显，我们需要的模糊区域和不需要的边缘线差距不大。
对这个数据采用滤色和正片叠底处理。
滤色和正片叠底的描述来自PS，如下：
正片叠底模式：查看对应像素的颜色信息，并将基色与混合色复合，结果色总是较暗的颜色，任何颜色与黑色复合产生黑色，任何颜色与白色复合保持不变，当用黑色或白色以外的颜色绘画时，绘画工具绘制的连续描边产生逐渐变暗的颜色。
“滤色”模式：查看每个通道中的颜色信息，并将混合色的互补色与基色复合，结果色总是较亮的颜色。任何颜色与白色复合产生白色，任何颜色与黑色复合保持不变。
对应公式为： 正片叠底：f = a * b;滤色：f = 1 - (1-a)*(1-b) 。公式中的颜色范围是0-1 。
考虑到现在的高反差保留中，我们需要的信息偏暗，我们不需要的信息偏亮，这是由于获取高反差时手动+0.5造成的。于是对于颜色低于0.5的部分，我们实行正片叠底，对颜色高于0.5的部分，我们实行滤色。如下：
```
float hardLight(float color)
{
	if(color <= 0.5)
	color = color * color * 2.0;
	else
	color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);
	return color;
}
```
这个处理过程可以多重复几次，使得暗部更暗，亮部更亮，我们不需要的细节更明显。
接下来用原图减去处理后的高反差保留就可以获取模糊部分。为了新图像效果足够好，要确定合适的系数。
首先，由于高反差被放大，原图噪声变成暗部，先使用灰度图进行正片叠底操作使得整个表面变得比较平滑，同时变暗一次。再乘上系数0.1使得亮度提升合适。
将这个值和原色彩相加获得了新图像smoothColor。
为了使得图片更自然，使用内建的mix函数再次混合两个图像，公式：x⋅(1−a)+y⋅a。
这时候输出，图像就比较自然了。代码如下：
```
	for(int i=0;i<5;i++)highPassColor=hardLight(highPassColor);
	float lumance=dot(currentColor.rgb,W);
	float alpha = pow(lumance, level);
	vec3 smoothColor = currentColor.rgb + (currentColor.rgb-vec3(highPassColor))*alpha*0.1;
	gl_FragColor = vec4(mix(smoothColor.rgb,max(smoothColor.rgb,currentColor.rgb), alpha), 1.0);
```
##### 可能的改进
基于以上方法实现的美颜效果对于边缘信息的保留实际上还是不够好。如果能够使用保边滤波（例如双边滤波)的方式进行磨皮，那么就可以保留边缘的同时，对内部进行处理，效果会更加自然。由于算法计算量和处理比较复杂，根据网络上的算法进行实现后没有获得比较好的效率，也没有能力进行优化，本次没有使用双边滤波。
#### 局部图像变形完成瘦脸和大眼
星期四, 29. 八月 2019 
一般而言，对局部的图像的操作进行变形会出现边界明显不自然，像素信息被覆盖丢失等问题。这里采用局部图像变换，主要是对像素的位置进行偏移，通过改变指定范围内的像素位置但是不改变数量的做法实现图像变形。这样处理出的图像就不会有明显边界，比较自然。
简单来说，就是要对给定坐标位置计算出合适的新位置，通过不同的映射关系实现缩放，平移等效果。
在实际应用当中，就是根据给出的坐标值，使用逆变换计算出原坐标值，采样原坐标值的颜色来实现变形。
本次使用的映射关系参考自论文：Interactive Image Warping。
##### 局部图像缩放完成大眼
局部图像变形的原理已经在上面说过了，缩放使用的核心映射关系代码如下：（可以在对应的glsl文件里看到整个过程）
```
		float weight=dis/radius;
		weight=1.0-intensity*(1.0-pow(weight,curve));
		weight=clamp(weight,0.0,1.0);   //到这里是权重计算过程
		result=center+offset*weight;  //实际坐标映射
```
上面的代码段中，dis是选取点到圆心的距离，radius是整体的半径；intensity是一个强度参数，curve是放大参数，调整这两个参数可以调整放大效果。如果要实际应用于放大眼睛推荐intensity设置在0.2~0.3之间。
本次实现固定intensity，修改curve来完成不同程度的放大。
下面解释这段的原理：将权重计算最简化，使intensity直接为1.0，可以看到权重计算变成了一个pow计算，以curve=2.0 为例

![curve = 2.0 时](https://ftp.bmp.ovh/imgs/2019/08/8ca3fdf301a17244.png)

从图中可以看到，在0.5处，实际使用平方计算取到的值是0.25，意味着基于此公式本来应该在0.25位置的像素被放在了0.5处，实现了放大效果
同时由于x一直是均匀分布的没有发生改变，整体的像素值没有发生改变，只是对中间的像素进行了位移导致了密度变化，实际上就可以实现放大效果。

但是实际用于眼睛放大并不能简单使得intensity为1.0，curve为2.0，需要不断调节。本次放大也没有单纯的进行单次放大，而是进行了两次以瞳孔为圆心，不同半径的放大。第一次放大以眼睛横向的距离作为半径，第二次放大以眼睛的纵向距离作为半径，两次的intensity为0.2,0.3；这是一个比较粗略的参数，效果比较差。
如果要实现更好的效果，建议更多次放大，不仅使用五个点，而使用九个点来对眼睛进行描述和放大。

##### 局部图像平移完成瘦脸
首先给出使用的映射关系，公式来自论文。
![变换公式](https://ftp.bmp.ovh/imgs/2019/08/4dbf8fd9ee99d37c.png)
这个公式已经是一个逆变换之后的公式了，x是需要的坐标，u是实际采样的坐标值。c是原本的区域圆心，r是半径，m是目标点。整个公式同样是以圆为区域进行的变换。
基于这个公式代码如下：
```
		vec2 direction =targetPosition - originPosition;    //偏移方向
		
		float length=distance(targetPosition,originPosition);
		float dradius=radius*radius;
		float ddmc = length*length;
		float ddis = dis*dis;
		
		float infect = (dradius-ddis)/(dradius-ddis+ddmc);   //计算权重
		infect=infect*infect;
		
		offset =infect*direction;
		result = textureCoord - ofvec2 direction =targetPosition - originPosition;
```
在本次实现中，基于上述代码，使用半径作为可变参数来完成图像平移瘦脸。一共使用了脸部左右各八个采样点。
以左边为例：半径是对应的右边的采样点的距离*系数，圆心是采样点自己，目标是右边对应的采样点。
系数组成：程度系数*距离补偿，程度由外部控制，补偿是固定值，最小补偿为1.00，最大补偿为1.21，间距0.03；
补偿值主要用于对靠近下巴的点半径结果过小导致效果差进行效果增强，使得瘦脸后的脸部脸型更加好看。补偿系数**并没有进行科学计算只是随意选取了线性增加的函数**，可以选取更合理的曲线来对半径的计算进行补偿，或者选取更合理的半径计算方法。
右边的计算与左边相同。
### 关键过程
#### 离屏渲染
---
OpenGL ES允许使用帧缓存（Frame Buffer Object）使得渲染后的输出不只是可以输出到屏幕上，还可以输出到帧缓存当中，从而实现离屏渲染。

在camera的预览当中使用帧缓存，可以成功将整个绘制过程转换。如下图：
![结构对比](https://b2.bmp.ovh/imgs/2019/08/12916fe166f6dc10.png)

因此，选择使用FBO之后，我们就可以拆解整个绘制过程，先将预览图片存储到内部，经过渲染之后再输出。
这时纹理也发生了从OES Texture到2D Texture的改变，于是使用FBO之后的结构也使得拍照的结构容易输出处理后的图片。

##### 本次实现：
本项目基于帧缓存，给每一个Filter对象赋予了帧缓存并且独立实现draw方法，于是绘制过程就是获取上一个有图像的texture作为输入参数调用filter的draw方法绘制一次图像，当绘制完成之后再输出自己的纹理作为下一次绘制的输入。基于这样的结构，可以在输入图像和输出图像之间添加任意个滤镜（直到影响了性能为止），于是实际着色器的功能也可以变得专一且简单，可以在有需要的时候直接组合简单的滤镜形成更加复杂的效果，无需重新编写滤镜；而实际影响性能的是着色器内部的算法，多次渲染和单次渲染的达成一个效果的实际时间差影响是比较小的。

本次实现的缺点在于每一个Filter都具有一个FBO，意味着对每一个Filter，都会实际在显存空间里开辟出空间来存储图像，这就使得处理一次图像实际使用了==Filter个数+1== 的图片内存空间，对于性能不足的手机来说负担比较大。

优化方向：将FBO对象从Filter的管理当中脱离出来作为Filter的draw方法的输入，无论使用多少滤镜，始终只有窗口系统的FBO和中间离屏的一个FBO被使用，使得内存空间的占用被降低。如果要实现这样的优化，在本项目中需要将FBO的管理移交给My2DProgramManager类，且将FBO对象作为draw方法的输入参数。

#### 美颜实现
---

基于离屏渲染的结构，本次的实时美颜功能由三个滤镜共同组成，每个滤镜单独持有一个可以调节的参数。
这三个滤镜分别实现的功能是：

* 磨皮：由于皮肤偏红润，因此RGB色中红色附带的信息可能会比较少，取绿色进行模糊处理后和本身的颜色使用mix()混合,实现磨皮效果。可以调节磨皮程度。
* 美白：基于公式对颜色上调，使得整个画面变白，实现美白效果。可以调节颜色值的上调比例。
* 锐化：由于进行了模糊处理并且并不是能够保边的模糊，对图像进行低程度的锐化可以使得图片更清晰。可以调节锐化程度。

#### 瘦脸和大眼
---
星期四, 29. 八月 2019
基于face++提供的第三方离线人脸识别sdk提供的81个关键点，可以通过局部图像缩放和局部图像平移来实现大眼和瘦脸。
81个关键点的具体位置可以在face++的官网查询。
本次使用了脸部周边的16个关键点和两眼的一共10个关键点。
局部图像变形的原理已经在上文说明。

##界面
主界面
![主界面](https://b2.bmp.ovh/imgs/2019/08/a0f599704300bf23.png)
左侧抽屉
![左侧抽屉](https://b2.bmp.ovh/imgs/2019/08/e351a2c7a3bf61d2.png)
右侧抽屉
![右侧抽屉](https://b2.bmp.ovh/imgs/2019/08/02e2db5927cbe201.png)
滤镜列表（开启美颜）
![滤镜列表](https://b2.bmp.ovh/imgs/2019/08/0f2cf1ba86cd5943.png)
设置对话框（以磨皮为例）
![设置对话框](https://b2.bmp.ovh/imgs/2019/08/0c6bcda14aaf8dff.png  )