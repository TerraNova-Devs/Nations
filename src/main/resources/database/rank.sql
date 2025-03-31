-- makes sure ruuid exists in db
INSERT INTO `rank` (`RUUID`)
SELECT ?
WHERE NOT EXISTS (
    SELECT 1 FROM `rank` WHERE `RUUID` = ?
);

-- fetches the data
SELECT `Level` FROM `rank` WHERE `RUUID` = ?;

-- remove a rank
DELETE FROM `rank` WHERE `RUUID` = ?;

-- level up
UPDATE `rank`
SET `Level` = `Level` + 1
WHERE `RUUID` = ?;
