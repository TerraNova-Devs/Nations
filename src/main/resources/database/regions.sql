-- grid region insert
INSERT INTO `grid_regions` (`RUUID`, `name`, `type`, `location`)
VALUES (?, ?, ?, ?);

-- grid region remove
DELETE FROM `grid_regions`
WHERE `RUUID` = ?;

-- grid update name
UPDATE `grid_regions`
SET `name` = ?
WHERE `RUUID` = ?;

-- grid region by type
SELECT *
FROM `grid_regions`
WHERE `type` = ?;