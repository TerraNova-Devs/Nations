-- get latest transactions
SELECT `user`, `credit` AS amount, `timestamp` AS date, `total`
FROM `bank`
WHERE `RUUID` = ?
ORDER BY `timestamp` DESC
LIMIT 50;

-- get bank credit
SELECT `user`, `credit` AS amount, `timestamp` AS date, `total`
FROM `bank`
WHERE `RUUID` = ?
ORDER BY `timestamp` DESC
LIMIT 1;

-- insert value into bank
INSERT INTO `bank` (`RUUID`, `user`, `credit`, `timestamp`, `total`)
VALUES (?, ?, ?, ?, ?);

-- delete ruuid from bank
DELETE FROM `bank`
WHERE `RUUID` = ?;

-- check for more than 50 entries
DELETE FROM `bank`
WHERE `RUUID` = ?
  AND `timestamp` NOT IN (
    SELECT `timestamp`
    FROM (
             SELECT `timestamp`
             FROM `bank`
             WHERE `RUUID` = ?
             ORDER BY `timestamp` DESC
             LIMIT 50
         ) AS recent_timestamps
)
LIMIT 1;