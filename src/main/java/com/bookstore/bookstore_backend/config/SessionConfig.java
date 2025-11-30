package com.bookstore.bookstore_backend.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Session配置类
 * 确保Spring Session使用JPA事务管理器，而不是Neo4j事务管理器
 */
@Configuration
public class SessionConfig {

    /**
     * 配置JPA事务管理器作为主要事务管理器
     * Spring Session JDBC需要使用JPA事务管理器来管理会话数据
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

