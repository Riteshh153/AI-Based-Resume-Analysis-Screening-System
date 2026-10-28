package com.hiresmart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final SecurityAuthInterceptor securityAuthInterceptor;

    public WebMvcConfig(SecurityAuthInterceptor securityAuthInterceptor) {
        this.securityAuthInterceptor = securityAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityAuthInterceptor)
                .addPathPatterns("/api/**");
    }
}
