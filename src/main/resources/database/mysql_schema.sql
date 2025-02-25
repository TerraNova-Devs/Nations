# Set the storage engine
SET DEFAULT_STORAGE_ENGINE = INNODB;
--
# Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;
--
CREATE TABLE IF NOT EXISTS `grid_regions` (
    `RUUID` varchar(36) NOT NULL,
    `name` varchar(36) NOT NULL,
    `type` varchar(36) NOT NULL,
    `location` varchar(60) NOT NULL,
    PRIMARY KEY (`RUUID`)
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `poly_regions` (
     `RUUID` varchar(36) NOT NULL,
     `name` varchar(36) NOT NULL,
     `type` varchar(36) NOT NULL,
     PRIMARY KEY (`RUUID`)
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `access` (
    `RUUID` varchar(36) NOT NULL,
    `PUUID` varchar(36) NOT NULL,
    `access` varchar(36) NOT NULL,
        PRIMARY KEY (`RUUID`, `PUUID`)
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `bank` (
    `RUUID` varchar(36) NOT NULL,
    `user` varchar(16) NOT NULL,
    `credit` mediumint NOT NULL,
    `timestamp` timestamp(6) NOT NULL,
    `total` mediumint NOT NULL,
        PRIMARY KEY (`RUUID`, `timestamp`)
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `rank` (
      `RUUID` varchar(36) NOT NULL,
      `Level` smallint NOT NULL DEFAULT 1,
      `obj_a` mediumint(11) NOT NULL DEFAULT 0,
      `obj_b` mediumint(11) NOT NULL DEFAULT 0,
      `obj_c` mediumint(11) NOT NULL DEFAULT 0,
      PRIMARY KEY (`RUUID`)
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `nations_table` (
      `NUUID` VARCHAR(36) NOT NULL,
      `name` VARCHAR(20) NOT NULL,
      `leader` VARCHAR(36) NOT NULL,
      PRIMARY KEY (`NUUID`)
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `settlement_nation_relations` (
      `SUUID` VARCHAR(36) NOT NULL,
      `NUUID` VARCHAR(36) NOT NULL,
      `rank` VARCHAR(20) NOT NULL,
      PRIMARY KEY (`SUUID`),
      FOREIGN KEY (`SUUID`) REFERENCES `grid_regions`(`RUUID`) ON DELETE CASCADE,
      FOREIGN KEY (`NUUID`) REFERENCES `nations_table`(`NUUID`) ON DELETE CASCADE
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `nation_relations` (
      `NUUID1` VARCHAR(36) NOT NULL,
      `NUUID2` VARCHAR(36) NOT NULL,
      `relation` VARCHAR(20) NOT NULL,
      PRIMARY KEY (`NUUID1`, `NUUID2`)
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;