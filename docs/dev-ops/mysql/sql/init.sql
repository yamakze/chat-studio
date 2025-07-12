-- auto-generated definition
create schema if not exists `ai-agent-station` collate utf8mb4_0900_ai_ci;

use `ai-agent-station`;

-- MySQL dump 10.13  Distrib 9.2.0, for macos15.2 (arm64)
--
-- Host: 127.0.0.1    Database: ai-agent-station
-- ------------------------------------------------------
-- Server version	9.2.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai_agent`
--

DROP TABLE IF EXISTS `ai_agent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `agent_name` varchar(50) NOT NULL COMMENT '智能体名称',
                            `description` varchar(255) DEFAULT NULL COMMENT '描述',
                            `channel` varchar(32) DEFAULT NULL COMMENT '渠道类型(agent，chat_stream)',
                            `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_agent_name` (`agent_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI智能体配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_agent`
--

LOCK TABLES `ai_agent` WRITE;
/*!40000 ALTER TABLE `ai_agent` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_agent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_agent_client`
--

DROP TABLE IF EXISTS `ai_agent_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent_client` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `agent_id` bigint NOT NULL COMMENT '智能体ID',
                                   `client_id` bigint NOT NULL COMMENT '客户端ID',
                                   `sequence` int NOT NULL COMMENT '序列号(执行顺序)',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_agent_client_seq` (`agent_id`,`client_id`,`sequence`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能体-客户端关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_agent_client`
--

LOCK TABLES `ai_agent_client` WRITE;
/*!40000 ALTER TABLE `ai_agent_client` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_agent_client` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_agent_task_schedule`
--

DROP TABLE IF EXISTS `ai_agent_task_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent_task_schedule` (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `agent_id` bigint NOT NULL COMMENT '智能体ID',
                                          `task_name` varchar(64) DEFAULT NULL COMMENT '任务名称',
                                          `description` varchar(255) DEFAULT NULL COMMENT '任务描述',
                                          `cron_expression` varchar(50) NOT NULL COMMENT '时间表达式(如: 0/3 * * * * *)',
                                          `task_param` text COMMENT '任务入参配置(JSON格式)',
                                          `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:无效,1:有效)',
                                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          PRIMARY KEY (`id`),
                                          KEY `idx_agent_id` (`agent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能体任务调度配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_agent_task_schedule`
--

LOCK TABLES `ai_agent_task_schedule` WRITE;
/*!40000 ALTER TABLE `ai_agent_task_schedule` DISABLE KEYS */;
INSERT INTO `ai_agent_task_schedule` VALUES (2,44,'测试智能体任务','测试任务功能是否正常','0/10 * * * * ?','今天天气怎么样？',0,'2025-06-26 10:48:25','2025-07-11 10:15:56'),(5,61,'B站评论回复','自动回复评论','0/10 * * * * *','获取需要回复的最新消息，调用相应工具进行回复，内容由你根据消息上下文撰写',0,'2025-07-11 10:53:30','2025-07-11 16:44:02');
/*!40000 ALTER TABLE `ai_agent_task_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_client`
--

DROP TABLE IF EXISTS `ai_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_client` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `model_id` bigint NOT NULL DEFAULT '3' COMMENT '模型id',
                             `system_prompt_id` bigint NOT NULL DEFAULT '8' COMMENT '系统提示词id',
                             `client_name` varchar(50) NOT NULL COMMENT '客户端名称',
                             `description` varchar(1024) DEFAULT NULL COMMENT '描述',
                             `options` text COMMENT '模型选项',
                             `status` tinyint DEFAULT '1' COMMENT '状态',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`),
                             KEY `uk_client_name` (`client_name`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI客户端配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_client`
--

LOCK TABLES `ai_client` WRITE;
/*!40000 ALTER TABLE `ai_client` DISABLE KEYS */;
INSERT INTO `ai_client` VALUES (26,287,8,'文件测试','用于测试附件上下文','{\"temperature\":1.2,\"maxTokens\":3096,\"topP\":1.0,\"presencePenalty\":0.0}',1,'2025-06-18 15:15:59','2025-06-30 19:49:45'),(29,280,7,'常规对话测试','测试对话功能是否正常','{\"temperature\":0.7,\"maxTokens\":3096,\"topP\":0.7,\"presencePenalty\":0.0}',1,'2025-06-18 18:40:54','2025-07-04 14:11:03'),(34,280,6,'知识库测试','测试rag检索功能','{\"temperature\":1.2,\"maxTokens\":3096,\"topP\":1.0,\"presencePenalty\":0.0}',1,'2025-06-23 11:28:39','2025-07-04 15:48:45'),(44,278,8,'默认话题','暂无描述','{\"temperature\":1.0,\"maxTokens\":3096,\"topP\":1.0,\"presencePenalty\":0.0}',0,'2025-06-26 11:02:05','2025-06-30 10:31:15'),(52,285,9,'角色构建','暂无描述','{\"temperature\":1.0,\"maxTokens\":10240,\"topP\":1.0,\"presencePenalty\":0.0}',1,'2025-07-03 15:46:38','2025-07-11 09:51:28'),(59,280,12,'开发命名','暂无描述','{\"temperature\":1.0,\"maxTokens\":3096,\"topP\":1.0,\"presencePenalty\":0.0}',1,'2025-07-07 13:59:11','2025-07-07 15:22:43'),(60,89,1,'提示词优化','暂无描述','{\"temperature\":1.0,\"maxTokens\":3096,\"topP\":1.0,\"presencePenalty\":0.0}',1,'2025-07-07 15:22:31','2025-07-07 15:23:29'),(61,89,13,'自定义MCP测试','测试本地MCP效果','{\"temperature\":1.0,\"maxTokens\":3096,\"topP\":1.0,\"presencePenalty\":0.0}',1,'2025-07-07 16:51:01','2025-07-09 21:19:55');
/*!40000 ALTER TABLE `ai_client` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_client_advisor`
--

DROP TABLE IF EXISTS `ai_client_advisor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_client_advisor` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `advisor_name` varchar(50) NOT NULL COMMENT '顾问名称',
                                     `advisor_type` varchar(50) NOT NULL COMMENT '顾问类型(PromptChatMemory/RagAnswer/SimpleLoggerAdvisor等)',
                                     `ext_param` varchar(2048) DEFAULT NULL COMMENT '扩展参数配置，json 记录',
                                     `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
                                     `basic` tinyint DEFAULT '0' COMMENT '基础顾问标志',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='顾问配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_client_advisor`
--

LOCK TABLES `ai_client_advisor` WRITE;
/*!40000 ALTER TABLE `ai_client_advisor` DISABLE KEYS */;
INSERT INTO `ai_client_advisor` VALUES (1,'记忆','ChatMemory','{\r\n  \"maxMessages\": 200,\r\n  \"retrievableK\": 35\r\n}',1,1,'2025-05-04 08:23:25','2025-07-04 15:57:42'),(5,'知识库','RagAnswer','{\r\n  \"topK\": \"5\",\r\n  \"filterExpression\": \"memoryType == \'SYNTHESIS\'\",\r\n  \"allowEmptyContext\": true,\r\n  \"similarityThreshold\": 0.65\r\n}',1,1,'2025-05-04 08:23:25','2025-07-04 15:57:29'),(6,'附件','CustomMedia','{\r\n   \"filePattern\": \"@file:([^\\\\s]+)\"\r\n}',1,1,'2025-06-19 21:49:24','2025-06-23 17:35:48');
/*!40000 ALTER TABLE `ai_client_advisor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_client_advisor_config`
--

DROP TABLE IF EXISTS `ai_client_advisor_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_client_advisor_config` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                            `client_id` bigint NOT NULL COMMENT '客户端ID',
                                            `advisor_id` bigint NOT NULL COMMENT '顾问ID',
                                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `uk_client_advisor` (`client_id`,`advisor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=503 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户端-顾问关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_client_advisor_config`
--

LOCK TABLES `ai_client_advisor_config` WRITE;
/*!40000 ALTER TABLE `ai_client_advisor_config` DISABLE KEYS */;
INSERT INTO `ai_client_advisor_config` VALUES (277,26,1,'2025-06-30 19:52:28'),(278,26,6,'2025-06-30 19:52:28'),(343,34,1,'2025-07-04 15:48:45'),(344,34,5,'2025-07-04 15:48:45'),(382,60,6,'2025-07-07 15:23:34'),(498,59,6,'2025-07-11 09:57:44'),(499,59,5,'2025-07-11 09:57:44'),(500,52,5,'2025-07-11 09:59:44'),(501,52,6,'2025-07-11 09:59:44'),(502,52,1,'2025-07-11 09:59:44');
/*!40000 ALTER TABLE `ai_client_advisor_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_client_model`
--

DROP TABLE IF EXISTS `ai_client_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_client_model` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `model_name` varchar(50) NOT NULL COMMENT '模型名称',
                                   `base_url` varchar(255) NOT NULL COMMENT '基础URL',
                                   `api_key` varchar(255) NOT NULL COMMENT 'API密钥',
                                   `completions_path` varchar(100) DEFAULT 'v1/chat/completions' COMMENT '完成路径',
                                   `model_type` varchar(50) NOT NULL COMMENT '模型类型(openai/azure等)',
                                   `model_version` varchar(70) DEFAULT 'gpt-4.1' COMMENT '模型版本',
                                   `timeout` int DEFAULT '180' COMMENT '超时时间(秒)',
                                   `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `ai_client_model_pk` (`model_version`)
) ENGINE=InnoDB AUTO_INCREMENT=323 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI接口模型配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_client_model`
--

LOCK TABLES `ai_client_model` WRITE;
/*!40000 ALTER TABLE `ai_client_model` DISABLE KEYS */;
INSERT INTO `ai_client_model` VALUES (6,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-tts-2025-05-22',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(7,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qvq-plus',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(8,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qvq-plus-2025-05-15',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(9,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qvq-max-2025-05-15',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(10,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen3-4b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(11,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen3-32b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(12,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen3-30b-a3b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(13,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen3-235b-a22b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(14,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen3-14b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(15,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen3-1.7b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(16,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen3-0.6b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(17,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen3-8b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(18,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-vl-max-2025-04-02',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(19,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','deepseek-v3',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(20,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','deepseek-r1-distill-llama-70b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(21,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','deepseek-r1-distill-qwen-32b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(22,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','deepseek-r1-distill-qwen-14b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(23,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','deepseek-r1-distill-llama-8b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(24,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','deepseek-r1-distill-qwen-1.5b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(25,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','deepseek-r1-distill-qwen-7b',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(26,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','deepseek-r1',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(27,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen1.5-7b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(28,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-vl-ocr-latest',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(29,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-vl-ocr',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(30,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-coder-plus-1106',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(31,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-coder-plus',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(32,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-coder-plus-latest',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(33,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-coder-3b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(34,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-coder-0.5b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(35,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-coder-14b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(36,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-coder-32b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(37,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-math-plus-0919',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(38,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-0.5b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(39,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-1.5b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(40,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-3b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(41,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-7b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(42,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-14b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(43,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-32b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(44,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-72b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(45,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-coder-7b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(46,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-math-1.5b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(47,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-math-7b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(48,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2.5-math-72b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(49,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-turbo-0919',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(50,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-turbo-latest',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(51,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-plus-0919',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(52,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-plus-latest',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(53,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-max-0919',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(54,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-max-latest',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(55,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-coder-turbo-0919',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(56,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-coder-turbo',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(57,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-coder-turbo-latest',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(58,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-math-turbo-0919',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(59,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-math-turbo',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(60,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-math-turbo-latest',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(61,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-math-plus',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(62,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-math-plus-latest',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(63,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2-57b-a14b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(64,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2-1.5b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(65,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2-0.5b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(66,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2-7b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(67,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen2-72b-instruct',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(68,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-long',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(69,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-vl-max',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(70,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-vl-plus',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(71,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-max-0428',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(72,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen1.5-110b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(73,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-1.8b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(74,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-1.8b-longcontext-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(75,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-7b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(76,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-14b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(77,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-72b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(78,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','codeqwen1.5-7b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(79,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen1.5-0.5b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(80,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen1.5-1.8b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(81,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen1.5-14b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(82,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen1.5-32b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(83,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen1.5-72b-chat',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(84,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-max-longcontext',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(85,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-max-1201',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(86,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-max-0107',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(87,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-max-0403',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(88,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-max',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(89,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-plus',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:15'),(90,'通义千问','https://dashscope.aliyuncs.com/compatible-mode/','xxx','/v1/chat/completions','百炼平台','qwen-turbo',30,1,'2025-06-27 15:20:04','2025-07-11 16:40:14'),(266,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-1.0-pro-vision-latest',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(267,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-pro-vision',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(277,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-pro-preview-03-25',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(278,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-flash-preview-04-17',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(279,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-flash-preview-05-20',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(280,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-flash',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(281,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-flash-preview-04-17-thinking',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(282,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-flash-lite-preview-06-17',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(283,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-pro-preview-05-06',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(284,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-pro-preview-06-05',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(285,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-pro',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(286,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-exp',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(287,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(288,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-001',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(289,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-exp-image-generation',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(290,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-lite-001',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(291,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-lite',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(292,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-preview-image-generation',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(293,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-lite-preview-02-05',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(294,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-lite-preview',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(295,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-pro-exp',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(296,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-pro-exp-02-05',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(297,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-exp-1206',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(298,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-thinking-exp-01-21',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(299,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-thinking-exp',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(300,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-thinking-exp-1219',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(301,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-flash-preview-tts',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(302,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-pro-preview-tts',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(303,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','learnlm-2.0-flash-experimental',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(304,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemma-3-1b-it',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(305,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemma-3-4b-it',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(306,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemma-3-12b-it',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(307,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemma-3-27b-it',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(308,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemma-3n-e4b-it',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(309,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemma-3n-e2b-it',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(312,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-embedding-exp-03-07',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(313,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-embedding-exp',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(314,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','aqa',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(315,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','imagen-3.0-generate-002',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(316,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','imagen-4.0-generate-preview-06-06',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(317,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','imagen-4.0-ultra-generate-preview-06-06',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(318,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','veo-2.0-generate-001',30,0,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(319,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-flash-preview-native-audio-dialog',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(320,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.5-flash-exp-native-audio-thinking-dialog',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(321,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-2.0-flash-live-001',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33'),(322,'gemini','https://generativelanguage.googleapis.com/v1beta','xxxx','/openai/chat/completions','ai-studio','gemini-live-2.5-flash-preview',30,1,'2025-06-27 16:37:47','2025-07-11 16:40:33');
/*!40000 ALTER TABLE `ai_client_model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_client_system_prompt`
--

DROP TABLE IF EXISTS `ai_client_system_prompt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_client_system_prompt` (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                           `prompt_name` varchar(50) NOT NULL COMMENT '提示词名称',
                                           `prompt_content` text NOT NULL COMMENT '提示词内容',
                                           `description` varchar(1024) DEFAULT NULL COMMENT '描述',
                                           `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
                                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           PRIMARY KEY (`id`),
                                           UNIQUE KEY `uk_prompt_name` (`prompt_name`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统提示词配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_client_system_prompt`
--

LOCK TABLES `ai_client_system_prompt` WRITE;
/*!40000 ALTER TABLE `ai_client_system_prompt` DISABLE KEYS */;
INSERT INTO `ai_client_system_prompt` VALUES (1,'提示词优化',' Based on the input, **rephrase or enhance** the user\'s question to make it clearer, more complete, and easier to understand or answer. Aim for a concise, well-defined, and semantically rich version of the original query.\n\nOptimization Guidelines:\n\n1. Preserve original intent but add any missing subjects, objects, or context.\n2. Use clear and standard phrasing to improve readability.\n3. Avoid ambiguous, slang, or overly brief expressions.\n4. Where the query could mean multiple things, try to clarify intent via rephrasing.\n5. Return the refined question and optionally explain your changes.','提示词优化，拆分执行动作',1,'2025-05-04 21:14:24','2025-07-07 15:22:26'),(6,'运维专家','You are an experienced Site Reliability Engineer (SRE) or DevOps specialist with expertise in Linux administration, container orchestration (Docker/Kubernetes), CI/CD pipelines, log analysis, system tuning, and incident response. Based on the input, provide accurate, secure, and practical operations support or troubleshooting suggestions.\n\nResponse Guidelines:\nBe step-by-step and actionable, not vague.\n\nInclude shell commands, config snippets, or YAML if relevant.\n\nPrioritize safe, reversible actions where possible.\n\nWarn about any potential risks with sensitive operations.\n\nAsk for additional info if necessary to proceed safely.','专注于解决运维部署问题',1,'2025-06-06 17:03:40','2025-07-07 18:26:29'),(7,'表情翻译','我要你把我写的句子翻译成表情符号。我只是想让你用表情符号来表达它。除了表情符号，我不希望你回复任何内容。','将文字转换为emoji',1,'2025-06-13 10:10:38','2025-07-01 11:14:24'),(8,'默认提示词','正常回应问题','默认提示词',1,'2025-06-13 10:53:50','2025-06-13 11:35:12'),(9,'创意写手','# 角色 \n 小说角色构建专家 \n ## 注意 \n 1. 角色构建专家需具备深入的人物心理分析能力，以确保角色的多维度发展。 \n 2. 专家设计应考虑小说家的创作需求和读者的阅读期待。 \n 3. 使用情感引导的方法来展现角色的内在世界和外在行为。 \n ## 性格类型指标 \n INFJ（内向直觉情感判断型） \n ## 背景 \n 小说角色构建专家致力于帮助小说家深入挖掘角色的内心世界，构建立体、有深度的人物形象，以增强故事的吸引力和感染力。 \n ## 约束条件 \n - 必须确保角色构建符合小说的整体风格和主题 \n - 角色的行为和心理活动需符合其背景设定和性格特点 \n - 使用中文进行回答\n ## 定义 \n 角色构建：指在小说创作中，通过细致的人物心理描写和行为表现，塑造出具有独特性格、动机和成长轨迹的角色。 \n ## 目标 \n - 帮助小说家构建具有深度和复杂性的角色 \n - 增强角色与读者的情感共鸣 \n - 促进故事情节的发展和主题的深化 \n ## Skills \n 为了在限制条件下实现目标，该专家需要具备以下技能： \n 1. 人物心理分析能力 \n 2. 角色背景和性格设定能力 \n 3. 情感引导和故事叙述技巧 \n ## 音调 \n - 温和而富有洞察力 \n - 富有同理心和情感共鸣 \n - 引导性和启发性 \n ## 价值观 \n - 重视角色的内在成长和心理变化 \n - 认为每个角色都有其独特的价值和意义 \n - 倡导多元和包容的人物设定 ','构建小说角色',1,'2025-06-24 22:16:28','2025-06-30 10:29:22'),(10,'电子书作家','# 角色 \n 电子书作家 \n ## 注意 \n 1. 确保角色配置能够满足用户在电子书创作过程中的需求。 \n 2. 设计专家应具备帮助用户深入挖掘角色特性的能力。 \n 3. 使用创造性思维和情感共鸣来强调角色的多维度发展。 \n ## 性格类型指标 \n INTP（内向直觉思维知觉型） \n ## 背景 \n 电子书作家旨在帮助用户构建丰富、立体的角色，以增强电子书的吸引力和可读性。 \n ## 约束条件 \n - 必须遵循用户设定的角色背景和故事情境。 \n - 避免提供与用户设定相冲突的角色特征。 \n ## 定义 \n - 角色深度：角色的内在心理和外在行为的复杂性。 \n - 角色发展：角色在故事中的成长和变化。 \n - 角色互动：角色与其他角色之间的相互作用和影响。 \n ## 目标 \n - 帮助用户创建具有深度和吸引力的角色。 \n - 促进用户对角色背景、性格和动机的深入理解。 \n - 引导用户探索角色在故事中的多维度发展。 \n ## Skills \n 1. 角色构建能力：能够设计具有独特性格和背景的角色。 \n 2. 故事叙述技巧：能够将角色融入引人入胜的故事情节中。 \n 3. 情感共鸣能力：能够通过角色的情感变化引发读者的共鸣。 \n ## 音调 \n - 鼓励性的：激励用户深入探索角色的内心世界。 \n - 启发性的：引导用户思考角色的多维度发展。 \n - 亲切的：与用户建立良好的沟通和互动。 \n ## 价值观 \n - 重视角色的个性化和独特性。 \n - 强调角色在故事中的重要性和影响力。 \n - 倡导创造性思维和情感共鸣在角色构建中的应用。 ','帮助构建电子书籍',1,'2025-06-25 08:57:15','2025-07-03 20:55:38'),(11,'总结内容','You are an AI assistant specialized in summarizing documents. Your task is to create concise and clear summaries for each section of the provided text,\n				as well as an overall summary for the entire document. Please adhere to the following guidelines:\n				\n				Section Summaries:\n					Summarize each section separately.\n					Each section summary should be no longer than 2 paragraphs.\n					Highlight the main points and essential information clearly and accurately.\n				\n				Overall Summary:\n					Provide a summary of the entire document.\n					The overall summary should be no longer than 1 paragraph.\n					Ensure it encapsulates the core themes and ideas of the document.\n					Tone and Clarity:\n					Maintain a friendly and polite tone throughout all summaries.\n					Use clear and straightforward language to ensure the content is easy to understand.\n				\n				Structure:\n					Organize the summaries logically, ensuring each section is distinct and coherent.\n					Review the summaries to confirm they meet the specified length and clarity requirements.\n				\n				Except for Code. Aside from the specific name and citation, your answer must be written in the same language as the question.','总结url或文档内容',1,'2025-06-30 14:16:49','2025-07-04 08:57:16'),(12,'开发命名','Please generate appropriate names based on the following context. Names should follow standard naming conventions for Java with high readability, semantic clarity, and consistent style.\n\nRequirements:\n1. The name should clearly reflect the purpose or responsibility of the entity.\n2. Prefer commonly used and semantically appropriate keywords.\n3. Avoid redundant, meaningless, or conflicting names.\n4. For methods, use \"verb + noun\" or action-oriented patterns.\n5. Return 1–3 suggested names with a brief explanation for each.','生成合适、符合 Java开发规范的命名',1,'2025-07-07 14:05:19','2025-07-07 15:27:25'),(13,'B站回复助手','你是一位雄辩且逻辑清晰的助手，擅长用刻薄、犀利的言辞回应那些操纵性、不合逻辑或谬误的言论。如果评论扭曲、前后矛盾或具有误导性，你的任务是运用合理的推理将其驳斥——突出矛盾之处，揭露逻辑缺陷，并揭示其中的谬误。\n\n回应指南：\n- 效仿发言者自身的逻辑，以揭示其弱点。\n- 避免使用人身攻击。\n- 使用讽刺、反讽或适度夸张来强调荒谬之处。\n- 重新构建或反转论点，以清晰有力的方式突出其缺陷。','用于自动回复评论',1,'2025-07-09 14:29:03','2025-07-11 16:42:13');
/*!40000 ALTER TABLE `ai_client_system_prompt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_client_tool_config`
--

DROP TABLE IF EXISTS `ai_client_tool_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_client_tool_config` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `client_id` bigint NOT NULL COMMENT '客户端ID',
                                         `tool_type` varchar(20) NOT NULL COMMENT '工具类型(mcp/function call)',
                                         `tool_id` bigint NOT NULL COMMENT 'MCP ID/ function call ID',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uk_client_mcp` (`client_id`,`tool_id`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户端-MCP关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_client_tool_config`
--

LOCK TABLES `ai_client_tool_config` WRITE;
/*!40000 ALTER TABLE `ai_client_tool_config` DISABLE KEYS */;
INSERT INTO `ai_client_tool_config` VALUES (85,61,'mcp',13,'2025-07-09 21:19:55');
/*!40000 ALTER TABLE `ai_client_tool_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_client_tool_mcp`
--

DROP TABLE IF EXISTS `ai_client_tool_mcp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_client_tool_mcp` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `mcp_name` varchar(50) NOT NULL COMMENT 'MCP名称',
                                      `transport_type` varchar(20) NOT NULL COMMENT '传输类型(sse/stdio)',
                                      `transport_config` varchar(1024) DEFAULT NULL COMMENT '传输配置(sse/stdio)',
                                      `request_timeout` int DEFAULT '180' COMMENT '请求超时时间(分钟)',
                                      `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
                                      `basic` tinyint DEFAULT '0' COMMENT '基础工具标记',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_mcp_name` (`mcp_name`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MCP客户端配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_client_tool_mcp`
--

LOCK TABLES `ai_client_tool_mcp` WRITE;
/*!40000 ALTER TABLE `ai_client_tool_mcp` DISABLE KEYS */;
INSERT INTO `ai_client_tool_mcp` VALUES (3,'文件操作工具','stdio','{\n     \"filesystem\": {\n       \"command\": \"npx\",\n       \"args\": [\n         \"-y\",\n         \"@modelcontextprotocol/server-filesystem\",\n         \"/Users/chenzihao/Desktop\",\n         \"/Users/chenzihao/Downloads\"\n       ]\n     }\n}',180,1,0,'2025-05-05 13:14:42','2025-06-13 15:02:21'),(9,'思维链','stdio','{\n  \"sequential-thinking\": {\n      \"command\": \"npx\",\n      \"args\": [\n        \"-y\",\n        \"@modelcontextprotocol/server-sequential-thinking\"\n      ]\n    }\n}',180,1,0,'2025-06-25 16:29:04','2025-06-25 16:29:04'),(10,'部署html','stdio','{\n    \"edgeone-pages-mcp-server\": {\n      \"command\": \"npx\",\n      \"args\": [\n        \"edgeone-pages-mcp\"\n      ]\n    }  \n}',180,1,0,'2025-06-26 14:07:02','2025-06-26 14:07:02'),(12,'网页自动化','stdio','{\n \"puppeteer\": {\n      \"command\": \"npx\",\n      \"args\": [\n        \"-y\",\n        \"@modelcontextprotocol/server-puppeteer\"\n      ]\n    }\n}',180,1,0,'2025-06-27 08:58:17','2025-06-27 08:58:17'),(13,'bilibili自动回复','stdio','{\n	\"mcp-server-csdn\": {\n		\"command\": \"java\",\n		\"args\": [\n			\"-Dspring.ai.mcp.server.stdio=true\",\n			\"-Dspring.main.web-application-type=none\",\n			\"-jar\",\n			\"/Users/chenzihao/开发/mcp/MCP-Services/BilibiliMessageConsole/target/BilibiliMessageConsole-1.0-SNAPSHOT.jar\",\n			\"--bilibili.api.cookie-file-path=/Users/chenzihao/Downloads/cookie.json\",\n              \"--bilibili.cron=0/10 * * * * *\",\n\"--bilibili.cursor-timestamp=1751970803\",\n\"--bilibili.api.cookie-value=\",\n\"--bilibili.max-retrieve-count=7\"\n		]\n	}\n}',30,1,1,'2025-07-09 09:57:44','2025-07-10 20:14:55');
/*!40000 ALTER TABLE `ai_client_tool_mcp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_rag_order`
--

DROP TABLE IF EXISTS `ai_rag_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_rag_order` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `rag_name` varchar(50) NOT NULL COMMENT '知识库名称',
                                `knowledge_tag` varchar(50) NOT NULL COMMENT '知识标签',
                                `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_rag_name` (`rag_name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_rag_order`
--

LOCK TABLES `ai_rag_order` WRITE;
/*!40000 ALTER TABLE `ai_rag_order` DISABLE KEYS */;
INSERT INTO `ai_rag_order` VALUES (3,'code-AIReview','https://github.com/yamakze/code-AIReview.git',1,'2025-06-23 11:28:09','2025-06-23 11:28:09'),(4,'特种设备接口文档','特种设备接口文档',1,'2025-06-23 11:37:00','2025-06-23 11:37:00');
/*!40000 ALTER TABLE `ai_rag_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_task_execution_record`
--

DROP TABLE IF EXISTS `ai_task_execution_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_task_execution_record` (
                                            `id` bigint NOT NULL AUTO_INCREMENT,
                                            `task_id` bigint NOT NULL COMMENT '任务ID',
                                            `request` text COMMENT '请求内容',
                                            `response` text COMMENT '响应内容',
                                            `total_tokens` int DEFAULT NULL COMMENT '总令牌数',
                                            `status` varchar(50) NOT NULL COMMENT '执行状态',
                                            `execute_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_task_execution_record`
--

LOCK TABLES `ai_task_execution_record` WRITE;
/*!40000 ALTER TABLE `ai_task_execution_record` DISABLE KEYS */;
INSERT INTO `ai_task_execution_record` VALUES (1,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:07:20'),(2,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:07:30'),(3,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:07:40'),(4,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:07:50'),(5,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:08:00'),(6,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:08:10'),(7,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:10:23'),(8,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:10:49'),(9,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:11:01'),(10,2,'今天天气怎么样？','无',0,'failure','2025-07-11 09:11:14'),(11,2,'今天天气怎么样？','作为一个AI，我无法直接获取实时的、您所在位置的详细天气信息。\n\n天气情况通常与具体地点有关，并且是不断变化的。\n\n**要查询今天的天气，建议您：**\n\n1.  **告诉我您所在的城市或地区：** 如果您提供地点，我可以尝试使用网络搜索工具（如果可用）为您查找最新的天气预报。\n2.  **直接查看天气App或网站：** 这是获取最准确和实时天气信息最快的方式。\n3.  **使用搜索引擎：** 在搜索框输入 \"XX城市/地区 天气\" 即可查询。\n\n如果您告诉我您想了解哪个地方的天气，我很乐意帮助您查找一下！',713,'completed','2025-07-11 10:15:44'),(12,2,'今天天气怎么样？','请告诉我您在哪个城市，我才能帮您查询今天的天气情况。',136,'completed','2025-07-11 10:15:51'),(13,2,'今天天气怎么样？','您好！我很乐意为您提供今天的天气情况，但为了给您最准确的信息，我需要知道您想查询哪个城市或地区的天气。\n\n请告诉我您想查询的城市名称，我就可以帮您查找了。',294,'completed','2025-07-11 10:16:01'),(14,2,'今天天气怎么样？','你好！\n\n我很抱歉，由于我无法获取实时的、具体地点的天气数据，所以无法直接告诉你今天的天气怎么样。\n\n请告诉我你所在的**城市**或**地区**，或者你可以查看：\n\n1.  你手机上的天气应用\n2.  天气网站（如中国天气网、AccuWeather等）\n3.  搜索引擎（如百度、谷歌）直接搜索 \"[你的城市名称] 天气\"\n\n这样才能获取最准确和实时的天气信息。',672,'completed','2025-07-11 10:16:13'),(15,2,'今天天气怎么样？','我无法直接告诉您今天的天气，因为天气情况是地区性的。\n\n请问您想知道哪个城市的天气呢？\n\n如果您告诉我您所在的城市或地区，我可以为您提供查找天气信息的建议。\n\n您也可以直接通过手机天气App、天气网站或搜索引擎来查询您当前位置的实时天气。',676,'completed','2025-07-11 10:16:23'),(16,2,'今天天气怎么样？','天气信息是和地点相关的，而且会实时变化。\n\n您能告诉我您想查询哪个城市的天气吗？\n\n或者，您可以通过以下方式获取最准确的天气信息：\n\n1.  使用手机上的天气应用。\n2.  访问专业的天气网站。\n3.  查看电视上的天气预报。\n\n希望这些方法能帮到您！',582,'completed','2025-07-11 10:16:33'),(18,5,'获取需要回复的最新消息，调用相应工具进行回复，内容由你根据消息上下文撰写，对于抽象的互联网用于则回复同样不明所以的emoji。','看来我已经成功回复了那些消息呢！如果你还有其他需要处理的消息或者任务，尽管告诉我哦！😎',2070,'completed','2025-07-11 11:08:09');
/*!40000 ALTER TABLE `ai_task_execution_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SPRING_AI_CHAT_MEMORY`
--

DROP TABLE IF EXISTS `SPRING_AI_CHAT_MEMORY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `SPRING_AI_CHAT_MEMORY` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
                                         `conversation_id` varchar(64) NOT NULL COMMENT '对话 ID',
                                         `type` varchar(20) NOT NULL COMMENT '消息类型（USER / ASSISTANT / SYSTEM）',
                                         `content` text NOT NULL COMMENT '消息内容',
                                         `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息时间戳',
                                         `status` varchar(20) NOT NULL DEFAULT 'created' COMMENT '状态(processed / created)',
                                         PRIMARY KEY (`id`),
                                         KEY `idx_conversation_id` (`conversation_id`),
                                         KEY `idx_conversation_time` (`conversation_id`,`timestamp`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Spring AI 聊天记忆表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SPRING_AI_CHAT_MEMORY`
--

LOCK TABLES `SPRING_AI_CHAT_MEMORY` WRITE;
/*!40000 ALTER TABLE `SPRING_AI_CHAT_MEMORY` DISABLE KEYS */;
/*!40000 ALTER TABLE `SPRING_AI_CHAT_MEMORY` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-11 16:45:11
