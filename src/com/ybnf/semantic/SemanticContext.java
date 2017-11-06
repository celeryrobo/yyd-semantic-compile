package com.ybnf.semantic;

import java.util.Map;

public interface SemanticContext {
	/**
	 * 根据用户标识加载用户上下文信息
	 * 
	 * @param userIdentify
	 *            用户标识
	 */
	void loadByUserIdentify(String userIdentify);

	/**
	 * 上一次请求所在场景
	 * 
	 * @return 场景标识字符串
	 */
	String getService();

	/**
	 * 上一次请求当前场景
	 * 
	 * @param service
	 *            场景标识字符串
	 */
	void setService(String service);

	/**
	 * 获取上次请求设置的语义参数信息
	 * 
	 * @return 语义实体字典
	 */
	Map<Object, Object> getParams();

}
