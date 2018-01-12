package com.ybnf.compiler.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractSemanticResult {
	public static enum Operation {
		/**
		 * 播放器播放
		 */
		PLAY,
		/**
		 * 文本转音频播放
		 */
		SPEAK,
		/**
		 * 机器人移动
		 */
		MOVE,
		/**
		 * 打开第三方APP
		 */
		APP
	}

	public static enum ParamType {
		/**
		 * 文本
		 */
		T,
		/**
		 * 资源URL
		 */
		U,
		/**
		 * 文本+资源URL
		 */
		TU,
		/**
		 * 图片URL+文本
		 */
		IT,
		/**
		 * 图片URL+资源URL
		 */
		IU,
		/**
		 * 图片URL+文本+资源URL
		 */
		ITU
	}

	@JsonIgnore
	private Integer errCode = 0;
	@JsonIgnore
	private String errMsg = "OK";
	@JsonIgnore
	private Operation operation = Operation.SPEAK;
	@JsonIgnore
	private ParamType paramType = ParamType.T;
	@JsonIgnore
	private Object resource = null;

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public Integer getErrCode() {
		return errCode;
	}

	public void setErrCode(Integer errCode) {
		this.errCode = errCode;
		if(errCode.equals(0)) {
			setErrMsg("OK");
		} else {
			setErrMsg("Semantic Match Fail !");
		}
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public ParamType getParamType() {
		return paramType;
	}

	public void setParamType(ParamType paramType) {
		this.paramType = paramType;
	}

	public Object getResource() {
		return resource;
	}

	public void setResource(Object resource) {
		this.resource = resource;
	}
}
