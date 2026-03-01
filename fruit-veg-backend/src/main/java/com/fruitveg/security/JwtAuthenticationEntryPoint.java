package com.fruitveg.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruitveg.common.Result;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT认证失败处理器
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // 设置响应状态码
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 设置响应内容类型
        response.setContentType("application/json;charset=UTF-8");

        // 构建响应结果
        Result<Object> result = Result.error(401, "未登录或Token已失效，请重新登录");

        // 输出响应结果
        PrintWriter writer = response.getWriter();
        writer.write(new ObjectMapper().writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}
