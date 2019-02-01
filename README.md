# Thallo
### 1. Thallo是什么？

Thallo是可以使yarn启停Docker容器的Yarn Application。它有以下特点：

+ 可以使yarn调度Docker
+ 可以动态的启停Docker容器
+ 基于Vert.X的异步非阻塞Web服务
+ 提供监控页面，对发布的容器进行管理

### 2. Thallo架构

+ 整体结构

### ![archice](./doc/archice.png)



Thallo运行于Yarn之上，所有它是典型的yarn app，由ApplicationMaster和DockerProxy组成。AM负责向yarn申请资源，启动DockerProxy。DockerProxy进程启停Docker容器，收集日志。CPU内存等资源的限制交给Docker完成，所有yarn的ContainerExecutor使用DefaultContainerExecutor即可，无需使用LinuxContainerExecutor。DockerProxy进程本身必须只占用很少的资源。

+ AM的结构

![](./doc/am.png)

AM是基于Vert.X异步非阻塞框架的Web服务。它主要的组件有：

1. Web Verticle: 提供Web服务，访问数据库。
2. AM Vertical： 保存AM的元信息，与RM交互申请资源
3. RpcServer：主要是与DockerProxy通信，保持心跳，传送监控信息。使用的hadoop的RPC框架。

4. 组件之间使用vertx的eventbus解耦。

   

+ DockerProxy





+ 前端

前端开发使用的React框架，数据流层采用dva，采用前后端分离的开发方式。但这种开发方式会导致yarn无法代理前端url，例如原始的css资源url为/index.css,  AM向RM注册时提供追踪url经过yarn代理后的路径变为/proxy/ $(APP_ID)/index.css。也就意味着要想访问到该资源AM返回的html文档中必须动态改变资源的路径，同理ajax的路径也要修改。但前后端分离是没办法动态改变资源的url，导致yarn代理产生404错误。我现在是直接访问AM Web服务，不经过RM，即想RM注册调用registerApplicationMaster接口时不提供appTrackingUrl。



### 3. 使用方式

1. 环境准备：

+ 由于Hadoop的依赖包并没有加入Thallo中，客户端要放置Hadoop的安装包，包括配置文件，并配置环境变量HADOOP_HOME指向安装包位置。目前开发采用的Hadoop版本是2.7.4；

+ NodeManager节点安装Docker；

2. 启动Thallo



![](/Users/yss/work/Thallo/doc/ui.png)















