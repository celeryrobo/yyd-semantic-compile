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
		 * 机器人命令
		 */
		COMMAND,
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
		 * 命令
		 */
		C,
		/**
		 * 资源URL
		 */
		U,
		/**
		 * 图片URL
		 */
		I,
	}

	@JsonIgnore
	private Integer errCode = 0;
	@JsonIgnore
	private String errMsg = "OK";
	@JsonIgnore
	private Operation operation = Operation.SPEAK;
	@JsonIgnore
	private String paramType = ParamType.T.toString();
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
		if (errCode.equals(0)) {
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

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public void setParamType(ParamType... paramTypes) {
		if (paramTypes.length == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (ParamType paramType : paramTypes) {
			sb.append(paramType);
		}
		this.paramType = sb.toString();
	}

	public Object getResource() {
		return resource;
	}

	public void setResource(Object resource) {
		this.resource = resource;
	}
}
