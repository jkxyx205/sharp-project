package com.rick.security;

/**
 * @author Rick
 * @createdAt 2021-09-17 22:53:00
 */

import java.util.List;

/**
 * Created by macro on 2020/6/22.
 */
public class PayloadDto {

    private String sub;

    private Long iat;

    private Long exp;

    private String jti;

    private String username;

    private List<String> authorities;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public Long getIat() {
        return iat;
    }

    public void setIat(Long iat) {
        this.iat = iat;
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }
}

