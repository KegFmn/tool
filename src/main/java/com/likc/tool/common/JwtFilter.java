package com.likc.tool.common;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Resource
    private IRoleService iRoleService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //检查token是否存在：如果token不存在，则直接放行请求，继续执行后续的过滤器或处理器。
        String token = getToken(request);
        if(StringUtils.isBlank(token)){
            //放行操作
            filterChain.doFilter(request,response);
            return;
        }

        Long userId;
        try {
            userId = Long.parseLong(JwtUtils.verify(token).getSubject());
        } catch (Exception e) {
            //放行操作
            filterChain.doFilter(request,response);
            return;
        }

        List<String> roleList = iRoleService.getRoleStrByUserId(userId);
        roleList.add(RoleEnum.USER.getName());

        User user = new User();
        user.setId(userId);
        user.setRoleList(roleList);
        user.setProjectIdList(getProjectId(request));

        LoginUser loginUser = new LoginUser(user, roleList);

        /**
         * 需要有一个Authentication类型的数据，所以先转换类型
         * UsernamePasswordAuthenticationToken参数解析如下：
         * principal：表示身份验证的主体，通常是用户的唯一标识，比如用户名、用户ID等。
         * credentials：表示身份验证的凭证，通常是用户的密码或其他认证信息。
         * authorities：表示用户的权限集合，即用户被授予的权限列表
         */
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

        //存入SecurityContextHolder：将获取到的用户信息存入SecurityContextHolder中，以便后续的权限验证和授权操作。
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        //放行
        filterChain.doFilter(request,response);
    }

    private String getToken(HttpServletRequest request) {
        // url
        if (StringUtils.isNotBlank(request.getParameter("token"))) {
            return request.getParameter("token");
        }

        // header
        if (StringUtils.isNotBlank(request.getHeader("token"))) {
            return request.getHeader("token");
        }

        // cookies
        Cookie[] cookies = request.getCookies();
        if (Objects.nonNull(cookies) && cookies.length != 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private List<Long> getProjectId(HttpServletRequest request) {
        String projectId = request.getHeader("e-project-id") == null ? request.getHeader("project-id") : request.getHeader("e-project-id");
        if (StringUtils.isNotBlank(projectId)) {
            return List.of(Long.parseLong(projectId));
        }

        return new ArrayList<>();
    }
}