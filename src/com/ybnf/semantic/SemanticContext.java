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
	 * 获取上次请求设置的语义参数信息，当场景切换时参数清空，用户可以自定义参数和值
	 * 
	 * @return 语义实体字典
	 */
	Map<Object, Object> getParams();

	/**
	 * 获取当前用户的属性，当前会话用户的属性参数，当会话过期或用户手动清空时清空参数，用户可以自定义参数和值
	 * 
	 * @return 语义实体字典
	 */
	Map<Object, Object> getAttrs();

	/**
	 * 设置当前会话的本地零时变量
	 * 
	 * @param localKey
	 *            本地变量key
	 * @param localValue
	 *            本地变量value
	 */
	void setLocalVar(Object localKey, Object localValue);

	/**
	 * 获取当前会话的本地零时变量
	 * 
	 * @param localKey
	 *            本地变量key
	 * @return Object 本地变量value
	 */
	Object getLocalVar(Object localKey);

	/**
	 * 获取当前会话的用户标识
	 * 
	 * @return String
	 */
	String getUserIdentify();

	/**
	 * 清除当前用户上下文
	 * 
	 */
	void clear();
}
