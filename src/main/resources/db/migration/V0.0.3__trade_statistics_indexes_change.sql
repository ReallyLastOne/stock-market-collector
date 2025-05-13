DROP INDEX idx_start_tmstp_end_tmstp;

CREATE UNIQUE INDEX idx_start_tmstp_end_tmstp ON trade_statistics(start_timestamp, end_timestamp, symbol);

ALTER TABLE trade_statistics DROP CONSTRAINT trade_statistics_start_timestamp_key;
ALTER TABLE trade_statistics DROP CONSTRAINT trade_statistics_end_timestamp_key;

CREATE INDEX idx_finnhub_trades_t ON finnhub_trades(t);
CREATE INDEX idx_finnhub_trades_s ON finnhub_trades(s);

create or replace
view v_trade_statistics as
select
	*
from
	trade_statistics t;
