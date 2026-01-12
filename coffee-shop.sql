-- 咖啡廳庫存管理系統資料庫結構
-- Database: coffee_shop

-- 建立資料庫（如果需要）
CREATE DATABASE IF NOT EXISTS coffee_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE coffee_shop;

-- ========================================
-- 產品主檔 (Product Table)
-- ========================================
CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '產品ID',
  `name` VARCHAR(50) NOT NULL COMMENT '產品名稱',
  `type` VARCHAR(20) NOT NULL COMMENT '產品類型: BEAN或DESSERT',
  `price` INT NOT NULL COMMENT '單價',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '庫存數量',
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_product_type` CHECK (`type` IN ('BEAN', 'DESSERT')),
  CONSTRAINT `chk_product_price` CHECK (`price` > 0),
  CONSTRAINT `chk_product_stock` CHECK (`stock` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='產品主檔';

-- ========================================
-- 交易記錄 (Transaction Table)
-- ========================================
CREATE TABLE IF NOT EXISTS `transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `product_id` BIGINT NOT NULL COMMENT '產品ID',
  `type` VARCHAR(10) NOT NULL COMMENT '交易類型: IN或OUT',
  `quantity` INT NOT NULL COMMENT '數量',
  `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易時間',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_transaction_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_transaction_type` CHECK (`type` IN ('IN', 'OUT')),
  CONSTRAINT `chk_transaction_quantity` CHECK (`quantity` > 0),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易記錄';

-- ========================================
-- 測試資料 (可選)
-- ========================================
-- INSERT INTO `product` (`name`, `type`, `price`, `stock`) VALUES
-- ('衣索比亞耶加雪菲', 'BEAN', 450, 120),
-- ('提拉米蘇', 'DESSERT', 150, 8);
