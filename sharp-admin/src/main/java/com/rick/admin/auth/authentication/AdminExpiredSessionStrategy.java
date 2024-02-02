package com.rick.admin.auth.authentication;

import com.rick.common.http.HttpServletResponseUtils;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;


/**
 * @author rick
 */
public class AdminExpiredSessionStrategy implements SessionInformationExpiredStrategy {

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) {
        HttpServletResponseUtils.write(event.getResponse()
                , "text/html;charset=UTF-8"
                , "<script>alert('你的帐号在另一台终端登录。如非本人操作，则密码可能已泄露，建议前往修改密码');location.href = '/login'</script>");
    }
}

