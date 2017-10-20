# securitycontroller
  
# Introduction
  securitycontroller实现主要基于以下几个框架：
		1、RabbitMQ
		系统模块之间的通信机制采用RabbitMQ实现。
		2、zookeeper
		该系统支持分布式部署，通过zookeeper来维持各个节点之间的联系。
		3、Restlet
		 Restlet是Restful 风格的一种实现，各个系统模块向外部应用提供的数据接口同一采用Rest API。
		4、存储
		支持多种存储方式，通过模块加载配置文件可以选取不同的存储方式，主要包括：MySQL、Redis、MongoDB。
