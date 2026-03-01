package com.fruitveg.security;

import com.fruitveg.config.JwtConfig;
import com.fruitveg.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证Token过滤器
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 对登录请求直接放行，不需要进行JWT验证
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("JwtAuthenticationTokenFilter - Received request: " + method + " " + requestURI);

        // 对于登录请求，直接放行，不需要进行JWT验证
        if (requestURI.contains("/auth/login") || requestURI.contains("/auth/register")) {
            logger.debug("JwtAuthenticationTokenFilter - Auth request detected, skipping JWT verification: " + requestURI);
            chain.doFilter(request, response);
            return;
        }

        // 对于其他请求，继续JWT验证逻辑
        // 从请求头中获取Token
        String authHeader = request.getHeader(JwtConfig.TOKEN_HEADER);

        if (authHeader != null && authHeader.startsWith(JwtConfig.TOKEN_PREFIX)) {
            String token = jwtUtils.extractToken(authHeader);

            try {
                // 验证Token的有效性
                if (token != null && !jwtUtils.isTokenExpired(token)) {
                    // 从Token中获取用户信息
                    String username = jwtUtils.getUsernameFromToken(token);
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    String role = jwtUtils.getRoleFromToken(token);

                    logger.debug("JwtAuthenticationTokenFilter - Token valid for user: " + username);

                    // 加载用户详细信息
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 创建认证Token
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                logger.error("JWT Token验证失败: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        // 继续过滤链
        chain.doFilter(request, response);
    }
}
