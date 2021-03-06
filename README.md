身份证识别仪客户端
====================

该项目提供了一种通用型的身份证识别客户端的企业一体化解决方案，实现了身份证识别业务和企业本身业务的分离，使得企业的业务规则可以自由变换，而不需要对客户端本身进行更改，节省了企业开发成本。


基本架构
* 客户端包括3个进程：
  1 delphi载入进程
  2 客户端主进程
  3 身份证识别进程。
  4 IC卡识别进程。
  
  由于java虚拟机初始化需要较长的时间，在载入的时候，用户可能怀疑程序没有启动成功，因而增加delphi载入进程，提供一个loading动画，使得用户可以耐心等待程序初始化完成；
  当主进程初始化完毕后，会自动关闭掉delphi载入进程。
  
* 主进程开启6个线程：
  1 启动身份证进程。如果进程挂掉需要重新启动
  2 身份证进程正常输出的读取
  3 身份证进程异常输出的读取；
  4 启动IC卡进程。如果该进程挂掉会重新启动它
  5 IC卡进程正常输出的读取
  6 IC卡进程异常输出的读取；

* 主进程启动一个浏览器，并调用js结果将身份证识别程序的输出结果提供给浏览器。

* 如果符合某种特定的网址规则，主进程会控制新启动一个浏览器窗口，在第二屏打开该浏览器，并提供控制鼠标移动的js接口。

* 客户端主界面使用swt实现，浏览器使用DJNativeSwing实现。
