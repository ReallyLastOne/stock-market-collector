drop table finnhub_trades;
create table finnhub_trades
(
	c varchar(11),
	p numeric,
	s varchar(20),
    t BIGINT,
    v numeric
);

drop table trade_statistics;
create table trade_statistics
(
    start_timestamp timestamp with time zone not null unique,
    end_timestamp timestamp with time zone not null unique,
    volume numeric,
    max_price numeric,
    min_price numeric,
    avg_price numeric,
    open_price numeric,
    close_price numeric,
    symbol varchar(20) not null
);
create unique index idx_start_tmstp_end_tmstp on trade_statistics(start_timestamp, end_timestamp);

drop table leader;
create table leader
(
    id int primary key,
    renew_time timestamp with time zone,
    acquire_time timestamp with time zone,
    process_id bigint
)
insert
	into
	leader(id,
	renew_time,
	acquire_time)
values (1,
now(),
now());

common
    ConnectionPool
    Registry
finnhub
    FinnhubMessageEvent
    FinnhubWebSocketClient
leadership
    AcquireLeadershipJob
    Leader
statistics
    CalculateStatisticsJob
    TradeStatistics
trade
    FinnhubTrade
    TradeWriter