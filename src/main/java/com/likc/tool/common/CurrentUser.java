package com.likc.tool.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * 操作用户threadLocal类
 */
public class CurrentUser {

    public static User get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication) && !"anonymousUser".equals(authentication.getPrincipal())) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal() ;
            return loginUser.getUser();
        }

        return new User();
    }

}
