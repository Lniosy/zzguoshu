package com.fruitveg.utils;

import com.fruitveg.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Component
public class JwtUtils {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @return Token字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtConfig.getExpireMs());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret().getBytes())
                .compact();
    }

    /**
     * 从Token中获取Claims
     *
     * @param token Token字符串
     * @return Claims对象
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtConfig.getSecret().getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token中获取用户名
     *
     * @param token Token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token Token字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }

    /**
     * 从Token中获取角色
     *
     * @param token Token字符串
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    /**
     * 验证Token是否过期
     *
     * @param token Token字符串
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证Token的有效性
     *
     * @param token Token字符串
     * @param username 用户名
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    /**
     * 刷新Token
     *
     * @param token 旧Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        return generateToken(userId, username, role);
    }

    /**
     * 提取Token（去除前缀）
     *
     * @param bearerToken 包含前缀的Token
     * @return 纯Token字符串
     */
    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(JwtConfig.TOKEN_PREFIX)) {
            return bearerToken.substring(JwtConfig.TOKEN_PREFIX.length());
        }
        return null;
    }
}
