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