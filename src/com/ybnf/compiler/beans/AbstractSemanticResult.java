package com.ybnf.compiler.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractSemanticResult {
	@JsonIgnore
	private Integer errCode = 0;

	public Integer getErrCode() {
		return errCode;
	}

	public void setErrCode(Integer errCode) {
		this.errCode = errCode;
	}

}
