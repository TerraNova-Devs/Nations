-- get all members access
SELECT *
FROM `access`
WHERE RUUID = ?;

-- remove a members access
DELETE
FROM `access`
WHERE RUUID = ? AND PUUID = ?;

-- add a members access
INSERT INTO `access` (RUUID, PUUID, access)
VALUES (?, ?, ?)
ON DUPLICATE KEY UPDATE access = VALUES(access);

-- remove every members access
DELETE FROM `access` WHERE `RUUID` = ?;