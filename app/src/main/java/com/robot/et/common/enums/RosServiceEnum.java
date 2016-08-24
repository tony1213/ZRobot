package com.robot.et.common.enums;

//ROS 服务
public enum RosServiceEnum {

	ROAMING("Roaming","漫游"),
	ROAMING1("Roaming","随便走走"),
	ROAMING2("Roaming","自己走"),
	STARTADDSERVICE("AddTWO","开启服务"),

	VISUALINITSERVICE("VisualInit","视觉初始化"),

	VISUALLEARNSERVICE("VisualLearn","视觉学习"),

	VISUALRECSERVICE("VisualRec","视觉识别"),
	VISUALRECSERVICE1("VisualRec","这个是什么"),
	VISUALRECSERVICE2("VisualRec","这个是啥"),
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
