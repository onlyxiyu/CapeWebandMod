-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        5.7.26 - MySQL Community Server (GPL)
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  11.0.0.5919
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- 导出 cape 的数据库结构
CREATE DATABASE IF NOT EXISTS `cape` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;
USE `cape`;

-- 导出  表 cape.users 结构
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `role` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'ROLE_USER',
  `delete_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- 正在导出表  cape.users 的数据：2 rows
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `username`, `password`, `url`, `role`) VALUES
	(1, 'adminOPxiyu', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'http://example.com', 'ROLE_ADMIN'),
	(2, 'test_user', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'http://example.com', 'ROLE_USER');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;

-- 使用新生成的加密密码更新用户
UPDATE users 
SET password = '新生成的加密密码'
WHERE username IN ('adminOPxiyu', 'test_user');

-- 确保adminOPxiyu的角色正确设置为ROLE_ADMIN
UPDATE users 
SET role = 'ROLE_ADMIN'
WHERE username = 'adminOPxiyu';

-- 重新插入用户数据
TRUNCATE TABLE users;
INSERT INTO `users` (`id`, `username`, `password`, `url`, `role`) VALUES
    (1, 'adminOPxiyu', '$2a$10$5.ncqQwGklMO5/K1oRqpb.cNWeeIE5S3vXdxzs3do1NthGjl.Xe3C', 'http://example.com', 'ROLE_ADMIN'),
    (2, 'test_user', '$2a$10$5.ncqQwGklMO5/K1oRqpb.cNWeeIE5S3vXdxzs3do1NthGjl.Xe3C', 'http://example.com', 'ROLE_USER');

-- 添加注册申请表
CREATE TABLE IF NOT EXISTS `registration_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `url` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `status` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
