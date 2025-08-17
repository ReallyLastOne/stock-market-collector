CREATE INDEX idx_finnhub_trades_t_ts ON finnhub_trades ((to_timestamp(t)));
