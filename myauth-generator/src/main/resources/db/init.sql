-- 创建数据库
CREATE DATABASE IF NOT EXISTS `myauth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `myauth`;

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 授权记录表
DROP TABLE IF EXISTS `license_record`;
CREATE TABLE `license_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `license_key` TEXT NOT NULL COMMENT '授权码',
  `expire_time` VARCHAR(20) NOT NULL COMMENT '过期时间',
  `machine_id` VARCHAR(100) DEFAULT NULL COMMENT '机器码',
  `type` VARCHAR(50) NOT NULL COMMENT '授权类型: trial, standard, professional, enterprise',
  `max_users` INT DEFAULT NULL COMMENT '最大用户数',
  `modules` TEXT DEFAULT NULL COMMENT '功能模块(JSON数组)',
  `customer_name` VARCHAR(200) DEFAULT NULL COMMENT '客户名称',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer_name` (`customer_name`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='授权记录表';

-- 插入默认管理员账号 (密码: admin123)
-- 注意：DefaultUserInitializer 会在启动时自动创建正确的admin账号
-- 这里不插入数据，避免密码哈希不正确的问题

-- 插入测试数据
INSERT INTO `license_record` (`license_key`, `expire_time`, `machine_id`, `type`, `max_users`, `modules`, `customer_name`, `remark`, `status`) VALUES
('TEST_LICENSE_KEY_001', '2026-12-31', NULL, 'professional', 100, '["module1", "module2"]', '测试客户A', '测试授权码', 1);

