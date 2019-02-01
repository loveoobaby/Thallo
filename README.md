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







+ DockerProxy





+ 前端







### 3. 使用方式

















