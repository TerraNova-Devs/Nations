# Set the storage engine
SET DEFAULT_STORAGE_ENGINE = INNODB;

# Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;

# Create settlements table if it does not exist
CREATE TABLE IF NOT EXISTS `settlements_table` (
    `SUUID` varchar(36) NOT NULL,
    `name` varchar(20) NOT NULL,
    `location` varchar(100) NOT NULL,
    `Level` mediumint(11) NOT NULL DEFAULT 1,
    `obj_a` mediumint(11) NOT NULL DEFAULT 0,
    `obj_b` mediumint(11) NOT NULL DEFAULT 0,
    `obj_c` mediumint(11) NOT NULL DEFAULT 0,
    `obj_d` mediumint(11) NOT NULL DEFAULT 0,
        PRIMARY KEY (`SUUID`)
) DEFAULT CHARSET=utf8
COLLATE=utf8_unicode_ci;

# Create user settlements table to save access authorization
CREATE TABLE IF NOT EXISTS `access_table` (
    `SUUID` varchar(36) NOT NULL,
    `PUUID` varchar(36) NOT NULL,
    `access` varchar(20) NOT NULL,
        PRIMARY KEY (`SUUID`, `PUUID`)
) DEFAULT CHARSET=utf8
COLLATE=utf8_unicode_ci;

-- Table to store nations
CREATE TABLE IF NOT EXISTS `nations_table` (
    `NUUID` VARCHAR(36) NOT NULL,
    `name` VARCHAR(20) NOT NULL,
    `leader` VARCHAR(36) NOT NULL,
    PRIMARY KEY (`NUUID`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Create the settlement_nation_relations table
CREATE TABLE IF NOT EXISTS `settlement_nation_relations` (
    `SUUID` VARCHAR(36) NOT NULL,
    `NUUID` VARCHAR(36) NOT NULL,
    `rank` VARCHAR(20) NOT NULL,
    PRIMARY KEY (`SUUID`),
    FOREIGN KEY (`SUUID`) REFERENCES `settlements_table`(`SUUID`) ON DELETE CASCADE,
    FOREIGN KEY (`NUUID`) REFERENCES `nations_table`(`NUUID`) ON DELETE CASCADE
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Table to store relations between nations
CREATE TABLE IF NOT EXISTS `nation_relations` (
    `NUUID1` VARCHAR(36) NOT NULL,
    `NUUID2` VARCHAR(36) NOT NULL,
    `relation` VARCHAR(20) NOT NULL,
    PRIMARY KEY (`NUUID1`, `NUUID2`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
