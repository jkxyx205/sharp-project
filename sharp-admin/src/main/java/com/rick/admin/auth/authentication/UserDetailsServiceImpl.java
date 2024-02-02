package com.rick.admin.auth.authentication;

import com.rick.admin.auth.common.AuthConstants;
import com.rick.admin.auth.exception.MaxTryLoginException;
import com.rick.admin.sys.user.entity.User;
import com.rick.admin.sys.user.service.UserService;
import com.rick.common.http.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * @author rick
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    private final CacheManager cacheManager;

    private final SessionRegistry sessionRegistry;

    /**
     * 进行认证授权的工作
     *
     * @param username 用户名
     * @return 用户登陆信息
     * @throws UsernameNotFoundException 用户未找到
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        username = username.toUpperCase();
        Cache<String, Integer> tryCache = cacheManager.getCache("loginMaxTry");

        Integer loginMaxTryCountInCache = tryCache.get(username);

        int loginMaxTryCount = 1;
        if (!Objects.isNull(loginMaxTryCountInCache)) {
            loginMaxTryCount = loginMaxTryCountInCache + 1;
        }
        tryCache.put(username, loginMaxTryCount);

        if (loginMaxTryCount >= AuthConstants.MAX_TRY_COUNT) {
            throw new MaxTryLoginException(MessageUtils.getMessage("MAX_TRY_LOGIN_ERROR"
                    , new Object[] { AuthConstants.MAX_TRY_COUNT, AuthConstants.LOCK_MINUTES }));
        }

        Optional<User> optional = userService.findByUsername(username);
        if (optional.isPresent()) {
            if (isLogin(username)) {
                log.info("LOGIN: {}已经登录,踢出登录", username );
            }
            return new AdminUserDetails(optional.get(), AuthorityUtils.createAuthorityList(optional.get().getAuthorityList().toArray(new String[]{})));
        } else {
            throw new UsernameNotFoundException("帐号不存在或者账号被锁定");
        }
    }

    /**
     * 用户是否处于登录状态
     * @param username 用户名
     * @return 返回登陆状态
     */
    private boolean isLogin(String username) {
        List<Object> list = sessionRegistry.getAllPrincipals();
        for (Object o : list) {
            if (StringUtils.equals(((AdminUserDetails)o).getUsername(), username)) {
                return sessionRegistry.getAllSessions(o, false).size() > 0;
            }

        }

        return false;
    }
}
