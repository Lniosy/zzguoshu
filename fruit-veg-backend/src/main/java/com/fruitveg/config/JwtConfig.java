package com.fruitveg.config;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Configuration
public class JwtConfig {

    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT有效期（小时）
     */
    @Value("${jwt.expire}")
    private int expire;

    /**
     * 刷新令牌有效期（天）
     */
    @Value("${jwt.refresh-expire}")
    private int refreshExpire;

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌头部
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * 签名算法
     */
    public static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public String getSecret() {
        return secret;
    }

    public int getExpire() {
        return expire;
    }

    public int getRefreshExpire() {
        return refreshExpire;
    }

    public long getExpireMs() {
        return expire * 60 * 60 * 1000L;
    }

    public long getRefreshExpireMs() {
        return refreshExpire * 24 * 60 * 60 * 1000L;
    }
}
