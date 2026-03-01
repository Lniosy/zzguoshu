package com.fruitveg.config;

import com.fruitveg.security.JwtAuthenticationEntryPoint;
import com.fruitveg.security.JwtAuthenticationTokenFilter;
import com.fruitveg.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 *
 * @author lniosy
 * @since 2026-02-27
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 配置用户认证
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * 配置HTTP安全
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护，因为我们使用JWT
                .csrf().disable()
                // 配置未认证请求的处理
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                // 配置会话管理：无状态
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 配置请求授权
                .authorizeRequests()
                // 允许所有/auth/开头的请求公开访问（包含上下文路径的版本）
                .antMatchers("/auth/login", "/api/auth/login").permitAll()
                .antMatchers("/auth/login/code", "/api/auth/login/code").permitAll()
                .antMatchers("/auth/register", "/api/auth/register").permitAll()
                .antMatchers("/auth/code", "/api/auth/code").permitAll()
                // 允许公开访问的接口（包含上下文路径的版本）
                .antMatchers("/product/list", "/api/product/list", "/product/detail/**", "/api/product/detail/**").permitAll()
                .antMatchers("/product/recommend", "/api/product/recommend", "/product/search", "/api/product/search", "/product/*", "/api/product/*").permitAll()
                .antMatchers("/category/list", "/api/category/list", "/category/tree", "/api/category/tree").permitAll()
                .antMatchers("/content/banners", "/api/content/banners", "/content/notices", "/api/content/notices").permitAll()
                .antMatchers("/circle/list", "/api/circle/list", "/circle/detail/**", "/api/circle/detail/**").permitAll()
                .antMatchers("/merchant/public/**", "/api/merchant/public/**").permitAll()
                .antMatchers("/trace/detail/**", "/api/trace/detail/**").permitAll()
                .antMatchers("/images/**", "/api/images/**").permitAll()
                // 允许OPTIONS请求
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 允许接口文档访问（包含上下文路径的版本）
                .antMatchers("/doc.html", "/api/doc.html").permitAll()
                .antMatchers("/webjars/**", "/api/webjars/**").permitAll()
                .antMatchers("/swagger-resources/**", "/api/swagger-resources/**").permitAll()
                .antMatchers("/v2/api-docs", "/api/v2/api-docs").permitAll()
                .antMatchers("/v3/api-docs", "/api/v3/api-docs").permitAll()
                // 允许文件上传下载（包含上下文路径的版本）
                .antMatchers("/upload/**", "/api/upload/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated();

        // 添加JWT过滤器
        http.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // 配置跨域
        http.cors();

        logger.debug("SecurityConfig.configure(HttpSecurity) called with configuration: ");
        logger.debug("- Permitted paths: /auth/login, /api/auth/login, /auth/register, /api/auth/register, /product/list, /api/product/list, /product/detail/**, /api/product/detail/**, /category/list, /api/category/list, /circle/list, /api/circle/list, /circle/detail/**, /api/circle/detail/**, /trace/detail/**, /api/trace/detail/**");
        logger.debug("- Session policy: STATELESS");
        logger.debug("- CSRF: disabled");
    }

    /**
     * 配置Web安全（忽略某些路径）
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/doc.html",
                "/webjars/**",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/v3/api-docs",
                "/upload/**",
                "/images/**",
                "/api/doc.html",
                "/api/webjars/**",
                "/api/swagger-resources/**",
                "/api/v2/api-docs",
                "/api/v3/api-docs",
                "/api/upload/**",
                "/api/images/**"
        );
    }
}
