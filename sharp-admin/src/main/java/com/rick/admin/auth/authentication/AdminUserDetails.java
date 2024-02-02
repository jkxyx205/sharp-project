package com.rick.admin.auth.authentication;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;


/**
 * @author rick
 */
@Getter
public class AdminUserDetails extends User {

    private final com.rick.admin.sys.user.entity.User user;

    public AdminUserDetails(com.rick.admin.sys.user.entity.User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getCode(), user.getPassword(), authorities);
        this.user = user;
    }
}
