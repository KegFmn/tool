package com.likc.tool.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author likc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private User user;

    private List<String> permissions;

    private List<GrantedAuthority> authorities;

    public LoginUser(User user, List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    /**
     * getAuthorities()方法是UserDetails接口中的一个方法，用于获取用户的权限信息。
     * 在Spring Security中，权限信息被封装成GrantedAuthority对象，该对象表示用户所拥有的权限。getAuthorities()方法返回一个Collection类型的对象，其中包含了用户的权限信息。
     * 通常情况下，getAuthorities()方法会返回一个包含用户权限的集合，每个权限都被封装成GrantedAuthority对象。这些权限可以是用户在系统中被授予的角色，也可以是用户被授予的特定权限。
     * 通过调用getAuthorities()方法，我们可以获取用户的权限信息，并在系统中进行相应的权限验证和授权操作。
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //首先判断authorities是否为null。如果为null，则表示权限信息还未被封装，需要进行封装。不为null则直接返回已有的使用
        if(authorities == null){
            authorities = new ArrayList<>();
            //将permissions中String类型的权限信息封装成SimpleGrantedAuthority（这是GrantedAuthority接口的实现类）对象
            for (String permission : permissions) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(permission);
                //封装成类型后，添加到List集合中
                authorities.add(authority);
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
