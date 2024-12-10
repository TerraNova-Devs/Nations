-- makes sure ruuid exists in db
INSERT INTO `rank` (`RUUID`)
SELECT ?
WHERE NOT EXISTS (
    SELECT 1 FROM `rank` WHERE `RUUID` = ?
);

-- fetches the data
SELECT `Level`, `obj_a`, `obj_b`, `obj_c` FROM `rank` WHERE `RUUID` = ?;

-- remove a rank
DELETE FROM `rank` WHERE `RUUID` = ?;

-- level up
UPDATE `rank`
SET `Level` = `Level` + 1,
    `obj_a` = 0,
    `obj_b` = 0,
    `obj_c` = 0
WHERE `RUUID` = ?;

-- update objective
UPDATE `rank`
SET %s = ?
WHERE `RUUID` = ?;