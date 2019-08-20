#相机实时美颜功能
---
##需求：
**编写apk，使用OpenGL ES方式，修改摄像头采集的视频数据，达到美颜磨皮效果，要求性能达到720P@30FPS**
##功能描述：
* 启动摄像头预览图像
* 自由的添加滤镜：已经实现的滤镜有磨皮，美白，锐化，黑白，马赛克，模糊，浮雕，边缘检测（可以在左侧抽屉来添加滤镜）
* 滤镜管理：右侧抽屉提供滤镜管理的功能，所有滤镜按照添加先后顺序排列，长按滤镜可以删除对应滤镜。磨皮，美白，锐化滤镜支持单击设置强度。
* 实时美颜功能：实时美颜功能由磨皮，美白，锐化三个滤镜组成，可以在右侧抽屉设置强度来调节美颜效果
##项目描述：
---
###开发环境

Android studio 3.4.2
Gradle 5.1.1

###程序类图
![类图](https://b2.bmp.ovh/imgs/2019/08/3ad29139435c7c3b.png)
###/gles/
---
OpenGL ES相关的类，来自Google的grafika项目的/gles/ 文件夹
####EglCore
---
存放egl context的类，里面存放着egl的环境建立，surface建立等操作
####EglSurfaceBase
---
egl surface的基类，存放着surface操作
#####WindowSurface
---
继承自surface base，连接实际surface的surface，最后一步绘制output即绘制到window surface绑定的表面上
#####OffscreenSurface
---
继承自surface base，没有实际表面的surface
####GlUtil
---
egl 工具类，编译shader并载入
###/mytestapplication/
---
####MainActivity
---
提供界面和交互，左侧抽屉用于添加滤镜，右侧用于管理滤镜。按钮用于切换前后摄像头。
####MyCamera
---
使用Android Camera API，适配Android5.0以下版本，用于启动相机开启预览，需要OpenGL提供的surfaceTexture作为目标。

提供的函数：

- initCamera(int width,int height)：初始化camera，主要是进行了屏幕尺寸和输出尺寸的适配，返回一个ViewPort
- setTargetSurface(SurfaceTexture surface) ：设定相机预览图像的输出目标
- changeCamera(int width,int height)：切换相机，返回一个ViewPort
- openCamera()：启动预览
- destroyCamera()：释放相机资源
####MyCamera2
---
使用Android Camera2 API，适配Android5.0以上版本，构造器需要context作为参数，其他同Camera
####MyOpenGL
---
运行于OpenGL线程

需要传入：

* Surface表面，用于显示渲染的图像
* UIhandler，用于和主线程交互更新fps和开启摄像头预览

存放了OpenGL ES 2.0相关的流程，例如初始化，建立OpenGL环境，更新渲染画面，滤镜操作
####My2DFilterManager
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
####MyFeameRect & Config &ShaderLoader & ViewPort
---
- MyFrameRect: 提供了矩形相关的矩阵和参数用于绘制
- Config：存放消息代码，类型代码
- SharderLoader：读取存放在/res/raw/目录下的滤镜并编译和link，需要OpenGL 环境建立后才可以使用
- ViewPort ：规定显示图像Size的类

####/filter/
---
#####BaseFilter
filter的基类,所有filter都继承自这个基类。
绘制过程采用离屏渲染
#####其他filter
全部继承自BaseFilter；
除去input和output，所有中间滤镜采样texture类别为sampler2D，需要上一个滤镜的texture纹理；
input filter使用samplerExternalOES，其上一个纹理由外部输入；
output filter使用sampler2D，没有自己的纹理

###程序流程图![流程](https://b2.bmp.ovh/imgs/2019/08/db571b72d6d5c12b.png)

###关键过程
####离屏渲染
---
OpenGL ES允许使用帧缓存（Frame Buffer Object）使得渲染后的输出不只是可以输出到屏幕上，还可以输出到帧缓存当中，从而实现离屏渲染。

在camera的预览当中使用帧缓存，可以成功将整个绘制过程转换。如下图：
![结构对比](https://b2.bmp.ovh/imgs/2019/08/12916fe166f6dc10.png)

因此，选择使用FBO之后，我们就可以拆解整个绘制过程，先将预览图片存储到内部，经过渲染之后再输出。
这时纹理也发生了从OES Texture到2D Texture的改变，于是使用FBO之后的结构也使得拍照的结构容易输出处理后的图片。

#####本次实现：
本项目基于帧缓存，给每一个Filter对象赋予了帧缓存并且独立实现draw方法，于是绘制过程就是获取上一个有图像的texture作为输入参数调用filter的draw方法绘制一次图像，当绘制完成之后再输出自己的纹理作为下一次绘制的输入。基于这样的结构，可以在输入图像和输出图像之间添加任意个滤镜（直到影响了性能为止），于是实际着色器的功能也可以变得专一且简单，可以在有需要的时候直接组合简单的滤镜形成更加复杂的效果，无需重新编写滤镜；而实际影响性能的是着色器内部的算法，多次渲染和单次渲染的达成一个效果的实际时间差影响是比较小的。

本次实现的缺点在于每一个Filter都具有一个FBO，意味着对每一个Filter，都会实际在显存空间里开辟出空间来存储图像，这就使得处理一次图像实际使用了==Filter个数+1==的图片内存空间，对于性能不足的手机来说负担比较大。

优化方向：将FBO对象从Filter的管理当中脱离出来作为Filter的draw方法的输入，无论使用多少滤镜，始终只有窗口系统的FBO和中间离屏的一个FBO被使用，使得内存空间的占用被降低。如果要实现这样的优化，在本项目中需要将FBO的管理移交给My2DProgramManager类，且将FBO对象作为draw方法的输入参数。

####美颜实现
---
基于离屏渲染的结构，本次的实时美颜功能由三个滤镜共同组成，每个滤镜单独持有一个可以调节的参数。
这三个滤镜分别实现的功能是：

* 磨皮：由于皮肤偏红润，因此RGB色中红色附带的信息可能会比较少，取绿色进行模糊处理后和本身的颜色使用mix()混合,实现磨皮效果。可以调节磨皮程度。
* 美白：基于公式对颜色上调，使得整个画面变白，实现美白效果。可以调节颜色值的上调比例。
* 锐化：由于进行了模糊处理并且并不是能够保边的模糊，对图像进行低程度的锐化可以使得图片更清晰。可以调节锐化程度。

