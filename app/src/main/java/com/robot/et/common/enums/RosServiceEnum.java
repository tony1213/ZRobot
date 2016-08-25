package com.robot.et.common.enums;

//ROS 服务
public enum RosServiceEnum {

	ROAMING("Roaming","漫游"),
	ROAMING1("Roaming","随便走走"),
	ROAMING2("Roaming","自己走"),

	SAVEAMAP("SaveAMap","保存地图"),
	SAVEAMAP2("SaveAMap","地图保存"),

	WORLDNAVIGATION("World Navigation","世界地图导航"),

	FORWARDONEMETER("ForwardOneMeter","走一米"),

	MAKEAMAP("MakeAMap","创建地图"),

	FOLLOWER("Follower","跟我走"),
	FOLLOWER2("Follower","跟我来"),

	STARTADDSERVICE("AddTWO","开启服务"),

	DEEPLEARN("OpenDeepLearn","开启深度学习"),

	DEEPLEARNINIT("DeepLearnInit","深度学习初始化"),

//	VISUALLEARNSERVICE("VisualLearn","视觉学习"),

	DEEPLEARNREC("DeepLearnRec","看看这是什么"),
	DEEPLEARNREC2("DeepLearnRec","看看这是啥"),

	DEEPLEARNCLOSE("DeepLearnClose","关闭深度学习"),

	STOP("Stop","停下来");


	private String serviceKey;
	private String serviceName;

	private RosServiceEnum(String serviceKey, String serviceName){
		this.serviceKey = serviceKey;
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getServiceKey() {
		return serviceKey;
	}

}
