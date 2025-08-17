CREATE OR REPLACE VIEW v_last_minute_trades AS
SELECT
    s,
    date_trunc('minute', ts) AS minute,
    count(*) AS trade_count,
    sum(v) AS total_volume,
    avg(p) AS avg_price,
    min(p) AS min_price,
    max(p) AS max_price
FROM (
    SELECT *, to_timestamp(t / 1000) AS ts
    FROM finnhub_trades
    WHERE t / 1000 >= extract(epoch from now() - interval '1 minute')
) sub
GROUP BY s, date_trunc('minute', ts)
ORDER BY minute;

CREATE OR REPLACE VIEW v_hourly_trades_summary AS
SELECT
    s,
    date_trunc('hour', ts) AS hour,
    count(*) AS trade_count,
    sum(v) AS total_volume,
    avg(p) AS avg_price,
    min(p) AS min_price,
    max(p) AS max_price
FROM (
    SELECT *, to_timestamp(t / 1000) AS ts
    FROM finnhub_trades
    WHERE t / 1000 >= extract(epoch from now() - interval '1 hour')
) sub
GROUP BY s, date_trunc('hour', ts)
ORDER BY hour;

CREATE OR REPLACE VIEW v_symbol_volume_rank AS
SELECT
    s,
    sum(v) AS total_volume
FROM finnhub_trades
WHERE t / 1000 >= extract(epoch from now() - interval '1 hour')
GROUP BY s
ORDER BY total_volume DESC;

CREATE OR REPLACE VIEW v_last_trade_per_symbol AS
SELECT
    s.s,
    to_timestamp(f.t / 1000) AS last_trade_time,
    f.p AS last_price,
    f.v AS last_volume
FROM (
    SELECT DISTINCT s FROM finnhub_trades
) s
CROSS JOIN LATERAL (
    SELECT *
    FROM finnhub_trades f
    WHERE f.s = s.s
    ORDER BY t DESC
    LIMIT 1
) f;

-- Last 10 minutes aggregated
CREATE OR REPLACE VIEW v_trade_stats_last_10_min AS
SELECT DISTINCT ON (symbol, minute)
    symbol,
    minute,
    total_volume,
    avg_price,
    min_price,
    max_price,
    open_price,
    close_price
FROM (
    SELECT
        symbol,
        date_trunc('minute', start_timestamp) AS minute,
        sum(volume) OVER (PARTITION BY symbol, date_trunc('minute', start_timestamp)) AS total_volume,
        avg(avg_price) OVER (PARTITION BY symbol, date_trunc('minute', start_timestamp)) AS avg_price,
        min(min_price) OVER (PARTITION BY symbol, date_trunc('minute', start_timestamp)) AS min_price,
        max(max_price) OVER (PARTITION BY symbol, date_trunc('minute', start_timestamp)) AS max_price,
        first_value(open_price) OVER (PARTITION BY symbol, date_trunc('minute', start_timestamp) ORDER BY start_timestamp) AS open_price,
        last_value(close_price) OVER (PARTITION BY symbol, date_trunc('minute', start_timestamp) ORDER BY start_timestamp ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS close_price
    FROM trade_statistics
    WHERE start_timestamp >= now() - interval '10 minutes'
) t
ORDER BY symbol, minute;

-- Volume per symbol last hour
CREATE OR REPLACE VIEW v_trade_stats_hourly_volume AS
SELECT
    symbol,
    date_trunc('hour', start_timestamp) AS hour,
    sum(volume) AS total_volume
FROM trade_statistics
WHERE start_timestamp >= now() - interval '1 hour'
GROUP BY symbol, date_trunc('hour', start_timestamp)
ORDER BY hour;

-- Top 5 symbols by volume last 30 minutes
CREATE OR REPLACE VIEW v_trade_stats_top_symbols AS
SELECT symbol, sum(volume) AS total_volume
FROM trade_statistics
WHERE start_timestamp >= now() - interval '30 minutes'
GROUP BY symbol
ORDER BY total_volume DESC
LIMIT 5;

-- Price change per symbol last 15 minutes
CREATE OR REPLACE VIEW v_trade_stats_price_change AS
WITH stats AS (
    SELECT
        symbol,
        start_timestamp,
        end_timestamp,
        open_price,
        close_price,
        first_value(open_price) OVER w AS first_open,
        last_value(close_price) OVER w AS last_close
    FROM trade_statistics
    WHERE start_timestamp >= now() - interval '15 minutes'
    WINDOW w AS (PARTITION BY symbol ORDER BY start_timestamp ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)
)
SELECT
    symbol,
    min(start_timestamp) AS period_start,
    max(end_timestamp) AS period_end,
    min(first_open) AS open_price,
    max(last_close) AS close_price,
    max(last_close) - min(first_open) AS price_change
FROM stats
GROUP BY symbol
ORDER BY symbol;

CREATE INDEX idx_finnhub_trades_symbol_time ON finnhub_trades(s, t DESC);