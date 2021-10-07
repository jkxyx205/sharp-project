
package com.rick.security.core.validate.code;

import com.rick.security.core.properties.SecurityProperties;
import com.rick.security.core.support.SecurityConstants;
import com.rick.security.core.validate.code.exception.ValidateCodeException;
import com.rick.security.core.validate.code.handler.ValidateCodeProcessorHolder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 校验验证码的过滤器
 * 
 * @author zhailiang
 *
 */
@Component
@RequiredArgsConstructor
public class ValidateCodeFilter extends OncePerRequestFilter implements InitializingBean {

	/**
	 * 验证码校验失败处理器
	 */
	private final AuthenticationFailureHandler authenticationFailureHandler;
	/**
	 * 系统配置信息
	 */
	private final SecurityProperties securityProperties;
	/**
	 * 系统中的校验码处理器
	 */
	private final ValidateCodeProcessorHolder validateCodeProcessorHolder;
	/**
	 * 存放所有需要校验验证码的url
	 */
	private Map<String, ValidateCodeTypeEnum> urlMap = new HashMap<>();
	/**
	 * 验证请求url与配置的url是否匹配的工具类
	 */
	private AntPathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * 初始化要拦截的url配置信息
	 */
	@Override
	public void afterPropertiesSet() throws ServletException {
		super.afterPropertiesSet();

		urlMap.put(SecurityConstants.DEFAULT_SIGN_IN_PROCESSING_URL_FORM, ValidateCodeTypeEnum.IMAGE);
		addUrlToMap(securityProperties.getCode().getImage().getUrl(), ValidateCodeTypeEnum.IMAGE);

		urlMap.put(SecurityConstants.DEFAULT_SIGN_IN_PROCESSING_URL_MOBILE, ValidateCodeTypeEnum.SMS);
		addUrlToMap(securityProperties.getCode().getSms().getUrl(), ValidateCodeTypeEnum.SMS);
	}

	/**
	 * 讲系统中配置的需要校验验证码的URL根据校验的类型放入map
	 * 
	 * @param urlString
	 * @param type
	 */
	protected void addUrlToMap(String urlString, ValidateCodeTypeEnum type) {
		if (StringUtils.isNotBlank(urlString)) {
			String[] urls = StringUtils.splitByWholeSeparatorPreserveAllTokens(urlString, ",");
			for (String url : urls) {
				urlMap.put(url, type);
			}
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		ValidateCodeTypeEnum type = getValidateCodeType(request);
		if (type != null) {
			logger.info("校验请求(" + request.getRequestURI() + ")中的验证码,验证码类型" + type);
			try {
				validateCodeProcessorHolder.findValidateCodeProcessor(type)
						.validate(new ServletWebRequest(request, response));
				logger.info("验证码校验通过");
			} catch (ValidateCodeException exception) {
				authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
				return;
			}
		}

		chain.doFilter(request, response);

	}

	/**
	 * 获取校验码的类型，如果当前请求不需要校验，则返回null
	 * 
	 * @param request
	 * @return
	 */
	private ValidateCodeTypeEnum getValidateCodeType(HttpServletRequest request) {
		ValidateCodeTypeEnum result = null;
		if (!StringUtils.equalsIgnoreCase(request.getMethod(), HttpMethod.GET.name())) {
			Set<String> urls = urlMap.keySet();
			for (String url : urls) {
				if (pathMatcher.match(url, request.getRequestURI())) {
					result = urlMap.get(url);
				}
			}
		}
		return result;
	}

}
