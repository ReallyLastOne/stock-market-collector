package org.reallylastone.leadership.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

public class LeadershipGateway {
    public static final long LEADER_TIMEOUT_MS = 30_000;
    private static final Logger log = LoggerFactory.getLogger(LeadershipGateway.class);

    private static final String UPDATE_HEARTBEAT_SQL =
            "UPDATE leader SET renew_time = now() WHERE id = 1 AND process_id = ?";
    private static final String TRY_ACQUIRE_SQL =
            "UPDATE leader SET renew_time = now(), acquire_time = now(), process_id = ? " +
                    "WHERE id = 1 AND (process_id IS NULL OR renew_time < ?)";

    private final Connection connection;
    private boolean amILeader;
    private ZonedDateTime renewTime;

    public LeadershipGateway(Connection connection) {
        this.connection = connection;
    }

    public boolean amILeader() {
        if (renewTime == null || renewTime.isBefore(ZonedDateTime.now().minusSeconds(LEADER_TIMEOUT_MS / 1000))) {
            amILeader = false;
        }
        return amILeader;
    }

    public boolean updateHeartbeat(long pid) {
        return executeUpdate(UPDATE_HEARTBEAT_SQL, stmt -> stmt.setLong(1, pid));
    }

    public boolean tryAcquireLeadership(long pid) {
        return executeUpdate(TRY_ACQUIRE_SQL, stmt -> {
            stmt.setLong(1, pid);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis() - LEADER_TIMEOUT_MS));
        });
    }

    private boolean executeUpdate(String sql, SQLConsumer<PreparedStatement> paramSetter) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            paramSetter.accept(stmt);
            boolean success = stmt.executeUpdate() > 0;
            amILeader = success;
            if (amILeader) renewTime = ZonedDateTime.now();
            return success;
        } catch (Exception e) {
            log.error("Error executing SQL: {}", sql, e);
            amILeader = false;
            return false;
        }
    }

    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws Exception;
    }
}
