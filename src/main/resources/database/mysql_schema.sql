# Set the storage engine
SET DEFAULT_STORAGE_ENGINE = INNODB;
--
# Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;
--
CREATE TABLE IF NOT EXISTS `grid_regions`
(
    `RUUID`    varchar(36) NOT NULL,
    `name`     varchar(36) NOT NULL,
    `type`     varchar(36) NOT NULL,
    `location` varchar(60) NOT NULL,
    PRIMARY KEY (`RUUID`)
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `poly_regions`
(
    `RUUID` varchar(36) NOT NULL,
    `name`  varchar(36) NOT NULL,
    `type`  varchar(36) NOT NULL,
    `price` int NOT NULL DEFAULT 0,
    `state` varchar(36) NOT NULL DEFAULT 'NONE',
    `parent` varchar(36) NOT NULL,
    `world` varchar(36) NOT NULL DEFAULT 'world',
    PRIMARY KEY (`RUUID`)
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `property_incomes`
(
    `RUUID`     varchar(36) NOT NULL,
    `PUUID`     varchar(36) NOT NULL,
    `PropertyID` varchar(36) NOT NULL,
    `income`    int NOT NULL DEFAULT 0,
    `timestamp` timestamp(6) NOT NULL,
    `collected`    TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (`RUUID`, `PUUID`, `timestamp`),
    FOREIGN KEY (`RUUID`) REFERENCES `grid_regions` (`RUUID`) ON DELETE CASCADE
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `access`
(
    `RUUID`  varchar(36) NOT NULL,
    `PUUID`  varchar(36) NOT NULL,
    `access` varchar(36) NOT NULL,
    PRIMARY KEY (`RUUID`, `PUUID`),
    KEY `idx_puuid` (`PUUID`)
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `property_access` (
    `RUUID`  VARCHAR(36) NOT NULL,
    `PUUID`  VARCHAR(36) NOT NULL,
    `access` VARCHAR(36) NOT NULL,
    PRIMARY KEY (`RUUID`, `PUUID`)
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `bank`
(
    `RUUID`     varchar(36)  NOT NULL,
    `user`      varchar(16)  NOT NULL,
    `credit`    mediumint    NOT NULL,
    `timestamp` timestamp(6) NOT NULL,
    `total`     mediumint    NOT NULL,
    PRIMARY KEY (`RUUID`, `timestamp`)
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `rank`
(
    `RUUID` varchar(36)   NOT NULL,
    `Level` smallint      NOT NULL DEFAULT 1,
    PRIMARY KEY (`RUUID`)
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `nations_table`
(
    `NUUID`         VARCHAR(36) NOT NULL,
    `name`          VARCHAR(30) NOT NULL,
    `banner_base64` TEXT        NULL,
    PRIMARY KEY (`NUUID`)
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `nations_table` (
      `NUUID` VARCHAR(36) NOT NULL,
      `name` VARCHAR(30) NOT NULL,
      `banner_base64` TEXT NULL,
      PRIMARY KEY (`NUUID`)
) DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `settlement_nation_relations`
(
    `SUUID` VARCHAR(36) NOT NULL,
    `NUUID` VARCHAR(36) NOT NULL,
    `rank`  VARCHAR(20) NOT NULL,
    PRIMARY KEY (`SUUID`),
    FOREIGN KEY (`SUUID`) REFERENCES `grid_regions` (`RUUID`) ON DELETE CASCADE,
    FOREIGN KEY (`NUUID`) REFERENCES `nations_table` (`NUUID`) ON DELETE CASCADE
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `nation_relations`
(
    `NUUID1`   VARCHAR(36) NOT NULL,
    `NUUID2`   VARCHAR(36) NOT NULL,
    `relation` VARCHAR(20) NOT NULL,
    PRIMARY KEY (`NUUID1`, `NUUID2`)
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `nation_ranks`
(
    `NUUID` VARCHAR(36) NOT NULL,
    `SUUID` VARCHAR(36) NOT NULL,
    `PUUID` VARCHAR(36) NOT NULL,
    `rank`  VARCHAR(20) NOT NULL,
    PRIMARY KEY (`PUUID`),
    FOREIGN KEY (`NUUID`) REFERENCES `nations_table` (`NUUID`) ON DELETE CASCADE,
    FOREIGN KEY (`PUUID`) REFERENCES `access` (`PUUID`) ON DELETE CASCADE,
    FOREIGN KEY (`SUUID`) REFERENCES `settlement_nation_relations` (`SUUID`) ON DELETE CASCADE
) DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `settlement_profession_relation`
(
    `RUUID`        VARCHAR(36) NOT NULL,
    `ProfessionID` VARCHAR(36) NOT NULL,
    `Status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (`RUUID`, `ProfessionID`),
    FOREIGN KEY (`RUUID`) REFERENCES `grid_regions` (`RUUID`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `settlement_objective_progress`
(
    `RUUID`       VARCHAR(36) NOT NULL,
    `ObjectiveID` VARCHAR(36) NOT NULL,
    `Progress`    BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`RUUID`, `ObjectiveID`),
    FOREIGN KEY (`RUUID`) REFERENCES `grid_regions` (`RUUID`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
--
CREATE TABLE IF NOT EXISTS `settlement_buildings`
(
    `RUUID`      VARCHAR(36) NOT NULL,
    `BuildingID` VARCHAR(36) NOT NULL,
    `IsBuilt`    TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (`RUUID`, `BuildingID`),
    FOREIGN KEY (`RUUID`) REFERENCES `grid_regions` (`RUUID`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;
