-- 创建 pgvector 扩展，如果它不存在的话
-- 这需要在数据库中启用向量类型支持
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建数据库 springai
CREATE DATABASE springai
    WITH OWNER postgres;

-- 连接到新创建的数据库，以便在其中创建表
-- 对于 Docker 的 /docker-entrypoint-initdb.d 目录，通常会在执行完数据库创建后自动连接到该数据库或默认数据库
-- 为了确保表在正确的数据库中创建，这里假设后续命令将在 springai 数据库的上下文中执行

-- 创建表 vector_store
CREATE TABLE vector_store
(
    id        uuid DEFAULT gen_random_uuid() NOT NULL
        CONSTRAINT vector_store_like_openai_pkey
            PRIMARY KEY,
    content   text                           NOT NULL,
    metadata  jsonb,
    embedding vector(1024)
);

-- 设置表的拥有者
ALTER TABLE vector_store
    OWNER TO postgres;