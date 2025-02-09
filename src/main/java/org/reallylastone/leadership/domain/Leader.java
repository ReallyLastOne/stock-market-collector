package org.reallylastone.leadership.domain;


import java.time.ZonedDateTime;

public record Leader(int id, ZonedDateTime renewTimestamp, ZonedDateTime acquireTime, long processId) {
}