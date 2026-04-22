# 数据库初始化指南

## 方式一：使用 MySQL 命令行

```bash
mysql -u root -p < myauth-generator/src/main/resources/db/init.sql
```

## 方式二：使用 MySQL Workbench

1. 打开 MySQL Workbench
2. 连接到数据库
3. 打开 `myauth-generator/src/main/resources/db/init.sql` 文件
4. 执行 SQL 脚本

## 方式三：手动创建

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS `myauth` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### 2. 创建表

```sql
USE `myauth`;

CREATE TABLE `license_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `license_key` TEXT NOT NULL COMMENT '授权码',
  `expire_time` VARCHAR(20) NOT NULL COMMENT '过期时间',
  `machine_id` VARCHAR(100) DEFAULT NULL COMMENT '机器码',
  `type` VARCHAR(50) NOT NULL COMMENT '授权类型',
  `max_users` INT DEFAULT NULL COMMENT '最大用户数',
  `modules` TEXT DEFAULT NULL COMMENT '功能模块',
  `customer_name` VARCHAR(200) DEFAULT NULL COMMENT '客户名称',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer_name` (`customer_name`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='授权记录表';
```

## 验证安装

执行以下查询确认表已创建：

```sql
USE myauth;
SHOW TABLES;
DESC license_record;
```

应该能看到 `license_record` 表及其结构。
