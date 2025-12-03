CREATE DATABASE IF NOT EXISTS `smsdb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `smsdb`;

CREATE TABLE IF NOT EXISTS `messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `phone_number` VARCHAR(32) NOT NULL,
  `address` VARCHAR(64) NULL,
  `body` TEXT NOT NULL,
  `type` TINYINT NOT NULL,
  `date_ts` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_phone` (`phone_number`),
  INDEX `idx_date` (`date_ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
