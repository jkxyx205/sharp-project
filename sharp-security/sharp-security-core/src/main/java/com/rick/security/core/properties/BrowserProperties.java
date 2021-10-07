
package com.rick.security.core.properties;

import com.rick.security.core.support.LoginResponseTypeEnum;
import com.rick.security.core.support.SecurityConstants;
import lombok.Data;

/**
 * 浏览器环境配置项
 * 
 * @author zhailiang
 *
 */
@Data
public class BrowserProperties {
	
	/**
	 * session管理配置项
	 */
	private SessionProperties session = new SessionProperties();
	/**
	 * 登录页面，当引发登录行为的url以html结尾时，会跳到这里配置的url上
	 */
	private String signInPage = SecurityConstants.DEFAULT_SIGN_IN_PAGE_URL;
	/**
	 * '记住我'功能的有效时间，默认1小时
	 */
	private int rememberMeSeconds = 3600;
	/**
	 * 退出成功时跳转的url，如果配置了，则跳到指定的url，如果没配置，则返回json数据。
	 */
	private String signOutUrl;

	/**
	 * 登录响应的方式，默认是json
	 */
	private LoginResponseTypeEnum signInResponseType = LoginResponseTypeEnum.JSON;
	/**
	 * 登录成功后跳转的地址，如果设置了此属性，则登录成功后总是会跳到这个地址上。
	 * 
	 * 只在signInResponseType为REDIRECT时生效
	 */
	private String singInSuccessUrl;
}
