package com.wokoba.czh.config;

import com.wokoba.czh.domain.agent.service.memory.RetrievableChatMemory;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    /**
     * 为 MyBatis 创建主数据源
     */
    @Bean("mybatisDataSource")
    @Primary
    public DataSource mybatisDataSource(@Value("${spring.datasource.driver-class-name}") String driverClassName,
                                        @Value("${spring.datasource.url}") String url,
                                        @Value("${spring.datasource.username}") String username,
                                        @Value("${spring.datasource.password}") String password,
                                        @Value("${spring.datasource.hikari.maximum-pool-size:10}") int maximumPoolSize,
                                        @Value("${spring.datasource.hikari.minimum-idle:5}") int minimumIdle,
                                        @Value("${spring.datasource.hikari.idle-timeout:30000}") long idleTimeout,
                                        @Value("${spring.datasource.hikari.connection-timeout:30000}") long connectionTimeout,
                                        @Value("${spring.datasource.hikari.max-lifetime:1800000}") long maxLifetime) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        // 连接池配置
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        dataSource.setPoolName("MainHikariPool");
        return dataSource;
    }


    /**
     * 为 PgVector 创建专用的数据源
     */
    @Bean("pgVectorDataSource")
    public DataSource pgVectorDataSource(@Value("${spring.vector.pgvector.datasource.driver-class-name}") String driverClassName,
                                         @Value("${spring.vector.pgvector.datasource.url}") String url,
                                         @Value("${spring.vector.pgvector.datasource.username}") String username,
                                         @Value("${spring.vector.pgvector.datasource.password}") String password,
                                         @Value("${spring.datasource.hikari.maximum-pool-size:10}") int maximumPoolSize,
                                         @Value("${spring.datasource.hikari.minimum-idle:5}") int minimumIdle,
                                         @Value("${spring.datasource.hikari.idle-timeout:30000}") long idleTimeout,
                                         @Value("${spring.datasource.hikari.connection-timeout:30000}") long connectionTimeout) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        // 连接池配置
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);
        // 确保在启动时连接数据库
        dataSource.setInitializationFailTimeout(1);  // 设置为1ms，如果连接失败则快速失败
        dataSource.setConnectionTestQuery("SELECT 1"); // 简单的连接测试查询
        dataSource.setAutoCommit(true);
        dataSource.setPoolName("PgVectorHikariPool");
        return dataSource;
    }

    /**
     * 为 PgVector 创建专用的 JdbcTemplate
     */
    @Bean("pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Bean("vectorStore")
    public PgVectorStore pgVectorStore(OpenAiEmbeddingModel embeddingModel,
                                       @Qualifier("pgVectorJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("vector_store")
                .build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }


    @Bean
    public RetrievableChatMemory retrievableChatMemory(ChatMemoryRepository chatMemoryRepository) {
        return RetrievableChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }


//    @Bean
//    public MysqlMemoryRepository chatMemoryRepository(@Qualifier("mybatisDataSource") DataSource mysqlDataSource) {
//        return JdbcChatMemoryRepository.builder()
//                .dataSource(mysqlDataSource)
//                .build();
//    }
}
