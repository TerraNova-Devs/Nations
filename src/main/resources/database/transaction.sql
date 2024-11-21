-- Insert or update transaction based on the current count of transactions
INSERT INTO `transaction_table` (`SUUID`, `ID`, `username`, `amount`, `timestamp`)
SELECT
    ? AS SUUID,
    CASE
        WHEN COUNT(*) = 50 THEN MIN(ID) -- If 50 transactions exist, reuse the oldest ID
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