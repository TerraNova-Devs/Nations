-- Insert or update transaction based on the current count of transactions
INSERT INTO `transaction_table` (`SUUID`, `ID`, `username`, `amount`, `timestamp`)
SELECT
    ? AS SUUID,
    CASE
        WHEN COUNT(*) = 50 THEN
            (SELECT ID FROM `transaction_table` WHERE SUUID = ? ORDER BY timestamp ASC LIMIT 1) -- Reuse the oldest ID by timestamp
        ELSE COUNT(*) + 1 -- Otherwise, use the next available ID
        END AS ID,
    ? AS username,
    ? AS amount,
    ? AS timestamp
FROM `transaction_table`
WHERE SUUID = ?
ON DUPLICATE KEY UPDATE
                     username = VALUES(username),
                     amount = VALUES(amount),
                     timestamp = VALUES(timestamp);