package org.reallylastone.leadership.gateway;

import org.reallylastone.leadership.domain.Leader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public class LeadershipGateway {
    private static final String GET_LEADER_SQL = "SELECT * FROM LEADER";
    private static final String UPDATE_HEARTBEAT_SQL = "UPDATE LEADER SET RENEW_TIME=now() WHERE ID = 1";
    private static final String ACQUIRE_LEADERSHIP_SQL = "UPDATE LEADER SET RENEW_TIME=now(), ACQUIRE_TIME=now(), process_id=? WHERE ID = 1";

    private final Connection connection;

    public LeadershipGateway(Connection connection) {
        this.connection = connection;
    }

    public Leader getLeader() {
        try (PreparedStatement stmt = connection.prepareStatement(GET_LEADER_SQL);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? buildLeader(rs) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateHeartbeat() {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_HEARTBEAT_SQL)) {
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void acquireLeadership(long pid) {
        try (PreparedStatement stmt = connection.prepareStatement(ACQUIRE_LEADERSHIP_SQL)) {
            stmt.setLong(1, pid);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Leader buildLeader(ResultSet rs) throws SQLException {
        return new Leader(
                rs.getInt(1),
                ZonedDateTime.from(rs.getObject(2, OffsetDateTime.class)),
                ZonedDateTime.from(rs.getObject(3, OffsetDateTime.class)),
                rs.getLong(4));
    }
}
