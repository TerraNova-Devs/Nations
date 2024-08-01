# Set the storage engine
SET DEFAULT_STORAGE_ENGINE = INNODB;

# Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;

# Create settlements table if it does not exist
CREATE TABLE IF NOT EXISTS `settlements_table` (
    `SUUID` varchar(36) NOT NULL,
    `name` varchar(20) NOT NULL,
    `location` varchar(100) NOT NULL,
    `Level` mediumint(11) NOT NULL DEFAULT 0,
    `obj_a` mediumint(11) NOT NULL DEFAULT 0,
    `obj_b` mediumint(11) NOT NULL DEFAULT 0,
    `obj_c` mediumint(11) NOT NULL DEFAULT 0,
    `obj_d` mediumint(11) NOT NULL DEFAULT 0,
        PRIMARY KEY (`SUUID`)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8
COLLATE=utf8_unicode_ci;

# Create user settlements table to save access authorization
CREATE TABLE IF NOT EXISTS `access_table` (
    `SUUID` varchar(36) NOT NULL,
    `PUUID` varchar(36) NOT NULL,
    `access` varchar(20) NOT NULL,
        PRIMARY KEY (`SUUID`, `PUUID`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8
  COLLATE=utf8_unicode_ci
