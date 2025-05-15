SELECT * FROM TipoRequisicion WHERE (IdTipoRequisicion = 1)

commit;

SELECT 
    c.session_id AS SPID,
    s.login_name AS UserName,
    c.client_net_address AS ClientIPAddress,    
    r.command AS CommandType,
    t.text AS SQLStatement
FROM sys.dm_exec_connections c
JOIN sys.dm_exec_requests r ON c.session_id = r.session_id
CROSS APPLY sys.dm_exec_sql_text(r.sql_handle) t
JOIN sys.dm_exec_sessions s ON c.session_id = s.session_id
WHERE DB_NAME(r.database_id) = 'INACIF';