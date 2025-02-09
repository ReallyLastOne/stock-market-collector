package org.reallylastone.trade.domain;

import java.math.BigDecimal;

public record Trade(Object c, BigDecimal p, String s, long t, BigDecimal v) {
}
