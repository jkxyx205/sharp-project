
package com.rick.security.core.properties;

import lombok.Data;

/**
 * 图片验证码配置项
 * 
 * @author zhailiang
 *
 */
@Data
public class ImageCodeProperties extends SmsCodeProperties {
	
	public ImageCodeProperties() {
		setLength(4);
	}
	
	/**
	 * 图片宽
	 */
	private int width = 67;
	/**
	 * 图片高
	 */
	private int height = 23;
}
