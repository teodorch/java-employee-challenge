package com.reliaquest.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static java.util.concurrent.TimeUnit.MINUTES;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@Configuration
@EnableAspectJAutoProxy
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("employees");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(2, MINUTES));
        return manager;
    }
}
