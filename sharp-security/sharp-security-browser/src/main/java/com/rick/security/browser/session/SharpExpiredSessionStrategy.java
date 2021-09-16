
package com.rick.security.browser.session;

import com.rick.security.core.properties.SecurityProperties;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * 并发登录导致session失效时，默认的处理策略
 * 
 * @author zhailiang
 *
 */
public class SharpExpiredSessionStrategy extends AbstractSessionStrategy implements SessionInformationExpiredStrategy {

	public SharpExpiredSessionStrategy(SecurityProperties securityPropertie) {
		super(securityPropertie);
	}

	@Override
	public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
		onSessionInvalid(event.getRequest(), event.getResponse());
	}
	
	@Override
	protected boolean isConcurrency() {
		return true;
	}

}
