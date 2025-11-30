package com.bookstore.bookstore_backend.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Neo4j配置类
 * 配置Neo4j的Driver和事务管理器
 */
@Configuration
public class Neo4jConfig {

    @Value("${spring.data.neo4j.uri}")
    private String uri;

    @Value("${spring.data.neo4j.authentication.username}")
    private String username;

    @Value("${spring.data.neo4j.authentication.password}")
    private String password;

    /**
     * 手动配置Neo4j Driver，确保认证信息被正确传递
     * 解决 "scheme 'none' is only allowed when auth is disabled" 错误
     */
    @Bean
    public Driver neo4jDriver() {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    /**
     * 配置Neo4j事务管理器
     * Spring Data Neo4j需要显式配置事务管理器才能正确使用@Transactional注解
     * 注意：不标记为@Primary，确保JPA事务管理器是默认的
     */
    @Bean(name = "neo4jTransactionManager")
    public PlatformTransactionManager neo4jTransactionManager(Driver driver) {
        return new Neo4jTransactionManager(driver);
    }
}

